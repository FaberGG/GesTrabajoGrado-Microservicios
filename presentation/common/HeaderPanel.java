/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.gestiontrabajogrado.presentation.common;

/**
 *
 * @author Lyz
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class HeaderPanel extends JPanel {
    private BufferedImage logoImage;

    public HeaderPanel() {
        setPreferredSize(new Dimension(10, 100)); // Aumenté la altura para mejor proporción
        setBackground(UIConstants.BLUE_MAIN);
        setLayout(new BorderLayout());

        // Cargar el logo
        loadLogo();

        // Panel principal con logo y texto
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setOpaque(false);

        // Panel del logo (izquierda)
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(90, 100));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel de texto mejorado
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        // Crear labels con mejor tipografía y efectos
        JLabel uni = createStyledLabel("Universidad", new Font("Arial", Font.BOLD, 26), Color.WHITE, true);
        JLabel del = createStyledLabel("del Cauca", new Font("Arial", Font.BOLD, 26), Color.WHITE, true);
        JLabel title = createStyledLabel("Gestión del Proceso de", new Font("Arial", Font.PLAIN, 16),
                new Color(220, 220, 220), false);
        JLabel subtitle = createStyledLabel("Trabajo de Grado", new Font("Arial", Font.PLAIN, 16),
                new Color(220, 220, 220), false);

        // Layout mejorado para el texto
        JPanel universityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        universityPanel.setOpaque(false);
        universityPanel.add(uni);
        universityPanel.add(Box.createHorizontalStrut(8));
        universityPanel.add(del);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createHorizontalStrut(8));
        titlePanel.add(subtitle);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(universityPanel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(titlePanel);
        textPanel.add(Box.createVerticalGlue());

        // Ensamblar el contenido principal
        mainContent.add(logoPanel, BorderLayout.WEST);
        mainContent.add(textPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
        add(new RibbonRight(), BorderLayout.EAST);

        // Borde con efecto de sombra mejorado
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0x6A1B9A)), // Morado más oscuro
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(156, 39, 176, 100)) // Sombra sutil con alpha
        ));
    }

    private void loadLogo() {
        // Rutas simplificadas para la nueva ubicación
        String[] possiblePaths = {
                "/images/logo.png",           // Ruta simple en resources/images/
                "/logo.png",                  // Ruta en raíz de resources
                "/co/unicauca/gestiontrabajogrado/presentation/resources/images/logo.png" // Ruta completa
        };

        for (String path : possiblePaths) {
            try {
                var logoStream = getClass().getResourceAsStream(path);
                if (logoStream != null) {
                    logoImage = ImageIO.read(logoStream);
                    logoStream.close();
                    return;
                } else {
                    System.out.println("✗ No encontrado en: " + path);
                }
            } catch (IOException e) {
                System.err.println("✗ Error al cargar " + path + ": " + e.getMessage());
            }
        }

        System.err.println("No se encontró el logo PNG, usando placeholder");
        createPlaceholderLogo();
    }

    private void createPlaceholderLogo() {
        logoImage = new BufferedImage(60, 60, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = logoImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Crear un logo circular de placeholder
        g2.setColor(Color.WHITE);
        g2.fillOval(5, 5, 50, 50);
        g2.setColor(UIConstants.ACCENT_RED);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(5, 5, 50, 50);

        // Agregar texto "UC"
        g2.setColor(UIConstants.BLUE_MAIN);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        String text = "UC";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2.drawString(text, 30 - textWidth/2, 30 + textHeight/4);

        g2.dispose();
    }

    private JLabel createStyledLabel(String text, Font font, Color color, boolean withShadow) {
        JLabel label = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                if (withShadow) {
                    // Dibujar sombra del texto
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.setFont(getFont());
                    g2.drawString(getText(), 2, getFont().getSize() + 2);
                }

                // Dibujar texto principal
                g2.setColor(getForeground());
                g2.setFont(getFont());
                g2.drawString(getText(), 0, getFont().getSize());
                g2.dispose();
            }
        };
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    // Ribbon mejorado con degradado
    static class RibbonRight extends JComponent {
        RibbonRight() {
            setPreferredSize(new Dimension(120, 100));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Fondo con degradado
            GradientPaint gradient = new GradientPaint(
                    0, 0, UIConstants.BLUE_MAIN,
                    0, getHeight(), UIConstants.BLUE_DARK
            );
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Crear efecto de "mordidas" con sombra
            g2.setColor(UIConstants.ACCENT_RED);
            int w = getWidth();
            int h = getHeight();
            int tooth = 22;
            int x = w - tooth;

            for (int y = 0; y < h; y += tooth) {
                // Sombra de la mordida
                g2.setColor(new Color(UIConstants.ACCENT_RED.getRed(),
                        UIConstants.ACCENT_RED.getGreen(),
                        UIConstants.ACCENT_RED.getBlue(), 100));
                int[] shadowXs = {w + 2, x + 2, w + 2};
                int[] shadowYs = {y + 2, y + tooth/2 + 2, y + tooth + 2};
                g2.fillPolygon(shadowXs, shadowYs, 3);

                // Mordida principal
                g2.setColor(UIConstants.ACCENT_RED);
                int[] xs = {w, x, w};
                int[] ys = {y, y + tooth/2, y + tooth};
                g2.fillPolygon(xs, ys, 3);
            }
            g2.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Degradado de fondo mejorado con múltiples colores
        GradientPaint gradient = new GradientPaint(
                0, 0, UIConstants.BLUE_MAIN,
                0, getHeight(), UIConstants.BLUE_DARK
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Agregar efecto de brillo sutil en la parte superior
        GradientPaint highlight = new GradientPaint(
                0, 0, new Color(255, 255, 255, 30),
                0, getHeight()/3, new Color(255, 255, 255, 0)
        );
        g2.setPaint(highlight);
        g2.fillRect(0, 0, getWidth(), getHeight()/3);

        // Dibujar el logo si está disponible
        if (logoImage != null) {
            // Calcular dimensiones manteniendo proporción
            int originalWidth = logoImage.getWidth();
            int originalHeight = logoImage.getHeight();
            double aspectRatio = (double) originalWidth / originalHeight;

            // Definir altura máxima disponible (con margen)
            int maxHeight = getHeight() - 10; // 10px margen arriba y abajo
            int maxWidth = 80; // Ancho máximo permitido

            // Calcular tamaño final respetando proporciones
            int logoWidth, logoHeight;

            // Ajustar por altura
            if (maxHeight * aspectRatio <= maxWidth) {
                logoHeight = maxHeight;
                logoWidth = (int) (logoHeight * aspectRatio);
            } else {
                // Ajustar por ancho
                logoWidth = maxWidth;
                logoHeight = (int) (logoWidth / aspectRatio);
            }

            // Centrar el logo en su espacio asignado
            int logoX = 15 + (80 - logoWidth) / 2; // Centrado en el espacio de 80px
            int logoY = (getHeight() - logoHeight) / 2;

            // Dibujar sombra del logo
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2.drawImage(logoImage, logoX + 2, logoY + 2, logoWidth, logoHeight, null);

            // Dibujar logo principal
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(logoImage, logoX, logoY, logoWidth, logoHeight, null);

        }

        g2.dispose();
    }
}