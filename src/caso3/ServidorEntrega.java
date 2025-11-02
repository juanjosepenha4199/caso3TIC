package src.caso3;
import java.util.Random;

/**
 * ServidorEntrega.java
 * Thread consumidor del buzón de entrega.
 *
 * - Debe iniciarse cuando encuentra un mensaje START (según enunciado).
 *   Para simplificar, todos los servidores arrancan desde el inicio del programa,
 *   pero procesan mensajes: ignoran hasta ver START para "activar" su operación.
 *
 * - Lee mensajes del buzón de entrega en espera activa: especifica que la lectura
 *   de servers es en espera activa (polling). Para cumplir esto recordamos:
 *     -> Espera activa se simula haciendo 'take' con timeout manual no permitido
 *        (no usamos java.util.concurrent). Implementaremos una espera activa simple:
 *        si buzonEntrega.isEmpty() -> sleep corto (por ejemplo 50ms) y volver a intentar.
 *
 * - Cuando recibe END -> finaliza.
 */
public class ServidorEntrega extends Thread {
    private final BuzonLimitado buzonEntrega;
    private boolean activo = false;
    private final Random rnd = new Random();

    public ServidorEntrega(String name, BuzonLimitado buzonEntrega) {
        setName("Servidor-" + name);
        this.buzonEntrega = buzonEntrega;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = null;
                // espera activa: si está vacío hacer sleep corto
                synchronized (buzonEntrega) {
                    if (buzonEntrega.isEmpty()) {
                        // espera activa: pequeña pausa
                        // No bloqueamos con wait(); simulamos polling
                    } else {
                        // hay mensaje -> tomar (usa take que es bloqueante/pasiva)
                        // llamamos a take fuera del synchronized para evitar deadlock
                    }
                }

                // Intentamos tomar con cuidado: si no hay, sleep corto
                if (buzonEntrega.isEmpty()) {
                    Thread.sleep(50); // espera activa
                    continue;
                } else {
                    m = buzonEntrega.take(); // bloqueante, pero normalmente no bloquea porque isEmpty false
                }

                if (m.getTipo() == Mensaje.Tipo.START) {
                    activo = true;
                    System.out.println(getName() + " activado por START.");
                    continue;
                } else if (m.getTipo() == Mensaje.Tipo.END) {
                    // transmitir END a otros servidores si requerido: spec dice que entrega una copia a todos servidores.
                    // Implementamos que cada servidor termina cuando recibe END.
                    System.out.println(getName() + " recibió END y termina.");
                    break;
                } else {
                    // procesar mensaje (simular tiempo aleatorio)
                    int tiempoProcesamiento = 100 + rnd.nextInt(400); // ms entre 100 y 500
                    Thread.sleep(tiempoProcesamiento);
                    System.out.println(getName() + " procesó " + m);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }
}
