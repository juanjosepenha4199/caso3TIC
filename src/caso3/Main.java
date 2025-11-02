package src.caso3;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("========================================");
        System.out.println("  CONFIGURACION DEL SISTEMA");
        System.out.println("========================================");
        
        System.out.print("Numero de clientes: ");
        int numClientes = scanner.nextInt();
        
        System.out.print("Mensajes por cliente: ");
        int mensajesPorCliente = scanner.nextInt();
        
        System.out.print("Numero de filtros de spam: ");
        int numFiltros = scanner.nextInt();
        
        System.out.print("Numero de servidores de entrega: ");
        int numServidores = scanner.nextInt();
        
        System.out.print("Capacidad del buzon de entrada: ");
        int capacidadBuzonEntrada = scanner.nextInt();
        
        System.out.print("Capacidad del buzon de entrega: ");
        int capacidadBuzonEntrega = scanner.nextInt();
        
        scanner.close();

        System.out.println("\n========================================");
        System.out.println("  INICIANDO SISTEMA DE MENSAJERIA");
        System.out.println("========================================");
        System.out.println("Configuracion del sistema:");
        System.out.println("  - Clientes: " + numClientes);
        System.out.println("  - Mensajes por cliente: " + mensajesPorCliente);
        System.out.println("  - Filtros de spam: " + numFiltros);
        System.out.println("  - Servidores de entrega: " + numServidores);
        System.out.println("  - Capacidad buzon entrada: " + capacidadBuzonEntrada);
        System.out.println("  - Capacidad buzon entrega: " + capacidadBuzonEntrega);
        System.out.println("========================================\n");

        BuzonLimitado buzonEntrada = new BuzonLimitado(capacidadBuzonEntrada);
        BuzonLimitado buzonEntrega = new BuzonLimitado(capacidadBuzonEntrega);
        BuzonCuarentena buzonCuarentena = new BuzonCuarentena();
        Coordinador coordinador = new Coordinador(numClientes);

        ManejadorCuarentena manejador = new ManejadorCuarentena(buzonCuarentena, buzonEntrega);
        manejador.start();

        List<FiltroSpam> filtros = new ArrayList<>();
        for (int i = 1; i <= numFiltros; i++) {
            FiltroSpam f = new FiltroSpam(String.valueOf(i), buzonEntrada, buzonCuarentena, buzonEntrega, numServidores, coordinador);
            filtros.add(f);
            f.start();
        }

        List<ServidorEntrega> servidores = new ArrayList<>();
        for (int i = 1; i <= numServidores; i++) {
            ServidorEntrega s = new ServidorEntrega(String.valueOf(i), buzonEntrega);
            servidores.add(s);
            s.start();
        }

        List<ClienteEmisor> clientes = new ArrayList<>();
        for (int i = 1; i <= numClientes; i++) {
            ClienteEmisor c = new ClienteEmisor("C" + i, mensajesPorCliente, buzonEntrada);
            clientes.add(c);
            c.start();
        }

        for (ClienteEmisor c : clientes) c.join();
        System.out.println("\n[OK] Todos los clientes terminaron de enviar mensajes.");

        for (FiltroSpam f : filtros) f.join();
        System.out.println("[OK] Todos los filtros terminaron de procesar.");

        synchronized (coordinador) {
            if (!coordinador.todosEndsRecibidos()) {
                try {
                    buzonEntrega.put(Mensaje.end());
                    buzonCuarentena.put(Mensaje.end());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }

        manejador.join();
        System.out.println("[OK] Manejador de cuarentena termino.");

        for (int i = 0; i < numServidores; i++) {
            buzonEntrega.put(Mensaje.end());
        }

        for (ServidorEntrega s : servidores) s.join();
        System.out.println("[OK] Todos los servidores terminaron.");
        System.out.println("\n========================================");
        System.out.println("  RESUMEN FINAL DEL SISTEMA");
        System.out.println("========================================");
        System.out.println("Estado de los buzones:");
        System.out.println("  - Buzon entrada: " + buzonEntrada.size() + " mensajes");
        System.out.println("  - Buzon cuarentena: " + buzonCuarentena.size() + " mensajes");
        System.out.println("  - Buzon entrega: " + buzonEntrega.size() + " mensajes");
        System.out.println("========================================");
        System.out.println("Proceso terminado exitosamente, vuelta pronto");
        System.out.println("========================================\n");
    }
}
