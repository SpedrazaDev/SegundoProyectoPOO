package main.view;

import main.controller.GameController;
import main.model.GameRegistry.GameInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MenuView extends JInternalFrame {
    private final GameController controller;
    private final JPanel gamesPanel;

    public MenuView(MainView mainView) {
        super("Menu Principal", true, false, true, true);
        this.controller = new GameController(mainView);

        setSize(420, 360);
        setLocation(50, 50);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Plataforma de Juegos", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnLoadPlugin = new JButton("Cargar juego (.jar)");
        JButton btnStats = new JButton("Ver Estadisticas");
        buttonPanel.add(btnLoadPlugin);
        buttonPanel.add(btnStats);
        add(buttonPanel, BorderLayout.SOUTH);

        btnStats.addActionListener(e -> controller.showStats());
        btnLoadPlugin.addActionListener(e -> openPluginDialog());

        refreshGames();
    }

    private void openPluginDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar juego externo");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivo JAR", "jar"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File jar = chooser.getSelectedFile();
            try {
                List<String> loaded = controller.loadPlugin(jar);
                JOptionPane.showMessageDialog(
                        this,
                        "Se cargaron: " + String.join(", ", loaded),
                        "Plugins agregados",
                        JOptionPane.INFORMATION_MESSAGE
                );
                refreshGames();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        this,
                        "No se pudo cargar el plugin: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    public void refreshGames() {
        gamesPanel.removeAll();
        List<GameInfo> games = controller.getAvailableGames();
        if (games.isEmpty()) {
            JLabel noGames = new JLabel("No hay juegos registrados.", SwingConstants.CENTER);
            noGames.setForeground(Color.GRAY);
            noGames.setAlignmentX(Component.CENTER_ALIGNMENT);
            gamesPanel.add(noGames);
        } else {
            for (GameInfo info : games) {
                JButton button = new JButton(buildButtonLabel(info));
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.addActionListener(e -> controller.loadGame(info.getId()));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                gamesPanel.add(button);
                gamesPanel.add(Box.createVerticalStrut(8));
            }
        }
        gamesPanel.revalidate();
        gamesPanel.repaint();
    }

    private String buildButtonLabel(GameInfo info) {
        return info.isExternal()
                ? info.getDisplayName() + " (externo)"
                : info.getDisplayName();
    }
}
