package main.view;

import main.controller.GameController;
import javax.swing.*;
import java.awt.*;

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

        JButton btnGame1 = new JButton("Juego de Memoria");
        JButton btnGame2 = new JButton("Tres en Línea");
        JButton btnGame3 = new JButton("Snake");
        JButton btnStats = new JButton("Ver Estadísticas");

        // Listeners
        btnGame1.addActionListener(e -> controller.loadGame("memorygame"));
        btnGame2.addActionListener(e -> controller.loadGame("tictactoe"));
        btnGame3.addActionListener(e -> controller.loadGame("snake"));
        btnStats.addActionListener(e -> controller.showStats());

        centerPanel.add(btnGame1);
        centerPanel.add(btnGame2);
        centerPanel.add(btnGame3);
        centerPanel.add(btnStats);

        add(titleLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
    }
}
