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

        System.out.println("========================================");
        System.out.println("  INICIANDO SISTEMA DE MENSAJERIA");
        System.out.println("========================================");
        System.out.println("Configuración:");
        System.out.println("  - Clientes: " + cfg.numClientes);
        System.out.println("  - Mensajes por cliente: " + cfg.mensajesPorCliente);
        System.out.println("  - Filtros de spam: " + cfg.numFiltros);
        System.out.println("  - Servidores de entrega: " + cfg.numServidores);
        System.out.println("  - Capacidad buzón entrada: " + cfg.capacidadBuzonEntrada);
        System.out.println("  - Capacidad buzón entrega: " + cfg.capacidadBuzonEntrega);
        System.out.println("========================================\n");

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
            FiltroSpam f = new FiltroSpam(String.valueOf(i), buzonEntrada, buzonCuarentena, buzonEntrega, cfg.numClientes, cfg.numServidores, coordenador);
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
        System.out.println("\n[OK] Todos los clientes terminaron de enviar mensajes.");

        // esperar a que filtros terminen
        for (FiltroSpam f : filtros) f.join();
        System.out.println("[OK] Todos los filtros terminaron de procesar.");

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
        System.out.println("[OK] Manejador de cuarentena termino.");

        // ahora depositar un END por cada servidor para asegurar que terminan (si no recibieron copia)
        // según enunciado: buzón de entrega debe "copiar" mensaje de END a todos los servidores.
        // Implementamos: depositamos N ENDs para servidores (o, alternativamente, un END y los servidores terminan cuando lo reciben)
        for (int i = 0; i < cfg.numServidores; i++) {
            buzonEntrega.put(Mensaje.end());
        }

        // esperar servidores
        for (ServidorEntrega s : servidores) s.join();
        System.out.println("[OK] Todos los servidores terminaron.");

        // chequeo final: todos los buzones vacíos
        System.out.println("\n========================================");
        System.out.println("  RESUMEN FINAL DEL SISTEMA");
        System.out.println("========================================");
        System.out.println("Estado de los buzones:");
        System.out.println("  - Buzón entrada: " + buzonEntrada.size() + " mensajes");
        System.out.println("  - Buzón cuarentena: " + buzonCuarentena.size() + " mensajes");
        System.out.println("  - Buzón entrega: " + buzonEntrega.size() + " mensajes");
        System.out.println("========================================");
        System.out.println("[OK] Simulacion finalizada correctamente.");
        System.out.println("========================================\n");
    }
}
