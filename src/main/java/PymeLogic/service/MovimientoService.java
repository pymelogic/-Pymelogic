package PymeLogic.service;

import PymeLogic.models.Movimiento;
import PymeLogic.repositories.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

@Service
@Transactional
public class MovimientoService implements CrudService<Movimiento, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(MovimientoService.class);
    
    @Autowired
    private MovimientoRepository movimientoRepository;
    
    @Override
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Movimiento> findAll(Pageable pageable) {
        try {
            // Limpiar movimientos inválidos si los hay
            cleanInvalidMovimientos();
            
            // Usar la consulta optimizada que hace JOIN FETCH
            return movimientoRepository.findAllWithRelations(pageable);
        } catch (Exception e) {
            logger.error("Error al obtener todos los movimientos: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    @Transactional
    public void cleanInvalidMovimientos() {
        try {
            movimientoRepository.deleteMovimientosWithInvalidProducts();
        } catch (Exception e) {
            logger.error("Error al limpiar movimientos inválidos: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Movimiento> findById(Long id) {
        return movimientoRepository.findById(id);
    }
    
    @Override
    public Movimiento save(Movimiento movimiento) {
        if (movimiento.getFecha() == null) {
            movimiento.setFecha(LocalDateTime.now());
        }
        return movimientoRepository.save(movimiento);
    }
    
    @Override
    public void deleteById(Long id) {
        movimientoRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return movimientoRepository.existsById(id);
    }
    
    // Métodos específicos para Movimiento
    @Transactional(readOnly = true)
    public Page<Movimiento> findByProductoNombre(String nombre, Pageable pageable) {
        try {
            // Limpiar movimientos inválidos si los hay
            cleanInvalidMovimientos();
            
            return movimientoRepository.findByProductoNombreContainingIgnoreCase(nombre, pageable);
        } catch (Exception e) {
            logger.error("Error al buscar movimientos por nombre de producto: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public Page<Movimiento> findByFecha(LocalDateTime fecha, Pageable pageable) {
        try {
            // Limpiar movimientos inválidos si los hay
            cleanInvalidMovimientos();
            
            // Crear un rango de 24 horas para la fecha seleccionada
            LocalDateTime fechaInicio = fecha.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime fechaFin = fecha.withHour(23).withMinute(59).withSecond(59);
            
            return movimientoRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
        } catch (Exception e) {
            logger.error("Error al buscar movimientos por fecha: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }
    
    public Page<Movimiento> findByProductoNombreAndFecha(String nombre, LocalDateTime fecha, Pageable pageable) {
        LocalDateTime fechaInicio = fecha.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fechaFin = fecha.withHour(23).withMinute(59).withSecond(59);
        return movimientoRepository.findByProductoNombreContainingIgnoreCaseAndFechaBetween(nombre, fechaInicio, fechaFin, pageable);
    }
}
