package games.memory;

import gamesplugin.GameFunction;
import gamesplugin.GameListener;
import gamesplugin.GameStats;
import gamesplugin.Stat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGame extends JInternalFrame implements GameFunction {
    private static final int GRID_ROWS = 4;
    private static final int GRID_COLS = 4;
    private static final Font CARD_FONT = new Font("Arial", Font.BOLD, 26);

    private GameListener listener;
    private String playerName = "Jugador";
    private int attempts;
    private MemoryPanel memoryPanel;

    public MemoryGame() {
        super("Juego de Memoria", true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(500, 580);
        setLocation(160, 120);
    }

    @Override
    public void iniciar() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del jugador:", playerName);
        if (nombre != null && !nombre.trim().isEmpty()) {
            playerName = nombre.trim();
        }
        attempts = 0;
        memoryPanel = new MemoryPanel();
        setContentPane(memoryPanel);
        pack();
        setVisible(true);
        memoryPanel.startRound();
    }

    @Override
    public Stat getStats() {
        return new Stat("Intentos", playerName, attempts);
    }

    @Override
    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    private class MemoryPanel extends JPanel implements ActionListener {
        private final JButton[] cards;
        private List<String> symbols;
        private final JLabel infoLabel;
        private final JButton startButton;
        private JButton firstCard;
        private JButton secondCard;
        private boolean processing;
        private int matches;
        private boolean gameRunning;

        MemoryPanel() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel grid = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, 10, 10));
            cards = new JButton[GRID_ROWS * GRID_COLS];
            symbols = buildSymbols();

            for (int i = 0; i < cards.length; i++) {
                JButton button = new JButton("?");
                button.setFont(CARD_FONT);
                button.setFocusPainted(false);
                button.setBackground(new Color(33, 150, 243));
                button.setForeground(Color.WHITE);
                button.putClientProperty("symbol", symbols.get(i));
                button.addActionListener(this);
                button.setEnabled(false);
                cards[i] = button;
                grid.add(button);
            }

            infoLabel = new JLabel("Encuentra las parejas. Intentos: 0");
            infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

            add(infoLabel, BorderLayout.NORTH);
            add(grid, BorderLayout.CENTER);

            startButton = new JButton("Iniciar");
            startButton.addActionListener(e -> startRound());
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.add(startButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private List<String> buildSymbols() {
            String[] base = {"A", "B", "C", "D", "E", "F", "G", "H"};
            List<String> list = new ArrayList<>();
            for (String s : base) {
                list.add(s);
                list.add(s);
            }
            Collections.shuffle(list);
            return list;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (processing || !gameRunning) return;
            JButton clicked = (JButton) e.getSource();
            if (!clicked.isEnabled() || clicked.getText().equals("OK")) return;

            reveal(clicked);

            if (firstCard == null) {
                firstCard = clicked;
                return;
            }
            if (clicked == firstCard) return;

            secondCard = clicked;
            processing = true;
            attempts++;
            updateInfoLabel();

            if (firstCard.getClientProperty("symbol").equals(secondCard.getClientProperty("symbol"))) {
                markMatched(firstCard);
                markMatched(secondCard);
                matches += 2;
                resetSelection();
                processing = false;
                if (matches == cards.length) {
                    finishGame();
                }
            } else {
                Timer timer = new Timer(700, evt -> {
                    hideCard(firstCard);
                    hideCard(secondCard);
                    resetSelection();
                    processing = false;
                });
                timer.setRepeats(false);
                timer.start();
            }
        }

        private void reveal(JButton button) {
            String symbol = (String) button.getClientProperty("symbol");
            button.setText(symbol);
            button.setBackground(new Color(13, 71, 161));
        }

        private void hideCard(JButton button) {
            button.setText("?");
            button.setBackground(new Color(33, 150, 243));
        }

        private void markMatched(JButton button) {
            button.setText("OK");
            button.setBackground(new Color(76, 175, 80));
            button.setEnabled(false);
        }

        private void resetSelection() {
            firstCard = null;
            secondCard = null;
        }

        private void startRound() {
            attempts = 0;
            matches = 0;
            processing = false;
            gameRunning = true;
            startButton.setEnabled(false);
            resetSelection();
            symbols = buildSymbols();
            for (int i = 0; i < cards.length; i++) {
                cards[i].putClientProperty("symbol", symbols.get(i));
                hideCard(cards[i]);
                cards[i].setEnabled(true);
            }
            updateInfoLabel();
        }

        private void finishGame() {
            if (listener != null) {
                listener.onGameFinished(new GameStats("Intentos", playerName, attempts));
            }
            gameRunning = false;
            startButton.setEnabled(true);
        }

        private void updateInfoLabel() {
            infoLabel.setText("Encuentra las parejas. Intentos: " + attempts);
        }
    }
}
