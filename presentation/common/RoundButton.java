/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.unicauca.gestiontrabajogrado.presentation.common;

/**
 *
 * @author Sofia
 */

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundButton extends JButton {
    private Color bg = new Color(189, 41, 41); // rojo del mockup
    private Color bgHover = new Color(168, 32, 32);

    public RoundButton(String text) {
        super(text);
        setFocusPainted(false);
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(getFont().deriveFont(Font.BOLD, 18f));
        setMargin(new Insets(12, 24, 12, 24));
        // efecto hover
        addChangeListener(e -> repaint());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getModel().isRollover() ? bgHover : bg;
        Shape rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 28, 28);
        g2.setColor(fill);
        g2.fill(rr);
        super.paintComponent(g);
        g2.dispose();
    }

    @Override
    public void setContentAreaFilled(boolean b) { /* noop para evitar L&F */ }
}