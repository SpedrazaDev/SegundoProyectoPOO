package main.view;

import main.controller.GameController;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuView extends JInternalFrame {
    private MainView mainView;
    private GameController controller;

    public MenuView(MainView mainView) {
        super("Menú Principal", true, false, true, true);
        this.mainView = mainView;
        this.controller = new GameController(mainView);

        setSize(400, 300);
        setLocation(50, 50);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Plataforma de Juegos", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        gamesPanel = new JPanel();
        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        gamesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(gamesPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnStats = new JButton("Ver Estadisticas");
        buttonPanel.add(btnStats);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        btnGame1.addActionListener(e -> controller.loadGame("memorygame"));
        btnGame2.addActionListener(e -> controller.loadGame("tictactoe"));
        btnGame3.addActionListener(e -> controller.loadGame("snake"));
        btnStats.addActionListener(e -> controller.showStats());

        refreshGames();
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
        return info.getDisplayName();
    }
}
