package src.caso3;
import java.util.LinkedList;
import java.util.List;

/**
 * BuzonCuarentena.java
 * Buzón ilimitado que almacena mensajes con contador de tiempo.
 *
 * Características:
 * - Los filtros depositan mensajes en cuarentena en espera semiactiva: aquí put()
 *   no bloquea por capacidad (pero notifica al manejador).
 * - El manejador inspecciona periódicamente (cada segundo). Para facilitar la
 *   inspección, se provee un método 'obtenerSnapshot' que devuelve una copia de la lista
 *   para iterar sobre los mensajes y decrementar tiempos de forma segura.
 *
 * Sincronización:
 * - Todas las operaciones sobre la lista interna se sincronizan en 'this'.
 */
public class BuzonCuarentena {
    private final LinkedList<Mensaje> lista = new LinkedList<>();

    public BuzonCuarentena() {}

    // Put: semiactiva -> no espera por capacidad, pero notifica al manejador
    public synchronized void put(Mensaje m) {
        lista.addLast(m);
        notifyAll(); // notificar al manejador que hay algo nuevo
    }

    // Remover un mensaje específico (cuando su tiempo llega a 0 o es descartado)
    public synchronized boolean remove(Mensaje m) {
        boolean removed = lista.remove(m);
        if (removed) notifyAll();
        return removed;
    }

    // Obtener una snapshot para iterar (copia)
    public synchronized List<Mensaje> obtenerSnapshot() {
        return new LinkedList<>(lista);
    }

    public synchronized int size() { return lista.size(); }

    public synchronized boolean isEmpty() { return lista.isEmpty(); }
}
