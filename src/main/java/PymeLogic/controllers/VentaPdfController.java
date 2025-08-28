package PymeLogic.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/ventas")
public class VentaPdfController {
    @Autowired
    private MovimientoRepository movimientoRepository;

    public ResponseEntity<ByteArrayResource> generarFactura(List<Movimiento> movimientos) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        try {
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // Encabezado
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("FACTURA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));
            
            // Fecha
            document.add(new Paragraph("Fecha: " + LocalDateTime.now().format(formatter)));
            document.add(new Paragraph("Número de Factura: " + System.currentTimeMillis()));
            document.add(new Paragraph("\n"));
            
            // Detalles de la venta
            document.add(new Paragraph("DETALLES DE LA VENTA"));
            document.add(new Paragraph("===================="));
            
            double total = 0.0;
            
            // Información de cada producto
            for (Movimiento mov : movimientos) {
                document.add(new Paragraph(String.format(
                    "Producto: %s\n" +
                    "Cantidad: %d unidades\n" +
                    "Precio unitario: $%.2f\n" +
                    "Subtotal: $%.2f\n" +
                    "-------------------",
                    mov.getProducto().getNombre(),
                    Math.abs(mov.getCantidad()),
                    mov.getProducto().getPrecio(),
                    mov.getProducto().getPrecio().multiply(new java.math.BigDecimal(Math.abs(mov.getCantidad())))
                )));
                
                total += mov.getProducto().getPrecio().multiply(new java.math.BigDecimal(Math.abs(mov.getCantidad()))).doubleValue();
            }
            
            // Totales
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(String.format("Subtotal: $%.2f", total)));
            document.add(new Paragraph(String.format("IVA (13%%): $%.2f", total * 0.13)));
            document.add(new Paragraph(String.format("Total: $%.2f", total * 1.13)));
            
            document.close();
            
            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
                    
        } catch (DocumentException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/descargar-pdf")
    public ResponseEntity<ByteArrayResource> descargarPdfVentas() {
        List<Movimiento> ventas = movimientoRepository.findAll();
        return generarFactura(ventas.stream()
                .filter(mov -> mov.getTipo() == Movimiento.TipoMovimiento.SALIDA)
                .toList());
    }
}
