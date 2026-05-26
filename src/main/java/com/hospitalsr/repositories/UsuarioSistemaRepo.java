package com.hospitalsr.repositories;

import com.hospitalsr.entities.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioSistemaRepo extends JpaRepository<UsuarioSistema, Long> {
    Optional<UsuarioSistema> findByUsername(String username);

    Optional<UsuarioSistema> findByCedula(String cedula);

    boolean existsByUsername(String username);

    boolean existsByCedula(String cedula);

    Optional<UsuarioSistema> findByEstudianteId(Long estudianteId);

    List<UsuarioSistema> findByRol(String rol);
}
