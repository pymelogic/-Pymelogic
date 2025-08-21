package PymeLogic.service;

import PymeLogic.models.Proveedor;
import PymeLogic.repositories.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class ProveedorService implements CrudService<Proveedor, Long> {
    
    @Autowired
    private ProveedorRepository proveedorRepository;
    
    @Override
    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    @Override
    public Page<Proveedor> findAll(Pageable pageable) {
        return proveedorRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }
    
    @Override
    public Proveedor save(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }
    
    @Override
    public void deleteById(Long id) {
        proveedorRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return proveedorRepository.existsById(id);
    }
    
    // Métodos específicos para Proveedor
    public List<Proveedor> findByNombreContaining(String nombre) {
        return proveedorRepository.findAll().stream()
            .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
            .toList();
    }
}
