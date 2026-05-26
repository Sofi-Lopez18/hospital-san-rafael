package com.hospitalsr.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estudiante")
public class Estudiante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombres;

    @Column(nullable = false, length = 150)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 50)
    private String documento;

    @Column(name = "estado_civil", length = 50)
    private String estadoCivil;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "lugar_nacimiento", length = 100)
    private String lugarNacimiento;

    @Column(name = "direccion_tunja", length = 200)
    private String direccionTunja;

    @Column(length = 20)
    private String celular;

    @Column(length = 150)
    private String correo;

    @Column(length = 150)
    private String programa;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    private Integer semestre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "universidad_id")
    private Universidad universidad;

    @Column(precision = 4, scale = 2)
    private BigDecimal promedio;

    @Column(name = "tipo_vinculacion", length = 100)
    private String tipoVinculacion;

    @Column(name = "grupo_sanguineo", length = 10)
    private String grupoSanguineo;

    @Column(name = "induccion_completa")
    private Boolean induccionCompleta = false;

    @Column(name = "vacunas_completas")
    private Boolean vacunasCompletas = false;

    private Boolean activo = true;

    // ── Rutas de archivos adjuntos ──────────────────────────────────────
    @Column(name = "foto_path", length = 255)
    private String fotoPath;

    @Column(name = "documento_soporte_path", length = 255)
    private String documentoSoportePath;

    @Column(name = "documento_vacunas_path", length = 255)
    private String documentoVacunasPath;

    @Column(name = "certificado_cursos_path", length = 255)
    private String certificadoCursosPath;
    // ────────────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Programacion> programaciones = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Acceso> accesos = new ArrayList<>();

    public Estudiante() {
    }

    public Estudiante(String nombres, String apellidos, String documento, Universidad universidad, String programa) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.documento = documento;
        this.universidad = universidad;
        this.programa = programa;
        this.activo = true;
        this.tipoVinculacion = "Estudiante en práctica";
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // ── Getters & Setters ────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getLugarNacimiento() {
        return lugarNacimiento;
    }

    public void setLugarNacimiento(String lugarNacimiento) {
        this.lugarNacimiento = lugarNacimiento;
    }

    public String getDireccionTunja() {
        return direccionTunja;
    }

    public void setDireccionTunja(String direccionTunja) {
        this.direccionTunja = direccionTunja;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }

    public Universidad getUniversidad() {
        return universidad;
    }

    public void setUniversidad(Universidad universidad) {
        this.universidad = universidad;
    }

    public BigDecimal getPromedio() {
        return promedio;
    }

    public void setPromedio(BigDecimal promedio) {
        this.promedio = promedio;
    }

    public String getTipoVinculacion() {
        return tipoVinculacion;
    }

    public void setTipoVinculacion(String tipoVinculacion) {
        this.tipoVinculacion = tipoVinculacion;
    }

    public String getGrupoSanguineo() {
        return grupoSanguineo;
    }

    public void setGrupoSanguineo(String grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    public Boolean getInduccionCompleta() {
        return induccionCompleta;
    }

    public void setInduccionCompleta(Boolean induccionCompleta) {
        this.induccionCompleta = induccionCompleta;
    }

    public Boolean getVacunasCompletas() {
        return vacunasCompletas;
    }

    public void setVacunasCompletas(Boolean vacunasCompletas) {
        this.vacunasCompletas = vacunasCompletas;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getDocumentoSoportePath() {
        return documentoSoportePath;
    }

    public void setDocumentoSoportePath(String documentoSoportePath) {
        this.documentoSoportePath = documentoSoportePath;
    }

    public String getDocumentoVacunasPath() {
        return documentoVacunasPath;
    }

    public void setDocumentoVacunasPath(String documentoVacunasPath) {
        this.documentoVacunasPath = documentoVacunasPath;
    }

    public String getCertificadoCursosPath() {
        return certificadoCursosPath;
    }

    public void setCertificadoCursosPath(String certificadoCursosPath) {
        this.certificadoCursosPath = certificadoCursosPath;
    }

    public List<Programacion> getProgramaciones() {
        return programaciones;
    }

    public void setProgramaciones(List<Programacion> programaciones) {
        this.programaciones = programaciones;
    }

    public List<Acceso> getAccesos() {
        return accesos;
    }

    public void setAccesos(List<Acceso> accesos) {
        this.accesos = accesos;
    }
}