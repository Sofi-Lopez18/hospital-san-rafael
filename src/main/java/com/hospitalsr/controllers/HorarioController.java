package com.hospitalsr.controllers;

import com.hospitalsr.entities.*;
import com.hospitalsr.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.*;

@Controller
public class HorarioController extends BaseController {

    private final EstudianteRepo estudianteRepo;
    private final EspecialidadRepo especialidadRepo;
    private final TutorRepo tutorRepo;
    private final ProgramacionRepo programacionRepo;
    private final ProgramacionDetalleRepo detalleRepo;

    public HorarioController(UsuarioSistemaRepo usuarioRepo,
            EstudianteRepo estudianteRepo,
            EspecialidadRepo especialidadRepo,
            TutorRepo tutorRepo,
            ProgramacionRepo programacionRepo,
            ProgramacionDetalleRepo detalleRepo) {
        super(usuarioRepo);
        this.estudianteRepo = estudianteRepo;
        this.especialidadRepo = especialidadRepo;
        this.tutorRepo = tutorRepo;
        this.programacionRepo = programacionRepo;
        this.detalleRepo = detalleRepo;
    }

    @GetMapping("/horarios")
    public String horarios(Model model, Authentication auth,
            @RequestParam(required = false) String vista,
            @RequestParam(required = false) String fecha) {
        addUserToModel(model, auth);
        String vistaActual = (vista != null) ? vista : "semanal";
        LocalDate fechaRef = (fecha != null && !fecha.isBlank()) ? LocalDate.parse(fecha) : LocalDate.now();
        LocalDate hoy = LocalDate.now();

        LocalDate inicio, fin;
        if ("diaria".equals(vistaActual)) {
            inicio = fin = fechaRef;
        } else if ("semanal".equals(vistaActual)) {
            inicio = fechaRef.with(DayOfWeek.MONDAY);
            fin = fechaRef.with(DayOfWeek.SUNDAY);
        } else {
            inicio = fechaRef.withDayOfMonth(1);
            fin = fechaRef.withDayOfMonth(fechaRef.lengthOfMonth());
        }

        List<LocalDate> diasMes = new ArrayList<>();
        LocalDate lunesInicio = inicio.with(DayOfWeek.MONDAY);
        for (int i = 0; i < 42; i++)
            diasMes.add(lunesInicio.plusDays(i));

        List<LocalDate> diasSemana = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            diasSemana.add(inicio.plusDays(i));

        List<Integer> horasDia = new ArrayList<>();
        for (int h = 6; h <= 21; h++)
            horasDia.add(h);
        List<Integer> horasSemana = new ArrayList<>();
        for (int h = 7; h <= 20; h++)
            horasSemana.add(h);

        model.addAttribute("estudiantes", estudianteRepo.findByActivoTrue());
        model.addAttribute("tutores", tutorRepo.findAll());
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("detalles", detalleRepo.findByFechaRange(inicio, fin));
        model.addAttribute("vista", vistaActual);
        model.addAttribute("fechaRef", fechaRef.toString());
        model.addAttribute("hoy", hoy);
        model.addAttribute("fechaInicio", inicio.toString());
        model.addAttribute("fechaFin", fin.toString());
        model.addAttribute("inicioDt", inicio);
        model.addAttribute("finDt", fin);
        model.addAttribute("diasMes", diasMes);
        model.addAttribute("diasSemana", diasSemana);
        model.addAttribute("horasDia", horasDia);
        model.addAttribute("horasSemana", horasSemana);
        model.addAttribute("mesDt", inicio);
        model.addAttribute("activePage", "horarios");
        return "horarios";
    }

    @PostMapping("/horarios/guardar")
    public String guardar(@RequestParam List<Long> estudiantesIds,
            @RequestParam(required = false) Long tutorId,
            @RequestParam Long areaId,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            RedirectAttributes redirectAttrs) {

        Especialidad area = especialidadRepo.findById(areaId).orElse(null);
        Tutor tutor = tutorId != null ? tutorRepo.findById(tutorId).orElse(null) : null;

        if (area == null || estudiantesIds == null || estudiantesIds.isEmpty()) {
            redirectAttrs.addFlashAttribute("mensaje", "Debe seleccionar al menos un estudiante y un área.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/horarios?vista=semanal";
        }

        LocalDate dInicio = LocalDate.parse(fechaInicio);
        LocalDate dFin = (fechaFin != null && !fechaFin.isBlank()) ? LocalDate.parse(fechaFin) : dInicio;
        LocalTime tInicio = LocalTime.parse(horaInicio);
        LocalTime tFin = LocalTime.parse(horaFin);

        int count = 0;
        for (Long estId : estudiantesIds) {
            Estudiante est = estudianteRepo.findById(estId).orElse(null);
            if (est == null)
                continue;
            LocalDate cursor = dInicio;
            while (!cursor.isAfter(dFin)) {
                final LocalDate cursorFinal = cursor;
                final Tutor tutorFinal = tutor;
                Programacion prog = programacionRepo
                        .findByEstudianteAndMesAndAnio(est, cursor.getMonthValue(), cursor.getYear())
                        .orElseGet(() -> {
                            Programacion p = new Programacion(est, cursorFinal.getMonthValue(), cursorFinal.getYear());
                            p.setTutorHospital(tutorFinal);
                            return programacionRepo.save(p);
                        });
                ProgramacionDetalle det = new ProgramacionDetalle();
                det.setProgramacion(prog);
                det.setFecha(cursorFinal);
                det.setHoraInicio(tInicio);
                det.setHoraFin(tFin);
                det.setEspecialidad(area);
                detalleRepo.save(det);
                count++;
                cursor = cursor.plusDays(1);
            }
        }
        redirectAttrs.addFlashAttribute("mensaje", "Se crearon " + count + " turno(s) correctamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/horarios?vista=semanal&fecha=" + fechaInicio;
    }

    @PostMapping("/horarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
            @RequestParam(required = false) String vista,
            @RequestParam(required = false) String fecha,
            RedirectAttributes redirectAttrs) {
        try {
            detalleRepo.deleteById(id);
            redirectAttrs.addFlashAttribute("mensaje", "Turno eliminado correctamente.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("mensaje", "No se pudo eliminar el turno.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/horarios?vista=" + (vista != null ? vista : "semanal")
                + "&fecha=" + (fecha != null ? fecha : LocalDate.now());
    }
}
