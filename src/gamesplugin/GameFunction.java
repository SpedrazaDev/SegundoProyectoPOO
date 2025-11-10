package gamesplugin;


import javax.swing.*;

public interface GameFunction {
    public void iniciar();
    public Stat getStats();
    void setGameListener(GameListener listener);

}
