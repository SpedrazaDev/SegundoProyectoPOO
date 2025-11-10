package main.model;

import gamesplugin.Stat;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public class StatsManager {
    private static StatsManager instance;
    private Map<String, List<Stat>> allStats;
    private static final String STATS_FILE = "stats.json";
    private static final Logger LOGGER = Logger.getLogger(StatsManager.class.getName());


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
            writer.println("{");
            int juegoCount = 0;
            for (Map.Entry<String, List<Stat>> entry : allStats.entrySet()) {
                String juego = entry.getKey();
                List<Stat> statsList = entry.getValue();
                writer.printf("  \"%s\": [\n", juego);
                for (int i = 0; i < statsList.size(); i++) {
                    Stat stat = statsList.get(i);
                    writer.printf(
                            "    {\"nombre\": \"%s\", \"valor\": %d}%s\n",
                            stat.getNombre().replace("\"", "\\\""),
                            stat.getValor(),
                            (i < statsList.size() - 1 ? "," : "")
                    );
                }
                writer.print("  ]");
                writer.print(++juegoCount < allStats.size() ? ",\n" : "\n");
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadStats() {
        File file = new File(STATS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Map<String, List<Stat>> loaded = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                // Simple parseo manual de JSON
                line = line.replace("{", "").replace("}", "").replace("\"", "");
                String[] parts = line.split(",");
                String juego = null;
                String nombre = null;
                int valor = 0;
                for (String p : parts) {
                    String[] kv = p.split(":");
                    if (kv.length == 2) {
                        if (kv[0].trim().equals("juego")) juego = kv[1].trim();
                        else if (kv[0].trim().equals("nombre")) nombre = kv[1].trim();
                        else if (kv[0].trim().equals("valor")) valor = Integer.parseInt(kv[1].trim());
                    }
                }
                if (juego != null && nombre != null) {
                    loaded.putIfAbsent(juego, new ArrayList<>());
                    loaded.get(juego).add(new Stat(juego, nombre, valor));
                }
            }
            allStats = loaded;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
