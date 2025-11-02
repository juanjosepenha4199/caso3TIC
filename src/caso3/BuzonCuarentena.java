package src.caso3;
import java.util.LinkedList;
import java.util.List;

public class BuzonCuarentena {
    private final LinkedList<Mensaje> lista = new LinkedList<>();

    public BuzonCuarentena() {}

    public synchronized void put(Mensaje m) {
        lista.addLast(m);
        notifyAll();
    }

    public synchronized boolean remove(Mensaje m) {
        boolean removed = lista.remove(m);
        if (removed) notifyAll();
        return removed;
    }

    public synchronized List<Mensaje> obtenerSnapshot() {
        return new LinkedList<>(lista);
    }

    public synchronized int size() { return lista.size(); }

    public synchronized boolean isEmpty() { return lista.isEmpty(); }
}
