package src.caso3;
import java.util.List;
import java.util.Random;

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
                List<Mensaje> snapshot = buzonCuarentena.obtenerSnapshot();
                for (Mensaje m : snapshot) {
                    if (m.getTipo() == Mensaje.Tipo.END) {
                        if (buzonCuarentena.remove(m)) {
                            System.out.println("  " + getName() + " Proceso END de cuarentena -> Terminando");
                        }
                        running = false;
                        break;
                    }
                    m.decrementarTiempo();
                    int valor = 1 + rnd.nextInt(21);
                    boolean descartar = (valor % 7 == 0);
                    if (m.getTiempoCuarentena() <= 0) {
                        boolean removed = buzonCuarentena.remove(m);
                        if (removed) {
                            if (!descartar) {
                                m.setFromCuarentena(true);
                                while (!buzonEntrega.tryPut(m)) {
                                    Thread.sleep(50);
                                }
                            } else {
                                System.out.println("  " + getName() + " DESCARTADO (malicioso) [" + m.getId() + "]");
                            }
                        }
                    } else {
                        if (descartar) {
                            boolean removed = buzonCuarentena.remove(m);
                            if (removed) {
                                System.out.println("  " + getName() + " DESCARTADO (aleatorio) [" + m.getId() + "]");
                            }
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }
}
