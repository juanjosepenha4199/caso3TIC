package src.caso3;
import java.util.Random;

/**
 * FiltroSpam.java
 * Thread consumidor que toma mensajes del buzón de entrada (espera pasiva).
 *
 * Comportamiento:
 * - Consume mensaje por mensaje.
 * - START: incrementa contador local (o global) de clientes iniciados.
 * - END: cuenta los ends recibidos. Cuando se han recibido ends == numeroClientes,
 *   el filtro sabe que no se producirán más mensajes (pero debe ayudar a vaciar colas).
 * - NORMAL: si es spamFlag == true -> lo envía al buzón de cuarentena (semiactiva),
 *           y le asigna un tiempo aleatorio [10000,20000] ms (expresado en ticks de 1).
 *         si no es spam -> lo envía al buzón de entrega (semiactiva).
 *
 * Además:
 * - Cuando detecta que condiciones de terminación cumplen (entrada vacía, cuarentena vacía,
 *   y se han recibido todos los ENDs de clientes), un filtro depositará el mensaje END en
 *   el buzón de entrega y buzón de cuarentena según lo requiere el enunciado.
 *
 * Nota: para coordinación de finalización entre filtros (quién pone el END en entrega) se
 * usa un objeto monitor compartido (coordinador).
 */
public class FiltroSpam extends Thread {
    private final BuzonLimitado buzonEntrada;
    private final BuzonCuarentena buzonCuarentena;
    private final BuzonLimitado buzonEntrega;
    private final int totalClientes;
    private final Coordinador coordenador;
    private final Random rnd = new Random();

    // rango en ms para tiempo de cuarentena
    private static final int MIN_QUAR = 10000;
    private static final int MAX_QUAR = 20000;

    public FiltroSpam(String name,
                      BuzonLimitado buzonEntrada,
                      BuzonCuarentena buzonCuarentena,
                      BuzonLimitado buzonEntrega,
                      int totalClientes,
                      Coordinador coordenador) {
        setName("Filtro-" + name);
        this.buzonEntrada = buzonEntrada;
        this.buzonCuarentena = buzonCuarentena;
        this.buzonEntrega = buzonEntrega;
        this.totalClientes = totalClientes;
        this.coordenador = coordenador;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = buzonEntrada.take(); // espera pasiva
                if (m.getTipo() == Mensaje.Tipo.START) {
                    sincronizarStart();
                    continue;
                } else if (m.getTipo() == Mensaje.Tipo.END) {
                    sincronizarEnd();
                    // After registering END, continue - filters only finish when global conditions met
                    if (coordenador.todosEndsRecibidos() && coordenador.condicionesParaFin(buzonEntrada, buzonCuarentena)) {
                        // one filter must insert END in buzonEntrega and buzonCuarentena (spec says one deposits in entrega)
                        if (coordenador.debeDepositarEndEntrega()) {
                            // depositar END en buzonEntrega solo cuando entrada vacía y cuarentena vacía y no se generarán nuevos mensajes
                            // but ensure delivery buzon has capacity: use put (may block)
                            buzonEntrega.put(Mensaje.end());
                            // Also deposit END in cuarentena so that manejador ends when processes it (spec asked also a FIN in cuarentena)
                            buzonCuarentena.put(Mensaje.end());
                            System.out.println(getName() + " depositó END en entrega y cuarentena.");
                        }
                        break; // finish this filter
                    }
                    continue;
                }

                // NORMAL message
                if (m.isSpamFlag()) {
                    // asignar tiempo aleatorio entre [10000,20000] ms
                    int tiempo = MIN_QUAR + rnd.nextInt(MAX_QUAR - MIN_QUAR + 1);
                    m.setTiempoCuarentena(tiempo / 1000); // guardamos en segundos para ticks del manejador
                    // mark and put in cuarentena (semiactiva)
                    buzonCuarentena.put(m);
                } else {
                    // mensaje válido => enviar al buzonEntrega (semiactiva)
                    buzonEntrega.put(m);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }

    private void sincronizarStart() {
        synchronized (coordenador) {
            coordenador.registrarStart();
            coordenador.notifyAll();
        }
    }

    private void sincronizarEnd() {
        synchronized (coordenador) {
            coordenador.registrarEnd();
            coordenador.notifyAll();
        }
    }
}
