package main.view;

import main.controller.GameController;
import main.model.GameRegistry;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuView extends JInternalFrame {
    private final GameController controller;
    private JPanel gamesPanel;

    public MenuView(MainView mainView) {
        super("Menú Principal", true, false, true, true);
        this.controller = new GameController(mainView);

        setSize(400, 300);
        setLocation(50, 50);
        initComponents();
    }

    private void initComponents() {
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
        JButton btnStats = new JButton("Ver Estadísticas");
        buttonPanel.add(btnStats);
        add(buttonPanel, BorderLayout.SOUTH);

        btnStats.addActionListener(e -> controller.showStats());

        refreshGames();
    }

    private void refreshGames() {
        gamesPanel.removeAll();
        List<GameRegistry.GameInfo> games = controller.getAvailableGames();
        if (games.isEmpty()) {
            JLabel noGames = new JLabel("No hay juegos registrados.", SwingConstants.CENTER);
            noGames.setForeground(Color.GRAY);
            noGames.setAlignmentX(Component.CENTER_ALIGNMENT);
            gamesPanel.add(noGames);
        } else {
            for (GameRegistry.GameInfo info : games) {
                JButton button = new JButton(info.displayName());
                button.setAlignmentX(Component.CENTER_ALIGNMENT);
                button.addActionListener(e -> controller.loadGame(info.id()));
                button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                gamesPanel.add(button);
                gamesPanel.add(Box.createVerticalStrut(8));
            }
        }
        gamesPanel.revalidate();
        gamesPanel.repaint();
    }
}
