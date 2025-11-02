package src.caso3;

/**
 * Coordinador.java
 * Monitor de sincronización global entre los filtros de spam.
 *
 * Controla:
 * - Cuántos mensajes START y END se han recibido.
 * - Cuándo se han recibido todos los END.
 * - Cuál filtro debe depositar el END final en los buzones de entrega y cuarentena.
 * - Verifica condiciones globales de terminación.
 */
public class Coordinador {

    private int starts = 0;
    private int ends = 0;
    private final int totalClientes;
    private boolean endDepositedInEntrega = false;
    private int startDepositedInEntrega = 0;

    public Coordinador(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public synchronized void registrarStart() {
        starts++;
        System.out.println("  [Coordinador] START recibido (" + starts + "/" + totalClientes + " clientes)");
    }

    public synchronized void registrarEnd() {
        ends++;
        System.out.println("  [Coordinador] END recibido (" + ends + "/" + totalClientes + " clientes)");
    }

    public synchronized boolean todosEndsRecibidos() {
        return ends >= totalClientes;
    }

    /** 
     * Solo un filtro debe depositar el END final. 
     * Este método devuelve true solo una vez.
     */
    public synchronized boolean debeDepositarEndEntrega() {
        if (!endDepositedInEntrega) {
            endDepositedInEntrega = true;
            return true;
        }
        return false;
    }

    /**
     * Verifica si ya se depositó el END final sin modificar el estado.
     */
    public synchronized boolean yaSeDepositoEnd() {
        return endDepositedInEntrega;
    }

    /**
     * Deposita START en el buzón de entrega para activar servidores.
     * Este método devuelve cuántos STARTs faltan depositar (necesitamos uno por cada servidor).
     */
    public synchronized int cuantosStartFaltan(int totalServidores) {
        int faltan = totalServidores - startDepositedInEntrega;
        return Math.max(0, faltan);
    }

    /**
     * Registra que se depositó un START en el buzón de entrega.
     * Devuelve true si aún faltan STARTs por depositar.
     */
    public synchronized boolean registrarStartDepositado(int totalServidores) {
        startDepositedInEntrega++;
        return startDepositedInEntrega < totalServidores;
    }

    /**
     * Verifica si se cumplieron las condiciones globales de terminación:
     * - Todos los END recibidos
     * - Buzón de entrada vacío
     * - Cuarentena vacía
     */
    public synchronized boolean condicionesParaFin(BuzonLimitado buzonEntrada, BuzonCuarentena buzonCuarentena) {
        return todosEndsRecibidos() && buzonEntrada.isEmpty() && buzonCuarentena.isEmpty();
    }
}
