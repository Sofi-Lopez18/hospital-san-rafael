package com.hospitalsr.controllers;

import com.hospitalsr.entities.*;
import com.hospitalsr.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminController extends BaseController {

    private final EstudianteRepo estudianteRepo;
    private final EspecialidadRepo especialidadRepo;
    private final UniversidadRepo universidadRepo;
    private final AccesoRepo accesoRepo;
    private final ProgramacionDetalleRepo detalleRepo;

    public AdminController(UsuarioSistemaRepo usuarioRepo,
            EstudianteRepo estudianteRepo,
            EspecialidadRepo especialidadRepo,
            UniversidadRepo universidadRepo,
            AccesoRepo accesoRepo,
            ProgramacionDetalleRepo detalleRepo) {
        super(usuarioRepo);
        this.estudianteRepo = estudianteRepo;
        this.especialidadRepo = especialidadRepo;
        this.universidadRepo = universidadRepo;
        this.accesoRepo = accesoRepo;
        this.detalleRepo = detalleRepo;
    }

    // ── Dashboard ──────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        addUserToModel(model, auth);
        LocalDate hoy = LocalDate.now();
        model.addAttribute("totalEstudiantes", estudianteRepo.countByActivoTrue());
        model.addAttribute("dentroCount", accesoRepo.countByFechaAndEstado(hoy, "DENTRO"));
        model.addAttribute("accesosHoy", accesoRepo.findByFecha(hoy));
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    // ── Áreas ──────────────────────────────────────────────────────────
    @GetMapping("/areas")
    public String areas(Model model, Authentication auth) {
        addUserToModel(model, auth);
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("activePage", "areas");
        return "areas";
    }

    @PostMapping("/areas/guardar")
    public String guardarArea(@RequestParam String nombre,
            @RequestParam String sede,
            @RequestParam Integer capacidadMaxima,
            @RequestParam(required = false) Long areaId,
            RedirectAttributes redirectAttrs) {
        Especialidad e = areaId != null
                ? especialidadRepo.findById(areaId).orElse(new Especialidad())
                : new Especialidad();
        e.setNombre(nombre);
        e.setSede(sede);
        e.setCapacidadMaxima(capacidadMaxima);
        especialidadRepo.save(e);
        redirectAttrs.addFlashAttribute("mensaje", "Área guardada correctamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/areas";
    }

    @PostMapping("/areas/eliminar/{id}")
    public String eliminarArea(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            especialidadRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("mensaje", "Área eliminada.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensaje",
                    "No se puede eliminar esta área porque tiene horarios o accesos asociados.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/areas";
    }

    // ── Universidades ──────────────────────────────────────────────────
    @GetMapping("/universidades")
    public String universidades(Model model, Authentication auth) {
        addUserToModel(model, auth);
        model.addAttribute("universidades", universidadRepo.findAll());
        model.addAttribute("activePage", "universidades");
        return "universidades";
    }

    @PostMapping("/universidades/guardar")
    public String guardarUniversidad(@RequestParam String nombre,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Long univId,
            RedirectAttributes redirectAttrs) {
        Universidad u = univId != null
                ? universidadRepo.findById(univId).orElse(new Universidad())
                : new Universidad();
        u.setNombre(nombre);
        u.setCiudad(ciudad);
        u.setEstado(true);
        universidadRepo.save(u);
        redirectAttrs.addFlashAttribute("mensaje", "Universidad guardada.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/universidades";
    }

    @PostMapping("/universidades/eliminar/{id}")
    public String eliminarUniversidad(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            estudianteRepo.findAll().stream()
                    .filter(e -> e.getUniversidad() != null && e.getUniversidad().getId().equals(id))
                    .forEach(e -> {
                        e.setUniversidad(null);
                        estudianteRepo.save(e);
                    });
            universidadRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("mensaje", "Universidad eliminada.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensaje", "No se pudo eliminar la universidad.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/universidades";
    }

    // ── Cronograma ─────────────────────────────────────────────────────
    @GetMapping("/cronograma")
    public String cronograma(Model model, Authentication auth,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        addUserToModel(model, auth);
        LocalDate now = LocalDate.now();
        int m = mes != null ? mes : now.getMonthValue();
        int a = anio != null ? anio : now.getYear();
        model.addAttribute("detalles", detalleRepo.findByMesAndAnio(m, a));
        model.addAttribute("estudiantes", estudianteRepo.findByActivoTrue());
        model.addAttribute("mes", m);
        model.addAttribute("anio", a);
        model.addAttribute("mesDate", LocalDate.of(a, m, 1));
        model.addAttribute("activePage", "cronograma");
        return "cronograma";
    }

    // ── Reportes ───────────────────────────────────────────────────────
    @GetMapping("/reportes")
    public String reportes(Model model, Authentication auth) {
        addUserToModel(model, auth);
        model.addAttribute("totalEstudiantes", estudianteRepo.countByActivoTrue());
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("estudiantes", estudianteRepo.findByActivoTrue());
        model.addAttribute("activePage", "reportes");
        return "reportes";
    }
}
