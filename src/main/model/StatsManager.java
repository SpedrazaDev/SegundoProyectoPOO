package main.model;

import gamesplugin.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatsManager {
    private static final Logger LOGGER = Logger.getLogger(StatsManager.class.getName());
    private static final Path STATS_FILE = Paths.get("stats.json");

    private static StatsManager instance;
    private final Map<String, List<Stat>> statsByGame;

    private StatsManager() {
        statsByGame = new LinkedHashMap<>();
        loadStats();
    }

    public static synchronized StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }

    public synchronized void addStat(String gameId, Stat stat) {
        List<Stat> stats = statsByGame.computeIfAbsent(gameId, key -> new ArrayList<>());
        stats.add(stat);
        stats.sort(Comparator.comparingInt(Stat::getValor).reversed());
        if (stats.size() > 3) {
            stats.subList(3, stats.size()).clear();
        }
    }

    public synchronized Map<String, List<Stat>> getAllStats() {
        Map<String, List<Stat>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<Stat>> entry : statsByGame.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
        }
        return Collections.unmodifiableMap(copy);
    }

    public synchronized void saveStats() {
        try {
            if (statsByGame.isEmpty()) {
                Files.deleteIfExists(STATS_FILE);
                return;
            }
            List<String> lines = buildJson();
            Files.write(STATS_FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "No se pudo guardar el archivo de estadisticas.", e);
        }
    }

    private List<String> buildJson() {
        List<String> lines = new ArrayList<>();
        lines.add("{");
        int counter = 0;
        for (Map.Entry<String, List<Stat>> entry : statsByGame.entrySet()) {
            String game = escape(entry.getKey());
            lines.add(String.format(Locale.ROOT, "  \"%s\": [", game));
            List<Stat> stats = entry.getValue();
            for (int i = 0; i < stats.size(); i++) {
                Stat stat = stats.get(i);
                String line = String.format(
                        Locale.ROOT,
                        "    {\"clave\": \"%s\", \"nombre\": \"%s\", \"valor\": %d}%s",
                        escape(stat.getClave()),
                        escape(stat.getNombre()),
                        stat.getValor(),
                        (i < stats.size() - 1 ? "," : "")
                );
                lines.add(line);
            }
            counter++;
            lines.add(counter < statsByGame.size() ? "  ]," : "  ]");
        }
        lines.add("}");
        return lines;
    }

    private String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String unescape(String value) {
        return value
                .replace("\\\\", "\\")
                .replace("\\\"", "\"");
    }

    private void loadStats() {
        if (!Files.exists(STATS_FILE)) {
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(STATS_FILE);
            String content = new String(bytes, StandardCharsets.UTF_8);
            Pattern gamePattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Pattern statPattern = Pattern.compile("\\{\\s*\"clave\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"nombre\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"valor\"\\s*:\\s*(\\d+)\\s*\\}");

            Matcher gameMatcher = gamePattern.matcher(content);
            while (gameMatcher.find()) {
                String gameId = unescape(gameMatcher.group(1));
                String block = gameMatcher.group(2);
                Matcher statMatcher = statPattern.matcher(block);
                List<Stat> stats = new ArrayList<>();
                while (statMatcher.find()) {
                    String clave = unescape(statMatcher.group(1));
                    String nombre = unescape(statMatcher.group(2));
                    int valor = Integer.parseInt(statMatcher.group(3));
                    stats.add(new Stat(clave, nombre, valor));
                }
                stats.sort(Comparator.comparingInt(Stat::getValor).reversed());
                if (stats.size() > 3) {
                    stats = new ArrayList<>(stats.subList(0, 3));
                }
                if (!stats.isEmpty()) {
                    statsByGame.put(gameId, stats);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer el archivo de estadisticas.", e);
        }
    }
}
