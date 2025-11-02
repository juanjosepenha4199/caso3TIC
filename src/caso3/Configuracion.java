package src.caso3;

public class Configuracion {
    public final int numClientes;
    public final int mensajesPorCliente;
    public final int numFiltros;
    public final int numServidores;
    public final int capacidadBuzonEntrada;
    public final int capacidadBuzonEntrega;

    public Configuracion(int numClientes, int mensajesPorCliente, int numFiltros,
                         int numServidores, int capacidadBuzonEntrada, int capacidadBuzonEntrega) {
        this.numClientes = numClientes;
        this.mensajesPorCliente = mensajesPorCliente;
        this.numFiltros = numFiltros;
        this.numServidores = numServidores;
        this.capacidadBuzonEntrada = capacidadBuzonEntrada;
        this.capacidadBuzonEntrega = capacidadBuzonEntrega;
    }

    @Override
    public String toString() {
        return "Clientes: " + numClientes + ", Mensajes: " + mensajesPorCliente + 
               ", Filtros: " + numFiltros + ", Servidores: " + numServidores +
               ", Capacidad entrada: " + capacidadBuzonEntrada + 
               ", Capacidad entrega: " + capacidadBuzonEntrega;
    }
}
