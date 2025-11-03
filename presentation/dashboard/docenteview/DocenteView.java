package co.unicauca.gestiontrabajogrado.presentation.dashboard.docenteview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import co.unicauca.gestiontrabajogrado.domain.model.enumEstadoProyecto;
import co.unicauca.gestiontrabajogrado.presentation.common.GradientePanel;
import co.unicauca.gestiontrabajogrado.presentation.common.DropFileField;
import co.unicauca.gestiontrabajogrado.domain.model.User;

// NUEVO: para llamar al controller y armar el DTO
import co.unicauca.gestiontrabajogrado.controller.DocenteController;
import co.unicauca.gestiontrabajogrado.domain.model.enumModalidad;
import co.unicauca.gestiontrabajogrado.dto.ProyectoGradoRequestDTO;
import co.unicauca.gestiontrabajogrado.dto.*;
public class DocenteView extends JFrame {

    // ===== Paleta / Tipos =====
    static final Color C_AZUL_HEADER = new Color(8, 78, 130);
    static final Color C_ROJO_1      = new Color(166, 15, 21);
    static final Color C_ROJO_2      = new Color(204, 39, 29);
    static final Color C_GRIS_FONDO  = new Color(245, 246, 248);
    static final Color C_BORDE_SUAVE = new Color(220, 220, 220);
    static final Color C_SOMBRA      = new Color(0,0,0,28);

    static final Font  F_H2   = new Font("SansSerif", Font.BOLD, 22);
    static final Font  F_H3   = new Font("SansSerif", Font.BOLD, 16);
    static final Font  F_BODY = new Font("SansSerif", Font.PLAIN, 14);

    // ===== Estado =====
    private User currentUser;

    // >>> NUEVO: referencia al controller (para llamar handleCrearProyecto)
    private DocenteController controller;

    // ===== UI raíz =====
    private final JPanel center = new JPanel(new BorderLayout());
    private final HomePanel homePanel = new HomePanel();

    // ===== Modal =====
    private final ModalLayer modalLayer = new ModalLayer();
    private final SubirPropuestaModal modalSubir = new SubirPropuestaModal();

    // Hooks externos (los dejo, no estorban)
    private Runnable onDescargarPlantilla;
    private Runnable onLogout;

    // Agregar como campo de instancia:
    private final DetalleProyectoModal modalDetalle = new DetalleProyectoModal();


    public DocenteView() {
        setTitle("Gestión de Trabajos de Grado - Docente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1240, 800));
        setLocationRelativeTo(null);

        buildUI();

        setGlassPane(modalLayer);
        modalLayer.setVisible(false);

        homePanel.btnNuevaPropuesta.addActionListener(e -> abrirModalSubir());
        homePanel.btnDescargarPlantilla.addActionListener(e -> {
            if (onDescargarPlantilla != null) onDescargarPlantilla.run();
            else JOptionPane.showMessageDialog(this,
                    "Acción de descarga no configurada.\nUsa setOnDescargarPlantilla(...) para conectarla.",
                    "Descargar Plantilla", JOptionPane.INFORMATION_MESSAGE);
        });

        homePanel.btnMenu.addActionListener(this::mostrarMenu);
        homePanel.setOnProyectoClick(this::abrirModalDetalleProyecto);
    }
    public DocenteView(User user) {
        this();
        setUser(user);
    }

    // ===== Conexión con controller (NUEVO) =====
    public void setController(DocenteController controller){
        this.controller = controller;
        // si quieres, cableo también el logout al controller:
        setOnLogout(() -> {
            if (this.controller != null) this.controller.handleCerrarSesion();
        });
    }

    // Hooks opcionales
    public void setOnDescargarPlantilla(Runnable action) { this.onDescargarPlantilla = action; }
    public void setOnLogout(Runnable action) { this.onLogout = action; }

    // Datos reales
    public void setEstudiantes(List<String> nombres){ homePanel.listEstudiantes.setData(nombres); }
    public void setPropuestas(List<PropuestaItem> items){ homePanel.listPropuestas.setData(items); }

    public void setUser(User user){
        this.currentUser = user;
        homePanel.setAvatarText(inicialesDe(user));
    }

    private static String inicialesDe(User u){
        if (u == null) return "CC";
        String n = (u.getNombres() != null ? u.getNombres() : "").trim();
        String a = (u.getApellidos()!= null ? u.getApellidos(): "").trim();
        String i1 = n.isEmpty()? "" : n.substring(0,1);
        String i2 = a.isEmpty()? "" : a.substring(0,1);
        String r = (i1 + i2).toUpperCase();
        return r.isEmpty()? "CC" : r;
    }

    // ================= UI principal =================
    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());

        // Header azul
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(C_AZUL_HEADER);
        topBar.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel brand = new JLabel("Universidad del Cauca · Gestión del Proceso de Trabajo de Grado");
        brand.setForeground(Color.WHITE);
        brand.setFont(F_H3);
        topBar.add(brand, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton btnBell = iconBtn("🔔");
        JButton btnMenu = iconBtn("☰");
        homePanel.btnMenu = btnMenu;

        // Avatar con menú "Cerrar sesión"
        JLabel avatar = new JLabel("CC", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36,36));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(255,255,255,40));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(F_H3);
        avatar.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255,255,255,120), 1, true));
        avatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JPopupMenu pm = new JPopupMenu();
                JMenuItem out = new JMenuItem("Cerrar sesión");
                out.addActionListener(ev -> {
                    if (onLogout != null) onLogout.run();
                    else JOptionPane.showMessageDialog(DocenteView.this,
                            "Acción de cerrar sesión no configurada.\nUsa setOnLogout(...).",
                            "Cerrar sesión", JOptionPane.INFORMATION_MESSAGE);
                });
                pm.add(out);
                pm.show(avatar, 0, avatar.getHeight());
            }
        });
        homePanel.avatar = avatar;

        right.add(btnBell);
        right.add(btnMenu);
        right.add(avatar);

        topBar.add(right, BorderLayout.EAST);
        getContentPane().add(topBar, BorderLayout.NORTH);

        // Centro
        center.setBackground(C_GRIS_FONDO);
        center.add(homePanel, BorderLayout.CENTER);
        getContentPane().add(center, BorderLayout.CENTER);
    }

    private JButton iconBtn(String txt){
        JButton b = new JButton(txt);
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(255, 255, 255, 80)); // Más opaco para que se vea mejor
        b.setFont(F_H3);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(42, 36));
        
        // Efecto hover
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(255, 255, 255, 120));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(255, 255, 255, 80));
            }
        });
        b.setBorder(new EmptyBorder(4,10,4,10));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private void abrirModalDetalleProyecto(PropuestaItem item) {
        if (controller == null) return;

        ProyectoGradoResponseDTO proyecto = controller.obtenerProyectoPorId(item.id);
        FormatoADetalleDTO formato = controller.obtenerUltimoFormatoA(item.id);

        if (proyecto == null) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el proyecto",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modalDetalle.cargarProyecto(proyecto, formato);

        modalDetalle.setOnSubmit(() -> {
            boolean exitoso = controller.handleSubirNuevaVersion(
                    modalDetalle.getProyectoId(),
                    modalDetalle.getNuevoFormatoA(),
                    modalDetalle.getNuevaCarta(),
                    modalDetalle.getObjetivoGeneral(),        // NUEVO
                    modalDetalle.getObjetivosEspecificos()    // NUEVO
            );
            if (exitoso) modalLayer.cerrar();
        });

        modalDetalle.setOnCancel(modalLayer::cerrar);
        modalLayer.showModal(modalDetalle, modalDetalle::reset, new Dimension(900, 700));
    }

    // ===== Menú del docente (☰) =====
    private void mostrarMenu(ActionEvent ev){
        JPopupMenu menu = new JPopupMenu();
        JMenuItem mi1 = new JMenuItem("Subir formato A");
        JMenuItem mi2 = new JMenuItem("Revisar avances del anteproyecto");
        JMenuItem mi3 = new JMenuItem("Emitir evaluación del anteproyecto");
        JMenuItem mi4 = new JMenuItem("Hacer Seguimiento al proyecto de grado");

        mi1.addActionListener(e -> abrirModalSubir());
        menu.add(mi1); menu.add(mi2); menu.add(mi3); menu.add(mi4);

        Component src = (Component) ev.getSource();
        menu.show(src, 0, src.getHeight());
    }

    private void abrirModalSubir() {
        modalSubir.setOnSubmitValid(() -> {
            ProyectoGradoRequestDTO req = modalSubir.construirDTO();
            java.io.File formatoA = modalSubir.getFormatoA();
            java.io.File carta = modalSubir.getCartaEmpresa();

            if (controller != null) {
                boolean exitoso = controller.handleCrearProyecto(req, formatoA, carta);

                if (exitoso) {
                    modalLayer.cerrar();  // ← Solo cierra si fue exitoso
                }
                // Si NO fue exitoso, el modal permanece abierto
            } else {
                JOptionPane.showMessageDialog(this,
                        "Controller no conectado", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        modalSubir.setOnCancel(modalLayer::cerrar);
        modalLayer.showModal(modalSubir, modalSubir::reset, new Dimension(920, 620));
    }


    // --- mapeo seguro de las 3 modalidades del combo a tu enum del dominio ---
    private static enumModalidad toEnumModalidad(String etiqueta){
        if (etiqueta == null) return null;
        String s = Normalizer.normalize(etiqueta, Normalizer.Form.NFD)
                              .replaceAll("\\p{M}", "")
                              .toLowerCase().trim();
        //if (s.startsWith("plan"))            //return enumModalidad.PLAN_COTERMINAL;
        if (s.startsWith("invest"))          return enumModalidad.INVESTIGACION;
        if (s.startsWith("practica"))        return enumModalidad.PRACTICA_PROFESIONAL;
        return null;
    }

    // ============================ HOME ============================
    private static class HomePanel extends JPanel {
        final GradientePanel banda = new GradientePanel(C_ROJO_1, C_ROJO_2, 16);
        final RoundedButton btnNuevaPropuesta     = RoundedButton.primary("+ Nueva Propuesta de Proyecto");
        final RoundedButton btnDescargarPlantilla = RoundedButton.neutral("Descargar Plantilla Formato A");

        JButton btnMenu;
        JLabel  avatar;

        final CardPanel cardEstudiantes = new CardPanel("Estudiantes Dirigidos");
        final CardPanel cardPropuestas  = new CardPanel("Propuestas");

        final EstudiantesList listEstudiantes = new EstudiantesList();
        final PropuestasList  listPropuestas  = new PropuestasList();

        HomePanel() {
            setLayout(new BorderLayout(16,16));
            setBorder(new EmptyBorder(16,16,16,16));
            setBackground(C_GRIS_FONDO);

            banda.setLayout(new BorderLayout());
            JLabel title = new JLabel("Panel Docente", SwingConstants.LEFT);
            title.setForeground(Color.WHITE);
            title.setFont(F_H2);
            title.setBorder(new EmptyBorder(12,16,12,16));
            banda.add(title, BorderLayout.CENTER);
            add(banda, BorderLayout.NORTH);

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            botones.setOpaque(false);
            botones.add(btnNuevaPropuesta);
            botones.add(btnDescargarPlantilla);

            JScrollPane spEst = scrollFor(listEstudiantes);
            JScrollPane spPro = scrollFor(listPropuestas);

            cardEstudiantes.setContent(spEst);
            cardPropuestas.setContent(spPro);

            JPanel grid = new JPanel(new GridLayout(1,2,16,16));
            grid.setOpaque(false);
            grid.add(cardEstudiantes);
            grid.add(cardPropuestas);

            JPanel center = new JPanel(new BorderLayout(0,12));
            center.setOpaque(false);
            center.add(botones, BorderLayout.NORTH);
            center.add(grid, BorderLayout.CENTER);

            add(center, BorderLayout.CENTER);
            listPropuestas.setOnProyectoClick(this::onProyectoClick);
        }
        private void onProyectoClick(PropuestaItem item) {
            // Este método se llamará desde DocenteView
            if (clickHandler != null) {
                clickHandler.accept(item);
            }
        }
        private java.util.function.Consumer<PropuestaItem> clickHandler;
        void setOnProyectoClick(java.util.function.Consumer<PropuestaItem> handler) {
            this.clickHandler = handler;
        }
        private JScrollPane scrollFor(JComponent c){
            JScrollPane sp = new JScrollPane(c);
            sp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            sp.getViewport().setBackground(Color.WHITE);
            sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            return sp;
        }

        void setAvatarText(String initials){
            if (avatar != null) avatar.setText(initials);
        }
    }

    // ====== Panel tarjeta ======
    private static class CardPanel extends JPanel {
        private final JLabel header = new JLabel("", SwingConstants.LEFT);
        private final JPanel contentHolder = new JPanel(new BorderLayout());
        CardPanel(String titulo){
            setOpaque(false);
            setLayout(new BorderLayout());
            header.setText(titulo);
            header.setFont(F_H3);
            header.setBorder(new EmptyBorder(10,14,6,14));
            add(header, BorderLayout.NORTH);
            contentHolder.setOpaque(false);
            contentHolder.setBorder(new EmptyBorder(0,14,14,14));
            add(contentHolder, BorderLayout.CENTER);
        }
        void setContent(JComponent c){
            contentHolder.removeAll();
            CardBody body = new CardBody();
            body.setLayout(new BorderLayout());
            body.add(c, BorderLayout.CENTER);
            contentHolder.add(body, BorderLayout.CENTER);
        }
        private static class CardBody extends JPanel {
            CardBody(){ setOpaque(false); setBorder(new EmptyBorder(12,12,12,12)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int arc = 18;
                g2.setColor(C_SOMBRA);
                for(int i=0;i<6;i++){
                    g2.drawRoundRect(6+i,6+i,getWidth()-12-i*2,getHeight()-12-i*2,arc,arc);
                }
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0,0,getWidth()-12,getHeight()-12,arc,arc);
                g2.setColor(C_BORDE_SUAVE);
                g2.drawRoundRect(0,0,getWidth()-12,getHeight()-12,arc,arc);
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    // ====== Estudiantes con avatar azul + scroll ======
    private static class EstudiantesList extends JPanel {
        EstudiantesList(){ setOpaque(false); setLayout(new BorderLayout()); }
        void setData(List<String> nombres){
            removeAll();
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);
            if (nombres != null && !nombres.isEmpty()) {
                for (int i = 0; i < nombres.size(); i++) {
                    listPanel.add(fila(nombres.get(i)));
                    if (i < nombres.size() - 1) listPanel.add(Box.createVerticalStrut(8));
                }
            } else {
                JLabel emptyLabel = new JLabel("No hay estudiantes asignados", SwingConstants.CENTER);
                emptyLabel.setForeground(new Color(120,120,120));
                emptyLabel.setFont(F_BODY);
                listPanel.add(emptyLabel);
            }
            add(listPanel, BorderLayout.NORTH);
            revalidate(); repaint();
        }
        private JComponent fila(String nombre){
            JLabel avatar = new JLabel(iniciales(nombre), SwingConstants.CENTER);
            avatar.setPreferredSize(new Dimension(32,32));
            avatar.setOpaque(true);
            avatar.setBackground(new Color(215,234,255));
            avatar.setForeground(new Color(10,96,180));
            avatar.setFont(F_BODY.deriveFont(Font.BOLD, 13f));
            avatar.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(150,190,240), 1, true));
            JLabel lbl = new JLabel(nombre);
            lbl.setFont(F_BODY);
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            chip.setOpaque(false);
            chip.add(avatar); chip.add(lbl);
            JPanel card = new JPanel(new BorderLayout());
            card.setOpaque(true);
            card.setBackground(Color.WHITE);
            card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(C_BORDE_SUAVE,1,true),
                    new EmptyBorder(6,10,6,10)
            ));
            card.add(chip, BorderLayout.WEST);
            card.setPreferredSize(new Dimension(card.getPreferredSize().width, 50));
            card.setMinimumSize(new Dimension(200, 50));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            return card;
        }
        private static String iniciales(String n){
            if (n == null || n.trim().isEmpty()) return "?";
            String[] p = n.trim().split("\\s+");
            String a = p.length>0 ? p[0].substring(0,1) : "";
            String b = p.length>1 ? p[1].substring(0,1) : "";
            String r = (a+b).toUpperCase();
            return r.isEmpty()? "?" : r;
        }
    }

    // ====== Propuestas con ícono documento rojo + scroll ======
    public static class PropuestaItem {
        public final String titulo;
        public final String fecha;
        public final Integer id;  // NUEVO
        public final String estado;  // NUEVO
        public final Integer numeroIntentos;  // NUEVO

        public PropuestaItem(String titulo, String fecha, Integer id, String estado, Integer numeroIntentos) {
            this.titulo = titulo;
            this.fecha = fecha;
            this.id = id;
            this.estado = estado;
            this.numeroIntentos = numeroIntentos;
        }
    }

    private static class PropuestasList extends JPanel {
        private java.util.function.Consumer<PropuestaItem> onProyectoClick;

        PropuestasList(){
            setOpaque(false);
            setLayout(new BorderLayout());
        }

        void setOnProyectoClick(java.util.function.Consumer<PropuestaItem> callback) {
            this.onProyectoClick = callback;
        }

        void setData(List<PropuestaItem> items){
            removeAll();
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);
            if (items != null && !items.isEmpty()) {
                for (int i = 0; i < items.size(); i++) {
                    listPanel.add(item(items.get(i)));
                    if (i < items.size() - 1) listPanel.add(Box.createVerticalStrut(8));
                }
            } else {
                JLabel emptyLabel = new JLabel("No hay propuestas registradas", SwingConstants.CENTER);
                emptyLabel.setForeground(new Color(120,120,120));
                emptyLabel.setFont(F_BODY);
                listPanel.add(emptyLabel);
            }
            add(listPanel, BorderLayout.NORTH);
            revalidate(); repaint();
        }

        private JComponent item(PropuestaItem it){
            JLabel icon = new JLabel(new DocIcon(18, C_ROJO_1));
            icon.setPreferredSize(new Dimension(18, 22));

            JLabel title = new JLabel(it.titulo);
            title.setFont(F_BODY);

            JLabel time = new JLabel(it.fecha, SwingConstants.RIGHT);
            time.setFont(new Font("SansSerif", Font.PLAIN, 12));
            time.setForeground(new Color(90,90,90));

            // NUEVO: Indicador de estado
            JLabel lblEstado = new JLabel();
            lblEstado.setFont(new Font("SansSerif", Font.BOLD, 12));

            Color bgColor = Color.WHITE;

            if (it.estado != null) {
                switch (it.estado) {
                    case "RECHAZADO":
                        lblEstado.setText("⚠ Rechazado (" + it.numeroIntentos + "/3)");
                        lblEstado.setForeground(C_ROJO_1);
                        bgColor = new Color(255, 245, 245);
                        break;
                    case "EN_PROCESO":
                        lblEstado.setText("[*] En proceso");
                        lblEstado.setForeground(new Color(200, 140, 0));
                        bgColor = new Color(255, 250, 240);
                        break;
                    case "APROBADO":
                        lblEstado.setText("✓ Aprobado");
                        lblEstado.setForeground(new Color(0, 150, 0));
                        bgColor = new Color(240, 255, 245);
                        break;
                    case "RECHAZADO_DEFINITIVO":
                        lblEstado.setText("✕ Rechazado definitivo");
                        lblEstado.setForeground(new Color(120, 0, 0));
                        bgColor = new Color(250, 235, 235);
                        break;
                    default:
                        lblEstado.setText(it.estado);
                        lblEstado.setForeground(new Color(90,90,90));
                }
            }

            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            left.setOpaque(false);
            left.add(icon);
            left.add(title);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
            right.setOpaque(false);
            right.add(lblEstado);
            right.add(time);

            JPanel line = new JPanel(new BorderLayout());
            line.setOpaque(false);
            line.add(left, BorderLayout.WEST);
            line.add(right, BorderLayout.EAST);

            JPanel card = new JPanel(new BorderLayout());
            card.setOpaque(true);
            card.setBackground(bgColor);
            card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(C_BORDE_SUAVE,1,true),
                    new EmptyBorder(6,10,6,10)
            ));
            card.add(line, BorderLayout.CENTER);
            card.setPreferredSize(new Dimension(card.getPreferredSize().width, 50));
            card.setMinimumSize(new Dimension(200, 50));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            // NUEVO: Hacer clickeable
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onProyectoClick != null) {
                        onProyectoClick.accept(it);
                    }
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new javax.swing.border.LineBorder(C_ROJO_1, 2, true),
                            new EmptyBorder(5,9,5,9)
                    ));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            new javax.swing.border.LineBorder(C_BORDE_SUAVE, 1, true),
                            new EmptyBorder(6,10,6,10)
                    ));
                }
            });

            return card;
        }
    }

    // ====== Ícono documento rojo ======
    private static class DocIcon implements javax.swing.Icon {
        private final int s; private final Color c;
        DocIcon(int size, Color color){ this.s=size; this.c=color; }
        @Override public void paintIcon(Component cpt, Graphics g, int x, int y) {
            Graphics2D g2=(Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.c);
            int w=s, h=(int)(s*1.2);
            int rx=x, ry=y+(getIconHeight()-h)/2;
            g2.fillRoundRect(rx, ry, w, h, 3, 3);
            g2.setColor(Color.WHITE);
            int m=3; int lw=w-2*m;
            g2.fillRect(rx+m, ry+m, lw, 2);
            g2.fillRect(rx+m, ry+m+5, lw, 2);
            g2.fillRect(rx+m, ry+m+10, lw, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return s; }
        @Override public int getIconHeight(){ return (int)(s*1.2); }
    }

    // ====== Botón con esquinas redondeadas ======
    private static class RoundedButton extends JButton {
        private final Color color;
        private final Color colorHover;
        private final Color colorDisabled;
        private static final int RADIUS = 14;
        private RoundedButton(String txt, Color c, Color h, Color d){
            super(txt);
            this.color = c; this.colorHover = h; this.colorDisabled = d;
            setForeground(Color.WHITE);
            setFont(F_BODY.deriveFont(Font.BOLD));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(new EmptyBorder(10,18,10,18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        public static RoundedButton primary(String txt){
            return new RoundedButton(txt, C_ROJO_1, C_ROJO_2, new Color(170,170,170));
        }
        public static RoundedButton neutral(String txt){
            return new RoundedButton(txt, new Color(140,140,140), new Color(120,120,120), new Color(170,170,170));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = isEnabled() ? (getModel().isRollover()? colorHover : color) : colorDisabled;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
            g2.setColor(new Color(0,0,0,30));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, RADIUS, RADIUS);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ========================= Capa Modal (GlassPane) =========================
    private class ModalLayer extends JComponent {
        private JPanel wrapper;
        private Runnable afterClose = () -> {};

        ModalLayer() {
            setLayout(new GridBagLayout());
            setOpaque(false);
            registerKeyboardAction(e -> cerrar(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (wrapper == null) { cerrar(); return; }
                    Point p = SwingUtilities.convertPoint(ModalLayer.this, e.getPoint(), wrapper);
                    if (p.x < 0 || p.y < 0 || p.x > wrapper.getWidth() || p.y > wrapper.getHeight()) cerrar();
                }
            });
        }

        void showModal(JComponent content, Runnable afterClose, Dimension preferredSize){
            // Forzar popups heavyweight para que aparezcan sobre el glass pane
            javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
            javax.swing.ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
            UIManager.put("ComboBox.isPopupLightWeight", Boolean.FALSE);

            this.afterClose = (afterClose!=null)? afterClose : () -> {};
            removeAll();
            setVisible(true);

            wrapper = new JPanel(new BorderLayout()){
                @Override protected void paintComponent(Graphics g){
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(C_SOMBRA);
                    for(int i=0;i<10;i++){
                        g2.drawRoundRect(i,i,getWidth()-1-i*2,getHeight()-1-i*2,16,16);
                    }
                    g2.dispose();
                }
            };
            wrapper.setOpaque(false);

            JButton close = new JButton("✕");
            close.setFocusPainted(false);
            close.setBorder(javax.swing.BorderFactory.createEmptyBorder(6,10,6,10));
            close.setContentAreaFilled(false);
            close.setFont(F_H3);
            close.addActionListener(e -> cerrar());

            JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            top.setOpaque(false);
            top.add(close);

            JPanel box = new JPanel(new BorderLayout());
            box.setOpaque(false);
            box.add(top, BorderLayout.NORTH);
            box.add(content, BorderLayout.CENTER);

            if (preferredSize != null) content.setPreferredSize(preferredSize);

            wrapper.add(box, BorderLayout.CENTER);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx=0; gbc.gridy=0; gbc.weightx=1; gbc.weighty=1;
            add(wrapper, gbc);

            attachCancelButtons(content);
            revalidate(); repaint();
            content.requestFocusInWindow();
        }

        private void attachCancelButtons(Component root){
            Deque<Component> stack = new ArrayDeque<>();
            stack.push(root);
            while(!stack.isEmpty()){
                Component c = stack.pop();
                if (c instanceof AbstractButton ab) {
                    Object role = ab.getClientProperty("role");
                    if ("cancel".equals(role)) ab.addActionListener(e -> cerrar());
                }
                if (c instanceof Container cont) {
                    for (Component ch : cont.getComponents()) stack.push(ch);
                }
            }
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0,0,0,140));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose();
        }

        void cerrar(){
            javax.swing.ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
            UIManager.put("ComboBox.isPopupLightWeight", Boolean.TRUE);
            // restaurar comportamiento por defecto
            javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(true);

            setVisible(false);
            removeAll();
            revalidate();
            repaint();
            afterClose.run();
        }
    }

    // ====== DatePickerField: calendario ======
    private static class DatePickerField extends JPanel {
        private final JTextField tf = new JTextField();
        private final JButton btn = new JButton("📅");
        private final SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");

        DatePickerField(){
            super(new BorderLayout(4,0));
            setOpaque(false);

            tf.putClientProperty("JTextField.placeholderText", "MM/DD/YYYY");
            tf.setFont(F_BODY);
            tf.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    new javax.swing.border.LineBorder(C_BORDE_SUAVE,1,true),
                    new EmptyBorder(8,10,8,10)));

            btn.setBorder(new EmptyBorder(6,8,6,8));
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            add(tf, BorderLayout.CENTER);
            add(btn, BorderLayout.EAST);

            btn.addActionListener(e -> showPopup());
        }

        private void showPopup(){
            JPopupMenu pm = new JPopupMenu();
            pm.setBorder(new javax.swing.border.LineBorder(C_BORDE_SUAVE,1,true));

            SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH);
            JSpinner spinner = new JSpinner(model);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "MM/dd/yyyy");
            spinner.setEditor(editor);

            JButton btnHoy = new JButton("Hoy");
            btnHoy.addActionListener(e -> spinner.setValue(new Date()));

            JButton btnOk = new JButton("Aceptar");
            btnOk.addActionListener(e -> {
                Date d = (Date) spinner.getValue();
                tf.setText(fmt.format(d));
                pm.setVisible(false);
            });

            JPanel content = new JPanel(new BorderLayout(8,8));
            content.setBorder(new EmptyBorder(8,8,8,8));
            content.add(spinner, BorderLayout.CENTER);

            JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            south.add(btnHoy);
            south.add(btnOk);
            content.add(south, BorderLayout.SOUTH);

            pm.add(content);
            pm.show(this, getWidth()-220, getHeight());
        }

        String getDateString(){ return tf.getText(); }
        void clear(){ tf.setText(""); }
        @Override public void setEnabled(boolean enabled) {
            super.setEnabled(enabled); tf.setEnabled(enabled); btn.setEnabled(enabled);
        }
    }




    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents2() {

        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 679, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>                        


    // Variables declaration - do not modify                     
    private javax.swing.JPanel jPanel1;
    // End of variables declaration                   

 
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
   // ================= MAIN para probar la vista =================
    // ---- main de prueba local (puedes quitarlo si ya lo lanzas desde Login) ----
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DocenteView().setVisible(true));
    }
}
