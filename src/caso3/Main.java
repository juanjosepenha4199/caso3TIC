package src.caso3;
import java.util.ArrayList;
import java.util.List;

/**
 * Main.java
 * Clase principal que:
 * - Lee parámetros (aquí usamos valores hardcode para ejemplo; puedes leer de archivo).
 * - Crea buzones, instancias y threads.
 * - Arranca y espera terminación.
 *
 * NOTA: Debes adaptar la lectura de archivo para cumplir con la especificación (archivo texto).
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Ejemplo de configuración (cambiar por parseo de archivo)
        Configuracion cfg = new Configuracion(
                3,      // numClientes
                5,      // mensajesPorCliente
                2,      // numFiltros
                2,      // numServidores
                5,      // capacidadBuzonEntrada
                10      // capacidadBuzonEntrega
        );

        System.out.println("Iniciando sistema con: " + cfg);

        // buzones
        BuzonLimitado buzonEntrada = new BuzonLimitado(cfg.capacidadBuzonEntrada);
        BuzonLimitado buzonEntrega = new BuzonLimitado(cfg.capacidadBuzonEntrega);
        BuzonCuarentena buzonCuarentena = new BuzonCuarentena();

        // coordinador entre filtros
        Coordinador coordenador = new Coordinador(cfg.numClientes);

        // crear y arrancar manejador de cuarentena
        ManejadorCuarentena manejador = new ManejadorCuarentena(buzonCuarentena, buzonEntrega);
        manejador.start();

        // crear y arrancar filtros
        List<FiltroSpam> filtros = new ArrayList<>();
        for (int i = 1; i <= cfg.numFiltros; i++) {
            FiltroSpam f = new FiltroSpam(String.valueOf(i), buzonEntrada, buzonCuarentena, buzonEntrega, cfg.numClientes, coordenador);
            filtros.add(f);
            f.start();
        }

        // crear y arrancar servidores
        List<ServidorEntrega> servidores = new ArrayList<>();
        for (int i = 1; i <= cfg.numServidores; i++) {
            ServidorEntrega s = new ServidorEntrega(String.valueOf(i), buzonEntrega);
            servidores.add(s);
            s.start();
        }

        // crear y arrancar clientes
        List<ClienteEmisor> clientes = new ArrayList<>();
        for (int i = 1; i <= cfg.numClientes; i++) {
            ClienteEmisor c = new ClienteEmisor("C" + i, cfg.mensajesPorCliente, buzonEntrada);
            clientes.add(c);
            c.start();
        }

        // esperar a que todos los clientes terminen
        for (ClienteEmisor c : clientes) c.join();
        System.out.println("Todos los clientes terminaron.");

        // esperar a que filtros terminen
        for (FiltroSpam f : filtros) f.join();
        System.out.println("Todos los filtros terminaron.");

        // deposit END en entrega en caso de que no se haya depositado
        // (el coordinador y filtros deberían haberlo hecho, pero por seguridad:)
        synchronized (coordenador) {
            if (!coordenador.todosEndsRecibidos()) {
                // improbable si clientes terminaron bien, pero aquí por seguridad
                try {
                    buzonEntrega.put(Mensaje.end());
                    buzonCuarentena.put(Mensaje.end());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }

        // esperar manejador de cuarentena
        manejador.join();
        System.out.println("Manejador de cuarentena terminó.");

        // ahora depositar un END por cada servidor para asegurar que terminan (si no recibieron copia)
        // según enunciado: buzón de entrega debe "copiar" mensaje de END a todos los servidores.
        // Implementamos: depositamos N ENDs para servidores (o, alternativamente, un END y los servidores terminan cuando lo reciben)
        for (int i = 0; i < cfg.numServidores; i++) {
            buzonEntrega.put(Mensaje.end());
        }

        // esperar servidores
        for (ServidorEntrega s : servidores) s.join();
        System.out.println("Todos los servidores terminaron.");

        // chequeo final: todos los buzones vacíos
        System.out.println("Buzón entrada size: " + buzonEntrada.size());
        System.out.println("Buzón cuarentena size: " + buzonCuarentena.size());
        System.out.println("Buzón entrega size: " + buzonEntrega.size());
        System.out.println("Simulación finalizada correctamente.");
    }
}
