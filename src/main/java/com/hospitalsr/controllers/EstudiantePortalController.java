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

import java.nio.file.*;
import java.time.*;
import java.util.List;

@Controller
@RequestMapping("/estudiante")
public class EstudiantePortalController extends BaseController {

    private final EstudianteRepo estudianteRepo;
    private final AccesoRepo accesoRepo;
    private final EspecialidadRepo especialidadRepo;
    private final ProgramacionDetalleRepo detalleRepo;
    private final PasswordEncoder passwordEncoder;

    public EstudiantePortalController(UsuarioSistemaRepo usuarioRepo,
            EstudianteRepo estudianteRepo,
            AccesoRepo accesoRepo,
            EspecialidadRepo especialidadRepo,
            ProgramacionDetalleRepo detalleRepo,
            PasswordEncoder passwordEncoder) {
        super(usuarioRepo);
        this.estudianteRepo = estudianteRepo;
        this.accesoRepo = accesoRepo;
        this.especialidadRepo = especialidadRepo;
        this.detalleRepo = detalleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    private Estudiante getEstudianteActual(Authentication auth) {
        return usuarioRepo.findByUsername(auth.getName())
                .filter(u -> u.getEstudianteId() != null)
                .flatMap(u -> estudianteRepo.findById(u.getEstudianteId()))
                .orElse(null);
    }

    private UsuarioSistema getUsuarioActual(Authentication auth) {
        return usuarioRepo.findByUsername(auth.getName()).orElse(null);
    }

    @GetMapping("/presencia")
    public String presencia(Model model, Authentication auth) {
        addUserToModel(model, auth);
        Estudiante est = getEstudianteActual(auth);
        if (est == null)
            return "redirect:/login?error=true";

        LocalDate hoy = LocalDate.now();
        List<Acceso> historial = accesoRepo.findByEstudianteOrderByFechaDesc(est);
        boolean dentroAhora = historial.stream()
                .anyMatch(a -> hoy.equals(a.getFecha()) && "DENTRO".equals(a.getEstado()));

        model.addAttribute("estudiante", est);
        model.addAttribute("historial", historial);
        model.addAttribute("dentroAhora", dentroAhora);
        model.addAttribute("areas", especialidadRepo.findAll());
        model.addAttribute("activePage", "est-presencia");
        return "estudiante/presencia";
    }

    @PostMapping("/presencia/checkin")
    public String checkin(Authentication auth,
            @RequestParam(required = false) Long areaId,
            RedirectAttributes redirectAttrs) {
        Estudiante est = getEstudianteActual(auth);
        if (est == null)
            return "redirect:/login";

        LocalDate hoy = LocalDate.now();
        boolean yaEntro = accesoRepo.findByEstudianteOrderByFechaDesc(est).stream()
                .anyMatch(a -> hoy.equals(a.getFecha()) && "DENTRO".equals(a.getEstado()));

        if (yaEntro) {
            redirectAttrs.addFlashAttribute("mensaje", "Ya registraste tu entrada hoy.");
            redirectAttrs.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/estudiante/presencia";
        }

        Acceso acceso = new Acceso();
        acceso.setEstudiante(est);
        acceso.setFecha(hoy);
        acceso.setHoraIngreso(LocalDateTime.now());
        acceso.setEstado("DENTRO");
        if (areaId != null)
            especialidadRepo.findById(areaId).ifPresent(acceso::setEspecialidad);
        accesoRepo.save(acceso);

        redirectAttrs.addFlashAttribute("mensaje", "Entrada registrada correctamente. ¡Buen turno!");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/estudiante/presencia";
    }

    @PostMapping("/presencia/checkout")
    public String checkout(Authentication auth, RedirectAttributes redirectAttrs) {
        Estudiante est = getEstudianteActual(auth);
        if (est == null)
            return "redirect:/login";

        LocalDate hoy = LocalDate.now();
        accesoRepo.findByEstudianteOrderByFechaDesc(est).stream()
                .filter(a -> hoy.equals(a.getFecha()) && "DENTRO".equals(a.getEstado()))
                .findFirst()
                .ifPresentOrElse(a -> {
                    a.setHoraSalida(LocalDateTime.now());
                    a.setEstado("FUERA");
                    accesoRepo.save(a);
                    redirectAttrs.addFlashAttribute("mensaje", "Salida registrada correctamente. ¡Hasta pronto!");
                    redirectAttrs.addFlashAttribute("tipoMensaje", "success");
                }, () -> {
                    redirectAttrs.addFlashAttribute("mensaje", "No tienes una entrada activa hoy.");
                    redirectAttrs.addFlashAttribute("tipoMensaje", "error");
                });
        return "redirect:/estudiante/presencia";
    }

    @GetMapping("/mi-horario")
    public String miHorario(Model model, Authentication auth,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {
        addUserToModel(model, auth);
        Estudiante est = getEstudianteActual(auth);
        if (est == null)
            return "redirect:/login";

        LocalDate now = LocalDate.now();
        int m = mes != null ? mes : now.getMonthValue();
        int a = anio != null ? anio : now.getYear();

        List<ProgramacionDetalle> turnos = detalleRepo.findByMesAndAnio(m, a).stream()
                .filter(d -> d.getProgramacion().getEstudiante().getId().equals(est.getId()))
                .sorted(java.util.Comparator.comparing(ProgramacionDetalle::getFecha))
                .toList();

        model.addAttribute("estudiante", est);
        model.addAttribute("turnos", turnos);
        model.addAttribute("mes", m);
        model.addAttribute("anio", a);
        model.addAttribute("mesDate", LocalDate.of(a, m, 1));
        model.addAttribute("activePage", "est-horario");
        return "estudiante/mi-horario";
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Authentication auth) {
        addUserToModel(model, auth);
        Estudiante est = getEstudianteActual(auth);
        if (est == null)
            return "redirect:/login";
        model.addAttribute("estudiante", est);
        model.addAttribute("activePage", "est-perfil");
        return "estudiante/perfil";
    }

    @PostMapping("/perfil/guardar")
    public String guardarPerfil(
            Authentication auth,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String estadoCivil,
            @RequestParam(required = false) String fechaNacimiento,
            @RequestParam(required = false) String lugarNacimiento,
            @RequestParam(required = false) String direccionTunja,
            @RequestParam(required = false) String celular,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String grupoSanguineo,
            @RequestParam(required = false) String programa,
            @RequestParam(required = false) Integer semestre,
            @RequestParam(required = false) java.math.BigDecimal promedio,
            @RequestParam(required = false) String tipoVinculacion,
            // Archivos
            @RequestParam(required = false) MultipartFile foto,
            @RequestParam(required = false) String fotoPathActual,
            @RequestParam(required = false) MultipartFile documentoSoporte,
            @RequestParam(required = false) MultipartFile documentoVacunas,
            @RequestParam(required = false) MultipartFile certificadoCursos,
            // Contraseña
            @RequestParam(required = false) String passwordActual,
            @RequestParam(required = false) String passwordNuevo,
            RedirectAttributes redirectAttrs) {

        Estudiante est = getEstudianteActual(auth);
        UsuarioSistema u = getUsuarioActual(auth);
        if (est == null || u == null)
            return "redirect:/login";

        if (nombres != null && !nombres.isBlank())
            est.setNombres(nombres);
        if (apellidos != null && !apellidos.isBlank())
            est.setApellidos(apellidos);
        est.setEstadoCivil(estadoCivil);
        est.setLugarNacimiento(lugarNacimiento);
        est.setDireccionTunja(direccionTunja);
        est.setCelular(celular);
        est.setCorreo(correo);
        est.setGrupoSanguineo(grupoSanguineo);
        est.setPrograma(programa);
        est.setSemestre(semestre);
        est.setPromedio(promedio);
        if (tipoVinculacion != null && !tipoVinculacion.isBlank())
            est.setTipoVinculacion(tipoVinculacion);
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            try {
                est.setFechaNacimiento(java.time.LocalDate.parse(fechaNacimiento));
            } catch (Exception ignored) {
            }
        }

        // Guardar archivos
        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);

            String fotoNombre = guardarArchivo(foto, uploadDir, est.getDocumento() + "_foto");
            if (fotoNombre != null)
                est.setFotoPath(fotoNombre);
            else if (fotoPathActual != null && !fotoPathActual.isBlank())
                est.setFotoPath(fotoPathActual);

            String docNombre = guardarArchivo(documentoSoporte, uploadDir, est.getDocumento() + "_hdv");
            if (docNombre != null)
                est.setDocumentoSoportePath(docNombre);

            String vacNombre = guardarArchivo(documentoVacunas, uploadDir, est.getDocumento() + "_vacunas");
            if (vacNombre != null)
                est.setDocumentoVacunasPath(vacNombre);

            String certNombre = guardarArchivo(certificadoCursos, uploadDir, est.getDocumento() + "_cursos");
            if (certNombre != null)
                est.setCertificadoCursosPath(certNombre);

        } catch (Exception ignored) {
        }

        estudianteRepo.save(est);

        if (correo != null) {
            u.setCorreo(correo);
            u.setNombre(est.getNombreCompleto());
        }

        if (passwordNuevo != null && !passwordNuevo.isBlank()) {
            if (passwordActual != null && passwordEncoder.matches(passwordActual, u.getPassword())) {
                u.setPassword(passwordEncoder.encode(passwordNuevo));
                redirectAttrs.addFlashAttribute("mensaje", "Perfil y contraseña actualizados correctamente.");
            } else {
                redirectAttrs.addFlashAttribute("mensaje", "La contraseña actual no es correcta.");
                redirectAttrs.addFlashAttribute("tipoMensaje", "error");
                usuarioRepo.save(u);
                return "redirect:/estudiante/perfil";
            }
        } else {
            redirectAttrs.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");
        }

        usuarioRepo.save(u);
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/estudiante/perfil";
    }
}