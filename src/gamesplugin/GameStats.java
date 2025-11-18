package gamesplugin;

/**
 * Tipo auxiliar para cumplir con la firma solicitada por GameListener
 * sin dejar de reutilizar la implementación de Stat existente.
 */
public class GameStats extends Stat {
    public GameStats(String clave, String nombre, int valor) {
        super(clave, nombre, valor);
    }

    public static GameStats fromStat(Stat stat) {
        if (stat instanceof GameStats gameStats) {
            return gameStats;
        }
        return new GameStats(stat.getClave(), stat.getNombre(), stat.getValor());
    }
}
