package src.caso3;
import java.util.List;
import java.util.Random;

/**
 * ManejadorCuarentena.java
 * Thread que inspecciona el buzón de cuarentena cada segundo:
 *
 * - Recorre snapshot de mensajes en cuarentena.
 * - Para cada mensaje decrementa contador (de 1 en 1).
 * - Si el contador llega a 0 => retira el mensaje y lo pone en buzón de entrega (semiactiva).
 * - Cada vez que revisa un mensaje se genera un número aleatorio 1..21;
 *   si es múltiplo de 7 => el mensaje es descartado (malicioso) y no llega a entrega.
 * - Corriendo cada segundo (Thread.sleep(1000)).
 * - Termina cuando recibe un mensaje END (debe procesarlo y luego terminar).
 */
public class ManejadorCuarentena extends Thread {
    private final BuzonCuarentena buzonCuarentena;
    private final BuzonLimitado buzonEntrega;
    private volatile boolean running = true;
    private final Random rnd = new Random();

    public ManejadorCuarentena(BuzonCuarentena buzonCuarentena, BuzonLimitado buzonEntrega) {
        setName("ManejadorCuarentena");
        this.buzonCuarentena = buzonCuarentena;
        this.buzonEntrega = buzonEntrega;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // obtener snapshot y procesar
                List<Mensaje> snapshot = buzonCuarentena.obtenerSnapshot();
                for (Mensaje m : snapshot) {
                    // si es END => retirar y terminar
                    if (m.getTipo() == Mensaje.Tipo.END) {
                        // remover y terminar
                        if (buzonCuarentena.remove(m)) {
                            System.out.println(getName() + " procesó END de cuarentena y termina.");
                        }
                        running = false;
                        break;
                    }
                    // decrementar tiempo
                    m.decrementarTiempo();
                    // cada vez que inspecciona, genera aleatorio 1..21
                    int valor = 1 + rnd.nextInt(21);
                    boolean descartar = (valor % 7 == 0);
                    if (m.getTiempoCuarentena() <= 0) {
                        // tiempo listo -> remover y si no es descartado -> enviar a entrega
                        boolean removed = buzonCuarentena.remove(m);
                        if (removed) {
                            if (!descartar) {
                                m.setFromCuarentena(true);
                                buzonEntrega.put(m);
                            } else {
                                // descartado
                                System.out.println(getName() + " descartó (malicioso) mensaje " + m);
                            }
                        }
                    } else {
                        // no listo aún; si se decide descartar aleatoriamente, opcional: descartes solo cuando tiempo llega 0 según enunciado
                        // En el enunciado: genera número cada vez que revisa un mensaje; si múltiplo de 7, mensaje se descarta.
                        if (descartar) {
                            boolean removed = buzonCuarentena.remove(m);
                            if (removed) {
                                System.out.println(getName() + " descartó (aleatorio) mensaje " + m);
                            }
                        }
                    }
                }

                // dormir 1 segundo entre chequeos
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }
}
