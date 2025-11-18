package main.plugin;

import gamesplugin.GameFunction;
import main.model.GameRegistry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads external game modules that are packaged as JAR files.
 * Each JAR must declare a META-INF/services/gamesplugin.GameFunction file
 * so ServiceLoader can discover the implementations.
 */
public class PluginLoader {
    private static final Logger LOGGER = Logger.getLogger(PluginLoader.class.getName());

    private final GameRegistry registry;
    private final List<URLClassLoader> activeClassLoaders;

    public PluginLoader(GameRegistry registry) {
        this.registry = registry;
        this.activeClassLoaders = new ArrayList<>();
    }

    public List<String> loadFromJar(File jarFile) throws IOException {
        if (jarFile == null || !jarFile.isFile()) {
            throw new IOException("Seleccione un archivo .jar valido.");
        }

        URLClassLoader loader = new URLClassLoader(
                new URL[]{jarFile.toURI().toURL()},
                getClass().getClassLoader()
        );

        List<String> loadedGames = new ArrayList<>();
        try {
            ServiceLoader<GameFunction> serviceLoader = ServiceLoader.load(GameFunction.class, loader);
            for (GameFunction game : serviceLoader) {
                try {
                    String simpleName = game.getClass().getSimpleName();
                    String id = deriveId(simpleName);
                    String displayName = deriveDisplayName(simpleName);
                    registry.registerExternalGame(id, displayName, game);
                    loadedGames.add(displayName);
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, "Juego ya registrado: {0}", ex.getMessage());
                }
            }
        } catch (ServiceConfigurationError error) {
            closeQuietly(loader);
            throw new IOException("El archivo no cumple con el contrato de servicios de GameFunction.", error);
        }

        if (loadedGames.isEmpty()) {
            closeQuietly(loader);
            throw new IOException("El archivo seleccionado no contenia juegos compatibles.");
        }

        activeClassLoaders.add(loader);
        return loadedGames;
    }

    private String deriveId(String simpleName) {
        String base = stripGameSuffix(simpleName);
        return base.toLowerCase(Locale.ROOT);
    }

    private String deriveDisplayName(String simpleName) {
        String base = stripGameSuffix(simpleName);
        return base.replaceAll("([a-z])([A-Z])", "$1 $2").trim();
    }

    private String stripGameSuffix(String simpleName) {
        if (simpleName.toLowerCase(Locale.ROOT).endsWith("game") && simpleName.length() > 4) {
            return simpleName.substring(0, simpleName.length() - 4);
        }
        return simpleName;
    }

    private void closeQuietly(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "No fue posible cerrar el classloader del plugin.", e);
        }
    }
}
