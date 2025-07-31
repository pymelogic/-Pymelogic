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
                logger.info("No hay productos en la base de datos");
            } else {
                logger.info("Productos recuperados correctamente. Primer producto: {}", productos.get(0).getNombre());
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

    private String generarCodigoBarras() {
        // Generar un código de barras único basado en timestamp y número aleatorio
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("%d%03d", timestamp, random);
    }

    @PostMapping("/guardar")
    public String guardarProducto(@RequestParam String nombre,
                                @RequestParam String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam Integer stock,
                                @RequestParam(required = false) String categoria,
                                @RequestParam(required = false) Integer limiteMinimo,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando guardado de producto - Datos recibidos:");
            logger.info("Nombre: {}", nombre);
            logger.info("Precio: {}", precio);
            logger.info("Stock: {}", stock);
            logger.info("Categoría: {}", categoria);
            logger.info("Límite mínimo: {}", limiteMinimo);
            
            Producto producto = new Producto();
            String codigoBarras = generarCodigoBarras();
            logger.info("Código de barras generado: {}", codigoBarras);
            
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
            producto.setCodigoBarras(codigoBarras);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setCategoria(categoria);

            if (limiteMinimo != null) {
                logger.info("Configurando límite de stock mínimo: {}", limiteMinimo);
                LimiteStock limite = new LimiteStock();
                limite.setLimiteMinimo(limiteMinimo);
                // Establecemos un límite máximo por defecto (2 veces el límite mínimo)
                limite.setLimiteMaximo(limiteMinimo * 2);
                limite.setProducto(producto);
                producto.setLimiteStock(limite);
            }

            producto = productoRepository.save(producto);
            logger.info("Producto guardado exitosamente con ID: {}", producto.getId());
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto '" + nombre + "' guardado exitosamente");
            return "redirect:/productos";
        } catch (Exception e) {
            logger.error("Error al guardar el producto: {}", e.getMessage(), e);
            logger.error("Causa del error: ", e);
            
            String mensajeError = e.getMessage();
            if (e.getCause() != null) {
                mensajeError += " - Causa: " + e.getCause().getMessage();
            }
            
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + mensajeError);
            return "redirect:/productos/nuevo";
        }
    }

    @GetMapping("/stock-bajo")
    public String listarProductosStockBajo(Model model) {
        List<Producto> productosStockBajo = productoRepository.findProductosConStockBajo();
        model.addAttribute("productos", productosStockBajo);
        model.addAttribute("titulo", "Productos con Stock Bajo");
        return "productos/lista";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            model.addAttribute("producto", producto);
            return "productos/form";
        } catch (Exception e) {
            logger.error("Error al cargar el producto para editar: {}", e.getMessage(), e);
            return "redirect:/productos";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            productoRepository.delete(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar el producto: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }
}
