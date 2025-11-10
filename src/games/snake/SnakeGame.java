package games.snake;

import gamesplugin.GameFunction;
import gamesplugin.GameListener;
import gamesplugin.Stat;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SnakeGame extends JInternalFrame implements GameFunction {
    private GameListener listener;
    private int score;
    private String playerName;
    private GamePanel panel;

    public SnakeGame() {
        super("Snake", true, true, true, true);
        setSize(400, 400);
        setLocation(80, 80);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    @Override
    public void iniciar() {
        playerName = JOptionPane.showInputDialog(this, "Nombre jugador: ");
        if (playerName == null || playerName.trim().isEmpty()) playerName = "Jugador";
        score = 0;
        panel = new GamePanel();
        setContentPane(panel);
        setVisible(true);
        panel.requestFocusInWindow();
    }

    @Override
    public void setGameListener(GameListener listener) {
        this.listener = listener;
    }

    @Override
    public Stat getStats() {
        return new Stat("Snake", playerName, score);
    }

    private class GamePanel extends JPanel implements ActionListener, KeyListener {
        private final int UNIT_SIZE = 20;
        private final int GAME_UNITS = (400 / UNIT_SIZE) * (400 / UNIT_SIZE);
        private final int DELAY = 120;
        private final int SCREEN_WIDTH = 400;
        private final int SCREEN_HEIGHT = 400;

        private final int[] x = new int[GAME_UNITS];
        private final int[] y = new int[GAME_UNITS];
        private int bodyParts = 6;
        private int applesEaten;
        private int appleX;
        private int appleY;
        private char direction = 'R';
        private boolean running = false;
        private boolean paused = false;
        private javax.swing.Timer timer;
        private JButton btnReiniciar;

        public GamePanel() {
            setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
            setLayout(null); // posición absoluta para el botón
            setBackground(Color.black);
            setFocusable(true);
            addKeyListener(this);
            mostrarControles();
            startGame();

            btnReiniciar = new JButton("Reiniciar");
            btnReiniciar.setBounds((SCREEN_WIDTH-120)/2, SCREEN_HEIGHT/2+40, 120, 30);
            btnReiniciar.setVisible(false);
            btnReiniciar.setFocusable(false);
            btnReiniciar.addActionListener(e -> {
                reiniciarJuego();
                btnReiniciar.setVisible(false);
                requestFocusInWindow();
            });
            add(btnReiniciar);
        }

        private void mostrarControles() {
            JOptionPane.showMessageDialog(this,
                    "CONTROL SNAKE:\n- Flechas: mover\n- Barra espaciadora: pausar/continuar\n- Aparecen al azar manzanas (rojas).\n¡No choques contigo o los bordes!",
                    "Controles", JOptionPane.INFORMATION_MESSAGE);
        }

        public void startGame() {
            newApple();
            running = true;
            paused = false;
            timer = new javax.swing.Timer(DELAY, this);
            timer.start();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(0, 0, SCREEN_WIDTH-1, SCREEN_HEIGHT-1); // Borde visible
            draw(g);
        }

        public void draw(Graphics g) {
            if (running) {
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);
                for (int i = 0; i < bodyParts; i++) {
                    if (i == 0) {
                        g.setColor(Color.green);
                        g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                    } else {
                        g.setColor(new Color(45, 180, 0));
                        g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                    }
                }
                // Puntaje centrado arriba
                String puntajeTxt = "Puntaje: " + applesEaten;
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                int xTxt = (SCREEN_WIDTH - fm.stringWidth(puntajeTxt)) / 2;
                g.drawString(puntajeTxt, xTxt, 18);

                if(paused) {
                    g.setFont(new Font("Arial", Font.BOLD, 32));
                    String pauseMsg = "PAUSA";
                    int px = (SCREEN_WIDTH - fm.stringWidth(pauseMsg)) / 2;
                    g.setColor(Color.yellow);
                    g.drawString(pauseMsg, px, SCREEN_HEIGHT/2);
                }
            } else {
                gameOver(g);
            }
        }

        public void newApple() {
            java.util.List<Point> libres = new java.util.ArrayList<>();
            for (int i = 0; i < SCREEN_WIDTH / UNIT_SIZE; i++) {
                for (int j = 0; j < SCREEN_HEIGHT / UNIT_SIZE; j++) {
                    boolean ocupado = false;
                    int px = i * UNIT_SIZE;
                    int py = j * UNIT_SIZE;
                    for (int k = 0; k < bodyParts; k++) {
                        if (x[k] == px && y[k] == py) {
                            ocupado = true;
                            break;
                        }
                    }
                    if (!ocupado) {
                        libres.add(new Point(px, py));
                    }
                }
            }
            if (!libres.isEmpty()) {
                Point p = libres.get((int) (Math.random() * libres.size()));
                appleX = p.x;
                appleY = p.y;
            }
        }


        public void move() {
            for (int i = bodyParts; i > 0; i--) {
                x[i] = x[i - 1];
                y[i] = y[i - 1];
            }
            switch (direction) {
                case 'U': y[0] -= UNIT_SIZE; break;
                case 'D': y[0] += UNIT_SIZE; break;
                case 'L': x[0] -= UNIT_SIZE; break;
                case 'R': x[0] += UNIT_SIZE; break;
            }
        }

        public void checkApple() {
            if ((x[0] == appleX) && (y[0] == appleY)) {
                bodyParts++;
                applesEaten++;
                newApple();
            }
        }

        public void checkCollisions() {
            for (int i = bodyParts; i > 0; i--) {
                if ((x[0] == x[i]) && (y[0] == y[i])) {
                    running = false;
                }
            }
            if (x[0] < 0 || x[0] > SCREEN_WIDTH - UNIT_SIZE || y[0] < 0 || y[0] > SCREEN_HEIGHT - UNIT_SIZE) {
                running = false;
            }

            if (!running) {
                timer.stop();
                score = applesEaten;
                if (listener != null) {
                    listener.onGameFinished(new Stat("Snake", playerName, score));
                }
                JOptionPane.showMessageDialog(this, "Fin del juego! Puntaje: " + applesEaten);
                btnReiniciar.setVisible(true);
            }
        }

        private void reiniciarJuego() {
            bodyParts = 6;
            applesEaten = 0;
            direction = 'R';
            running = true;
            paused = false;
            for (int i = 0; i < x.length; i++) x[i] = 0;
            for (int i = 0; i < y.length; i++) y[i] = 0;
            newApple();
            timer.restart();
            repaint();
        }

        public void gameOver(Graphics g) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics fm = g.getFontMetrics();
            String msj = "Game Over";
            g.drawString(msj, (SCREEN_WIDTH - fm.stringWidth(msj)) / 2, SCREEN_HEIGHT / 2);
        }

        // ----- ActionListener -----
        @Override
        public void actionPerformed(ActionEvent e) {
            if (running && !paused) {
                move();
                checkApple();
                checkCollisions();
            }
            repaint();
        }

        // ----- KeyListener -----
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                paused = !paused;
                repaint();
            }
            if (!paused) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: if (direction != 'R') direction = 'L'; break;
                    case KeyEvent.VK_RIGHT: if (direction != 'L') direction = 'R'; break;
                    case KeyEvent.VK_UP: if (direction != 'D') direction = 'U'; break;
                    case KeyEvent.VK_DOWN: if (direction != 'U') direction = 'D'; break;
                }
            }
        }
        public void keyReleased(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
    }
}
