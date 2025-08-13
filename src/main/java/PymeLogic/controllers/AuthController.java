package PymeLogic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import PymeLogic.models.Usuario;
import PymeLogic.repositories.UsuarioRepository;
import PymeLogic.repositories.RolRepository;
import PymeLogic.models.Rol;
import java.util.Collections;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/auth/login")
    public String showLoginForm() {
        return "auth/login";
    }

    // Se eliminaron los métodos de registro ya que se usará un usuario predefinido

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Model model) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEmail(email);

        // Asignar rol de usuario por defecto
        Rol rolUsuario = rolRepository.findByNombre(Rol.TipoRol.ROLE_USER);
        if (rolUsuario == null) {
            rolUsuario = new Rol();
            rolUsuario.setNombre(Rol.TipoRol.ROLE_USER);
            rolRepository.save(rolUsuario);
        }
        usuario.setRoles(Collections.singleton(rolUsuario));

        usuarioRepository.save(usuario);
        return "redirect:/login?registered";
    }
}
