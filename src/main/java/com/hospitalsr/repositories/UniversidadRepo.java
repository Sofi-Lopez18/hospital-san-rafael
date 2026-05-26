package com.hospitalsr.repositories;
import com.hospitalsr.entities.Universidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface UniversidadRepo extends JpaRepository<Universidad, Long> {
    List<Universidad> findByEstadoTrue();
}
