package PymeLogic.repositories;

import PymeLogic.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Rol findByNombre(Rol.TipoRol nombre);
}
