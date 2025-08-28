package PymeLogic.controllers;

import PymeLogic.dto.VentaDTO;
import PymeLogic.models.Movimiento;
import PymeLogic.models.Producto;
import PymeLogic.models.Usuario;
import PymeLogic.service.MovimientoService;
import PymeLogic.service.ProductoService;
import PymeLogic.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaApiController {

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private VentaPdfController ventaPdfController;

    @PostMapping
    public ResponseEntity<?> procesarVenta(@RequestBody VentaDTO venta) {
        if (venta.getItems() == null || venta.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("No hay items en la venta");
        }

        try {
            List<Movimiento> movimientos = procesarVentaInternal(venta);
            return ventaPdfController.generarFactura(movimientos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected List<Movimiento> procesarVentaInternal(VentaDTO venta) {
        // Obtener el usuario actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = userDetailsService.findByUsername(username);
        
        if (usuario == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        // Primero validamos todo
        for (var item : venta.getItems()) {
            Producto producto = productoService.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para " + producto.getNombre());
            }
        }

        List<Movimiento> movimientos = new ArrayList<>();

        // Si llegamos aquí, todo está validado, procedemos con la venta
        for (var item : venta.getItems()) {
            Producto producto = productoService.findById(item.getProductoId()).get();
            
            // Crear movimiento
            Movimiento movimiento = new Movimiento();
            movimiento.setProducto(producto);
            movimiento.setCantidad(-item.getCantidad());
            movimiento.setTipo(Movimiento.TipoMovimiento.SALIDA);
            movimiento.setMotivo("Venta de producto");
            movimiento.setFecha(LocalDateTime.now());
            movimiento.setUsuario(usuario); // Asignar el usuario al movimiento
            
            // Actualizar stock del producto
            producto.setStock(producto.getStock() - item.getCantidad());
            
            // Guardar los cambios
            productoService.save(producto);
            movimientoService.save(movimiento);
            
            movimientos.add(movimiento);
        }
        
        return movimientos;
    }
}
