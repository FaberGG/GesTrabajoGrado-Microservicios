package co.unicauca.gestiontrabajogrado;

import co.unicauca.gestiontrabajogrado.presentation.auth.LoginView;
import co.unicauca.gestiontrabajogrado.controller.LoginController;

import javax.swing.*;

/**
 * Clase principal para ejecutar la aplicación de escritorio
 * Sistema de Gestión de Trabajo de Grado - Universidad del Cauca
 *
 * @author Lyz
 */
public class Main {

    public static void main(String[] args) {
        // Configurar el Look and Feel del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("No se pudo establecer el Look and Feel: " + e.getMessage());
            // Continuar con el Look and Feel por defecto
        }

        // Ejecutar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                // Crear el controlador de login
                LoginController loginController = new LoginController();

                // Crear y mostrar la vista de login
                LoginView loginView = new LoginView(loginController);
                loginView.setLocationRelativeTo(null); // Centrar en la pantalla
                loginView.setVisible(true);

                System.out.println("=================================================");
                System.out.println("  Sistema de Gestión de Trabajo de Grado");
                System.out.println("  Universidad del Cauca");
                System.out.println("=================================================");
                System.out.println("Aplicación iniciada correctamente.");
                System.out.println("Por favor, inicie sesión en la ventana.");
                System.out.println();
                System.out.println("Nota: Esta versión usa datos de prueba.");
                System.out.println("Para conectar con los microservicios, configure");
                System.out.println("las URLs en el archivo de configuración.");
                System.out.println("=================================================");

            } catch (Exception e) {
                System.err.println("Error al iniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación:\n" + e.getMessage(),
                        "Error de Inicio",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

