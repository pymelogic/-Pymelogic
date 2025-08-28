package PymeLogic.repositories;

import PymeLogic.models.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p JOIN FETCH m.usuario u LEFT JOIN FETCH m.proveedor WHERE p.nombre LIKE %:nombre%")
    Page<Movimiento> findByProductoNombreContainingIgnoreCase(@Param("nombre") String nombre, Pageable pageable);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p JOIN FETCH m.usuario u LEFT JOIN FETCH m.proveedor WHERE m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Page<Movimiento> findByFechaBetween(@Param("fechaInicio") LocalDateTime fechaInicio, @Param("fechaFin") LocalDateTime fechaFin, Pageable pageable);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p JOIN FETCH m.usuario u LEFT JOIN FETCH m.proveedor WHERE p.nombre LIKE %:nombre% AND m.fecha BETWEEN :fechaInicio AND :fechaFin")
    Page<Movimiento> findByProductoNombreContainingIgnoreCaseAndFechaBetween(
        @Param("nombre") String nombre, 
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin, 
        Pageable pageable);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.producto p JOIN FETCH m.usuario u LEFT JOIN FETCH m.proveedor")
    Page<Movimiento> findAllWithRelations(Pageable pageable);

    @Modifying
    @Query("DELETE FROM Movimiento m WHERE m.producto.id NOT IN (SELECT p.id FROM Producto p)")
    void deleteMovimientosWithInvalidProducts();
}
