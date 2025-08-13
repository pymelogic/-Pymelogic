package PymeLogic.repositories;

import PymeLogic.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String username);
    Page<Usuario> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);
}
