package PymeLogic.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import PymeLogic.models.Proveedor;
import PymeLogic.repositories.ProveedorRepository;
import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @GetMapping
    public String listarProveedores(Model model) {
        try {
            List<Proveedor> proveedores = proveedorRepository.findAll();
            model.addAttribute("proveedores", proveedores);
            return "proveedores/lista";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los proveedores: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        model.addAttribute("esEdicion", false);
        return "proveedores/form";
    }

    @PostMapping("/nuevo")
    public String guardarProveedor(@Valid @ModelAttribute Proveedor proveedor, 
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return "proveedores/form";
        }

        try {
            proveedorRepository.save(proveedor);
            redirectAttributes.addFlashAttribute("mensaje", "Proveedor guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al guardar el proveedor: " + e.getMessage());
            model.addAttribute("tipoMensaje", "danger");
            model.addAttribute("esEdicion", false);
            return "proveedores/form";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        try {
            Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("esEdicion", true);
            return "proveedores/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el proveedor: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/editar/{id}")
    public String actualizarProveedor(@PathVariable Long id, 
                                    @Valid @ModelAttribute Proveedor proveedor, 
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", true);
            return "proveedores/form";
        }

        try {
            if (!proveedorRepository.existsById(id)) {
                throw new RuntimeException("Proveedor no encontrado");
            }
            proveedor.setId(id);
            proveedorRepository.save(proveedor);
            redirectAttributes.addFlashAttribute("mensaje", "Proveedor actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
            return "redirect:/proveedores";
        } catch (Exception e) {
            model.addAttribute("mensaje", "Error al actualizar el proveedor: " + e.getMessage());
            model.addAttribute("tipoMensaje", "danger");
            model.addAttribute("esEdicion", true);
            return "proveedores/form";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!proveedorRepository.existsById(id)) {
                throw new RuntimeException("Proveedor no encontrado");
            }
            proveedorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "Proveedor eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el proveedor: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }
        return "redirect:/proveedores";
    }
}
