package com.hospitalsr.controllers;

import com.hospitalsr.entities.*;
import com.hospitalsr.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;

@Controller
public class PresenciaController extends BaseController {

    private final EstudianteRepo estudianteRepo;
    private final AccesoRepo accesoRepo;
    private final EspecialidadRepo especialidadRepo;

    public PresenciaController(UsuarioSistemaRepo usuarioRepo,
            EstudianteRepo estudianteRepo,
            AccesoRepo accesoRepo,
            EspecialidadRepo especialidadRepo) {
        super(usuarioRepo);
        this.estudianteRepo = estudianteRepo;
        this.accesoRepo = accesoRepo;
        this.especialidadRepo = especialidadRepo;
    }

    @GetMapping("/presencia")
    public String presencia(Model model, Authentication auth) {
        addUserToModel(model, auth);
        LocalDate hoy = LocalDate.now();
        var dentroList = accesoRepo.findByFechaAndEstado(hoy, "DENTRO");
        model.addAttribute("accesosHoy", dentroList);
        model.addAttribute("estudiantesDentro", dentroList.size());
        model.addAttribute("estudiantesActivos", estudianteRepo.countByActivoTrue());
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("activePage", "presencia");
        return "presencia";
    }

    @PostMapping("/presencia/checkin")
    public String checkin(@RequestParam String cedula,
            @RequestParam(required = false) Long areaId,
            RedirectAttributes redirectAttrs) {
        Estudiante est = estudianteRepo.findByDocumento(cedula).orElse(null);
        if (est == null) {
            redirectAttrs.addFlashAttribute("mensaje", "Estudiante no encontrado con cédula: " + cedula);
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/presencia";
        }
        LocalDate hoy = LocalDate.now();
        boolean yaEntro = accesoRepo.findByFecha(hoy).stream()
                .anyMatch(a -> a.getEstudiante().getId().equals(est.getId()) && "DENTRO".equals(a.getEstado()));
        if (yaEntro) {
            redirectAttrs.addFlashAttribute("mensaje", est.getNombreCompleto() + " ya registró entrada hoy.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/presencia";
        }
        Acceso acceso = new Acceso();
        acceso.setEstudiante(est);
        acceso.setFecha(hoy);
        acceso.setHoraIngreso(LocalDateTime.now());
        acceso.setEstado("DENTRO");
        if (areaId != null)
            especialidadRepo.findById(areaId).ifPresent(acceso::setEspecialidad);
        accesoRepo.save(acceso);
        redirectAttrs.addFlashAttribute("mensaje", "Entrada registrada para " + est.getNombreCompleto());
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/presencia";
    }

    @PostMapping("/presencia/checkout")
    public String checkout(@RequestParam String cedula, RedirectAttributes redirectAttrs) {
        Estudiante est = estudianteRepo.findByDocumento(cedula).orElse(null);
        if (est == null) {
            redirectAttrs.addFlashAttribute("mensaje", "Estudiante no encontrado con cédula: " + cedula);
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/presencia";
        }
        LocalDate hoy = LocalDate.now();
        accesoRepo.findByFecha(hoy).stream()
                .filter(a -> a.getEstudiante().getId().equals(est.getId()) && "DENTRO".equals(a.getEstado()))
                .findFirst()
                .ifPresentOrElse(a -> {
                    a.setHoraSalida(LocalDateTime.now());
                    a.setEstado("FUERA");
                    accesoRepo.save(a);
                    redirectAttrs.addFlashAttribute("mensaje", "Salida registrada para " + est.getNombreCompleto());
                    redirectAttrs.addFlashAttribute("tipoMensaje", "success");
                }, () -> {
                    redirectAttrs.addFlashAttribute("mensaje",
                            est.getNombreCompleto() + " no tiene entrada activa hoy.");
                    redirectAttrs.addFlashAttribute("tipoMensaje", "error");
                });
        return "redirect:/presencia";
    }
}