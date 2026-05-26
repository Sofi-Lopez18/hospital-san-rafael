package com.hospitalsr.controllers;

import com.hospitalsr.repositories.UsuarioSistemaRepo;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

/**
 * Clase base con utilidades compartidas por todos los controladores.
 */
public abstract class BaseController {

    protected final UsuarioSistemaRepo usuarioRepo;

    protected BaseController(UsuarioSistemaRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    protected void addUserToModel(Model model, Authentication auth) {
        if (auth != null) {
            usuarioRepo.findByUsername(auth.getName())
                    .ifPresent(u -> model.addAttribute("usuarioActual", u));
        }
    }

    protected String guardarArchivo(org.springframework.web.multipart.MultipartFile file,
            java.nio.file.Path dir, String prefix) throws Exception {
        if (file != null && !file.isEmpty()) {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains("."))
                    ? original.substring(original.lastIndexOf('.'))
                    : "";
            String nombre = prefix + ext;
            file.transferTo(dir.resolve(nombre).toFile());
            return nombre;
        }
        return null;
    }
}