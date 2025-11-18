package main.view;

import gamesplugin.Stat;
import main.model.StatsManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StatsView extends JInternalFrame {
    private final StatsManager statsManager;

    public StatsView(StatsManager statsManager) {
        super("Estadisticas", true, true, true, true);
        this.statsManager = statsManager;
        setSize(520, 320);
        setLocation(150, 100);
        setLayout(new BorderLayout());

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Juego", "Rubro", "Nombre", "Valor"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fillModel(model);

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
    }

    private void fillModel(DefaultTableModel model) {
        Map<String, List<Stat>> stats = statsManager.getAllStats();
        for (Map.Entry<String, List<Stat>> entry : stats.entrySet()) {
            String gameName = entry.getKey();
            for (Stat stat : entry.getValue()) {
                model.addRow(new Object[]{
                        gameName,
                        stat.getClave(),
                        stat.getNombre(),
                        stat.getValor()
                });
            }
        }
    }
}
