package co.unicauca.gestiontrabajogrado.controller;

import co.unicauca.gestiontrabajogrado.domain.model.User;
import co.unicauca.gestiontrabajogrado.domain.model.enumRol;
import co.unicauca.gestiontrabajogrado.domain.service.IAutenticacionService;
import co.unicauca.gestiontrabajogrado.presentation.auth.LoginView;

/**
 * Controlador para el manejo de login
 */
public class LoginController {

    private static User currentUser;
    private IAutenticacionService autenticacionService;
    private LoginView loginView;

    public LoginController() {
    }

    public LoginController(IAutenticacionService autenticacionService, LoginView loginView) {
        this.autenticacionService = autenticacionService;
        this.loginView = loginView;
    }

    /**
     * Intenta autenticar al usuario
     *
     * CREDENCIALES DE PRUEBA:
     * - DOCENTE: email: docente@unicauca.edu.co, password: docente123
     * - COORDINADOR: email: coordinador@unicauca.edu.co, password: coord123
     * - ESTUDIANTE: email: estudiante@unicauca.edu.co, password: est123
     */
    public boolean login(String email, String password, boolean rememberMe) {
        // TODO: Implementar llamada al microservicio de identity
        // Por ahora, simulación con usuarios de prueba

        // Validar credenciales de DOCENTE
        if ("docente@unicauca.edu.co".equals(email) && "docente123".equals(password)) {
            currentUser = new User(1L, "Juan Carlos", "García López", email, enumRol.DOCENTE);
            System.out.println("✓ Login exitoso como DOCENTE");
            return true;
        }

        // Validar credenciales de ADMIN
        if ("admin@unicauca.edu.co".equals(email) && "admin123".equals(password)) {
            currentUser = new User(2L, "María", "Rodríguez", email, enumRol.ADMIN);
            System.out.println("✓ Login exitoso como ADMIN");
            return true;
        }

        // Validar credenciales de ESTUDIANTE
        if ("estudiante@unicauca.edu.co".equals(email) && "est123".equals(password)) {
            currentUser = new User(3L, "Pedro", "Martínez", email, enumRol.ESTUDIANTE);
            System.out.println("✓ Login exitoso como ESTUDIANTE");
            return true;
        }

        System.out.println("✗ Credenciales inválidas");
        return false;
    }

    /**
     * Maneja el proceso de login desde la vista
     */
    public void handleLogin(String email, String password, boolean rememberMe) {
        if (login(email, password, rememberMe)) {
            System.out.println("Login exitoso para: " + email);

            // Cerrar la vista de login
            if (loginView != null) {
                loginView.dispose();
            }

            // Abrir la vista correspondiente según el rol del usuario
            abrirVistaPorRol();
        } else {
            System.out.println("Login fallido para: " + email);
            javax.swing.JOptionPane.showMessageDialog(null,
                "Credenciales inválidas. Por favor verifique su email y contraseña.",
                "Error de Login",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre la vista correspondiente según el rol del usuario
     */
    private void abrirVistaPorRol() {
        if (currentUser == null) {
            System.err.println("Error: Usuario actual es null");
            return;
        }

        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                switch (currentUser.getRol()) {
                    case DOCENTE:
                        abrirVistaDocente();
                        break;
                    case ADMIN:
                        javax.swing.JOptionPane.showMessageDialog(null,
                            "Vista de Administrador en desarrollo",
                            "Información",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case ESTUDIANTE:
                        javax.swing.JOptionPane.showMessageDialog(null,
                            "Vista de Estudiante en desarrollo",
                            "Información",
                            javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        break;
                    default:
                        javax.swing.JOptionPane.showMessageDialog(null,
                            "Rol no reconocido",
                            "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                System.err.println("Error al abrir vista: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Abre la vista del docente
     */
    private void abrirVistaDocente() {
        try {
            // Crear la vista del docente pasando el usuario actual
            // DocenteView ya es un JFrame, no necesita ser agregado a otro JFrame
            co.unicauca.gestiontrabajogrado.presentation.dashboard.docenteview.DocenteView docenteView =
                new co.unicauca.gestiontrabajogrado.presentation.dashboard.docenteview.DocenteView(currentUser);

            // DocenteView ya configura su propio título, tamaño y posición
            // Solo necesitamos hacerlo visible
            docenteView.setVisible(true);

            System.out.println("✓ Vista de Docente abierta exitosamente");
        } catch (Exception e) {
            System.err.println("✗ Error al abrir DocenteView: " + e.getMessage());
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                "Error al abrir la vista del docente:\n" + e.getMessage(),
                "Error",
                javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre la vista de registro
     */
    public void handleRegistrarse() {
        System.out.println("Abriendo vista de registro...");
        try {
            co.unicauca.gestiontrabajogrado.presentation.auth.RegisterView registerView =
                new co.unicauca.gestiontrabajogrado.presentation.auth.RegisterView();
            registerView.setVisible(true);
        } catch (Exception e) {
            System.err.println("Error al abrir RegisterView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el email guardado (si existe)
     */
    public String getRememberedEmail() {
        // TODO: Implementar carga desde preferencias
        return "";
    }

    /**
     * Cierra la sesión del usuario
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Obtiene el usuario actual autenticado
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Guarda las credenciales para recordar
     */
    public void saveCredentials(String email, String password) {
        // TODO: Implementar almacenamiento seguro de credenciales
    }

    /**
     * Carga las credenciales guardadas
     */
    public String[] loadCredentials() {
        // TODO: Implementar carga de credenciales
        return new String[]{"", ""};
    }
}

