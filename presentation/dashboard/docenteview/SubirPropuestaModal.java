package co.unicauca.gestiontrabajogrado.presentation.dashboard.docenteview;

import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoRequestDTO;
import co.unicauca.gestiontrabajogrado.domain.model.enumModalidad;
import co.unicauca.gestiontrabajogrado.presentation.common.DropFileField;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Modal para subir una nueva propuesta de proyecto de grado
 * Diseño mejorado para coincidir con la interfaz de usuario
 */
public class SubirPropuestaModal extends JPanel {

    private JTextField txtTitulo;
    private JComboBox<String> cboModalidad;
    private JTextField txtIdentificacionDirector;
    private JTextField txtCodirector;
    private JTextArea txtObjetivoGeneral;
    private JTextArea txtObjetivosEspecificos;
    private DropFileField dropFormatoA;
    private DropFileField dropCarta;
    private JButton btnEnviar;
    private JButton btnCancelar;

    private Consumer<ProyectoGradoRequestDTO> onSubmit;
    private Runnable onCancel;

    private DatePicker datePicker;
    private JLabel lblCarta;
    private JPanel cartaContainer;

    public SubirPropuestaModal() {
        // Forzar popups heavyweight para que los menús se abran sobre el glass pane
        UIManager.put("ComboBox.isPopupLightWeight", Boolean.FALSE);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 650));

        initComponents();
    }

    private void initComponents() {
        // Panel principal con scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // Título del modal
        JLabel lblTitulo = new JLabel("NUEVA PROPUESTA DE PROYECTO DE GRADO");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Panel de dos columnas
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);

        // Fila 1: Título del proyecto (ancho completo)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(crearLabel("Título del Proyecto *"), gbc);

        gbc.gridy = 1;
        txtTitulo = crearTextField("Ingrese el título del proyecto de grado");
        formPanel.add(txtTitulo, gbc);

        // Fila 2: Modalidad y Fecha Actual
        gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(crearLabel("Modalidad *"), gbc);

        gbc.gridx = 1;
        formPanel.add(crearLabel("Fecha Actual *"), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        String[] modalidades = {"Seleccione modalidad", "INVESTIGACION", "PRACTICA_PROFESIONAL", "EMPRENDIMIENTO"};
        cboModalidad = new JComboBox<>(modalidades);
        cboModalidad.setPreferredSize(new Dimension(280, 40));
        cboModalidad.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cboModalidad.setLightWeightPopupEnabled(false);
        cboModalidad.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(cboModalidad, gbc);

        // DatePicker
        gbc.gridx = 1;
        DatePickerSettings settings = new DatePickerSettings(new Locale("es", "CO"));
        settings.setAllowEmptyDates(false);
        settings.setFormatForDatesCommonEra("MM/dd/yyyy");
        settings.setFirstDayOfWeek(java.time.DayOfWeek.MONDAY);
        settings.setAllowKeyboardEditing(false);
        datePicker = new DatePicker(settings);
        datePicker.setDate(LocalDate.now());
        datePicker.setBackground(Color.WHITE);
        datePicker.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        datePicker.getComponentToggleCalendarButton().setText("📅");
        formPanel.add(datePicker, gbc);

        // Fila 3: Identificación de director y Codirector
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(crearLabel("Identificación de director *"), gbc);

        gbc.gridx = 1;
        formPanel.add(crearLabel("Codirector del proyecto *"), gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        txtIdentificacionDirector = crearTextField("Número de indentificación del director");
        formPanel.add(txtIdentificacionDirector, gbc);

        gbc.gridx = 1;
        txtCodirector = crearTextField("Nombre completo del codirector");
        formPanel.add(txtCodirector, gbc);

        // Fila 4: Objetivo General
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(crearLabel("Objetivo General *"), gbc);

        gbc.gridy = 7;
        txtObjetivoGeneral = new JTextArea(3, 20);
        txtObjetivoGeneral.setLineWrap(true);
        txtObjetivoGeneral.setWrapStyleWord(true);
        txtObjetivoGeneral.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObjetivoGeneral.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollObjetivo = new JScrollPane(txtObjetivoGeneral);
        scrollObjetivo.setPreferredSize(new Dimension(590, 70));
        formPanel.add(scrollObjetivo, gbc);

        // Fila 5: Objetivos Específicos
        gbc.gridy = 8;
        formPanel.add(crearLabel("Objetivos Específicos *"), gbc);

        gbc.gridy = 9;
        txtObjetivosEspecificos = new JTextArea(3, 20);
        txtObjetivosEspecificos.setLineWrap(true);
        txtObjetivosEspecificos.setWrapStyleWord(true);
        txtObjetivosEspecificos.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObjetivosEspecificos.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollEspecificos = new JScrollPane(txtObjetivosEspecificos);
        scrollEspecificos.setPreferredSize(new Dimension(590, 70));
        formPanel.add(scrollEspecificos, gbc);

        // Fila 6: Formato A (PDF)
        gbc.gridy = 10;
        formPanel.add(crearLabel("Formato A (PDF) *"), gbc);

        gbc.gridy = 11;
        dropFormatoA = new DropFileField();
        dropFormatoA.setLine1("📎 Arrastre el archivo aquí o haga clic para seleccionar");
        dropFormatoA.setLine2("Solo un archivo PDF");
        dropFormatoA.setPreferredSize(new Dimension(590, 90));
        formPanel.add(dropFormatoA, gbc);

        // Fila 7: Carta de Aceptación de la empresa (PDF)
        gbc.gridy = 12;
        lblCarta = crearLabel("Carta de Aceptación de la empresa (PDF) *");
        formPanel.add(lblCarta, gbc);

        gbc.gridy = 13;
        cartaContainer = new JPanel(new BorderLayout());
        cartaContainer.setOpaque(false);
        dropCarta = new DropFileField();
        dropCarta.setLine1("📎 Arrastre el archivo aquí o haga clic para seleccionar");
        dropCarta.setLine2("Solo un archivo PDF");
        dropCarta.setPreferredSize(new Dimension(590, 90));
        cartaContainer.add(dropCarta, BorderLayout.CENTER);
        formPanel.add(cartaContainer, gbc);

        // Mostrar u ocultar carta según modalidad
        cboModalidad.addActionListener(e -> actualizarVisibilidadCarta());
        actualizarVisibilidadCarta();

        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setBackground(Color.WHITE);
        panelBotones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(140, 45));
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btnCancelar.setBackground(new Color(150, 150, 150));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
        });

        btnEnviar = new JButton("Enviar Propuesta");
        btnEnviar.setPreferredSize(new Dimension(180, 45));
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnEnviar.setBackground(new Color(166, 15, 21));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.addActionListener(e -> submitPropuesta());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnEnviar);
        mainPanel.add(panelBotones);

        // Agregar scroll
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        if (texto.endsWith("*")) {
            label.setForeground(new Color(50, 50, 50));
        }
        return label;
    }

    private JTextField crearTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(280, 40));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Placeholder
        textField.setForeground(Color.GRAY);
        textField.setText(placeholder);
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
                textField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 200, 200), 1),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });

        return textField;
    }

    /**
     * Marcar un campo con borde rojo para indicar error
     */
    private void marcarCampoError(Component campo) {
        if (campo instanceof JTextField) {
            JTextField txt = (JTextField) campo;
            txt.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 53, 69), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else if (campo instanceof JTextArea) {
            JTextArea txt = (JTextArea) campo;
            txt.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 53, 69), 2),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        } else if (campo instanceof JComboBox) {
            JComboBox<?> cmb = (JComboBox<?>) campo;
            cmb.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(220, 53, 69), 2),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else if (campo instanceof DropFileField) {
            DropFileField drop = (DropFileField) campo;
            drop.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 2));
        }
    }

    /**
     * Resetear todos los bordes a su color normal
     */
    private void resetearBordesValidacion() {
        txtTitulo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtIdentificacionDirector.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        txtCodirector.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        txtObjetivoGeneral.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtObjetivosEspecificos.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        cboModalidad.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        if (dropFormatoA != null) dropFormatoA.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        if (dropCarta != null) dropCarta.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    private void submitPropuesta() {
        // Resetear bordes a color normal
        resetearBordesValidacion();

        // Validar campos obligatorios
        String titulo = txtTitulo.getText().trim();
        String modalidad = (String) cboModalidad.getSelectedItem();
        String directorId = txtIdentificacionDirector.getText().trim();
        String codirector = txtCodirector.getText().trim();
        String objetivoGeneral = txtObjetivoGeneral.getText().trim();
        String objetivosEspecificos = txtObjetivosEspecificos.getText().trim();

        Component primerCampoConError = null;
        StringBuilder mensajeError = new StringBuilder("Por favor complete los siguientes campos:\n\n");
        boolean hayError = false;

        // Validar título
        if (titulo.isEmpty() || titulo.equals("Ingrese el título del proyecto de grado")) {
            marcarCampoError(txtTitulo);
            mensajeError.append("• Título del Proyecto\n");
            if (primerCampoConError == null) primerCampoConError = txtTitulo;
            hayError = true;
        }

        // Validar modalidad
        if (modalidad == null || modalidad.equals("Seleccione modalidad")) {
            marcarCampoError(cboModalidad);
            mensajeError.append("• Modalidad\n");
            if (primerCampoConError == null) primerCampoConError = cboModalidad;
            hayError = true;
        }

        // Validar identificación del director
        if (directorId.isEmpty() || directorId.equals("Número de indentificación del director")) {
            marcarCampoError(txtIdentificacionDirector);
            mensajeError.append("• Identificación de director\n");
            if (primerCampoConError == null) primerCampoConError = txtIdentificacionDirector;
            hayError = true;
        }

        // Validar codirector
        if (codirector.isEmpty() || codirector.equals("Nombre completo del codirector")) {
            marcarCampoError(txtCodirector);
            mensajeError.append("• Codirector del proyecto\n");
            if (primerCampoConError == null) primerCampoConError = txtCodirector;
            hayError = true;
        }

        // Validar objetivo general
        if (objetivoGeneral.isEmpty()) {
            marcarCampoError(txtObjetivoGeneral);
            mensajeError.append("• Objetivo General\n");
            if (primerCampoConError == null) primerCampoConError = txtObjetivoGeneral;
            hayError = true;
        }

        // Validar objetivos específicos
        if (objetivosEspecificos.isEmpty()) {
            marcarCampoError(txtObjetivosEspecificos);
            mensajeError.append("• Objetivos Específicos\n");
            if (primerCampoConError == null) primerCampoConError = txtObjetivosEspecificos;
            hayError = true;
        }

        // Validar Formato A
        File formatoA = dropFormatoA.getSelectedFile();
        if (formatoA == null) {
            marcarCampoError(dropFormatoA);
            mensajeError.append("• Formato A (PDF)\n");
            if (primerCampoConError == null) primerCampoConError = dropFormatoA;
            hayError = true;
        }

        // Validar Carta de Aceptación solo si es PRACTICA_PROFESIONAL
        boolean esPractica = "PRACTICA_PROFESIONAL".equals(modalidad);
        File carta = dropCarta.getSelectedFile();
        if (esPractica && carta == null) {
            marcarCampoError(dropCarta);
            mensajeError.append("• Carta de Aceptación de la empresa (PDF)\n");
            if (primerCampoConError == null) primerCampoConError = dropCarta;
            hayError = true;
        }

        // Si hay errores, mostrar mensaje y hacer scroll al primer campo con error
        if (hayError) {
            JOptionPane.showMessageDialog(this,
                    mensajeError.toString(),
                    "Campos requeridos",
                    JOptionPane.WARNING_MESSAGE);

            if (primerCampoConError != null) {
                primerCampoConError.requestFocus();
                if (primerCampoConError instanceof JComponent) {
                    ((JComponent) primerCampoConError).scrollRectToVisible(primerCampoConError.getBounds());
                }
            }
            return;
        }

        // Crear DTO
        ProyectoGradoRequestDTO request = new ProyectoGradoRequestDTO();
        request.setTitulo(titulo);

        // Convertir modalidad String a enum
        try {
            enumModalidad modalidadEnum = enumModalidad.valueOf(modalidad);
            request.setModalidad(modalidadEnum);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this,
                    "Modalidad inválida",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        request.setObjetivoGeneral(objetivoGeneral);
        request.setObjetivosEspecificos(objetivosEspecificos);

        // Log de archivos
        System.out.println("📄 Formato A: " + (formatoA != null ? formatoA.getAbsolutePath() : "no adjunto"));
        System.out.println("📄 Carta: " + (esPractica ? (carta != null ? carta.getAbsolutePath() : "no adjunta") : "no aplica"));

        // Notificar al listener
        if (onSubmit != null) onSubmit.accept(request);

        // Mensaje de éxito
        JOptionPane.showMessageDialog(this,
                "✅ Propuesta enviada exitosamente\n\n" +
                        "Título: " + titulo + "\n" +
                        "Modalidad: " + modalidad + "\n" +
                        "Estado: FORMATO_A_DILIGENCIADO",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

        // Limpiar campos
        limpiarCampos();
    }

    private void limpiarCampos() {
        txtTitulo.setForeground(Color.GRAY);
        txtTitulo.setText("Ingrese el título del proyecto de grado");

        cboModalidad.setSelectedIndex(0);

        txtIdentificacionDirector.setForeground(Color.GRAY);
        txtIdentificacionDirector.setText("Número de indentificación del director");

        txtCodirector.setForeground(Color.GRAY);
        txtCodirector.setText("Nombre completo del codirector");

        txtObjetivoGeneral.setText("");
        txtObjetivosEspecificos.setText("");

        if (datePicker != null) datePicker.setDate(LocalDate.now());
        if (dropFormatoA != null) dropFormatoA.clearFile();
        if (dropCarta != null) dropCarta.clearFile();

        actualizarVisibilidadCarta();
    }

    private void actualizarVisibilidadCarta() {
        String selected = (String) cboModalidad.getSelectedItem();
        boolean esPractica = "PRACTICA_PROFESIONAL".equals(selected);
        if (lblCarta != null) lblCarta.setVisible(esPractica);
        if (cartaContainer != null) cartaContainer.setVisible(esPractica);
        revalidate();
        repaint();
    }

    /**
     * Método para compatibilidad con DocenteView
     */
    public ProyectoGradoRequestDTO construirDTO() {
        ProyectoGradoRequestDTO request = new ProyectoGradoRequestDTO();

        String titulo = txtTitulo.getText().trim();
        if (!titulo.equals("Ingrese el título del proyecto de grado")) {
            request.setTitulo(titulo);
        }

        String modalidad = (String) cboModalidad.getSelectedItem();
        if (modalidad != null && !modalidad.equals("Seleccione modalidad")) {
            try {
                enumModalidad modalidadEnum = enumModalidad.valueOf(modalidad);
                request.setModalidad(modalidadEnum);
            } catch (IllegalArgumentException e) {
                // Modalidad no válida. Ignorar.
            }
        }

        request.setObjetivoGeneral(txtObjetivoGeneral.getText().trim());
        request.setObjetivosEspecificos(txtObjetivosEspecificos.getText().trim());

        return request;
    }

    /**
     * Obtener el archivo de Formato A
     */
    public java.io.File getFormatoA() {
        return dropFormatoA != null ? dropFormatoA.getSelectedFile() : null;
    }

    /**
     * Obtener el archivo de Carta de Empresa
     */
    public java.io.File getCartaEmpresa() {
        return dropCarta != null ? dropCarta.getSelectedFile() : null;
    }

    /**
     * Reiniciar el formulario
     */
    public void reset() {
        limpiarCampos();
    }

    /**
     * Establecer el listener para cuando se envíe el formulario
     */
    public void setOnSubmit(Consumer<ProyectoGradoRequestDTO> onSubmit) {
        this.onSubmit = onSubmit;
    }

    /**
     * Establecer el listener para cuando se cancele
     */
    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    /**
     * Método alternativo para compatibilidad
     */
    public void setOnSubmitValid(Runnable onValid) {
        // Compatibilidad con código existente
    }
}

