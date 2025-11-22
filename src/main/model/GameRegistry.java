package main.model;

import games.memory.MemoryGame;
import games.simon.SimonGame;
import games.snake.SnakeGame;
import gamesplugin.GameFunction;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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

    public synchronized List<GameInfo> loadGamesFromJar(File jarFile) throws Exception {
        if (jarFile == null || !jarFile.isFile()) {
            throw new IllegalArgumentException("Archivo JAR inv\u00E1lido");
        }
        List<GameInfo> addedGames = new ArrayList<>();
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, GameFunction.class.getClassLoader());
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String mainClass = manifest.getMainAttributes().getValue("Main-Class");
                if (mainClass != null && !mainClass.isBlank()) {
                    try {
                        Class<?> rawMain = Class.forName(mainClass.trim(), true, loader);
                        GameInfo info = tryRegisterLoadedClass(rawMain);
                        if (info != null) {
                            addedGames.add(info);
                        }
                    } catch (UnsupportedClassVersionError e) {
                        throw new Exception("El JAR fue compilado con una versi\u00F3n de Java m\u00E1s reciente.", e);
                    }
                }
            }

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!isClassEntry(entry)) {
                    continue;
                }
                String className = entry.getName()
                        .replace('/', '.')
                        .replace(".class", "");
                try {
                    Class<?> rawClass = Class.forName(className, true, loader);
                    GameInfo info = tryRegisterLoadedClass(rawClass);
                    if (info != null) {
                        addedGames.add(info);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError |
                         NoSuchMethodException | IllegalArgumentException ignored) {
                    // Ignoramos clases que no cumplen con los requisitos
                } catch (UnsupportedClassVersionError e) {
                    throw new Exception("El JAR fue compilado con una versi\u00F3n de Java m\u00E1s reciente.", e);
                }
            }
        } catch (IOException e) {
            throw new Exception("No se pudo leer el archivo JAR: " + jarFile.getName(), e);
        }
        if (addedGames.isEmpty()) {
            throw new Exception("No se encontraron juegos compatibles en el archivo seleccionado.");
        }
        return addedGames;
    }

    public synchronized void registerExternalGame(String id, String displayName, GameFunction instance) {
        registerInstanceInternal(id, displayName, instance, true);
    }

    private void registerInstanceInternal(String id, String displayName, GameFunction instance, boolean external) {
        String normalizedId = normalize(id);
        ensureNotRegistered(normalizedId);
        GameEntry entry = new GameEntry(normalizedId, displayName, () -> instance, external);
        entry.setInitialInstance(instance);
        games.put(normalizedId, entry);
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
            return entry.getOrCreateInstance();
        } catch (Exception e) {
            throw new Exception("No se pudo cargar el juego: " + id, e);
        }
    }

    private String normalize(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador no puede estar vac\u00EDo");
        }
        return id.trim().toLowerCase();
    }

    private boolean isClassEntry(JarEntry entry) {
        return entry != null
                && !entry.isDirectory()
                && entry.getName().endsWith(".class")
                && !entry.getName().contains("$");
    }

    private GameInfo tryRegisterLoadedClass(Class<?> rawClass) throws Exception {
        if (rawClass == null) {
            return null;
        }
        if (!GameFunction.class.isAssignableFrom(rawClass)) {
            return null;
        }
        Class<? extends GameFunction> gameClass = rawClass.asSubclass(GameFunction.class);
        if (Modifier.isAbstract(gameClass.getModifiers())) {
            return null;
        }

        GameFunction instance = instantiateGame(gameClass);
        String uniqueId = buildAvailableId(gameClass.getSimpleName());
        String displayName = buildDisplayName(gameClass.getSimpleName());
        registerInstanceInternal(uniqueId, displayName, instance, true);
        return new GameInfo(uniqueId, displayName, true);
    }

    private GameFunction instantiateGame(Class<? extends GameFunction> gameClass) throws Exception {
        try {
            var getInstance = gameClass.getMethod("getInstance");
            if (Modifier.isStatic(getInstance.getModifiers())
                    && GameFunction.class.isAssignableFrom(getInstance.getReturnType())) {
                return (GameFunction) getInstance.invoke(null);
            }
        } catch (NoSuchMethodException ignored) {
            // Sin singleton, seguimos con el constructor.
        }
        return gameClass.getDeclaredConstructor().newInstance();
    }

    private String buildDisplayName(String simpleName) {
        if (simpleName == null || simpleName.isEmpty()) {
            return "Juego Externo";
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = simpleName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char current = chars[i];
            if (i > 0 && Character.isUpperCase(current) && Character.isLowerCase(chars[i - 1])) {
                builder.append(' ');
            }
            builder.append(current);
        }
        return builder.toString().trim();
    }

    private String buildAvailableId(String preferredId) {
        String normalized = normalize(preferredId);
        String candidate = normalized;
        int suffix = 2;
        while (games.containsKey(candidate)) {
            candidate = normalized + suffix;
            suffix++;
        }
        return candidate;
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

    private static final class GameEntry {
        private final String id;
        private final String displayName;
        private final Supplier<GameFunction> supplier;
        private final boolean external;
        private GameFunction instance;

        private GameEntry(String id, String displayName,
                          Supplier<GameFunction> supplier, boolean external) {
            this.id = id;
            this.displayName = displayName;
            this.supplier = supplier;
            this.external = external;
        }

        private synchronized GameFunction getOrCreateInstance() {
            if (instance == null) {
                instance = supplier.get();
            }
            return instance;
        }

        private synchronized void setInitialInstance(GameFunction instance) {
            this.instance = instance;
        }
    }

    public record GameInfo(String id, String displayName, boolean external) {}

    @FunctionalInterface
    private interface GameFactory {
        GameFunction newInstance() throws Exception;
    }
}
