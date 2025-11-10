package main;

import main.view.MainView;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainView frame = new MainView();
            frame.setVisible(true);
        });
    }
}
