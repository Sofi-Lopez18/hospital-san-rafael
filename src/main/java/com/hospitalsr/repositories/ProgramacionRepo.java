package com.hospitalsr.repositories;
import com.hospitalsr.entities.Estudiante;
import com.hospitalsr.entities.Programacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ProgramacionRepo extends JpaRepository<Programacion, Long> {
    List<Programacion> findByMesAndAnio(Integer mes, Integer anio);
    List<Programacion> findByEstudiante(Estudiante estudiante);
    Optional<Programacion> findByEstudianteAndMesAndAnio(Estudiante estudiante, Integer mes, Integer anio);
}
