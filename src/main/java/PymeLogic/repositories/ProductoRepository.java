package PymeLogic.repositories;

import PymeLogic.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarras(String codigoBarras);
    
    // Buscar productos por categoría
    List<Producto> findByCategoria(String categoria);
    
    // Buscar productos con stock bajo el límite mínimo
    @Query("SELECT p FROM Producto p JOIN p.limiteStock ls WHERE p.stock <= ls.limiteMinimo")
    List<Producto> findProductosConStockBajo();
}
