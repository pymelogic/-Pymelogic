package PymeLogic.controllers;

import PymeLogic.models.Movimiento;
import PymeLogic.service.MovimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoController.class);

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private Environment env;

    @GetMapping
    public String listarMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) String producto,
            Model model) {
        Page<Movimiento> movimientosPage;
        LocalDateTime fechaFiltro = null;

        try {
            logger.debug("Iniciando listarMovimientos con parámetros: page={}, size={}, fecha={}, producto={}",
                    page, size, fecha, producto);

            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());

            if (fecha != null && !fecha.isEmpty()) {
                logger.debug("Intentando parsear fecha: {}", fecha);
                if (fecha.contains("T")) {
                    // Formato ISO
                    try {
                        fechaFiltro = LocalDateTime.parse(fecha);
                        logger.debug("Fecha parseada en formato ISO: {}", fechaFiltro);
                    } catch (DateTimeParseException e) {
                        logger.warn("No se pudo parsear la fecha en formato ISO: {}", e.getMessage());
                    }
                } else {
                    // Intentar formatos personalizados
                    String[] patterns = {
                            "yyyy-MM-dd HH:mm:ss",
                            "yyyy-MM-dd HH:mm",
                            "yyyy-MM-dd"
                    };

                    for (String pattern : patterns) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                            if (pattern.equals("yyyy-MM-dd")) {
                                // Si solo es fecha, agregar hora 00:00:00
                                fechaFiltro = LocalDate.parse(fecha, formatter).atStartOfDay();
                            } else {
                                fechaFiltro = LocalDateTime.parse(fecha, formatter);
                            }
                            logger.debug("Fecha parseada con patrón {}: {}", pattern, fechaFiltro);
                            break;
                        } catch (DateTimeParseException e) {
                            logger.debug("No se pudo parsear la fecha con patrón {}: {}", pattern, e.getMessage());
                        }
                    }
                    if (fechaFiltro == null) {
                        logger.warn("No se pudo parsear la fecha con ningún formato: {}", fecha);
                    }
                }
            }

            logger.debug("Aplicando filtros de búsqueda");
            if (producto != null && !producto.isEmpty() && fechaFiltro != null) {
                logger.debug("Buscando por producto '{}' y fecha {}", producto, fechaFiltro);
                movimientosPage = movimientoService.findByProductoNombreAndFecha(producto, fechaFiltro, pageable);
            } else if (producto != null && !producto.isEmpty()) {
                logger.debug("Buscando solo por producto '{}'", producto);
                movimientosPage = movimientoService.findByProductoNombre(producto, pageable);
            } else if (fechaFiltro != null) {
                logger.debug("Buscando solo por fecha {}", fechaFiltro);
                movimientosPage = movimientoService.findByFecha(fechaFiltro, pageable);
            } else {
                logger.debug("No hay filtros, mostrando todos los movimientos");
                movimientosPage = movimientoService.findAll(pageable);
            }
            logger.debug("Búsqueda completada. Encontrados {} resultados", movimientosPage.getTotalElements());

            // Agregar datos al modelo
            model.addAttribute("movimientos", movimientosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", movimientosPage.getTotalPages());
            model.addAttribute("totalItems", movimientosPage.getTotalElements());

            // Agregar filtros seleccionados al modelo
            model.addAttribute("fecha", fecha);
            model.addAttribute("productoSeleccionado", producto);

            return "movimientos/lista";
        } catch (Exception e) {
            logger.error("Error al listar movimientos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los movimientos");
            model.addAttribute("message", e.getMessage());
            // Solo incluir el stack trace en desarrollo
            if (env.acceptsProfiles(Profiles.of("dev", "development"))) {
                model.addAttribute("trace", e.getStackTrace());
            }
            return "error";
        }
    }

    @GetMapping("/exportar")
    public String exportarMovimientos(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String producto) {
        // Implementaremos la exportación a Excel aquí
        return "redirect:/movimientos";
    }
}
