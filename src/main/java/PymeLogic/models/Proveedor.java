package PymeLogic.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Proveedor")
public class Proveedor extends BaseEntity {
    @Column(nullable = false)
    private String nombre;

    private String contacto;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
