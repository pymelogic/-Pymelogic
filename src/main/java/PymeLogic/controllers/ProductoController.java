package PymeLogic.controllers;

import PymeLogic.models.Producto;
import PymeLogic.models.LimiteStock;
import PymeLogic.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

@Controller
@RequestMapping("/productos")
public class ProductoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public String listarProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Producto> productosPage;
            
            if (search != null && !search.trim().isEmpty()) {
                productosPage = productoRepository.findByNombreContainingIgnoreCase(search, pageable);
                model.addAttribute("search", search);
            } else {
                productosPage = productoRepository.findAll(pageable);
            }
            
            logger.info("Productos encontrados: {}", productosPage.getTotalElements());
            
            if (productosPage.isEmpty()) {
                logger.info("No hay productos en la base de datos");
            } else {
                logger.info("Productos recuperados correctamente. Primer producto: {}", 
                    productosPage.getContent().get(0).getNombre());
            }
            
            model.addAttribute("productos", productosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productosPage.getTotalPages());
            model.addAttribute("totalItems", productosPage.getTotalElements());
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
        model.addAttribute("esEdicion", false);
        return "productos/form";
    }

    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        try {
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            model.addAttribute("producto", producto);
            model.addAttribute("esEdicion", true);
            return "productos/form";
        } catch (Exception e) {
            logger.error("Error al cargar producto para editar: {}", e.getMessage(), e);
            return "redirect:/productos";
        }
    }

    private String generarCodigoBarras() {
        // Generar un código de barras único basado en timestamp y número aleatorio
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return String.format("%d%03d", timestamp, random);
    }

    @PostMapping("/guardar")
    public String guardarProducto(@RequestParam(required = false) Long id,
                                @RequestParam String nombre,
                                @RequestParam String descripcion,
                                @RequestParam BigDecimal precio,
                                @RequestParam Integer stock,
                                @RequestParam(required = false) String categoria,
                                @RequestParam(required = false) Integer limiteMinimo,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando guardado/actualización de producto - Datos recibidos:");
            logger.info("ID: {}", id);
            logger.info("Nombre: {}", nombre);
            logger.info("Precio: {}", precio);
            logger.info("Stock: {}", stock);
            logger.info("Categoría: {}", categoria);
            logger.info("Límite mínimo: {}", limiteMinimo);
            
            Producto producto;
            if (id != null) {
                producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            } else {
                producto = new Producto();
                String codigoBarras = generarCodigoBarras();
                logger.info("Código de barras generado: {}", codigoBarras);
                producto.setCodigoBarras(codigoBarras);
            }
            
            producto.setNombre(nombre);
            producto.setDescripcion(descripcion);
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
            logger.error("Stack trace completo: ", e);
            
            String mensajeError = e.getMessage();
            if (e.getCause() != null) {
                mensajeError += " - Causa: " + e.getCause().getMessage();
                logger.error("Causa raíz: {}", e.getCause().getMessage());
            }
            
            // Log de los detalles del producto que se intentó guardar
            logger.error("Detalles del producto que falló:");
            logger.error("ID: {}, Nombre: {}, Precio: {}, Stock: {}, Categoría: {}", 
                id, nombre, precio, stock, categoria);
            
            redirectAttributes.addFlashAttribute("error", "Error al guardar el producto: " + mensajeError);
            return "redirect:/productos/nuevo";
        }
    }

    @GetMapping("/stock-bajo")
    public String listarProductosStockBajo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Producto> productosStockBajo = productoRepository.findProductosConStockBajo(pageable);
        model.addAttribute("productos", productosStockBajo.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productosStockBajo.getTotalPages());
        model.addAttribute("totalItems", productosStockBajo.getTotalElements());
        model.addAttribute("titulo", "Productos con Stock Bajo");
        return "productos/lista";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Intentando eliminar producto con ID: {}", id);
            
            Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            logger.info("Producto encontrado: {}", producto.getNombre());
            productoRepository.delete(producto);
            logger.info("Producto eliminado exitosamente");
            
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar el producto: {}", e.getMessage(), e);
            logger.error("Stack trace completo: ", e);
            if (e.getCause() != null) {
                logger.error("Causa raíz: {}", e.getCause().getMessage());
            }
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        return "redirect:/productos";
    }
}
