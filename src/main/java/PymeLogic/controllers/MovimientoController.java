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

@Controller
@RequestMapping("/movimientos")
public class MovimientoController {
    
    private static final Logger logger = LoggerFactory.getLogger(MovimientoController.class);

    @Autowired
    private MovimientoService movimientoService;

    @GetMapping
    public String listarMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String producto,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
            Page<Movimiento> movimientosPage;

            // Construir filtros basados en los parámetros
            if (tipo != null || fechaInicio != null || fechaFin != null || producto != null) {
                // Aquí implementaremos la búsqueda con filtros
                movimientosPage = movimientoService.findAll(pageable); // Temporal
            } else {
                movimientosPage = movimientoService.findAll(pageable);
            }

            // Agregar datos al modelo
            model.addAttribute("movimientos", movimientosPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", movimientosPage.getTotalPages());
            model.addAttribute("totalItems", movimientosPage.getTotalElements());
            
            // Agregar filtros seleccionados al modelo
            model.addAttribute("tipoSeleccionado", tipo);
            model.addAttribute("fechaInicio", fechaInicio);
            model.addAttribute("fechaFin", fechaFin);
            model.addAttribute("productoSeleccionado", producto);

            // Agregar enums para los filtros
            model.addAttribute("tiposMovimiento", Movimiento.TipoMovimiento.values());

            return "movimientos/lista";
        } catch (Exception e) {
            logger.error("Error al listar movimientos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los movimientos: " + e.getMessage());
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
