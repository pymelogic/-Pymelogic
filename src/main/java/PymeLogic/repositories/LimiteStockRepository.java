package PymeLogic.repositories;

import PymeLogic.models.LimiteStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LimiteStockRepository extends JpaRepository<LimiteStock, Long> {
    LimiteStock findByProductoId(Long productoId);
}
