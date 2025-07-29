package PymeLogic.models;

import jakarta.persistence.*;

@Entity
@Table(name = "Rol")
public class Rol extends BaseEntity {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoRol nombre;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String permisos;

    public enum TipoRol {
        ADMIN, OPERADOR, CONSULTA
    }

    // Getters y Setters
    public TipoRol getNombre() {
        return nombre;
    }

    public void setNombre(TipoRol nombre) {
        this.nombre = nombre;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String permisos) {
        this.permisos = permisos;
    }
}
