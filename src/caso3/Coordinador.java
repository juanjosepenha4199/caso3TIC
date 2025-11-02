package src.caso3;

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

    public synchronized boolean debeDepositarEndEntrega() {
        if (!endDepositedInEntrega) {
            endDepositedInEntrega = true;
            return true;
        }
        return false;
    }

    public synchronized boolean yaSeDepositoEnd() {
        return endDepositedInEntrega;
    }

    public synchronized int cuantosStartFaltan(int totalServidores) {
        int faltan = totalServidores - startDepositedInEntrega;
        return Math.max(0, faltan);
    }

    public synchronized boolean registrarStartDepositado(int totalServidores) {
        startDepositedInEntrega++;
        return startDepositedInEntrega < totalServidores;
    }

    public synchronized boolean condicionesParaFin(BuzonLimitado buzonEntrada, BuzonCuarentena buzonCuarentena) {
        return todosEndsRecibidos() && buzonEntrada.isEmpty() && buzonCuarentena.isEmpty();
    }
}
