package src.caso3;

import java.util.Random;

public class FiltroSpam extends Thread {

    private final BuzonLimitado buzonEntrada;
    private final BuzonCuarentena buzonCuarentena;
    private final BuzonLimitado buzonEntrega;
    private final int totalServidores;
    private final Coordinador coordinador;
    private final Random rnd = new Random();
    private static final int MIN_QUAR = 10000;
    private static final int MAX_QUAR = 20000;

    public FiltroSpam(String name,
                      BuzonLimitado buzonEntrada,
                      BuzonCuarentena buzonCuarentena,
                      BuzonLimitado buzonEntrega,
                      int totalServidores,
                      Coordinador coordinador) {

        setName("Filtro-" + name);
        this.buzonEntrada = buzonEntrada;
        this.buzonCuarentena = buzonCuarentena;
        this.buzonEntrega = buzonEntrega;
        this.totalServidores = totalServidores;
        this.coordinador = coordinador;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = buzonEntrada.take();

                switch (m.getTipo()) {
                    case START:
                        coordinador.registrarStart();
                        synchronized (coordinador) {
                            int faltan = coordinador.cuantosStartFaltan(totalServidores);
                            for (int i = 0; i < faltan; i++) {
                                Mensaje startMsg = Mensaje.start();
                                while (!buzonEntrega.tryPut(startMsg)) {
                                    Thread.sleep(50);
                                }
                                coordinador.registrarStartDepositado(totalServidores);
                            }
                            if (faltan > 0) {
                                System.out.println("  " + getName() + " Deposita " + faltan + " mensaje(s) START en buzon de entrega (activando servidores)");
                            }
                        }
                        break;

                    case END:
                        coordinador.registrarEnd();
                        synchronized (coordinador) {
                            if (coordinador.condicionesParaFin(buzonEntrada, buzonCuarentena)
                                    && coordinador.debeDepositarEndEntrega()) {
                                System.out.println("  " + getName() + " Deposita mensaje END final en entrega y cuarentena (iniciando terminacion)");
                                Mensaje endMsg = Mensaje.end();
                                while (!buzonEntrega.tryPut(endMsg)) {
                                    Thread.sleep(50);
                                }
                                buzonCuarentena.put(endMsg);
                                return;
                            }
                        }
                        if (coordinador.todosEndsRecibidos()) {
                            while (true) {
                                synchronized (coordinador) {
                                    if (coordinador.yaSeDepositoEnd()) {
                                        return;
                                    }
                                    if (coordinador.condicionesParaFin(buzonEntrada, buzonCuarentena)
                                            && coordinador.debeDepositarEndEntrega()) {
                                        System.out.println("  " + getName() + " Deposita mensaje END final en entrega y cuarentena (iniciando terminacion)");
                                        Mensaje endMsg = Mensaje.end();
                                        while (!buzonEntrega.tryPut(endMsg)) {
                                            Thread.sleep(50);
                                        }
                                        buzonCuarentena.put(endMsg);
                                        return;
                                    }
                                }
                                Thread.sleep(100);
                            }
                        }
                        break;

                    case NORMAL:
                        procesarMensajeNormal(m);
                        break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(getName() + " interrumpido.");
        }
    }

    private void procesarMensajeNormal(Mensaje m) throws InterruptedException {
        if (m.isSpamFlag()) {
            int tiempo = MIN_QUAR + rnd.nextInt(MAX_QUAR - MIN_QUAR + 1);
            m.setTiempoCuarentena(tiempo / 1000);
            buzonCuarentena.put(m);
            System.out.println("  " + getName() + " SPAM detectado -> Cuarentena [" + m.getId() + "] (tiempo: " + m.getTiempoCuarentena() + "s)");
        } else {
            while (!buzonEntrega.tryPut(m)) {
                Thread.sleep(50);
            }
            System.out.println("  " + getName() + " Correo valido -> Entrega [" + m.getId() + "]");
        }
    }
}
