package PymeLogic.repositories;

import PymeLogic.models.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    Page<Movimiento> findByProductoNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<Movimiento> findByFechaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    Page<Movimiento> findByProductoNombreContainingIgnoreCaseAndFechaBetween(String nombre, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
}
