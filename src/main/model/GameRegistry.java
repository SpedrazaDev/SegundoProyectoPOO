package main.model;

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

    public GameFunction getGame(String gameName) throws Exception {
        if (games.containsKey(gameName)) {
            return games.get(gameName);
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
