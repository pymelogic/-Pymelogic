package PymeLogic.repositories;

import PymeLogic.models.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
}
