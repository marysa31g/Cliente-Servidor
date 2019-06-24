import java.net.*;
import java.io.*;
import java.awt.*;
import java.util.Scanner;
import javax.swing.*;

public class Cliente {

    private static int PUERTO;
    private static String IP = "";
    private Socket socketCliente;
    Scanner salida = new Scanner(System.in);

    public static void main(String args[]) {
        new Cliente(args);
    }
//9005

    public Cliente(String args[]) {

        System.out.println("Arrancando el servidor por el puerto " + PUERTO);
        System.out.println("Arrancando el cliente.");
        arrancarCliente(args);
        procesarMensajes();
    }

    private void arrancarCliente(String[] args) {
        try {

            System.out.println("Introduce IP:");
            IP = salida.nextLine();
            System.out.println("Introduce Puerto:");
            PUERTO = salida.nextInt();
            if (args.length == 2) {
                socketCliente = new Socket(args[0], Integer.parseInt(args[1]));
            } else {
                socketCliente = new Socket(IP, PUERTO); // puerto del servidor por omisión
            }
            System.out.println("Arrancado el cliente.");
        } catch (java.lang.NumberFormatException e1) {
            errorFatal(e1, "Número de puerto inválido.");
        } catch (java.net.UnknownHostException e2) {
            errorFatal(e2, "No se localiza el ordenador servidor con ese nombre.");
        } catch (java.lang.SecurityException e3) {
            String mensaje = "Hay restricciones de seguridad en el servidor para conectarse por el "
                    + "puerto " + PUERTO;
            errorFatal(e3, mensaje);
        } catch (IOException e4) {
            String mensaje = "No se puede conectar con el puerto " + PUERTO + " de la máquina "
                    + "servidora. Asegúrese de que el servidor está en marcha.";
            errorFatal(e4, mensaje);
        }
    }

    private void procesarMensajes() {
        BufferedReader entrada = null;
        PrintWriter salida = null;
        try {
            entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salida = new PrintWriter(socketCliente.getOutputStream(), true);
            BufferedReader entradaConsola = new BufferedReader(new InputStreamReader(System.in));
            new ThreadCliente(entrada);
            while (true) {
                salida.println(entradaConsola.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (Exception e1) {
                    entrada = null;
                }
            }
            if (salida != null) {
                try {
                    salida.close();
                } catch (Exception e1) {
                    salida = null;
                }
            }
            if (socketCliente != null) {
                try {
                    socketCliente.close();
                } catch (Exception e1) {
                    socketCliente = null;
                }
            }
            String mensaje = "Se ha perdido la comunicación con el servidor. Seguramente se debe a "
                    + " que se ha cerrado el servidor o a errores de transmisión";
            errorFatal(e, mensaje);
        }
    }

    private static void errorFatal(Exception excepcion, String mensajeError) {
        excepcion.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fatal." + System.getProperty("line.separator")
                + mensajeError, "Información para el usuario", JOptionPane.WARNING_MESSAGE);
        System.exit(-1);
    }
}

class ThreadCliente extends Thread {

    private BufferedReader entrada;

    public ThreadCliente(BufferedReader entrada) throws IOException {
        this.entrada = entrada;
        start();
    }

    public void run() {
        String fin1 = "> *****************ADIOS*****************";
        String fin2 = "> ***********HASTA LA VISTA****************";
        String linea = null;
        try {
            while ((linea = entrada.readLine()) != null) {
                System.out.println(linea);
                if (linea.equals(fin1) || linea.equals(fin2)) {
                    break;
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if (entrada != null) {
                try {
                    entrada.close();
                } catch (IOException e2) {
                }
            }
            System.exit(-1);
        }
    }
}
