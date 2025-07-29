package PymeLogic.controllers;

import PymeLogic.models.Producto;
import PymeLogic.models.LimiteStock;
import PymeLogic.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public String listarProductos(Model model) {
        try {
            List<Producto> productos = productoRepository.findAll();
            logger.info("Productos encontrados: {}", productos.size());
            
            if (productos.isEmpty()) {
                logger.info("No hay productos en la base de datos. Creando productos de ejemplo...");
                productos = crearProductosEjemplo();
            }
            
            model.addAttribute("productos", productos);
            return "productos/lista";
        } catch (Exception e) {
            logger.error("Error al listar productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@RequestParam String nombre,
                                @RequestParam String descripcion,
                                @RequestParam String codigoBarras,
                                @RequestParam BigDecimal precio,
                                @RequestParam Integer stock,
                                @RequestParam(required = false) String categoria,
                                @RequestParam(required = false) Integer limiteMinimo,
                                RedirectAttributes redirectAttributes) {
        try {
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setCodigoBarras(codigoBarras);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setCategoria(categoria);

            if (limiteMinimo != null) {
                LimiteStock limite = new LimiteStock();
                limite.setLimiteMinimo(limiteMinimo);
                limite.setProducto(producto);
                producto.setLimiteStock(limite);
            }

            productoRepository.save(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
            return "redirect:/productos";
        } catch (Exception e) {
            logger.error("Error al guardar el producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + e.getMessage());
            return "redirect:/productos/nuevo";
        }
    }

    private List<Producto> crearProductosEjemplo() {
        List<Producto> productos = new ArrayList<>();
        try {
            Producto p1 = new Producto();
            p1.setNombre("Producto Ejemplo 1");
            p1.setDescripcion("Descripción del producto 1");
            p1.setCodigoBarras("123456789");
            p1.setPrecio(new BigDecimal("19.99"));
            p1.setStock(10);
            p1.setCategoria("Ejemplo");
            
            LimiteStock limite1 = new LimiteStock();
            limite1.setLimiteMinimo(5);
            limite1.setProducto(p1);
            p1.setLimiteStock(limite1);
            
            Producto p2 = new Producto();
            p2.setNombre("Producto Ejemplo 2");
            p2.setDescripcion("Descripción del producto 2");
            p2.setCodigoBarras("987654321");
            p2.setPrecio(new BigDecimal("29.99"));
            p2.setStock(5);
            p2.setCategoria("Ejemplo");
            
            LimiteStock limite2 = new LimiteStock();
            limite2.setLimiteMinimo(3);
            limite2.setProducto(p2);
            p2.setLimiteStock(limite2);
            
            productoRepository.save(p1);
            productoRepository.save(p2);
            
            productos = productoRepository.findAll();
            logger.info("Productos de ejemplo creados exitosamente");
        } catch (Exception e) {
            logger.error("Error al crear productos de ejemplo: {}", e.getMessage(), e);
        }
        return productos;
    }

    @GetMapping("/stock-bajo")
    public String listarProductosStockBajo(Model model) {
        List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();
        model.addAttribute("productos", productosStockBajo);
        model.addAttribute("titulo", "Productos con Stock Bajo");
        return "productos/lista";
    }
}
