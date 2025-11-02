package src.caso3;
/**
 * Mensaje.java
 * Representa un mensaje en el sistema de mensajería.
 *
 * Tipos especiales:
 * - START: mensaje de inicio
 * - END: mensaje de fin
 * - NORMAL: correo normal (tiene id, flagSpam)
 *
 * Si el mensaje ha pasado por cuarentena, se puede marcar con isFromQuarantine = true.
 */
public class Mensaje {
    public enum Tipo { START, END, NORMAL }

    private final Tipo tipo;
    private final String id;       // identificador único (puede ser null para START/END)
    private final boolean isSpamFlag; // flag generado por cliente (solo relevante si tipo == NORMAL)
    private boolean fromCuarentena = false; // si proviene de cuarentena

    // campo usado por el manejador de cuarentena: contador de ticks (segundos) que faltan
    private int tiempoCuarentena = 0;

    public Mensaje(Tipo tipo, String id, boolean isSpamFlag) {
        this.tipo = tipo;
        this.id = id;
        this.isSpamFlag = isSpamFlag;
    }

    // Factory helpers
    public static Mensaje start() {
        return new Mensaje(Tipo.START, "START", false);
    }
    public static Mensaje end() {
        return new Mensaje(Tipo.END, "END", false);
    }
    public static Mensaje normal(String id, boolean isSpam) {
        return new Mensaje(Tipo.NORMAL, id, isSpam);
    }

    // getters / setters
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
        switch (tipo) {
            case START: return "[START]";
            case END:   return "[END]";
            default:
                return String.format("[MSG id=%s spam=%s fromCuar=%s t=%d]",
                        id, isSpamFlag, fromCuarentena, tiempoCuarentena);
        }
    }
}
