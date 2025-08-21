package PymeLogic.controllers;

import PymeLogic.models.Producto;
import PymeLogic.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoApiController {

    @Autowired
    private ProductoService productoService;

    @GetMapping("/buscar")
    public ResponseEntity<List<Producto>> buscarProductos(@RequestParam String q) {
        // Buscar por código de barras o nombre
        List<Producto> productos;
        if (q.matches("\\d+")) {  // Si es un número (posible código de barras)
            productos = productoService.findByCodigoBarras(q)
                    .map(List::of)
                    .orElseGet(() -> productoService.findByNombreContaining(q));
        } else {
            productos = productoService.findByNombreContaining(q);
        }
        return ResponseEntity.ok(productos);
    }
}
