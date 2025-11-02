package src.caso3;
/**
 * Coordinador.java
 * Monitor que coordina la sincronización global entre filtros de spam.
 *
 * Se encarga de:
 * - Contar cuántos mensajes START y END se han recibido.
 * - Determinar cuándo se han recibido todos los END de clientes.
 * - Decidir cuál filtro debe depositar el END final en el buzón de entrega.
 * - Verificar condiciones de finalización global.
 */
public class Coordinador {

    private int starts = 0;
    private int ends = 0;
    private final int totalClientes;
    private boolean endDepositedInEntrega = false;

    public Coordinador(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public synchronized void registrarStart() {
        starts++;
    }

    public synchronized void registrarEnd() {
        ends++;
    }

    public synchronized boolean todosEndsRecibidos() {
        return ends >= totalClientes;
    }

    public synchronized boolean debeDepositarEndEntrega() {
        if (!endDepositedInEntrega) {
            endDepositedInEntrega = true;
            return true;
        }
        return false;
    }

    public synchronized boolean condicionesParaFin(BuzonLimitado buzonEntrada, BuzonCuarentena buzonCuarentena) {
        return todosEndsRecibidos() && buzonEntrada.isEmpty() && buzonCuarentena.isEmpty();
    }
}
