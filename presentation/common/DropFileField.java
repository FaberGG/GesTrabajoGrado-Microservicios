package co.unicauca.gestiontrabajogrado.presentation.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DropFileField extends JPanel {

    private static final Color BORDE       = new Color(90, 90, 90);
    private static final Color BORDE_DIS   = new Color(180,180,180);
    private static final Color TEXTO       = new Color(90, 90, 90);
    private static final Color TEXTO_DIS   = new Color(160,160,160);
    private static final Font  F1          = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font  F2          = new Font("SansSerif", Font.PLAIN, 12);

    private String linea1 = "✎  Arrastre el archivo aquí o haga clic para seleccionar";
    private String linea2 = "Solo un archivo PDF";
    private File file;

    public DropFileField() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 72));
        setBorder(new EmptyBorder(10,12,10,12));

        // Abrir chooser al hacer click
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isEnabled()) return;
                seleccionarArchivo();
            }
        });

        // Drag & drop de archivos
        setDropTarget(new DropTarget() {
            @SuppressWarnings("unchecked")
            @Override public synchronized void drop(DropTargetDropEvent dtde) {
                if (!isEnabled()) { dtde.rejectDrop(); return; }
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> dropped = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (!dropped.isEmpty()) {
                        File f = dropped.get(0);
                        if (esPdf(f)) {
                            file = f;
                            repaint();
                        } else {
                            JOptionPane.showMessageDialog(DropFileField.this,
                                    "Selecciona un archivo PDF.", "Archivo inválido", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void seleccionarArchivo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter("Archivos PDF (*.pdf)", "pdf"));
        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (esPdf(f)) {
                file = f;
                repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un archivo PDF.",
                        "Archivo inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private boolean esPdf(File f) {
        if (f == null || !f.isFile()) return false;

        // Verificar por extensión (más flexible)
        String n = f.getName().toLowerCase(Locale.ROOT);
        boolean extOk = n.endsWith(".pdf");

        // Verificar por cabecera mágica %PDF (más robusto)
        boolean headerOk = false;
        try (FileInputStream fis = new FileInputStream(f)) {
            byte[] head = new byte[5];
            int read = fis.read(head);
            if (read >= 4) {
                String sig = new String(head, 0, 4, "US-ASCII");
                headerOk = "%PDF".equals(sig);
            }
        } catch (IOException ignored) { }

        // Aceptar si tiene extensión correcta O cabecera correcta
        return extOk || headerOk;
    }

    public boolean hasFile() { return file != null; }
    public File getFile()    { return file; }
    public File getSelectedFile() { return file; }
    public void clear()      { file = null; repaint(); }
    public void clearFile()  { file = null; repaint(); }

    public void setLine1(String s){ this.linea1 = s; repaint(); }
    public void setLine2(String s){ this.linea2 = s; repaint(); }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int x = 6, y = 6, w = getWidth() - 12, h = getHeight() - 12;

        // Fondo
        g2.setColor(new Color(255,255,255,200));
        g2.fillRect(x, y, w, h);

        // Borde punteado
        float[] dash = {6f, 6f};
        g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g2.setColor(isEnabled() ? BORDE : BORDE_DIS);
        g2.drawRect(x, y, w, h);

        // Texto
        g2.setFont(F1);
        g2.setColor(isEnabled() ? TEXTO : TEXTO_DIS);
        String l1 = hasFile() ? ("✓ " + file.getName()) : linea1;
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(l1, x + 12, y + h/2 - 4);

        g2.setFont(F2);
        String l2 = hasFile() ? "Archivo seleccionado (PDF)" : linea2;
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(l2, x + 12, y + h/2 + fm2.getAscent() + 2);

        g2.dispose();
    }
}
