package com.hospitalsr.controllers;

import com.hospitalsr.entities.*;
import com.hospitalsr.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class EstudianteController extends BaseController {

    private final EstudianteRepo estudianteRepo;
    private final UniversidadRepo universidadRepo;
    private final PasswordEncoder passwordEncoder;

    public EstudianteController(UsuarioSistemaRepo usuarioRepo,
            EstudianteRepo estudianteRepo,
            UniversidadRepo universidadRepo,
            PasswordEncoder passwordEncoder) {
        super(usuarioRepo);
        this.estudianteRepo = estudianteRepo;
        this.universidadRepo = universidadRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/estudiantes")
    public String listar(Model model, Authentication auth,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false, defaultValue = "activos") String filtro) {
        addUserToModel(model, auth);
        java.util.List<Estudiante> lista;
        if (buscar != null && !buscar.isBlank()) {
            lista = estudianteRepo.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(buscar, buscar);
        } else if ("todos".equals(filtro)) {
            lista = estudianteRepo.findAll();
        } else {
            lista = estudianteRepo.findByActivoTrue();
        }
        model.addAttribute("estudiantes", lista);
        model.addAttribute("buscar", buscar);
        model.addAttribute("filtro", filtro);
        model.addAttribute("universidades", universidadRepo.findByEstadoTrue());
        model.addAttribute("activePage", "estudiantes");
        return "estudiantes";
    }

    @PostMapping("/estudiantes/guardar")
    public String guardar(
            @RequestParam String nombres,
            @RequestParam String apellidos,
            @RequestParam String documento,
            @RequestParam Long universidadId,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) String tipoVinculacion,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String celular,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) BigDecimal promedio,
            @RequestParam(required = false) String estadoCivil,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String lugarNacimiento,
            @RequestParam(required = false) String direccionTunja,
            @RequestParam(required = false) String grupoSanguineo,
            @RequestParam(required = false) String fechaIngreso,
            @RequestParam(required = false) Boolean vacunasCompletas,
            @RequestParam(required = false) Boolean induccionCompleta,
            @RequestParam(required = false) Long estudianteId,
            @RequestParam(required = false) String fotoPathActual,
            @RequestParam(required = false) MultipartFile foto,
            @RequestParam(required = false) MultipartFile documentoSoporte,
            @RequestParam(required = false) MultipartFile documentoVacunas,
            @RequestParam(required = false) MultipartFile certificadoCursos,
            RedirectAttributes redirectAttrs) {

        boolean esNuevo = (estudianteId == null);
        Estudiante est = esNuevo
                ? new Estudiante()
                : estudianteRepo.findById(estudianteId).orElse(new Estudiante());

        est.setNombres(nombres);
        est.setApellidos(apellidos);
        est.setDocumento(documento);
        est.setPrograma(programa);
        est.setTipoVinculacion(tipoVinculacion != null ? tipoVinculacion : "Estudiante en práctica");
        est.setCorreo(correo);
        est.setCelular(celular);
        est.setSemestre(semestre);
        est.setPromedio(promedio);
        est.setEstadoCivil(estadoCivil);
        est.setLugarNacimiento(lugarNacimiento);
        est.setDireccionTunja(direccionTunja);
        est.setGrupoSanguineo(grupoSanguineo);
        est.setVacunasCompletas(Boolean.TRUE.equals(vacunasCompletas));
        est.setInduccionCompleta(Boolean.TRUE.equals(induccionCompleta));

        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            try {
                est.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            } catch (Exception ignored) {
            }
        }
        if (fechaIngreso != null && !fechaIngreso.isBlank()) {
            try {
                est.setFechaIngreso(LocalDate.parse(fechaIngreso));
            } catch (Exception ignored) {
            }
        }

        universidadRepo.findById(universidadId).ifPresent(est::setUniversidad);
        if (esNuevo)
            est.setActivo(true);

        Path uploadDir = Paths.get("uploads");
        try {
            java.nio.file.Files.createDirectories(uploadDir);
            String fotoNombre = guardarArchivo(foto, uploadDir, documento + "_foto");
            String docNombre = guardarArchivo(documentoSoporte, uploadDir, documento + "_hdv");
            String vacNombre = guardarArchivo(documentoVacunas, uploadDir, documento + "_vacunas");
            String certNombre = guardarArchivo(certificadoCursos, uploadDir, documento + "_cursos");

            if (fotoNombre != null)
                est.setFotoPath(fotoNombre);
            else if (fotoPathActual != null && !fotoPathActual.isBlank())
                est.setFotoPath(fotoPathActual);
            if (docNombre != null)
                est.setDocumentoSoportePath(docNombre);
            if (vacNombre != null)
                est.setDocumentoVacunasPath(vacNombre);
            if (certNombre != null)
                est.setCertificadoCursosPath(certNombre);
        } catch (Exception e) {
            if (fotoPathActual != null && !fotoPathActual.isBlank())
                est.setFotoPath(fotoPathActual);
        }

        Estudiante guardado = estudianteRepo.save(est);

        if (esNuevo) {
            boolean yaExiste = usuarioRepo.existsByUsername(documento) || usuarioRepo.existsByCedula(documento);
            if (!yaExiste) {
                UsuarioSistema u = new UsuarioSistema();
                u.setNombre(nombres + " " + apellidos);
                u.setUsername(documento);
                u.setCedula(documento);
                u.setPassword(passwordEncoder.encode(documento));
                u.setRol("ESTUDIANTE");
                u.setCorreo(correo);
                u.setActivo(true);
                u.setEstudianteId(guardado.getId());
                usuarioRepo.save(u);
            }
        }

        redirectAttrs.addFlashAttribute("mensaje", "Estudiante guardado correctamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/estudiantes";
    }

    // ── Toggle activo/inactivo ─────────────────────────────────────────
    @PostMapping("/estudiantes/eliminar/{id}")
    public String toggleActivo(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        estudianteRepo.findById(id).ifPresent(e -> {
            e.setActivo(!Boolean.TRUE.equals(e.getActivo()));
            estudianteRepo.save(e);
            usuarioRepo.findByEstudianteId(id).ifPresent(u -> {
                u.setActivo(e.getActivo());
                usuarioRepo.save(u);
            });
        });
        String estado = estudianteRepo.findById(id)
                .map(e -> Boolean.TRUE.equals(e.getActivo()) ? "activado" : "desactivado").orElse("actualizado");
        redirectAttrs.addFlashAttribute("mensaje", "Estudiante " + estado + " correctamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/estudiantes";
    }

    // ── Eliminar definitivamente ───────────────────────────────────────
    @PostMapping("/estudiantes/borrar/{id}")
    public String borrarDefinitivo(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        estudianteRepo.findById(id).ifPresent(e -> {
            usuarioRepo.findByEstudianteId(id).ifPresent(usuarioRepo::delete);
            estudianteRepo.delete(e);
        });
        redirectAttrs.addFlashAttribute("mensaje", "Estudiante eliminado definitivamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/estudiantes";
    }
}