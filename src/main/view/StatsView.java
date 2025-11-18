package main.view;

import main.model.StatsManager;
import gamesplugin.Stat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StatsView extends JInternalFrame {
    private final StatsManager statsManager;

    public StatsView(StatsManager statsManager) {
        super("Estadísticas", true, true, true, true);
        this.statsManager = statsManager;
        setSize(500, 300);
        setLocation(150, 100);
        setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Juego");
        model.addColumn("Clave");
        model.addColumn("Nombre");
        model.addColumn("Valor");

        Map<String, List<Stat>> stats = statsManager.getAllStats();
        for (Map.Entry<String, List<Stat>> entry : stats.entrySet()) {
            String gameId = entry.getKey();
            for (Stat stat : entry.getValue()) {
                model.addRow(new Object[] {
                        gameId,
                        stat.getClave(),
                        stat.getNombre(),
                        stat.getValor()
                });
            }
        }

        JTable tabla = new JTable(model);
        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);
    }
}
