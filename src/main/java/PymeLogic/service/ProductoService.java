package PymeLogic.service;

import PymeLogic.models.Producto;
import PymeLogic.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class ProductoService implements CrudService<Producto, Long> {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Page<Producto> findAll(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }
    
    @Override
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }
    
    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return productoRepository.existsById(id);
    }
    
    // Métodos específicos para Producto
    public List<Producto> findByNombreContaining(String nombre) {
        return productoRepository.findAll();  // Temporalmente retorna todos, ajustar cuando se implemente el método en el repositorio
    }
    
    public Optional<Producto> findByCodigoBarras(String codigoBarras) {
        return productoRepository.findAll().stream()
            .filter(p -> codigoBarras.equals(p.getCodigoBarras()))
            .findFirst();
    }
}
