package src.caso3;
/**
 * Buzon.java
 * Interfaz conceptual/abstracta para buzones. Aquí implementaremos
 * dos implementaciones concretas: BuzonLimitado (entrada/entrega)
 * y BuzonCuarentena (ilimitado con acceso iterativo).
 *
 * Las clases concretas manejarán la sincronización con wait/notify.
 */
public interface Buzon {
    void put(Mensaje m) throws InterruptedException;
    Mensaje take() throws InterruptedException;
    int size();
    boolean isEmpty();
}
