package main.model;

import games.memory.MemoryGame;
import games.simon.SimonGame;
import games.snake.SnakeGame;
import gamesplugin.GameFunction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GameRegistry implements Iterable<GameFunction> {
    private static GameRegistry instance;
    private Map<String, GameFunction> games;

    private GameRegistry() {
        games = new HashMap<>();
    }

    public static GameRegistry getInstance() {
        if (instance == null) {
            instance = new GameRegistry();
        }
        return instance;
    }

    private void registerBuiltInGames() {
        registerGame("snake", "Snake", SnakeGame.class);
        registerGame("simondice", "Simon Dice", SimonGame.class);
        registerGame("memory", "Memory Game", MemoryGame.class);
    }

    public synchronized void registerExternalGame(String id, String displayName, GameFunction instance) {
        registerInstanceInternal(id, displayName, instance, true);
    }

    public synchronized void registerGame(String id, String displayName, Class<? extends GameFunction> clazz) {
        registerGame(id, displayName, clazz, false);
    }

    public synchronized void registerGame(String id, String displayName, Class<? extends GameFunction> clazz, boolean external) {
        registerFactoryInternal(id, displayName, () -> clazz.getDeclaredConstructor().newInstance(), external);
    }

    private void registerInstanceInternal(String id, String displayName, GameFunction instance, boolean external) {
        String normalizedId = normalize(id);
        ensureNotRegistered(normalizedId);
        games.put(normalizedId, new GameEntry(normalizedId, displayName, null, instance, external));
    }

    private void registerFactoryInternal(String id, String displayName, GameFactory factory, boolean external) {
        String normalizedId = normalize(id);
        ensureNotRegistered(normalizedId);
        games.put(normalizedId, new GameEntry(normalizedId, displayName, factory, null, external));
    }

    public synchronized GameFunction getGame(String id) throws Exception {
        GameEntry entry = games.get(normalize(id));
        if (entry == null) {
            throw new IllegalArgumentException("Juego no registrado: " + id);
        }

        String className = "games." + gameName + "." +
                capitalize(gameName) + "Game";

        try {
            Class<?> gameClass = Class.forName(className);
            GameFunction game = (GameFunction) gameClass
                    .getDeclaredConstructor()
                    .newInstance();

            games.put(gameName, game);
            return game;
        } catch (Exception e) {
            throw new Exception("No se pudo cargar el juego: " + gameName);
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Aquí está el método para el Iterator:
    @Override
    public Iterator<GameFunction> iterator() {
        return games.values().iterator();
    }
}
