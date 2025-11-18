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

    public void addStat(Stat stat) {
        String key = stat.getClave();
        allStats.putIfAbsent(key, new ArrayList<>());
        List<Stat> statsList = allStats.get(key);
        statsList.add(stat);
        statsList.sort((s1, s2) -> Integer.compare(s2.getValor(), s1.getValor()));
        if (statsList.size() > 3) {
            statsList.remove(3);
        }
    }

    public Map<String, List<Stat>> getAllStats() {
        return allStats;
    }

    public void saveStats() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STATS_FILE))) {
            for (Map.Entry<String, List<Stat>> entry : allStats.entrySet()) {
                String juego = entry.getKey();
                for (Stat stat : entry.getValue()) {
                    writer.printf("%s;%s;%d%n",
                            juego,
                            stat.getNombre().replace(";", ""),
                            stat.getValor());
                }
            }
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
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 3) {
                    continue;
                }
                String juego = parts[0].trim();
                String nombre = parts[1].trim();
                int valor;
                try {
                    valor = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException ex) {
                    continue;
                }
                loaded.computeIfAbsent(juego, k -> new ArrayList<>())
                        .add(new Stat(juego, nombre, valor));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        allStats = loaded;
    }
}
