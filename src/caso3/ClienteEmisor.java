package src.caso3;
import java.util.Random;

public class ClienteEmisor extends Thread {
    private final String clienteId;
    private final int cantidad;
    private final BuzonLimitado buzonEntrada;
    private final Random rnd = new Random();

    public ClienteEmisor(String clienteId, int cantidad, BuzonLimitado buzonEntrada) {
        this.clienteId = clienteId;
        this.cantidad = cantidad;
        this.buzonEntrada = buzonEntrada;
        setName("Cliente-" + clienteId);
    }

    @Override
    public void run() {
        try {
            buzonEntrada.put(Mensaje.start());
            for (int i = 1; i <= cantidad; i++) {
                String id = clienteId + "-" + i;
                boolean spam = rnd.nextBoolean();
                Mensaje m = Mensaje.normal(id, spam);
                buzonEntrada.put(m);
                Thread.yield();
            }
            buzonEntrada.put(Mensaje.end());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }
}
