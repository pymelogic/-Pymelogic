package PymeLogic.service;

import PymeLogic.models.Movimiento;
import PymeLogic.repositories.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class MovimientoService implements CrudService<Movimiento, Long> {
    
    @Autowired
    private MovimientoRepository movimientoRepository;
    
    @Override
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Override
    public Page<Movimiento> findAll(Pageable pageable) {
        return movimientoRepository.findAll(pageable);
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
    public Page<Movimiento> findByProductoNombre(String nombre, Pageable pageable) {
        return movimientoRepository.findByProductoNombreContainingIgnoreCase(nombre, pageable);
    }
    
    public Page<Movimiento> findByFecha(LocalDateTime fecha, Pageable pageable) {
        // Crear un rango de 24 horas para la fecha seleccionada
        LocalDateTime fechaInicio = fecha.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fechaFin = fecha.withHour(23).withMinute(59).withSecond(59);
        return movimientoRepository.findByFechaBetween(fechaInicio, fechaFin, pageable);
    }
    
    public Page<Movimiento> findByProductoNombreAndFecha(String nombre, LocalDateTime fecha, Pageable pageable) {
        LocalDateTime fechaInicio = fecha.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fechaFin = fecha.withHour(23).withMinute(59).withSecond(59);
        return movimientoRepository.findByProductoNombreContainingIgnoreCaseAndFechaBetween(nombre, fechaInicio, fechaFin, pageable);
    }
}
