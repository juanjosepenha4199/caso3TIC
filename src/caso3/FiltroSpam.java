package src.caso3;

import java.util.Random;

/**
 * FiltroSpam.java
 * Thread consumidor que toma mensajes del buzón de entrada (espera pasiva).
 * Clasifica los correos como spam o válidos.
 */
public class FiltroSpam extends Thread {

    private final BuzonLimitado buzonEntrada;
    private final BuzonCuarentena buzonCuarentena;
    private final BuzonLimitado buzonEntrega;
    private final int totalClientes;
    private final int totalServidores;
    private final Coordinador coordinador;
    private final Random rnd = new Random();

    // Rango para tiempo de cuarentena (en milisegundos)
    private static final int MIN_QUAR = 10000;
    private static final int MAX_QUAR = 20000;

    public FiltroSpam(String name,
                      BuzonLimitado buzonEntrada,
                      BuzonCuarentena buzonCuarentena,
                      BuzonLimitado buzonEntrega,
                      int totalClientes,
                      int totalServidores,
                      Coordinador coordinador) {

        setName("Filtro-" + name);
        this.buzonEntrada = buzonEntrada;
        this.buzonCuarentena = buzonCuarentena;
        this.buzonEntrega = buzonEntrega;
        this.totalClientes = totalClientes;
        this.totalServidores = totalServidores;
        this.coordinador = coordinador;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Mensaje m = buzonEntrada.take(); // espera pasiva

                switch (m.getTipo()) {
                    case START:
                        coordinador.registrarStart();
                        // Depositar un START por cada servidor para activarlos
                        synchronized (coordinador) {
                            int faltan = coordinador.cuantosStartFaltan(totalServidores);
                            for (int i = 0; i < faltan; i++) {
                                buzonEntrega.put(Mensaje.start());
                                coordinador.registrarStartDepositado(totalServidores);
                            }
                            if (faltan > 0) {
                                System.out.println("  " + getName() + " → Deposita " + faltan + " mensaje(s) START en buzón de entrega (activando servidores)");
                            }
                        }
                        break;

                    case END:
                        coordinador.registrarEnd();

                        // Si todos los END fueron recibidos y no quedan mensajes
                        synchronized (coordinador) {
                            if (coordinador.condicionesParaFin(buzonEntrada, buzonCuarentena)
                                    && coordinador.debeDepositarEndEntrega()) {
                                System.out.println("  " + getName() + " → Deposita mensaje END final en entrega y cuarentena (iniciando terminación)");
                                buzonEntrega.put(Mensaje.end());
                                buzonCuarentena.put(Mensaje.end());
                                return; // termina este filtro
                            }
                        }
                        // Si recibimos todos los ENDs pero la cuarentena no está vacía,
                        // continuar verificando periódicamente hasta que se pueda terminar
                        if (coordinador.todosEndsRecibidos()) {
                            // Verificar periódicamente si podemos terminar
                            while (true) {
                                synchronized (coordinador) {
                                    // Si otro filtro ya depositó el END, este puede terminar
                                    if (coordinador.yaSeDepositoEnd()) {
                                        return; // termina este filtro
                                    }
                                    // Si las condiciones se cumplen, depositar END y terminar
                                    if (coordinador.condicionesParaFin(buzonEntrada, buzonCuarentena)
                                            && coordinador.debeDepositarEndEntrega()) {
                                        System.out.println("  " + getName() + " → Deposita mensaje END final en entrega y cuarentena (iniciando terminación)");
                                        buzonEntrega.put(Mensaje.end());
                                        buzonCuarentena.put(Mensaje.end());
                                        return; // termina este filtro
                                    }
                                }
                                // Esperar un poco antes de verificar nuevamente
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
            m.setTiempoCuarentena(tiempo / 1000); // convertir a segundos
            buzonCuarentena.put(m);
            System.out.println("  " + getName() + " → SPAM detectado → Cuarentena [" + m.getId() + "] (tiempo: " + m.getTiempoCuarentena() + "s)");
        } else {
            buzonEntrega.put(m);
            System.out.println("  " + getName() + " → Correo válido → Entrega [" + m.getId() + "]");
        }
    }
}
