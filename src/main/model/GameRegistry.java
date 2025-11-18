package main.model;

import games.memory.MemoryGame;
import games.simon.SimonGame;
import games.snake.SnakeGame;
import gamesplugin.GameFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class GameRegistry implements Iterable<GameFunction> {
    private static GameRegistry instance;
    private final Map<String, GameEntry> games;
    private final List<GameInfo> cachedInfos;

    private GameRegistry() {
        games = new HashMap<>();
        cachedInfos = new ArrayList<>();
        registerBuiltInGames();
    }

    public static GameRegistry getInstance() {
        if (instance == null) {
            instance = new GameRegistry();
        }
        return instance;
    }

    private void registerBuiltInGames() {
        registerGame("snake", "Snake", SnakeGame.class, false);
        registerGame("simondice", "Simon Dice", SimonGame.class, false);
        registerGame("memory", "Memory Game", MemoryGame.class, false);
    }

    public synchronized void registerExternalGame(String id, String displayName, GameFunction instance) {
        registerInstanceInternal(id, displayName, instance, true);
    }

    private void registerInstanceInternal(String id, String displayName, GameFunction instance, boolean external) {
        String normalizedId = normalize(id);
        ensureNotRegistered(normalizedId);
        games.put(normalizedId, new GameEntry(normalizedId, displayName, () -> instance, external));
        cachedInfos.clear();
    }

    private void registerFactoryInternal(String id, String displayName, Supplier<GameFunction> supplier, boolean external) {
        String normalizedId = normalize(id);
        ensureNotRegistered(normalizedId);
        games.put(normalizedId, new GameEntry(normalizedId, displayName, supplier, external));
        cachedInfos.clear();
    }

    public synchronized void registerGame(String id, String displayName, Class<? extends GameFunction> clazz, boolean external) {
        registerFactoryInternal(id, displayName, () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate game class: " + clazz.getName(), e);
            }
        }, external);
    }

    public synchronized List<GameInfo> getAvailableGames() {
        if (cachedInfos.isEmpty()) {
            for (GameEntry entry : games.values()) {
                cachedInfos.add(new GameInfo(entry.id, entry.displayName, entry.external));
            }
        }
        return Collections.unmodifiableList(cachedInfos);
    }

    public synchronized GameFunction getGame(String id) throws Exception {
        GameEntry entry = games.get(normalize(id));
        if (entry == null) {
            throw new IllegalArgumentException("Juego no registrado: " + id);
        }
        try {
            return entry.supplier.get();
        } catch (Exception e) {
            throw new Exception("No se pudo cargar el juego: " + id, e);
        }
    }

    private String normalize(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador no puede estar vacío");
        }
        return id.trim().toLowerCase();
    }

    private void ensureNotRegistered(String id) {
        if (games.containsKey(id)) {
            throw new IllegalArgumentException("Juego ya registrado: " + id);
        }
    }

    @Override
    public Iterator<GameFunction> iterator() {
        List<GameFunction> instances = new ArrayList<>();
        for (GameEntry entry : games.values()) {
            try {
                instances.add(entry.supplier.get());
            } catch (Exception e) {
                // Ignoramos juegos que no pudieron instanciarse
            }
        }
        return instances.iterator();
    }

    private record GameEntry(String id, String displayName,
                             Supplier<GameFunction> supplier, boolean external) {}

    public record GameInfo(String id, String displayName, boolean external) {}

    @FunctionalInterface
    private interface GameFactory {
        GameFunction newInstance() throws Exception;
    }
}
