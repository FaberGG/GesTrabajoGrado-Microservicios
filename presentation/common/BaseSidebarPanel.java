package co.unicauca.gestiontrabajogrado.presentation.common;

import co.unicauca.gestiontrabajogrado.domain.service.IAutenticacionService;
import co.unicauca.gestiontrabajogrado.controller.LoginController;
import co.unicauca.gestiontrabajogrado.presentation.auth.LoginView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clase base abstracta para los paneles laterales (sidebar) del sistema.
 * Proporciona funcionalidad común como botones de menú, cerrar sesión, etc.
 */
public abstract class BaseSidebarPanel extends JPanel {

    protected static final Dimension BTN_SIZE = new Dimension(220, 48);
    protected JPanel submenu; // contenedor de submenú
    private final JFrame parentFrame; // referencia al frame padre para poder cerrarlo

    public BaseSidebarPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        initializePanel();
        createCommonComponents();
        createRoleSpecificComponents();
        setupSubmenuToggle();
    }

    /**
     * Inicializa la configuración básica del panel
     */
    private void initializePanel() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(16, 16, 16, 12));
        setPreferredSize(new Dimension(240, 0));
    }

    /**
     * Crea los componentes comunes a todos los roles
     */
    private void createCommonComponents() {
        // 1) Menú de usuario (#A7C6EA)
        add(pillButton("Menú de usuario", new Color(0xA7C6EA), Color.BLACK));
        add(Box.createVerticalStrut(12));

        // 2) Header específico del rol con flecha y submenú
        JPanel roleHeader = headerWithArrow(getRoleHeaderText(), new Color(0x73AAEB), Color.WHITE);
        add(roleHeader);
        add(Box.createVerticalStrut(8));

        // 3) Crear submenú
        submenu = new JPanel();
        submenu.setOpaque(false);
        submenu.setLayout(new BoxLayout(submenu, BoxLayout.Y_AXIS));
        createSubmenuItems();
        submenu.setVisible(false);
        add(submenu);

        add(Box.createVerticalStrut(12));

        // 4) Cerrar sesión (#2665C4) - funcionalidad común
        JButton cerrarSesionBtn = (JButton) pillButton("Cerrar Sesión", new Color(0x2665C4), Color.WHITE);
        cerrarSesionBtn.addActionListener(e -> handleCerrarSesion());
        add(cerrarSesionBtn);
        add(Box.createVerticalGlue());

        // Configurar toggle del submenú
        roleHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleSubmenu();
            }
        });
    }

    /**
     * Maneja la acción de cerrar sesión
     */
    private void handleCerrarSesion() {
        int confirmResult = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmResult == JOptionPane.YES_OPTION) {
            // Cerrar la ventana actual
            parentFrame.dispose();

            // Abrir la ventana de login con controller inicializado
            SwingUtilities.invokeLater(() -> {
                try {
                    // Crear un servicio de autenticación (necesario para el controller)
                    IAutenticacionService authService = createAutenticacionService();

                    // Crear la vista de login
                    LoginView loginView = new LoginView();

                    // Crear el controller con el servicio
                    LoginController loginController = new LoginController(authService, loginView);

                    // Asignar el controller a la vista
                    loginView.setController(loginController);

                    // Mostrar la vista
                    loginView.setVisible(true);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                            "Error al abrir la ventana de login: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            });
        }
    }

    /**
     * Crea una instancia del servicio de autenticación usando ServiceManager
     */
    private IAutenticacionService createAutenticacionService() {
        return ServiceManager.getInstance().getAutenticacionService();
    }

    /**
     * Alterna la visibilidad del submenú
     */
    private void toggleSubmenu() {
        submenu.setVisible(!submenu.isVisible());
        submenu.revalidate();
        submenu.getParent().revalidate();
        submenu.repaint();
    }

    /**
     * Crea los elementos del submenú específicos de cada rol
     */
    protected void createSubmenuItems() {
        String[] menuItems = getSubmenuItems();
        for (int i = 0; i < menuItems.length; i++) {
            final String itemText = menuItems[i];
            submenu.add(subItemButton(itemText, () -> handleSubmenuAction(itemText)));
            if (i < menuItems.length - 1) {
                submenu.add(Box.createVerticalStrut(6));
            }
        }
    }

    /**
     * Crea un botón principal tipo pastilla
     */
    protected JComponent pillButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Sombra del botón
                g2.setColor(new Color(0, 0, 0, 15));
                g2.fillRoundRect(1, 2, getWidth() - 1, getHeight() - 1, 8, 8);

                // Fondo del botón
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setFont(UIConstants.BODY);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false); // Importante: desactivar el relleno por defecto
        b.setOpaque(false); // Cambiar a false ya que manejamos el pintado nosotros
        b.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16)); // Usar EmptyBorder para padding interno
        b.setPreferredSize(BTN_SIZE);
        b.setMaximumSize(BTN_SIZE);
        b.setMinimumSize(BTN_SIZE);

        // Opcional: Efecto hover
        final Color originalBg = bg;
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(originalBg.brighter());
                b.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(originalBg);
                b.repaint();
            }
        });

        return b;
    }

    /**
     * Crea una cabecera con flecha ▾
     */
    protected JPanel headerWithArrow(String text, Color bg, Color fg) {
        JPanel p = new JPanel(new BorderLayout());
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBackground(bg);
        p.setOpaque(true);
        p.setBorder(new LineBorder(bg.darker(), 1, true));
        p.setPreferredSize(BTN_SIZE);
        p.setMaximumSize(BTN_SIZE);
        p.setMinimumSize(BTN_SIZE);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel l = new JLabel(text);
        l.setFont(UIConstants.BODY);
        l.setForeground(fg);

        JLabel arrow = new JLabel("▾");
        arrow.setFont(UIConstants.BODY);
        arrow.setForeground(fg);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        center.setOpaque(false);
        center.add(l);
        p.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        right.setOpaque(false);
        right.add(arrow);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    /**
     * Crea botones del submenú
     */
    protected JComponent subItemButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setFont(UIConstants.BODY);
        b.setForeground(UIConstants.TEXT_PRIMARY);
        b.setBackground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD0D8E8), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        Dimension d = new Dimension(BTN_SIZE.width, 40);
        b.setPreferredSize(d);
        b.setMaximumSize(d);
        b.setMinimumSize(d);

        // Ejecutar la acción personalizada
        b.addActionListener(e -> action.run());
        return b;
    }

    // Métodos abstractos que deben implementar las clases hijas

    /**
     * Retorna el texto del header del rol (ej: "Docente", "Estudiante")
     */
    protected abstract String getRoleHeaderText();

    /**
     * Retorna los elementos del submenú específicos del rol
     */
    protected abstract String[] getSubmenuItems();

    /**
     * Crea componentes específicos del rol (si los hay)
     */
    protected abstract void createRoleSpecificComponents();

    /**
     * Configura el toggle del submenú (si necesita lógica específica)
     */
    protected abstract void setupSubmenuToggle();

    /**
     * Maneja las acciones del submenú
     */
    protected abstract void handleSubmenuAction(String actionText);
}