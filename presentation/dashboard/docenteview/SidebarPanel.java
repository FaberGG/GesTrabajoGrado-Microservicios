package co.unicauca.gestiontrabajogrado.presentation.dashboard.docenteview;

import co.unicauca.gestiontrabajogrado.presentation.common.BaseSidebarPanel;

import javax.swing.*;

/**
 * Panel lateral específico para el rol de Docente
 */
class SidebarPanel extends BaseSidebarPanel {

    SidebarPanel(JFrame parentFrame) {
        super(parentFrame);
    }

    @Override
    protected String getRoleHeaderText() {
        return "Docente";
    }

    @Override
    protected String[] getSubmenuItems() {
        return new String[]{
                "Nueva Propuesta",
                "Mis Propuestas",
        };
    }

    @Override
    protected void createRoleSpecificComponents() {
        // Aquí se pueden agregar componentes específicos del docente si es necesario
        // Por ahora no hay componentes adicionales
    }

    @Override
    protected void setupSubmenuToggle() {
        // La lógica de toggle está en la clase base, aquí se puede personalizar si es necesario
        // Por ahora usamos el comportamiento por defecto
    }

    @Override
    protected void handleSubmenuAction(String actionText) {
        // Manejar acciones específicas del docente
        switch (actionText) {
            case "Nueva Propuesta":
                handleNewProposal();
                break;
            case "Mis Propuestas":
                handleMyProposals();
                break;
            default:
                JOptionPane.showMessageDialog(this, "Acción: " + actionText, "Info",
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }
    private void handleNewProposal() {
        // Lógica para manejar la creación de una nueva propuesta
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de nueva propuesta en desarrollo.",
                "Nueva Propuesta",
                JOptionPane.INFORMATION_MESSAGE);
    }
    private void handleMyProposals() {
        // Lógica para manejar la visualización de las propuestas del docente
        JOptionPane.showMessageDialog(this,
                "Funcionalidad de mis propuestas en desarrollo.",
                "Mis Propuestas",
                JOptionPane.INFORMATION_MESSAGE);
    }
}