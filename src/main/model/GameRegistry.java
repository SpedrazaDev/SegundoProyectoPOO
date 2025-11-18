package main.model;

import games.snake.SnakeGame;
import gamesplugin.GameFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry in charge of keeping a single instance per game.
 * It also exposes metadata so the menu can show dynamically all the games,
 * including those loaded from plug-ins.
 */
public class GameRegistry implements Iterable<GameFunction> {
    private static GameRegistry instance;

    private final Map<String, GameEntry> games;

    private GameRegistry() {
        games = new LinkedHashMap<>();
        registerBuiltInGames();
    }

    public static synchronized GameRegistry getInstance() {
        if (instance == null) {
            instance = new GameRegistry();
        }
        return instance;
    }

    private void registerBuiltInGames() {
        registerGame("snake", "Snake", SnakeGame.class);
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

        if (entry.instance == null && entry.factory != null) {
            entry.instance = entry.factory.create();
        }

        if (entry.instance == null) {
            throw new IllegalStateException("Juego sin instancia disponible: " + id);
        }
        return entry.instance;
    }

    public synchronized List<GameInfo> getRegisteredGames() {
        List<GameInfo> result = new ArrayList<>();
        for (GameEntry entry : games.values()) {
            result.add(new GameInfo(entry.id, entry.displayName, entry.external));
        }
        return Collections.unmodifiableList(result);
    }

    private String normalize(String id) {
        return id.toLowerCase(Locale.ROOT).trim();
    }

    private void ensureNotRegistered(String normalizedId) {
        if (games.containsKey(normalizedId)) {
            throw new IllegalArgumentException("Juego ya registrado: " + normalizedId);
        }
    }

    @Override
    public Iterator<GameFunction> iterator() {
        List<GameFunction> loaded = new ArrayList<>();
        for (GameEntry entry : games.values()) {
            if (entry.instance != null) {
                loaded.add(entry.instance);
            }
        }
        return loaded.iterator();
    }

    private interface GameFactory {
        GameFunction create() throws Exception;
    }

    public static class GameInfo {
        private final String id;
        private final String displayName;
        private final boolean external;

        public GameInfo(String id, String displayName, boolean external) {
            this.id = id;
            this.displayName = displayName;
            this.external = external;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isExternal() {
            return external;
        }
    }

    private static class GameEntry {
        private final String id;
        private final String displayName;
        private final boolean external;
        private final GameFactory factory;
        private GameFunction instance;

        private GameEntry(String id, String displayName, GameFactory factory, GameFunction instance, boolean external) {
            this.id = id;
            this.displayName = displayName;
            this.factory = factory;
            this.instance = instance;
            this.external = external;
        }
    }
}
