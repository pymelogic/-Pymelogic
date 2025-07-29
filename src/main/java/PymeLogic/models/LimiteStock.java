package PymeLogic.models;

import jakarta.persistence.*;

@Entity
@Table(name = "LimiteStock")
public class LimiteStock extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "idProducto", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private Integer limiteMinimo;

    @Column(nullable = false)
    private Integer limiteMaximo;

    // Getters y Setters
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getLimiteMinimo() {
        return limiteMinimo;
    }

    public void setLimiteMinimo(Integer limiteMinimo) {
        this.limiteMinimo = limiteMinimo;
    }

    public Integer getLimiteMaximo() {
        return limiteMaximo;
    }

    public void setLimiteMaximo(Integer limiteMaximo) {
        this.limiteMaximo = limiteMaximo;
    }
}
