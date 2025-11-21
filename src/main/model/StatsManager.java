package main.model;

import gamesplugin.Stat;
import java.io.*;
import java.util.*;

public class StatsManager {
    private static StatsManager instance;
    private Map<String, List<Stat>> allStats;
    private static final String STATS_FILE = "stats.json";


    private StatsManager() {
        allStats = new HashMap<>();
        loadStats();
    }

    public static StatsManager getInstance() {
        if (instance == null) {
            instance = new StatsManager();
        }
        return instance;
    }

    public void addStat(String gameId, Stat stat) {
        String key = normalizeGameId(gameId);
        allStats.putIfAbsent(key, new ArrayList<>());
        List<Stat> statsList = allStats.get(key);
        String playerName = stat.getNombre() == null ? "" : stat.getNombre().trim();
        Stat existing = null;
        for (Stat s : statsList) {
            String currentName = s.getNombre() == null ? "" : s.getNombre().trim();
            if (currentName.equalsIgnoreCase(playerName)) {
                existing = s;
                break;
            }
        }

        if (existing != null) {
            if (stat.getValor() > existing.getValor()) {
                existing.setValor(stat.getValor());
                existing.setClave(stat.getClave());
            }
        } else {
            statsList.add(stat);
        }
        sortAndTrim(key, statsList);
    }

    public Map<String, List<Stat>> getAllStats() {
        Map<String, List<Stat>> copy = new HashMap<>();
        for (Map.Entry<String, List<Stat>> entry : allStats.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STATS_FILE))) {
            writer.println("{");
            Iterator<Map.Entry<String, List<Stat>>> iterator = allStats.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<Stat>> entry = iterator.next();
                writer.printf("  \"%s\": [%n", entry.getKey());
                List<Stat> statsList = entry.getValue();
                for (int i = 0; i < statsList.size(); i++) {
                    Stat stat = statsList.get(i);
                    writer.printf(
                            "    {\"clave\":\"%s\",\"nombre\":\"%s\",\"valor\":%d}%s%n",
                            escape(stat.getClave()),
                            escape(stat.getNombre()),
                            stat.getValor(),
                            (i < statsList.size() - 1 ? "," : "")
                    );
                }
                writer.printf("  ]%s%n", iterator.hasNext() ? "," : "");
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStats() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return;
        }
        Map<String, List<Stat>> loaded = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentGame = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.equals("{") || line.equals("}")) {
                    continue;
                }
                if (line.startsWith("\"")) {
                    int end = line.indexOf('"', 1);
                    if (end > 1) {
                        currentGame = normalizeGameId(line.substring(1, end));
                        loaded.putIfAbsent(currentGame, new ArrayList<>());
                    }
                    continue;
                }
                if (line.startsWith("]")) {
                    currentGame = null;
                    continue;
                }
                if (line.startsWith("{") && currentGame != null) {
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    if (line.startsWith("{") && line.endsWith("}")) {
                        line = line.substring(1, line.length() - 1);
                    }
                    String[] fields = line.split(",");
                    String clave = "";
                    String nombre = "";
                    int valor = 0;
                    for (String field : fields) {
                        String[] kv = field.split(":", 2);
                        if (kv.length < 2) {
                            continue;
                        }
                        String key = kv[0].replace("\"", "").trim();
                        String value = kv[1].trim();
                        if (value.startsWith("\"")) {
                            value = value.substring(1, value.length() - 1);
                        }
                        switch (key) {
                            case "clave" -> clave = unescape(value);
                            case "nombre" -> nombre = unescape(value);
                            case "valor" -> {
                                try {
                                    valor = Integer.parseInt(value);
                                } catch (NumberFormatException ignore) {
                                    valor = 0;
                                }
                            }
                            default -> {
                            }
                        }
                    }
                    loaded.computeIfAbsent(currentGame, k -> new ArrayList<>())
                            .add(new Stat(clave, nombre, valor));
                }
            }
            for (Map.Entry<String, List<Stat>> entry : loaded.entrySet()) {
                sortAndTrim(entry.getKey(), entry.getValue());
            }
            allStats = loaded;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String normalizeGameId(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            return "desconocido";
        }
        return gameId.trim().toLowerCase();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private void sortAndTrim(String gameId, List<Stat> statsList) {
        Comparator<Stat> comparator = comparatorFor(gameId);
        statsList.sort(comparator);
        if (statsList.size() > 3) {
            statsList.subList(3, statsList.size()).clear();
        }
    }

    private Comparator<Stat> comparatorFor(String gameId) {
        boolean ascending = normalizeGameId(gameId).equals("memory");
        return ascending
                ? Comparator.comparingInt(Stat::getValor)
                : (a, b) -> Integer.compare(b.getValor(), a.getValor());
    }
}
