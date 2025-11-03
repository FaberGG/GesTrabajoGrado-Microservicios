package co.unicauca.gestiontrabajogrado.domain.model;

/**
 * Modelo simplificado de Usuario para el cliente de escritorio
 * Representa los datos básicos del usuario autenticado
 */
public class User {
    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    private enumRol rol;
    private enumProgram programa;

    public User() {
    }

    public User(Long id, String nombres, String apellidos, String email, enumRol rol) {
        this.id = id;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
    }

    // Getters y Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public enumRol getRol() {
        return rol;
    }

    public void setRol(enumRol rol) {
        this.rol = rol;
    }

    public enumProgram getPrograma() {
        return programa;
    }

    public void setPrograma(enumProgram programa) {
        this.programa = programa;
    }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", rol=" + rol +
                '}';
    }
}

