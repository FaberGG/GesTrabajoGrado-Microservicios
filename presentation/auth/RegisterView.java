package co.unicauca.gestiontrabajogrado.presentation.auth;

import co.unicauca.gestiontrabajogrado.controller.RegisterController;
import co.unicauca.gestiontrabajogrado.presentation.common.UIConstants;
import co.unicauca.gestiontrabajogrado.presentation.common.HeaderPanel;
import co.unicauca.gestiontrabajogrado.domain.model.enumProgram;
import co.unicauca.gestiontrabajogrado.domain.model.enumRol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Formulario de registro para el sistema de gestión de trabajo de grado
 * @author Lyz
 */
public class RegisterView extends JFrame {

    private JTextField nombresField;
    private JTextField apellidosField;
    private JTextField identificacionField;
    private JTextField celularField;
    private JComboBox<ProgramItem> programaComboBox;
    private JComboBox<RolItem> rolComboBox;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JLabel volverLoginLabel;
    private RegisterController controller;
    private BufferedImage logoImage;

    public RegisterView() {
        super("Registrarse - Gestión del Proceso de Trabajo de Grado");
        loadLogo();
        initializeFrame();
        createComponents();
        setupLayout();
        setupEventListeners();
    }

    public RegisterView(RegisterController controller) {
        this();
        this.controller = controller;
    }

    public void setController(RegisterController controller) {
        this.controller = controller;
    }

    private void loadLogo() {
        String[] possiblePaths = {
            "/images/logo.png",
            "/logo.png",
            "/co/unicauca/gestiontrabajogrado/presentation/resources/images/logo.png"
        };

        for (String path : possiblePaths) {
            try {
                var logoStream = getClass().getResourceAsStream(path);
                if (logoStream != null) {
                    logoImage = ImageIO.read(logoStream);
                    logoStream.close();
                    return;
                }
            } catch (IOException e) {
                // Continuar con la siguiente ruta
            }
        }
        createPlaceholderLogo();
    }

    private void createPlaceholderLogo() {
        logoImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = logoImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0x2F5F9E));
        g2.fillOval(5, 5, 50, 50);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("UC", 20, 35);
        g2.dispose();
    }

    // NUEVO MÉTODO: Logo y título en línea horizontal (como LoginView)
    private JPanel createLogoAndTitleSection() {
        JPanel logoSection = new JPanel();
        logoSection.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoSection.setOpaque(false);
        
        // Panel horizontal para logo y título (igual que en LoginView)
        JPanel logoTitlePanel = new JPanel(new BorderLayout(15, 0));
        logoTitlePanel.setOpaque(false);
        logoTitlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Cargar logo de la Universidad del Cauca
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
                    logoLabel.setOpaque(false);
                    
                    logoLoaded = true;
                } else {
                    throw new Exception("Logo con dimensiones inválidas");
                }
            } else {
                throw new Exception("Archivo logo.png no encontrado");
            }
        } catch (Exception e) {
            // Si no se puede cargar el logo, usar el BufferedImage como backup
            if (logoImage != null) {
                logoLabel = new JLabel(new ImageIcon(logoImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
                logoLoaded = true;
            } else {
                logoLabel = createPlaceholderLogoLabel();
            }
        }
        
        // Panel para el texto de la universidad (igual que en LoginView)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel univLabel = new JLabel("Universidad");
        univLabel.setFont(createUniversityFont(Font.BOLD, 25));
        univLabel.setForeground(new Color(0x1A2E5A));
        univLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel caucaLabel = new JLabel("del Cauca");
        caucaLabel.setFont(createUniversityFont(Font.BOLD, 25));
        caucaLabel.setForeground(new Color(0x1A2E5A));
        caucaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(univLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(caucaLabel);
        textPanel.add(Box.createVerticalGlue());
        
        // Ensamblar logo y texto horizontalmente
        logoTitlePanel.add(logoLabel, BorderLayout.WEST);
        logoTitlePanel.add(textPanel, BorderLayout.CENTER);
        
        logoSection.add(logoTitlePanel);
        logoSection.setPreferredSize(new Dimension(300, 100));
        
        return logoSection;
    }

    // NUEVO MÉTODO: Placeholder si no se carga el logo
    private JLabel createPlaceholderLogoLabel() {
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

    private void initializeFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(new Color(0x2F5F9E));
    }

    private void createComponents() {
        // Campos de texto con placeholder mejorado
        nombresField = createStyledTextField("Nombres *");
        apellidosField = createStyledTextField("Apellidos *");
        identificacionField = createStyledTextField("Identificación *");
        celularField = createStyledTextField("Celular ");
        emailField = createStyledTextField("E-mail *");
        passwordField = createStyledPasswordField("Contraseña *");

        // ComboBoxes con bordes grises
        programaComboBox = createProgramComboBox();
        rolComboBox = createRolComboBox();

        // Botón de registro
        registerButton = new JButton("Registrarse") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0xB11F1F), 0, getHeight(), new Color(0xB11F1F));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setContentAreaFilled(false);
        registerButton.setOpaque(false);
        registerButton.setPreferredSize(new Dimension(200, 45));
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Borde gris más oscuro como solicitaste
                g2.setColor(new Color(0x9F9898)); // Color específico solicitado
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                
                // PLACEHOLDER MEJORADO: aparece cuando está vacío (SIN importar el foco)
                if (getText().trim().isEmpty()) {
                    g2.setColor(new Color(0x9CA3AF));
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
        field.setForeground(new Color(0x374151)); // Color del texto cuando se escribe
        field.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        field.setPreferredSize(new Dimension(250, 40));
        field.setOpaque(false);
        
        // LISTENERS SIMPLIFICADOS: solo repintar cuando cambie el contenido
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando se agregue texto
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando se borre texto
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando cambie el texto
            }
        });
        
        return field;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Borde gris más oscuro como solicitaste
                g2.setColor(new Color(0x9F9898)); // Color específico solicitado
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                
                // PLACEHOLDER MEJORADO: aparece cuando está vacío (SIN importar el foco)
                if (getPassword().length == 0) {
                    g2.setColor(new Color(0x9CA3AF));
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
        field.setForeground(new Color(0xD50F0F)); // Color rojo específico para los asteriscos
        field.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        field.setPreferredSize(new Dimension(250, 40));
        field.setOpaque(false);
        field.setEchoChar('*'); // Usar asteriscos rojos
        
        // LISTENER SIMPLIFICADO: solo repintar cuando cambie el contenido
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando se agregue texto
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando se borre texto
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                field.repaint(); // Repintar cuando cambie el texto
            }
        });
        
        return field;
    }

    private JComboBox<ProgramItem> createProgramComboBox() {
        ProgramItem[] programItems = {
                new ProgramItem(null, "Programa *"),
                new ProgramItem(enumProgram.INGENIERIA_DE_SISTEMAS, "Ingeniería de Sistemas"),
                new ProgramItem(enumProgram.INGENIERIA_ELECTRONICA_Y_TELECOMUNICACIONES, "Ingeniería Electrónica y Telecomunicaciones"),
                new ProgramItem(enumProgram.AUTOMATICA_INDUSTRIAL, "Automática Industrial"),
                new ProgramItem(enumProgram.TECNOLOGIA_EN_TELEMATICA, "Tecnología en Telemática")
        };

        JComboBox<ProgramItem> comboBox = new JComboBox<ProgramItem>(programItems) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Borde gris más oscuro
                g2.setColor(new Color(0x9F9898));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(0x9CA3AF));
        comboBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        comboBox.setPreferredSize(new Dimension(250, 40));
        comboBox.setOpaque(false);
        
        return comboBox;
    }

    private JComboBox<RolItem> createRolComboBox() {
        RolItem[] rolItems = {
                new RolItem(null, "Rol *"),
                new RolItem(enumRol.ESTUDIANTE, "Estudiante"),
                new RolItem(enumRol.DOCENTE, "Docente")
        };

        JComboBox<RolItem> comboBox = new JComboBox<RolItem>(rolItems) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Borde gris más oscuro
                g2.setColor(new Color(0x9F9898));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // Fondo interior blanco
                g2.setColor(getBackground());
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                
                super.paintComponent(g);
                g2.dispose();
            }
        };
        
        comboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(0x9CA3AF));
        comboBox.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        comboBox.setPreferredSize(new Dimension(250, 40));
        comboBox.setOpaque(false);
        
        return comboBox;
    }

    // MÉTODO MODIFICADO: setupLayout con el nuevo header
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Panel principal con fondo azul
        JPanel mainPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0x2F5F9E),
                    0, getHeight(), new Color(0x1E3A5F)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        
        // Card principal
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Sombra
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(5, 5, getWidth()-5, getHeight()-5, 15, 15);
                
                // Fondo del card
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(40, 50, 40, 50));
        card.setOpaque(false);
        
        // HEADER MODIFICADO: Logo y título de la universidad en línea horizontal
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        // Logo y título de la universidad en línea horizontal
        JPanel logoAndTitlePanel = createLogoAndTitleSection();
        headerPanel.add(logoAndTitlePanel, BorderLayout.NORTH);
        
        // Subtítulo - AQUÍ SE APLICA LA FUENTE ANTONIO
        JPanel subtitlePanel = new JPanel();
        subtitlePanel.setLayout(new BoxLayout(subtitlePanel, BoxLayout.Y_AXIS));
        subtitlePanel.setOpaque(false);
        subtitlePanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel subtitleLabel = new JLabel("Ingrese sus Datos");
        subtitleLabel.setFont(createTitleFont(Font.PLAIN, 35)); // AQUÍ SE USA ANTONIO
        subtitleLabel.setForeground(new Color(0x374151));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        subtitlePanel.add(subtitleLabel);
        headerPanel.add(subtitlePanel, BorderLayout.CENTER);
        
        // Pestañas de Registrarse e Iniciar sesión
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tabPanel.setOpaque(false);
        tabPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        JLabel registrarseTab = new JLabel("Registrarse");
        registrarseTab.setFont(new Font("SansSerif", Font.BOLD, 14));
        registrarseTab.setForeground(new Color(0x1A2E5A));
        registrarseTab.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xB91C1C)));
        
        JLabel iniciarSesionTab = new JLabel("Iniciar sesión");
        iniciarSesionTab.setFont(new Font("SansSerif", Font.PLAIN, 14));
        iniciarSesionTab.setForeground(new Color(0x6B7280));
        iniciarSesionTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iniciarSesionTab.setBorder(new EmptyBorder(0, 30, 2, 0));
        
        tabPanel.add(registrarseTab);
        tabPanel.add(iniciarSesionTab);
        
        // Panel de formulario con dos columnas
        JPanel formPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Columna izquierda
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setOpaque(false);
        
        leftColumn.add(nombresField);
        leftColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        leftColumn.add(identificacionField);
        leftColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        leftColumn.add(programaComboBox);
        leftColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        leftColumn.add(emailField);
        
        // Columna derecha
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setOpaque(false);
        
        rightColumn.add(apellidosField);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        rightColumn.add(celularField);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        rightColumn.add(rolComboBox);
        rightColumn.add(Box.createRigidArea(new Dimension(0, 15)));
        rightColumn.add(passwordField);
        
        formPanel.add(leftColumn);
        formPanel.add(rightColumn);
        
        // Panel del botón
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(registerButton);
        
        // Ensamblar el card
        card.add(headerPanel, BorderLayout.NORTH);
        card.add(tabPanel, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.SOUTH);
        
        // Agregar el card al panel principal
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.add(card, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Event listener para "Iniciar sesión"
        iniciarSesionTab.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleVolverLogin();
            }
        });
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> handleRegister());
        
        // Hover effects
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(0x991B1B));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(0xB91C1C));
            }
        });
    }

    private void handleRegister() {
        if (controller == null) {
            showError("Error interno: Controlador no inicializado.");
            return;
        }
        
        controller.handleRegister(
            getNombres(),
            getApellidos(),
            getIdentificacion(),
            getCelular(),
            getSelectedProgram(),
            getSelectedRol(),
            getEmail(),
            getPassword(),
            ""
        );
    }

    private void handleVolverLogin() {
        if (controller != null) {
            controller.handleVolverLogin();
        } else {
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    Class<?> loginViewClass = Class.forName("co.unicauca.gestiontrabajogrado.presentation.auth.LoginView");
                    JFrame loginView = (JFrame) loginViewClass.getDeclaredConstructor().newInstance();
                    loginView.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Error al abrir LoginView: " + e.getMessage());
                    System.exit(0);
                }
            });
        }
    }

    // Métodos públicos para el controller
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    // Getters actualizados para trabajar con placeholders mejorados
    public String getNombres() {
        return nombresField.getText().trim();
    }

    public String getApellidos() {
        return apellidosField.getText().trim();
    }
    
    public String getIdentificacion() {
        return identificacionField.getText().trim();
    }

    public String getCelular() {
        return celularField.getText().trim();
    }

    public enumProgram getSelectedProgram() {
        ProgramItem selected = (ProgramItem) programaComboBox.getSelectedItem();
        return selected != null ? selected.getEnumValue() : null;
    }

    public enumRol getSelectedRol() {
        RolItem selected = (RolItem) rolComboBox.getSelectedItem();
        return selected != null ? selected.getEnumValue() : null;
    }

    public String getEmail() {
        return emailField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // Clases internas para los ComboBox items
    private static class ProgramItem {
        private final enumProgram enumValue;
        private final String displayName;

        public ProgramItem(enumProgram enumValue, String displayName) {
            this.enumValue = enumValue;
            this.displayName = displayName;
        }

        public enumProgram getEnumValue() {
            return enumValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static class RolItem {
        private final enumRol enumValue;
        private final String displayName;

        public RolItem(enumRol enumValue, String displayName) {
            this.enumValue = enumValue;
            this.displayName = displayName;
        }

        public enumRol getEnumValue() {
            return enumValue;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        SwingUtilities.invokeLater(() -> new RegisterView().setVisible(true));
    }
    
    /**
     * Crear fuente para "Universidad del Cauca" (Kaisei Opti)
     * MEJORADO: Intenta cargar Kaisei Opti desde archivo o utiliza alternativas
     */
    private Font createUniversityFont(int style, int size) {
        // Primero intentar cargar Kaisei Opti desde recursos
        try {
            java.io.InputStream fontStream = getClass().getResourceAsStream("/fonts/KaiseiOpti-Regular.ttf");
            if (fontStream == null) {
                fontStream = getClass().getResourceAsStream("/fonts/KaiseiOpti-Bold.ttf");
            }
            if (fontStream != null) {
                Font kaiseiOpti = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
                fontStream.close();
                System.out.println("✓ Universidad del Cauca usando Kaisei Opti desde archivo");
                return kaiseiOpti;
            }
        } catch (Exception e) {
            System.out.println("⚠ No se pudo cargar Kaisei Opti desde archivo: " + e.getMessage());
        }
        
        // Intentar usar Kaisei Opti instalada en el sistema
        String[] fontNames = {
            "Kaisei Opti",      // Fuente preferida
            "Times New Roman",  // Alternativa serif elegante  
            "Georgia",          // Otra alternativa serif
            "Serif"             // Fallback genérico
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            // Verificar si la fuente existe
            if (!font.getFamily().equals("Dialog")) {
                System.out.println("✓ Universidad del Cauca usando fuente: " + fontName);
                return font;
            }
        }
        
        System.out.println(" Universidad del Cauca usando fuente por defecto: SansSerif");
        return new Font("SansSerif", style, size);
    }

    /**
     * Crear fuente para "Ingrese sus Datos" (Antonio)
     * MEJORADO: Intenta cargar Antonio desde archivo o utiliza alternativas
     */
    private Font createTitleFont(int style, int size) {
        // Primero intentar cargar Antonio desde recursos
        try {
            java.io.InputStream fontStream = getClass().getResourceAsStream("/fonts/Antonio-Regular.ttf");
            if (fontStream == null) {
                fontStream = getClass().getResourceAsStream("/fonts/Antonio-Bold.ttf");
            }
            if (fontStream != null) {
                Font antonio = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(style, size);
                fontStream.close();
                System.out.println("✓ Título 'Ingrese sus Datos' usando Antonio desde archivo");
                return antonio;
            }
        } catch (Exception e) {
            System.out.println("⚠ No se pudo cargar Antonio desde archivo: " + e.getMessage());
        }
        
        // Intentar usar Antonio instalada en el sistema
        String[] fontNames = {
            "Antonio",          // Fuente preferida
            "Impact",           // Alternativa condensada moderna
            "Arial Black",      // Alternativa bold
            "SansSerif"         // Fallback genérico
        };
        
        for (String fontName : fontNames) {
            Font font = new Font(fontName, style, size);
            // Verificar si la fuente existe
            if (!font.getFamily().equals("Dialog")) {
                System.out.println("✓ Título 'Ingrese sus Datos' usando fuente: " + fontName);
                return font;
            }
        }
        
        System.out.println("⚠ Título 'Ingrese sus Datos' usando fuente por defecto: SansSerif");
        return new Font("SansSerif", style, size);
    }

}