package com.hospitalsr.config;

import com.hospitalsr.entities.*;
import com.hospitalsr.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioSistemaRepo usuarioRepo;
    @Autowired
    private UniversidadRepo universidadRepo;
    @Autowired
    private EspecialidadRepo especialidadRepo;
    @Autowired
    private TutorRepo tutorRepo;
    @Autowired
    private EstudianteRepo estudianteRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin por defecto
        if (!usuarioRepo.existsByUsername("admin")) {
            UsuarioSistema admin = new UsuarioSistema(
                    "Administrador del Sistema", "admin",
                    passwordEncoder.encode("admin123"), "00000000", "ADMINISTRADOR");
            admin.setCorreo("admin@hospitalsr.edu.co");
            usuarioRepo.save(admin);
        }

        // Universidades
        if (universidadRepo.count() == 0) {
            universidadRepo.save(new Universidad("Universidad de Boyacá", "Tunja"));
            universidadRepo.save(new Universidad("Universidad Pedagógica y Tecnológica de Colombia", "Tunja"));
            universidadRepo.save(new Universidad("Uniboyacá", "Tunja"));
            universidadRepo.save(new Universidad("Otra Universidad", "Tunja"));
        }

        // Áreas hospitalarias
        if (especialidadRepo.count() == 0) {
            especialidadRepo.save(new Especialidad("Urgencias", "Tunja - Sede Principal", 5));
            especialidadRepo.save(new Especialidad("Cirugía", "Tunja - Sede Principal", 6));
            especialidadRepo.save(new Especialidad("Pediatría", "Tunja - Sede Principal", 3));
            especialidadRepo.save(new Especialidad("Cardiología", "Tunja - Sede Principal", 4));
            especialidadRepo.save(new Especialidad("Medicina Interna", "Tunja - Sede Principal", 5));
            especialidadRepo.save(new Especialidad("Ginecología", "Tunja - Sede Principal", 4));
        }

        // Usuarios del sistema
        if (tutorRepo.count() == 0) {
            if (!usuarioRepo.existsByUsername("mgonzalez")) {
                UsuarioSistema dir = new UsuarioSistema("Dr. María González", "mgonzalez",
                        passwordEncoder.encode("director123"), "52123456", "DIRECTOR");
                usuarioRepo.save(dir);
            }
            if (!usuarioRepo.existsByUsername("cmartinez")) {
                UsuarioSistema med = new UsuarioSistema("Dr. Carlos Martínez", "cmartinez",
                        passwordEncoder.encode("medico123"), "79234567", "MEDICO");
                usuarioRepo.save(med);
            }
            if (!usuarioRepo.existsByUsername("alopez")) {
                UsuarioSistema doc = new UsuarioSistema("Dra. Ana López", "alopez",
                        passwordEncoder.encode("docente123"), "52345678", "DOCENTE");
                usuarioRepo.save(doc);
            }
            Tutor t1 = new Tutor("Dr. María González", "HOSPITAL", "52123456", "Director");
            Tutor t2 = new Tutor("Dr. Carlos Martínez", "HOSPITAL", "79234567", "Medico");
            Tutor t3 = new Tutor("Dra. Ana López", "HOSPITAL", "52345678", "Docente");
            tutorRepo.save(t1);
            tutorRepo.save(t2);
            tutorRepo.save(t3);
        }

        // Estudiantes de prueba
        if (estudianteRepo.count() == 0) {
            var univs = universidadRepo.findAll();
            if (univs.size() >= 2) {
                Estudiante e1 = new Estudiante("Juan Carlos", "Pérez García", "1001234567", univs.get(0), "Medicina");
                e1.setTipoVinculacion("Estudiante en práctica");
                e1.setSemestre(8);
                Estudiante e2 = new Estudiante("María Fernanda", "López Ramírez", "1002345678", univs.get(1),
                        "Enfermería");
                e2.setTipoVinculacion("Estudiante en práctica");
                e2.setSemestre(6);
                Estudiante e3 = new Estudiante("Carlos Andrés", "Martínez Silva", "1003456789", univs.get(0),
                        "Medicina");
                e3.setTipoVinculacion("Médico Interno");
                e3.setSemestre(10);
                Estudiante e4 = new Estudiante("Andrea Carolina", "Rodríguez Gómez", "1004567890", univs.get(1),
                        "Fisioterapia");
                e4.setTipoVinculacion("Estudiante en práctica");
                e4.setSemestre(7);
                estudianteRepo.save(e1);
                estudianteRepo.save(e2);
                estudianteRepo.save(e3);
                estudianteRepo.save(e4);
            }
        }

        // ── Crear usuarios para estudiantes que no tengan uno ──────────────
        estudianteRepo.findAll().forEach(est -> {
            boolean yaExiste = usuarioRepo.existsByUsername(est.getDocumento())
                    || usuarioRepo.existsByCedula(est.getDocumento());
            if (!yaExiste) {
                UsuarioSistema u = new UsuarioSistema();
                u.setNombre(est.getNombres() + " " + est.getApellidos());
                u.setUsername(est.getDocumento());
                u.setCedula(est.getDocumento());
                u.setPassword(passwordEncoder.encode(est.getDocumento()));
                u.setRol("ESTUDIANTE");
                u.setCorreo(est.getCorreo());
                u.setActivo(est.getActivo());
                u.setEstudianteId(est.getId());
                usuarioRepo.save(u);
            }
        });
    }
}