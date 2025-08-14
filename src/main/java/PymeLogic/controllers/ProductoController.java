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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.http.MediaType;

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
                                @RequestParam(required = false) MultipartFile imagenFile,
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
            String previousImage = null;
            
            if (id != null) {
                producto = productoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
                previousImage = producto.getImagen(); // Guardamos la imagen anterior
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

            // Manejo del límite de stock
            if (limiteMinimo != null) {
                LimiteStock limite;
                if (producto.getLimiteStock() != null) {
                    limite = producto.getLimiteStock();
                } else {
                    limite = new LimiteStock();
                    limite.setProducto(producto);
                }
                limite.setLimiteMinimo(limiteMinimo);
                limite.setLimiteMaximo(limiteMinimo * 2);
                producto.setLimiteStock(limite);
            }

            // Manejo de imagen
            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    String originalFilename = StringUtils.cleanPath(imagenFile.getOriginalFilename());
                    String filename = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    Path uploadDir = Paths.get("src/main/resources/static/images");
                    Files.createDirectories(uploadDir);
                    Path targetPath = uploadDir.resolve(filename);
                    Files.copy(imagenFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Solo actualizamos la imagen si la nueva se guardó correctamente
                    String newPublicPath = "/images/" + filename;
                    producto.setImagen(newPublicPath);
                    
                    logger.info("Nueva imagen guardada en: {}", targetPath.toAbsolutePath());
                    
                    // Si teníamos una imagen previa y es diferente, la eliminamos
                    if (previousImage != null && !previousImage.equals(newPublicPath)) {
                        deleteImageFile(previousImage);
                        logger.info("Imagen anterior eliminada: {}", previousImage);
                    }
                } catch (IOException ex) {
                    logger.error("Error al guardar la imagen: {}", ex.getMessage());
                    redirectAttributes.addFlashAttribute("error", "No se pudo guardar la imagen: " + ex.getMessage());
                }
            } else if (id != null) {
                // Si estamos editando y no se subió una nueva imagen, mantenemos la anterior
                producto.setImagen(previousImage);
            }

            // Guardamos el producto
            producto = productoRepository.save(producto);
            logger.info("Producto guardado exitosamente con ID: {}", producto.getId());
            
            String mensaje = id != null ? "Producto actualizado exitosamente" : "Producto creado exitosamente";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
            return "redirect:/productos";
            
        } catch (Exception e) {
            logger.error("Error al guardar el producto: {}", e.getMessage(), e);
            logger.error("Stack trace completo: ", e);
            
            String mensajeError = "Error al " + (id != null ? "actualizar" : "crear") + " el producto: " + e.getMessage();
            if (e.getCause() != null) {
                logger.error("Causa raíz: {}", e.getCause().getMessage());
                mensajeError += " - " + e.getCause().getMessage();
            }
            
            redirectAttributes.addFlashAttribute("error", mensajeError);
            return id != null ? "redirect:/productos/editar/" + id : "redirect:/productos/nuevo";
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

            // Eliminar imagen asociada si existe
            if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
                deleteImageFile(producto.getImagen());
            }

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

    // Método auxiliar para eliminar archivo de imagen dado su URL pública (/images/filename)
    private void deleteImageFile(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) return;
            String relative = imageUrl;
            if (relative.startsWith("/")) relative = relative.substring(1);
            Path filePath = Paths.get("src/main/resources/static").resolve(relative).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Archivo de imagen eliminado: {}", filePath.toAbsolutePath());
            }
        } catch (IOException ex) {
            logger.error("No se pudo eliminar el archivo de imagen: {}", ex.getMessage(), ex);
        }
    }
}
