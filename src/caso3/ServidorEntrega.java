package src.caso3;

import java.util.Random;

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
                if (buzonEntrega.isEmpty()) {
                    Thread.sleep(50);
                    continue;
                }

                Mensaje m = buzonEntrega.take();

                if (m.getTipo() == Mensaje.Tipo.START) {
                    activo = true;
                    System.out.println("  " + getName() + " ACTIVADO");
                } else if (m.getTipo() == Mensaje.Tipo.END) {
                    System.out.println("  " + getName() + " Terminando");
                    break;
                } else {
                    if (activo) {
                        procesarCorreo(m);
                    } else {
                        System.out.println("  " + getName() + " Ignora mensaje: [" + m.getId() + "]");
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }

    private void procesarCorreo(Mensaje m) throws InterruptedException {
        int tiempo = 100 + rnd.nextInt(400);
        Thread.sleep(tiempo);
        String origen = m.isFromCuarentena() ? " (de cuarentena)" : "";
        System.out.println("  " + getName() + " ENTREGADO [" + m.getId() + "]" + origen);
    }
}
