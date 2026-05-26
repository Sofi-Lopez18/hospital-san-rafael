package com.hospitalsr.repositories;
import com.hospitalsr.entities.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface EspecialidadRepo extends JpaRepository<Especialidad, Long> {}
