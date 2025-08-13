package PymeLogic.repositories;

import PymeLogic.models.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Page<Proveedor> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
