package gamesplugin;

import javax.swing.JInternalFrame;

public interface GameFunction {
    public JInternalFrame iniciar();
    public Stat getStats();
    void setGameListener(GameListener listener);
}
