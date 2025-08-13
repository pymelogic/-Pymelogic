package PymeLogic.repositories;

import PymeLogic.models.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    // Buscar productos por categoría
    Page<Producto> findByCategoria(String categoria, Pageable pageable);
    
    // Buscar productos con stock bajo el límite mínimo
    @Query("SELECT p FROM Producto p JOIN p.limiteStock ls WHERE p.stock <= ls.limiteMinimo")
    Page<Producto> findProductosConStockBajo(Pageable pageable);
    
    // Búsqueda por nombre
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
