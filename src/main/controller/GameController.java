package main.controller;

import gamesplugin.GameFunction;
import gamesplugin.GameListener;
import gamesplugin.Stat;
import main.model.GameRegistry;
import main.model.GameRegistry.GameInfo;
import main.model.StatsManager;
import main.plugin.PluginLoader;
import main.view.MainView;
import main.view.StatsView;

import javax.swing.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameController {
    private final MainView mainView;
    private final GameRegistry gameRegistry;
    private final StatsManager statsManager;
    private final PluginLoader pluginLoader;

    public GameController(MainView mainView) {
        this.mainView = mainView;
        this.gameRegistry = GameRegistry.getInstance();
        this.statsManager = StatsManager.getInstance();
        this.pluginLoader = new PluginLoader(gameRegistry);
    }

    public List<GameInfo> getAvailableGames() {
        return gameRegistry.getRegisteredGames();
    }

    public void loadGame(String gameId) {
        try {
            GameFunction game = gameRegistry.getGame(gameId);
            game.setGameListener(new ControllerListener(gameId));
            if (!(game instanceof JInternalFrame)) {
                throw new IllegalStateException("El juego no esta basado en un JInternalFrame.");
            }

            JInternalFrame gameFrame = (JInternalFrame) game;
            attachFrame(gameFrame);
            game.iniciar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    mainView,
                    "Error al cargar el juego: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void attachFrame(JInternalFrame frame) {
        if (frame.getDesktopPane() == null) {
            mainView.addInternalFrame(frame);
        } else {
            try {
                frame.setIcon(false);
                frame.setSelected(true);
                frame.toFront();
            } catch (PropertyVetoException ignored) {
            }
        }
    }

    public void showStats() {
        StatsView statsView = new StatsView(statsManager);
        mainView.addInternalFrame(statsView);
    }

    public List<String> loadPlugin(File jarFile) throws IOException {
        return pluginLoader.loadFromJar(jarFile);
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
