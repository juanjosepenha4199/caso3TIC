package src.caso3;

import java.util.Random;

/**
 * ServidorEntrega.java
 * Thread consumidor del buzón de entrega.
 * Implementa espera activa: revisa constantemente si hay mensajes disponibles.
 */
public class ServidorEntrega extends Thread {

    private final BuzonLimitado buzonEntrega;
    private final Random rnd = new Random();
    private boolean activo = false;

    public ServidorEntrega(String name, BuzonLimitado buzonEntrega) {
        setName("Servidor-" + name);
        this.buzonEntrega = buzonEntrega;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Espera activa: revisa si el buzón tiene mensajes
                if (buzonEntrega.isEmpty()) {
                    Thread.sleep(50); // pausa breve
                    continue;
                }

                Mensaje m = buzonEntrega.take(); // leer mensaje disponible

                if (m.getTipo() == Mensaje.Tipo.START) {
                    activo = true;
                    System.out.println("  " + getName() + " ACTIVADO (recibio START)");
                } else if (m.getTipo() == Mensaje.Tipo.END) {
                    System.out.println("  " + getName() + " Terminando (recibio END)");
                    break;
                } else {
                    if (activo) {
                        procesarCorreo(m);
                    } else {
                        System.out.println("  " + getName() + " Ignora mensaje (aun no activado): [" + m.getId() + "]");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }

    private void procesarCorreo(Mensaje m) throws InterruptedException {
        int tiempo = 100 + rnd.nextInt(400); // procesamiento entre 100–500ms
        Thread.sleep(tiempo);
        String origen = m.isFromCuarentena() ? " (de cuarentena)" : "";
        System.out.println("  " + getName() + " ENTREGADO [" + m.getId() + "]" + origen);
    }
}
