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

class ProfileInfoRow extends JPanel {

    enum IconType { BOOK, IDCARD, USER, BRIEFCASE, EMAIL }

    ProfileInfoRow(String label, String value, IconType type) {
        setOpaque(false);
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 2));

        JLabel icon = new JLabel(getEmoji(type), SwingConstants.CENTER);
        icon.setFont(getFont().deriveFont(18f));
        icon.setForeground(UIConstants.TEXT_MUTED);
        icon.setPreferredSize(new Dimension(28, 28));
        add(icon, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2,1));
        text.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UIConstants.SMALL);
        l.setForeground(UIConstants.TEXT_MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UIConstants.BODY);
        v.setForeground(UIConstants.TEXT_PRIMARY);
        text.add(l);
        text.add(v);

        add(text, BorderLayout.CENTER);
    }

    private String getEmoji(IconType t) {
        switch (t) {
            case BOOK: return "📘";
            case IDCARD: return "🪪";
            case USER: return "👤";
            case BRIEFCASE: return "💼";
            case EMAIL: return "✉";
            default: return "•";
        }
    }
}
