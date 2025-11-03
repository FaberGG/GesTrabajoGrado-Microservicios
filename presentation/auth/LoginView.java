package co.unicauca.gestiontrabajogrado.presentation.auth;

import co.unicauca.gestiontrabajogrado.controller.LoginController;
import co.unicauca.gestiontrabajogrado.presentation.common.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Formulario de inicio de sesión moderno para el sistema de gestión de trabajo de grado
 * Rediseñado con estilo contemporáneo similar al mockup
 * @author Lyz
 */
public class LoginView extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel forgotPasswordLabel;
    private JButton registerTabButton;
    private JButton loginTabButton;
    private LoginController controller;
    
    // Ya no necesitamos las variables de fuentes personalizadas

    public LoginView() {
        super("Universidad del Cauca - Gestión del Proceso de Trabajo de Grado");
        initializeFrame();
        createComponents();
        setupLayout();
        setupEventListeners();
        loadRememberedData();
    }

    public LoginView(LoginController controller) {
        this();
        this.controller = controller;
        loadRememberedData();
    }

    public void setController(LoginController controller) {
        this.controller = controller;
        loadRememberedData();
    }
    
    /**
     * Crear fuente para "Universidad del Cauca" (estilo inteligente como RegisterView)
     */
    private Font createUniversityFont(int style, int size) {
        String[] fontNames = {
            "Kaisei Opti",      // Fuente preferida
            "Times New Roman",  // Alternativa serif elegante
            "Georgia",          // Otra alternativa serif
            "Serif"             // Fallback genérico
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            // Verificar si la fuente existe comparando con la fuente por defecto
            if (!font.getFamily().equals(Font.DIALOG)) {
                System.out.println("✓ Universidad del Cauca usando fuente: " + fontName);
                return font;
            }
        }
        
        System.out.println("⚠ Universidad del Cauca usando fuente por defecto: SansSerif");
        return new Font("SansSerif", style, size);
    }

    /**
     * Crear fuente para "Ingresar a la Plataforma" (estilo inteligente como RegisterView)
     */
    private Font createTitleFont(int style, int size) {
        String[] fontNames = {
            "Antonio",          // Fuente preferida
            "Impact",           // Alternativa condensada moderna
            "Arial Black",      // Alternativa bold
            "SansSerif"         // Fallback genérico
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            // Verificar si la fuente existe comparando con la fuente por defecto
            if (!font.getFamily().equals(Font.DIALOG)) {
                System.out.println("✓ Título usando fuente: " + fontName);
                return font;
            }
        }
        
        System.out.println("⚠ Título usando fuente por defecto: SansSerif");
        return new Font("SansSerif", style, size);
    }

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Fondo con degradado azul
        setContentPane(new BackgroundPanel());
    }

    private void createComponents() {
        // Tabs para Registrarse / Iniciar sesión
        registerTabButton = createTabButton("Registrarse", false);
        loginTabButton = createTabButton("Iniciar sesión", true);

        // Campo de email
        emailField = createStyledTextField("E-mail");

        // Campo de contraseña con icono de ojo
        passwordField = createStyledPasswordField("Password");

        // Enlace "¿Has olvidado tu contraseña?"
        forgotPasswordLabel = new JLabel("¿Has olvidado tu contraseña?");
        forgotPasswordLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotPasswordLabel.setForeground(new Color(0x4A90E2));
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Botón "Iniciar Sesión"
        loginButton = createModernButton("Iniciar Sesión");
    }

    private JButton createTabButton(String text, boolean selected) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Fondo del tab: si el botón está presionado o está marcado como seleccionado
                if (getModel().isPressed() || selected) {
                    g2.setColor(new Color(0xD52E2E)); // rojo
                } else {
                    g2.setColor(new Color(0x4A90E2)); // azul
                }

                // Línea inferior solo cuando este botón está seleccionado
                if (selected) {
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                }

                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Color del texto según si está seleccionado
        if (selected) {
            button.setForeground(new Color(0x1A2E5A));
            button.setFont(button.getFont().deriveFont(Font.BOLD));
        } else {
            button.setForeground(new Color(0x4A90E2));
        }

        return button;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo redondeado con borde gris
                g2.setColor(new Color(0x9F9898)); // Borde gris más claro
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                
                // Placeholder text
                if (getText().isEmpty() && !hasFocus()) {
                    g2.setColor(new Color(0xCCCCCC));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, x, y);
                }
                g2.dispose();
            }
        };
        
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(new EmptyBorder(12, 16, 12, 16));
        field.setPreferredSize(new Dimension(280, 45));
        
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        // Crear una clase anónima extendida para agregar los métodos personalizados
        JPasswordField field = new JPasswordField() {
            private boolean passwordVisible = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo redondeado con borde gris
                g2.setColor(new Color(0x9F9898)); // Borde gris más claro
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                
                // Placeholder text
                if (getPassword().length == 0 && !hasFocus()) {
                    g2.setColor(new Color(0xCCCCCC));
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(placeholder, x, y);
                }
                
                // Icono de ojo clickeable
                g2.setColor(new Color(0x666666));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                String eyeIcon = passwordVisible ? "O" : "X"; // Cambiar icono segun visibilidad
                FontMetrics fm = g2.getFontMetrics();
                int iconX = getWidth() - fm.stringWidth(eyeIcon) - 12;
                int iconY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(eyeIcon, iconX, iconY);
                
                g2.dispose();
            }
            
            // Método para alternar visibilidad de la contraseña
            public void togglePasswordVisibility() {
                passwordVisible = !passwordVisible;
                if (passwordVisible) {
                    setEchoChar((char) 0); // Mostrar caracteres
                } else {
                    setEchoChar('*'); // Ocultar caracteres
                }
                repaint();
            }
            
            // Método para verificar si el clic fue sobre el icono del ojo
            public boolean isEyeIconClicked(int x, int y) {
                FontMetrics fm = getFontMetrics(getFont());
                String eyeIcon = passwordVisible ? "O" : "X";
                int iconX = getWidth() - fm.stringWidth(eyeIcon) - 12;
                int iconWidth = fm.stringWidth(eyeIcon);
                int iconHeight = fm.getHeight();
                int iconY = (getHeight() - iconHeight) / 2;
                
                return x >= iconX && x <= iconX + iconWidth && 
                       y >= iconY && y <= iconY + iconHeight;
            }
        };
        
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setBorder(new EmptyBorder(12, 16, 12, 40)); // Más padding a la derecha para el icono
        field.setPreferredSize(new Dimension(280, 45));
        
        // Agregar MouseListener para detectar clics en el icono del ojo
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Usar reflexión para acceder a los métodos personalizados
                JPasswordField source = (JPasswordField) e.getSource();
                try {
                    java.lang.reflect.Method isEyeIconClickedMethod = 
                        source.getClass().getMethod("isEyeIconClicked", int.class, int.class);
                    java.lang.reflect.Method toggleMethod = 
                        source.getClass().getMethod("togglePasswordVisibility");
                    
                    boolean clicked = (Boolean) isEyeIconClickedMethod.invoke(source, e.getX(), e.getY());
                    if (clicked) {
                        toggleMethod.invoke(source);
                    }
                } catch (Exception ex) {
                    // Si falla la reflexión, fallback simple
                    System.err.println("Error en toggle password: " + ex.getMessage());
                }
            }
        });
        
        return field;
    }

    private JButton createModernButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Fondo del botón con degradado
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0xE53E3E),
                    0, getHeight(), new Color(0xC53030)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Texto centrado
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), textX, textY);
                
                g2.dispose();
            }
        };
        
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(280, 45));
        
        return button;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con la tarjeta centrada
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        
        // Tarjeta de login
        JPanel loginCard = createLoginCard();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginCard, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra de la tarjeta
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(5, 5, getWidth() - 5, getHeight() - 5, 20, 20);
                
                // Fondo blanco de la tarjeta
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 5, getHeight() - 5, 20, 20);
                
                g2.dispose();
            }
        };
        
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 500));
        
        // Logo y título de la Universidad
        JPanel logoAndTitle = createLogoAndTitleSection();
        logoAndTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoAndTitle);
        card.add(Box.createVerticalStrut(20));
        
        // Título - AQUÍ SE USA LA FUENTE ANTONIO
        JLabel titleLabel = new JLabel("Ingresar a la Plataforma");
        titleLabel.setFont(createTitleFont(Font.PLAIN, 30));
        titleLabel.setForeground(new Color(0x1A2E5A));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(30));
        
        // Tabs
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tabPanel.setOpaque(false);
        tabPanel.add(registerTabButton);
        tabPanel.add(loginTabButton);
        card.add(tabPanel);
        card.add(Box.createVerticalStrut(25));
        
        // Campos de entrada
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(emailField);
        card.add(Box.createVerticalStrut(15));
        
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));
        
        // Enlace "¿Has olvidado tu contraseña?"
        forgotPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(forgotPasswordLabel);
        card.add(Box.createVerticalStrut(25));
        
        // Botón de login
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginButton);
        
        return card;
    }

    private JPanel createLogoAndTitleSection() {
        JPanel logoSection = new JPanel();
        logoSection.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoSection.setOpaque(false);
        
        // Panel horizontal para logo y título
        JPanel logoTitlePanel = new JPanel(new BorderLayout(15, 0));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Cargar el nuevo logo de la Universidad del Cauca
        JLabel logoLabel = null;
        boolean logoLoaded = false;
        
        try {
            java.net.URL logoURL = getClass().getClassLoader().getResource("images/logo.png");
            
            if (logoURL != null) {
                ImageIcon originalIcon = new ImageIcon(logoURL);
                
                if (originalIcon.getIconWidth() > 0) {
                    logoLabel = new JLabel();
                    
                    // Redimensionar a tamaño apropiado
                    int logoSize = 80;
                    Image scaledImage = originalIcon.getImage()
                        .getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
                    
                    logoLabel.setIcon(new ImageIcon(scaledImage));
                    logoLabel.setPreferredSize(new Dimension(logoSize, logoSize));
                    logoLabel.setMinimumSize(new Dimension(logoSize, logoSize));
                    logoLabel.setMaximumSize(new Dimension(logoSize, logoSize));
                    
                    // Sin fondo para que se vea limpio
                    logoLabel.setOpaque(false);
                    
                    logoLoaded = true;
                    System.out.println("✓ Nuevo logo de la Universidad del Cauca cargado exitosamente");
                } else {
                    throw new Exception("Logo inválido");
                }
            } else {
                throw new Exception("Logo no encontrado");
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo: " + e.getMessage());
            System.out.println("Usando placeholder...");
            logoLabel = createPlaceholderLogo();
        }
        
        // Panel para el texto de la universidad - AQUÍ SE USA LA FUENTE KAISEI OPTI
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel univLabel = new JLabel("Universidad");
        univLabel.setFont(createUniversityFont(Font.BOLD, 18));
        univLabel.setForeground(new Color(0x1A2E5A));
        univLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel caucaLabel = new JLabel("del Cauca");
        caucaLabel.setFont(createUniversityFont(Font.BOLD, 18));
        caucaLabel.setForeground(new Color(0x1A2E5A));
        caucaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(univLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(caucaLabel);
        textPanel.add(Box.createVerticalGlue());
        
        // Ensamblar logo y texto
        logoTitlePanel.add(logoLabel, BorderLayout.WEST);
        logoTitlePanel.add(textPanel, BorderLayout.CENTER);
        
        logoSection.add(logoTitlePanel);
        logoSection.setPreferredSize(new Dimension(300, 100));
        
        return logoSection;
    }

    private JLabel createPlaceholderLogo() {
        JLabel placeholder = new JLabel();
        placeholder.setPreferredSize(new Dimension(80, 80));
        placeholder.setMinimumSize(new Dimension(80, 80));
        placeholder.setMaximumSize(new Dimension(80, 80));
        placeholder.setOpaque(true);
        placeholder.setBackground(new Color(0x1A2E5A));
        placeholder.setForeground(Color.WHITE);
        placeholder.setFont(new Font("SansSerif", Font.BOLD, 10));
        placeholder.setText("<html><center>UNICAUCA<br>🎓</center></html>");
        placeholder.setHorizontalAlignment(SwingConstants.CENTER);
        placeholder.setVerticalAlignment(SwingConstants.CENTER);
        placeholder.setBorder(BorderFactory.createLineBorder(new Color(0x1A2E5A), 2));
        
        return placeholder;
    }
    
    private void setupEventListeners() {
        // Acción del botón de login
        loginButton.addActionListener(e -> handleLogin());

        // Enter en los campos de texto
        ActionListener loginAction = e -> handleLogin();
        emailField.addActionListener(loginAction);
        passwordField.addActionListener(loginAction);

        // Hover effects
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.repaint(); // El efecto está en el paintComponent
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.repaint();
            }
        });

        // AQUÍ ESTÁ EL CAMBIO PRINCIPAL - ActionListener para el botón "Registrarse"
        registerTabButton.addActionListener(e -> {
            handleRegistrarse();
            // NO cambiar estados visuales ya que vamos a otra ventana
        });
        
        loginTabButton.addActionListener(e -> {
            // Mantener en login (ya está seleccionado)
            updateTabStates(true); // true = login seleccionado
        });
        
        // Forgot password
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleForgotPassword();
            }
        });
    }

    // Panel de fondo con degradado
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Degradado de azul oscuro a azul claro
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(0x1A365D),
                0, getHeight(), new Color(0x2B6CB0)
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.dispose();
        }
    }

    private void updateTabStates(boolean loginSelected) {
        if (loginSelected) {
            loginTabButton.setForeground(new Color(0x1A2E5A));
            loginTabButton.setFont(loginTabButton.getFont().deriveFont(Font.BOLD));
            registerTabButton.setForeground(new Color(0x4A90E2));
            registerTabButton.setFont(registerTabButton.getFont().deriveFont(Font.PLAIN));
        } else {
            registerTabButton.setForeground(new Color(0x1A2E5A));
            registerTabButton.setFont(registerTabButton.getFont().deriveFont(Font.BOLD));
            loginTabButton.setForeground(new Color(0x4A90E2));
            loginTabButton.setFont(loginTabButton.getFont().deriveFont(Font.PLAIN));
        }
        // Repintar los botones
        loginTabButton.repaint();
        registerTabButton.repaint();
    }

    private void handleLogin() {
        if (controller == null) {
            showError("Error interno: Controlador no inicializado.");
            return;
        }
        controller.handleLogin(getEmailText(), getPasswordText(), false);
    }

    private void handleRegistrarse() {
        if (controller != null) {
            controller.handleRegistrarse();
        } else {
            showError("Controlador no inicializado.");
        }
    }
    
    private void handleForgotPassword() {
        showSuccess("Funcionalidad de recuperación de contraseña próximamente.");
    }

    private void loadRememberedData() {
        if (controller != null) {
            String rememberedEmail = controller.getRememberedEmail();
            if (!rememberedEmail.isEmpty()) {
                emailField.setText(rememberedEmail);
                passwordField.requestFocus();
            }
        }
    }

    // Métodos públicos para el controller
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public String getEmailText() {
        return emailField.getText().trim();
    }

    public String getPasswordText() {
        return new String(passwordField.getPassword());
    }

    public boolean isRememberMeSelected() {
        return false; // No incluimos checkbox en este diseño
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { 
            // Fallback a look and feel por defecto
        }

        SwingUtilities.invokeLater(() -> {
            try {
                LoginView loginView = new LoginView();
                LoginController loginController = new LoginController();
                loginView.setController(loginController);
                loginView.setVisible(true);
                
                System.out.println("==============================================");
                System.out.println("  Sistema de Gestión de Trabajo de Grado");
                System.out.println("  Login View - Universidad del Cauca");
                System.out.println("==============================================");
                System.out.println("Usuario de prueba: cualquier email y password");
                System.out.println("==============================================");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Error al inicializar: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
}