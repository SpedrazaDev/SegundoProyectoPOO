package games.simon;

import gamesplugin.GameFunction;
import gamesplugin.GameListener;
import gamesplugin.Stat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimonGame extends JInternalFrame implements GameFunction {
    private static final int BUTTON_COUNT = 4;
    private static final Color[] BASE_COLORS = {
            new Color(76, 175, 80), // bright green
            new Color(244, 67, 54), // bright red
            new Color(33, 150, 243), // bright blue
            new Color(255, 193, 7)   // bright yellow
    };
    private static final Color[] ACTIVE_COLORS = new Color[BUTTON_COUNT];
    static {
        for (int i = 0; i < BUTTON_COUNT; i++) {
            ACTIVE_COLORS[i] = BASE_COLORS[i].darker().darker();
        }
    }

    private final JButton[] colorButtons;
    private final JLabel statusLabel;
    private final JButton startButton;

    private final List<Integer> sequence;
    private final Random random;

    private GameListener listener;
    private String playerName = "Jugador";
    private int userIndex;
    private boolean acceptingInput;
    private javax.swing.Timer playbackTimer;

    public SimonGame() {
        super("Simon Dice", true, true, true, true);
        this.colorButtons = new JButton[BUTTON_COUNT];
        this.sequence = new ArrayList<>();
        this.random = new Random();
        setSize(420, 420);
        setLocation(120, 100);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        for (int i = 0; i < BUTTON_COUNT; i++) {
            final int index = i;
            JButton btn = new JButton();
            btn.setBackground(BASE_COLORS[i]);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Arial", Font.BOLD, 28));
            btn.setForeground(Color.DARK_GRAY);
            btn.setText(Integer.toString(i + 1));
            btn.addActionListener(e -> handleUserInput(index));
            colorButtons[i] = btn;
            grid.add(btn);
        }
        content.add(grid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Presiona iniciar para comenzar.", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);

        startButton = new JButton("Iniciar");
        startButton.addActionListener(e -> reiniciarJuego());
        bottomPanel.add(startButton, BorderLayout.SOUTH);

        content.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(content);
        enableButtons(false);
    }

    private void reiniciarJuego() {
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        sequence.clear();
        userIndex = 0;
        acceptingInput = false;
        enableButtons(false);
        statusLabel.setText("Observa la secuencia...");
        addRandomColor();
        playSequence();
    }

    private void addRandomColor() {
        sequence.add(random.nextInt(BUTTON_COUNT));
    }

    private void playSequence() {
        enableButtons(false);
        acceptingInput = false;
        startButton.setEnabled(false);

        playbackTimer = new javax.swing.Timer(350, null);
        final int[] playbackIndex = {0};
        final boolean[] showing = {false};
        final int[] activeColor = {-1};

        playbackTimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showing[0]) {
                    resetButton(activeColor[0]);
                    showing[0] = false;
                    activeColor[0] = -1;

                    playbackIndex[0]++;
                    if (playbackIndex[0] >= sequence.size()) {
                        playbackTimer.stop();
                        statusLabel.setText("Repite la secuencia.");
                        acceptingInput = true;
                        startButton.setEnabled(true);
                        enableButtons(true);
                        userIndex = 0;
                    }
                    return;
                }

                if (playbackIndex[0] >= sequence.size()) {
                    return;
                }
                int colorIndex = sequence.get(playbackIndex[0]);
                highlightButton(colorIndex);
                activeColor[0] = colorIndex;
                showing[0] = true;
            }
        });
        playbackTimer.setInitialDelay(600);
        playbackTimer.start();
    }

    private void handleUserInput(int colorIndex) {
        if (!acceptingInput) return;
        highlightButton(colorIndex);
        Timer resetTimer = new Timer(200, e -> resetButton(colorIndex));
        resetTimer.setRepeats(false);
        resetTimer.start();

        if (sequence.get(userIndex) == colorIndex) {
            userIndex++;
            if (userIndex == sequence.size()) {
                statusLabel.setText("Bien hecho! Preparando siguiente ronda.");
                acceptingInput = false;
                enableButtons(false);
                SwingUtilities.invokeLater(() -> {
                    addRandomColor();
                    playSequence();
                });
            }
        } else {
            endGame();
        }
    }

    private void endGame() {
        acceptingInput = false;
        enableButtons(false);
        if (playbackTimer != null) {
            playbackTimer.stop();
        }
        int score = Math.max(sequence.size() - 1, 0);
        statusLabel.setText("Perdiste! Puntaje: " + score);
        if (listener != null) {
            listener.onGameFinished(new Stat("Rondas", playerName, score));
        }
    }

    private void highlightButton(int index) {
        colorButtons[index].setBackground(ACTIVE_COLORS[index]);
    }

    private void resetButton(int index) {
        if (index >= 0 && index < BUTTON_COUNT) {
            colorButtons[index].setBackground(BASE_COLORS[index]);
        }
    }

    private void enableButtons(boolean enable) {
        for (JButton button : colorButtons) {
            button.setEnabled(enable);
        }
    }

    @Override
    public void iniciar() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del jugador:", playerName);
        if (nombre != null && !nombre.trim().isEmpty()) {
            playerName = nombre.trim();
        }
        JOptionPane.showMessageDialog(
                this,
                "Observa cuidadosamente el orden de iluminacion y repetido con los botones.\n"
                        + "Cada ronda agrega un color a la secuencia. Pierdes al equivocarte.",
                "Instrucciones",
                JOptionPane.INFORMATION_MESSAGE
        );
        setVisible(true);
        reiniciarJuego();
    }

    @Override
    public Stat getStats() {
        int score = Math.max(sequence.size() - 1, 0);
        return new Stat("Rondas", playerName, score);
    }

    @Override
    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }
}
