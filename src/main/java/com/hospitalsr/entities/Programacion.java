package com.hospitalsr.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "programacion", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"estudiante_id", "mes", "anio"})
})
public class Programacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_universidad_id")
    private Tutor tutorUniversidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_hospital_id")
    private Tutor tutorHospital;

    @OneToMany(mappedBy = "programacion", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProgramacionDetalle> detalles = new ArrayList<>();

    public Programacion() {}
    public Programacion(Estudiante estudiante, Integer mes, Integer anio) {
        this.estudiante = estudiante; this.mes = mes; this.anio = anio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }
    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }
    public Tutor getTutorUniversidad() { return tutorUniversidad; }
    public void setTutorUniversidad(Tutor tutorUniversidad) { this.tutorUniversidad = tutorUniversidad; }
    public Tutor getTutorHospital() { return tutorHospital; }
    public void setTutorHospital(Tutor tutorHospital) { this.tutorHospital = tutorHospital; }
    public List<ProgramacionDetalle> getDetalles() { return detalles; }
    public void setDetalles(List<ProgramacionDetalle> detalles) { this.detalles = detalles; }
}
