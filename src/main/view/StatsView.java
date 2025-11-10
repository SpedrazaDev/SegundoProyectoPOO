package main.view;

import main.model.StatsManager;
import gamesplugin.Stat;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StatsView extends JInternalFrame {
    private StatsManager statsManager;

    public StatsView(StatsManager statsManager) {
        super("Estadísticas", true, true, true, true);
        this.statsManager = statsManager;
        setSize(500, 300);
        setLocation(150, 100);
        setLayout(new BorderLayout());

        // Crear el modelo de tabla
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Juego");
        model.addColumn("Nombre");
        model.addColumn("Valor");

        // Obtener las estadísticas
        Map<String, List<Stat>> stats = statsManager.getAllStats();
        for (String juego : stats.keySet()) {
            List<Stat> lista = stats.get(juego);
            for (Stat stat : lista) {
                model.addRow(new Object[] {
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
