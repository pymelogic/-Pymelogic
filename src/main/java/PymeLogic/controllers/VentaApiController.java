package PymeLogic.controllers;

import PymeLogic.dto.VentaDTO;
import PymeLogic.models.Movimiento;
import PymeLogic.models.Producto;
import PymeLogic.service.MovimientoService;
import PymeLogic.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ventas")
public class VentaApiController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private ProductoService productoService;

    @PostMapping
    @Transactional
    public ResponseEntity<?> procesarVenta(@RequestBody VentaDTO venta) {
        try {
            // Procesar cada item de la venta
            for (var item : venta.getItems()) {
                // Obtener el producto
                Producto producto = productoService.findById(item.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                // Verificar stock
                if (producto.getStock() < item.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para " + producto.getNombre());
                }

                // Crear movimiento de venta
                Movimiento movimiento = new Movimiento();
                movimiento.setProducto(producto);
                movimiento.setCantidad(-item.getCantidad()); // Negativo porque es una salida
                movimiento.setTipo(Movimiento.TipoMovimiento.VENTA);
                movimiento.setFecha(LocalDateTime.now());
                
                // Actualizar stock del producto
                producto.setStock(producto.getStock() - item.getCantidad());
                productoService.save(producto);
                
                // Guardar el movimiento
                movimientoService.save(movimiento);
            }

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
