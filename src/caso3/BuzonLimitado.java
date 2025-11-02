package src.caso3;
import java.util.LinkedList;
import java.util.Queue;

public class BuzonLimitado {
    private final Queue<Mensaje> cola = new LinkedList<>();
    private final int capacidad;

    public BuzonLimitado(int capacidad) {
        this.capacidad = capacidad;
    }

    public synchronized void put(Mensaje m) throws InterruptedException {
        while (cola.size() >= capacidad) {
            wait();
        }
        cola.add(m);
        notifyAll();
    }

    public synchronized boolean tryPut(Mensaje m) {
        if (cola.size() >= capacidad) {
            return false;
        }
        cola.add(m);
        notifyAll();
        return true;
    }

    public synchronized Mensaje take() throws InterruptedException {
        while (cola.isEmpty()) {
            wait(1000);
            if (cola.isEmpty()) {
                return null;
            }
        }
        Mensaje m = cola.poll();
        notifyAll();
        return m;
    }

    public synchronized int size() { return cola.size(); }
    public synchronized boolean isEmpty() { return cola.isEmpty(); }
    public synchronized boolean isFull() { return cola.size() >= capacidad; }
}
