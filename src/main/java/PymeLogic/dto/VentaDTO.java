package PymeLogic.dto;

import java.util.List;

public class VentaDTO {
    private List<VentaItemDTO> items;

    public List<VentaItemDTO> getItems() {
        return items;
    }

    public void setItems(List<VentaItemDTO> items) {
        this.items = items;
    }
}
