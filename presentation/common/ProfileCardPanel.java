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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ProfileCardPanel extends JPanel {

    public ProfileCardPanel(String nombre, String programa, String id, String usuario, String rol, String email) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1; gc.weighty = 1;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIConstants.CARD_BG);

        // Contenido interno
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.anchor = GridBagConstraints.WEST;

        // Fila 0: avatar + nombre
        c.gridx = 0; c.gridy = 0;
        CircleBadge avatar = new CircleBadge("EC", UIConstants.AVATAR_BG, Color.WHITE);
        avatar.setPreferredSize(new Dimension(96,96));
        content.add(avatar, c);

        c.gridx = 1;
        JLabel name = new JLabel(nombre);
        name.setFont(new Font("SansSerif", Font.BOLD, 26));
        name.setForeground(UIConstants.TEXT_PRIMARY);
        content.add(name, c);

        // Separador
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(16, 0, 8, 0);
        content.add(new JSeparator(), c);

        // A partir de aquí, añadimos filas 1..N SIN solaparse
        int row = 2;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 0, 6, 0);

        c.gridy = row++; content.add(new ProfileInfoRow("Programa", programa, ProfileInfoRow.IconType.BOOK), c);
        c.gridy = row++; content.add(new ProfileInfoRow("Número de Identificación", id, ProfileInfoRow.IconType.IDCARD), c);
        c.gridy = row++; content.add(new ProfileInfoRow("Usuario", usuario, ProfileInfoRow.IconType.USER), c);
        c.gridy = row++; content.add(new ProfileInfoRow("Rol", rol, ProfileInfoRow.IconType.BRIEFCASE), c);
        c.gridy = row++; content.add(new ProfileInfoRow("Email", email, ProfileInfoRow.IconType.EMAIL), c);

        card.add(content, BorderLayout.NORTH);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE0E7FF), 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        add(card, gc);
    }
}