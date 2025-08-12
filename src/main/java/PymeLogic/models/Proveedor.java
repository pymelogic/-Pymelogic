package PymeLogic.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "Proveedor")
public class Proveedor extends BaseEntity {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El contacto es obligatorio")
    @Size(min = 3, message = "El nombre del contacto debe tener al menos 3 caracteres")
    @Column(nullable = false)
    private String contacto;

    @Email(message = "El correo electrónico debe ser válido")
    private String email;

    private String direccion;

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
