package src.caso3;
import java.util.LinkedList;
import java.util.Queue;

/**
 * BuzonLimitado.java
 * Buzón con capacidad limitada. Implementa espera pasiva en put (productores
 * esperan si está lleno) y espera pasiva en take (consumidores esperan si vacío).
 *
 * Usado para:
 * - Buzón de entrada (capacidad configurada) -> productores: clientes; consumidores: filtros
 * - Buzón de entrega (capacidad configurada) -> productores: filtros/manejador, consumidores: servidores
 *
 * Implementación simple con LinkedList y synchronized sobre 'this'.
 */
public class BuzonLimitado implements Buzon {
    private final Queue<Mensaje> cola = new LinkedList<>();
    private final int capacidad;

    public BuzonLimitado(int capacidad) {
        this.capacidad = capacidad;
    }

    public synchronized void put(Mensaje m) throws InterruptedException {
        while (cola.size() >= capacidad) {
            wait(); // espera pasiva hasta que haya espacio
        }
        cola.add(m);
        notifyAll(); // despertar consumidores potenciales
    }

    public synchronized Mensaje take() throws InterruptedException {
        while (cola.isEmpty()) {
            wait(); // espera pasiva hasta que haya mensaje
        }
        Mensaje m = cola.poll();
        notifyAll(); // despertar productores potenciales
        return m;
    }

    public synchronized int size() { return cola.size(); }
    public synchronized boolean isEmpty() { return cola.isEmpty(); }
}
