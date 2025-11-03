
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

class CircleBadge extends JComponent {
    private final String text;
    private final Color bg;
    private final Color fg;

    CircleBadge(String text, Color bg, Color fg) {
        this.text = text;
        this.bg = bg;
        this.fg = fg;
        setPreferredSize(new Dimension(80, 80));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int d = Math.min(getWidth(), getHeight());
        g2.setColor(bg);
        g2.fillOval(0, 0, d, d);
        g2.setColor(fg);
        Font f = getFont().deriveFont(Font.BOLD, d * 0.35f);
        g2.setFont(f);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (d - fm.stringWidth(text)) / 2;
        int ty = (d - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(text, tx, ty);
        g2.dispose();
    }
}
