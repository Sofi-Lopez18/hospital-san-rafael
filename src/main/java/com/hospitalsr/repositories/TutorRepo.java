package com.hospitalsr.repositories;
import com.hospitalsr.entities.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface TutorRepo extends JpaRepository<Tutor, Long> {
    List<Tutor> findByTipo(String tipo);
    Optional<Tutor> findByCedula(String cedula);
}
