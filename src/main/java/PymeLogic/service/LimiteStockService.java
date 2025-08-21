package PymeLogic.service;

import PymeLogic.models.LimiteStock;
import PymeLogic.repositories.LimiteStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class LimiteStockService implements CrudService<LimiteStock, Long> {
    
    @Autowired
    private LimiteStockRepository limiteStockRepository;
    
    @Override
    public List<LimiteStock> findAll() {
        return limiteStockRepository.findAll();
    }

    @Override
    public Page<LimiteStock> findAll(Pageable pageable) {
        return limiteStockRepository.findAll(pageable);
    }
    
    @Override
    public Optional<LimiteStock> findById(Long id) {
        return limiteStockRepository.findById(id);
    }
    
    @Override
    public LimiteStock save(LimiteStock limiteStock) {
        return limiteStockRepository.save(limiteStock);
    }
    
    @Override
    public void deleteById(Long id) {
        limiteStockRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return limiteStockRepository.existsById(id);
    }
    
    // Métodos específicos para LimiteStock
    public Optional<LimiteStock> findByProductoId(Long productoId) {
        return limiteStockRepository.findAll().stream()
            .filter(ls -> ls.getProducto().getId().equals(productoId))
            .findFirst();
    }
    
    public List<LimiteStock> findStockBajoLimite() {
        return limiteStockRepository.findAll().stream()
            .filter(ls -> ls.getProducto().getStock() <= ls.getLimiteMinimo())
            .toList();
    }
}
