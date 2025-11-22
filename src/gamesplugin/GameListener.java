package gamesplugin;

public interface GameListener {
    void onGameFinished(GameStats stats);

    /**
     * Compatibilidad binaria con juegos externos que llaman onGameFinished(Stat).
     * Convierte a GameStats y delega en el nuevo método.
     */
    default void onGameFinished(Stat stats) {
        if (stats == null) {
            return;
        }
        onGameFinished(GameStats.fromStat(stats));
    }
}
