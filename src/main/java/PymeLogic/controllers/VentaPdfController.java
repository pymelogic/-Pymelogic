package PymeLogic.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import PymeLogic.repositories.MovimientoRepository;
import PymeLogic.models.Movimiento;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/ventas")
public class VentaPdfController {
    @Autowired
    private MovimientoRepository movimientoRepository;

    @GetMapping("/descargar-pdf")
    public ResponseEntity<ByteArrayResource> descargarPdfVentas() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try {
            List<Movimiento> ventas = movimientoRepository.findAll();
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Título
            document.add(new Paragraph("REPORTE DE VENTAS"));
            document.add(new Paragraph("===================="));
            document.add(new Paragraph("\n"));
            
            // Información de cada venta
            for (Movimiento mov : ventas) {
                if (mov.getTipo() == Movimiento.TipoMovimiento.SALIDA) {
                    document.add(new Paragraph(String.format(
                        "Fecha: %s\n" +
                        "Producto: %s\n" +
                        "Cantidad: %d unidades\n" +
                        "Usuario: %s\n" +
                        "-------------------",
                        mov.getFecha().format(formatter),
                        mov.getProducto().getNombre(),
                        mov.getCantidad(),
                        mov.getUsuario().getUsername()
                    )));
                    document.add(new Paragraph("\n"));
                }
            }
            document.close();
            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(baos.size())
                .body(resource);
        } catch (DocumentException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
