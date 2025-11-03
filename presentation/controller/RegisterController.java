package co.unicauca.gestiontrabajogrado.controller;

import co.unicauca.gestiontrabajogrado.domain.model.User;
import co.unicauca.gestiontrabajogrado.domain.model.enumProgram;
import co.unicauca.gestiontrabajogrado.domain.model.enumRol;

/**
 * Controlador para el registro de usuarios
 */
public class RegisterController {

    public RegisterController() {
    }

    /**
     * Registra un nuevo usuario
     */
    public boolean register(String nombres, String apellidos, String identificacion,
                           String celular, enumProgram programa, enumRol rol,
                           String email, String password) {
        // TODO: Implementar llamada al microservicio de identity
        // Validaciones básicas
        if (nombres == null || nombres.isEmpty()) return false;
        if (apellidos == null || apellidos.isEmpty()) return false;
        if (email == null || email.isEmpty()) return false;
        if (password == null || password.isEmpty()) return false;

        // Por ahora retornar true para simular éxito
        return true;
    }

    /**
     * Valida si el email ya existe
     */
    public boolean emailExists(String email) {
        // TODO: Implementar validación con microservicio
        return false;
    }

    /**
     * Maneja el proceso de registro completo desde la vista
     */
    public void handleRegister(String nombres, String apellidos, String identificacion,
                              String celular, enumProgram programa, enumRol rol,
                              String email, String password, String passwordConfirm) {
        // Validar que las contraseñas coincidan
        if (!password.equals(passwordConfirm)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Las contraseñas no coinciden",
                "Error de Registro",
                javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Intentar registrar
        if (register(nombres, apellidos, identificacion, celular, programa, rol, email, password)) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "¡Registro exitoso! Ya puede iniciar sesión.",
                "Éxito",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(null,
                "Error al registrar el usuario. Por favor verifique los datos.",
                "Error de Registro",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja el botón de volver al login
     */
    public void handleVolverLogin() {
        System.out.println("Volviendo a la pantalla de login...");
        try {
            co.unicauca.gestiontrabajogrado.presentation.auth.LoginView loginView =
                new co.unicauca.gestiontrabajogrado.presentation.auth.LoginView();
            loginView.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error al abrir LoginView: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

