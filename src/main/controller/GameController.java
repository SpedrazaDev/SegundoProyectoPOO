package main.controller;

import gamesplugin.*;
import main.model.GameRegistry;
import main.model.StatsManager;
import main.view.MainView;
import main.view.StatsView;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.util.List;

public class GameController {
    private final MainView mainView;
    private final GameRegistry gameRegistry;
    private final StatsManager statsManager;

    public GameController(MainView mainView) {
        this.mainView = mainView;
        this.gameRegistry = GameRegistry.getInstance();
        this.statsManager = StatsManager.getInstance();
    }

    public void loadGame(String gameName) {
        try {
            GameFunction game = gameRegistry.getGame(gameName);
            game.setGameListener(this);

            // Crear internal frame para el juego
            JInternalFrame gameFrame = (JInternalFrame) game;
            mainView.addInternalFrame(gameFrame);

            game.iniciar();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainView,
                    "Error al cargar el juego: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public void listarJuegosCargados() {
        GameRegistry registry = GameRegistry.getInstance();
        for (GameFunction game : registry) {
            System.out.println(game.getClass().getSimpleName());
        }
    }


    @Override
    public void onGameFinished(Stat stats) {
        // Observer pattern: el juego notifica cuando termina
        statsManager.addStat(stats);
        statsManager.saveStats();

        JOptionPane.showMessageDialog(mainView,
                "Juego terminado!\n" +
                        stats.getClave() + ": " + stats.getValor(),
                "Resultado",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showStats() {
        StatsView statsView = new StatsView(statsManager);
        mainView.addInternalFrame(statsView);
    }

    public void listarJuegosCargados() {
        for (GameFunction game : gameRegistry) {
            System.out.println(game.getClass().getSimpleName());
        }
    }

    private void handleGameFinished(String gameId, Stat stats) {
        statsManager.addStat(gameId, stats);
        statsManager.saveStats();
        JOptionPane.showMessageDialog(
                mainView,
                "Juego terminado!\n" + stats.getClave() + ": " + stats.getValor(),
                "Resultado",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private class ControllerListener implements GameListener {
        private final String gameId;

        private ControllerListener(String gameId) {
            this.gameId = gameId;
        }

        @Override
        public void onGameFinished(Stat stats) {
            handleGameFinished(gameId, stats);
        }
    }
}
