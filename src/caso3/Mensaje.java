package src.caso3;

public class Mensaje {
    public enum Tipo { START, END, NORMAL }

    private final Tipo tipo;
    private final String id;
    private final boolean isSpamFlag;
    private boolean fromCuarentena = false;
    private int tiempoCuarentena = 0;

    public Mensaje(Tipo tipo, String id, boolean isSpamFlag) {
        this.tipo = tipo;
        this.id = id;
        this.isSpamFlag = isSpamFlag;
    }

    public static Mensaje start() {
        return new Mensaje(Tipo.START, "Inicio", false);
    }
    
    public static Mensaje end() {
        return new Mensaje(Tipo.END, "Final", false);
    }
    
    public static Mensaje normal(String id, boolean isSpam) {
        return new Mensaje(Tipo.NORMAL, id, isSpam);
    }

    public Tipo getTipo() { return tipo; }
    public String getId() { return id; }
    public boolean isSpamFlag() { return isSpamFlag; }

    public void setTiempoCuarentena(int t) { this.tiempoCuarentena = t; }
    public int getTiempoCuarentena() { return tiempoCuarentena; }

    public void decrementarTiempo() {
        if (tiempoCuarentena > 0) tiempoCuarentena--;
    }

    public boolean isFromCuarentena() { return fromCuarentena; }
    public void setFromCuarentena(boolean b) { fromCuarentena = b; }

    @Override
    public String toString() {
        if (tipo == Tipo.START) return "[START]";
        if (tipo == Tipo.END) return "[END]";
        return "[Id del mensaje=" + id + " spam=" + isSpamFlag + " Desde cuarentena=" + fromCuarentena + " tiempo en cuarentena=" + tiempoCuarentena + "]";
    }
}
