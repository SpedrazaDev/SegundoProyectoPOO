package main.view;

import javax.swing.*;
import java.awt.*;

public class MainView extends JFrame {
    private JDesktopPane desktopPane;
    private MenuView menuView;

    public MainView() {
        setTitle("Plataforma de Juegos");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Desktop pane para internal frames
        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(45, 52, 54));
        add(desktopPane);

        // Mostrar menú principal
        showMenu();
    }

    private void showMenu() {
        menuView = new MenuView(this);
        desktopPane.add(menuView);
        menuView.setVisible(true);
        try {
            menuView.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
    }

    public JDesktopPane getDesktopPane() {
        return desktopPane;
    }

    public void addInternalFrame(JInternalFrame frame) {
        if (frame.getParent() == null) {
            desktopPane.add(frame);
        }
        frame.setVisible(true);
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {
            e.printStackTrace();
        }
        frame.toFront();
    }
}
