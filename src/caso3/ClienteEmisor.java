package src.caso3;
import java.util.Random;

/**
 * ClienteEmisor.java
 * Thread productor que genera N mensajes:
 * - Al comenzar: envía un mensaje START al buzón de entrada.
 * - Genera mensajes NORMAL con id cliente_i y flag de spam aleatorio.
 * - Al terminar: envía un mensaje END.
 *
 * Si el buzón de entrada está lleno, espera pasiva en put().
 */
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
            // mensaje de inicio
            buzonEntrada.put(Mensaje.start());
            // generar mensajes
            for (int i = 1; i <= cantidad; i++) {
                String id = clienteId + "-" + i;
                boolean spam = rnd.nextBoolean(); // flag aleatorio; puedes ajustar probabilidad
                Mensaje m = Mensaje.normal(id, spam);
                buzonEntrada.put(m);
                // yield para dar oportunidad a otros threads si hay congestión
                Thread.yield();
            }
            // mensaje de fin
            buzonEntrada.put(Mensaje.end());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }
}
