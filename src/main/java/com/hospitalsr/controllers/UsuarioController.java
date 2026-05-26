package com.hospitalsr.controllers;

import com.hospitalsr.entities.UsuarioSistema;
import com.hospitalsr.repositories.EstudianteRepo;
import com.hospitalsr.repositories.UsuarioSistemaRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController extends BaseController {

    private final PasswordEncoder passwordEncoder;
    private final EstudianteRepo estudianteRepo;

    public UsuarioController(UsuarioSistemaRepo usuarioRepo,
            PasswordEncoder passwordEncoder,
            EstudianteRepo estudianteRepo) {
        super(usuarioRepo);
        this.passwordEncoder = passwordEncoder;
        this.estudianteRepo = estudianteRepo;
    }

    @GetMapping("/usuarios")
    public String listar(Model model, Authentication auth) {
        addUserToModel(model, auth);
        model.addAttribute("usuarios", usuarioRepo.findAll());
        model.addAttribute("activePage", "usuarios");
        return "usuarios";
    }

    @PostMapping("/usuarios/guardar")
    public String guardar(@RequestParam String nombre,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String cedula,
            @RequestParam String rol,
            @RequestParam(required = false) Long usuarioId,
            RedirectAttributes redirectAttrs) {
        UsuarioSistema u = usuarioId != null
                ? usuarioRepo.findById(usuarioId).orElse(new UsuarioSistema())
                : new UsuarioSistema();

        u.setNombre(nombre);
        u.setUsername(username);
        u.setCedula(cedula);
        u.setRol(rol);
        if (password != null && !password.isBlank()) {
            u.setPassword(passwordEncoder.encode(password));
        }
        if (u.getActivo() == null)
            u.setActivo(true);

        // Si el rol es ESTUDIANTE, intentar vincular automáticamente
        if ("ESTUDIANTE".equals(rol)) {
            estudianteRepo.findAll().stream()
                    .filter(e -> cedula.equals(e.getDocumento()))
                    .findFirst()
                    .ifPresent(e -> u.setEstudianteId(e.getId()));
        }

        usuarioRepo.save(u);
        redirectAttrs.addFlashAttribute("mensaje", "Usuario guardado correctamente.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        usuarioRepo.deleteById(id);
        redirectAttrs.addFlashAttribute("mensaje", "Usuario eliminado.");
        redirectAttrs.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/usuarios";
    }
}