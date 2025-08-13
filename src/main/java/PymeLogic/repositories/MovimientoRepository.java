package PymeLogic.repositories;

import PymeLogic.models.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    Page<Movimiento> findByProductoId(Long productoId, Pageable pageable);
    Page<Movimiento> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
    Page<Movimiento> findByTipo(Movimiento.TipoMovimiento tipo, Pageable pageable);
}
