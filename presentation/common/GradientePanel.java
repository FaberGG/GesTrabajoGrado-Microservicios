/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.gestiontrabajogrado.presentation.common;

/**
 *
 * @author Sofia
 */

import java.awt.*;
import javax.swing.*;

public class GradientePanel extends JPanel {
    private Color color1 = new Color(0, 70, 120);   // arriba
    private Color color2 = new Color(20, 110, 160); // abajo
    private int arc = 0; // esquinas (0 = recto)

    public GradientePanel() { setOpaque(false); }

    public GradientePanel(Color c1, Color c2, int arc) {
        this();
        this.color1 = c1;
        this.color2 = c2;
        this.arc = arc;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth(), h = getHeight();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
        g2.setPaint(gp);
        if (arc > 0) {
            g2.fillRoundRect(0, 0, w, h, arc, arc);
        } else {
            g2.fillRect(0, 0, w, h);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    // setters para cambiar desde el diseñador
    public void setColor1(Color c){ this.color1 = c; repaint(); }
    public Color getColor1(){ return color1; }
    public void setColor2(Color c){ this.color2 = c; repaint(); }
    public Color getColor2(){ return color2; }
    public void setArc(int arc){ this.arc = arc; repaint(); }
    public int getArc(){ return arc; }
}