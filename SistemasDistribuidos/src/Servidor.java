import java.io.*;
import java.util.*;
import javax.swing.*;
import java.net.*;

public class Servidor {
    private static final int PUERTO= 9005;
//    protected static final int TIEMPO_DESCONEXION_AUTOMATICA = 600000;
    private ServerSocket socketServidor;

    public static void main(String[] args) {
        new Servidor();
        }
 

    public Servidor() {
        System.out.println("Arrancando el servidor por el puerto " + PUERTO);
        arrancarServidor();
        procesarClientes();
        }

    
    private void arrancarServidor() {
        try {
            socketServidor = new ServerSocket(PUERTO);
            System.out.println("El servidor está en marcha: escucha por el puerto " + PUERTO);
            }
        catch (java.net.BindException e1) {
            String mensaje = "No puede arrancarse el servidor por el puerto " + PUERTO +
            ". Seguramente, el puerto está ocupado.";
            errorFatal(e1, mensaje);
            }
        catch (java.lang.SecurityException e2) {
            String mensaje = "No puede arrancarse el servidor por el puerto " + PUERTO +
            ". Seguramente, hay restricciones de seguridad.";
            errorFatal(e2, mensaje);
            }
        catch (IOException e3) {
            String mensaje = "No puede arrancarse el servidor por el puerto " + PUERTO;
            errorFatal(e3, mensaje);
            }
        }

    private void procesarClientes() {
        Socket socketCliente = null; 
        while (true) {
            try {
                socketCliente = socketServidor.accept();
                try {
                    new ThreadServidor(socketCliente);
                    }
                catch (IOException e1) {
                    if (socketCliente != null) {
                        try {
                            socketCliente.close();
                            }
                        catch (IOException e2) {}
                        }
                    }
                } 
            catch (java.lang.SecurityException e3) {
                if (socketServidor != null) {
                    try {
                        socketServidor.close();
                        }
                    catch (IOException e4) {}
                    }
                String mensaje = "Con su configuración de seguridad, los clientes no pueden " +
                "conectarse por el puerto " + PUERTO;
                errorFatal(e3, mensaje);
                }
            catch (IOException e5) {
                }
            }
        }

    private static void errorFatal(Exception excepcion, String mensajeError) {
        excepcion.printStackTrace();
        JOptionPane.showMessageDialog(null, "Error fatal."+ System.getProperty("line.separator") +
        mensajeError, "Información para el usuario", JOptionPane.WARNING_MESSAGE);
        System.exit(-1);
        }
    }


class ThreadServidor extends Thread {
    private String nombreCliente;
    private String grupoCliente; 
    private static List clientesActivos = new ArrayList();
    private static List gruposActivos = new ArrayList();

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    public ThreadServidor(Socket socket) throws IOException {
        this.socket = socket;
        PrintWriter salidaArchivo = null;
        salida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        start();
        }
    
    public void run() {
        String textoUsuario = "";
        String Tienda = "";
        String textoTienda = "";
        String Grupo = "";
        String textoGrupo = "";
        String ClientesGrupo = "";
        try {
            salida.println("> Bienvenido a este chat.");
            salida.println("> Introduzca su nombre:");
            nombreCliente = (entrada.readLine()).trim();
            if ( (nombreCliente.equals("")) || (nombreCliente == null) ) {
                nombreCliente = "Invitado";
                }
          
        
            
            Iterator it = clientesActivos.iterator();
            while (it.hasNext()) {
                if (nombreCliente.equals(( (ThreadServidor) it.next()).nombreCliente)) {
                    nombreCliente = nombreCliente + socket.getPort();
                    break;
                    }
                }
            
            
            salida.println("> Introduzca su Grupo:");
            grupoCliente = (entrada.readLine()).trim();
            anyadirConexion(this);
            salida.println("> Se le asignado el alias de " + nombreCliente);
            salida.println(" --- MENU ---");
            salida.println("Menu de isabel");
            salida.println("1. Consultar puntos de venta conectados");
            salida.println("2. Enviar mensaje a otra tienda");
            salida.println("3. Enviar mensaje a todas las tiendas");
            salida.println("4. Enviar mensaje a un grupo");
            salida.println("5. Salir");
            salida.println("Selecciona una opcion:");
//            socket.setSoTimeout(Servidor.TIEMPO_DESCONEXION_AUTOMATICA);
            while ( (textoUsuario=entrada.readLine()) != null ) {
                if ((textoUsuario.equals("1"))) {
                    salida.println("CLIENTES CONECTADOS");
                    escribirCliente(this,"" + listarClientesActivos());
                }
                else if ((textoUsuario.equals("2"))) {
                    salida.println("Escriba el Nombre de la tienda");
                    if ((Tienda=entrada.readLine()) != null)
                    {
                         salida.println("Escriba el texto a enviar:");
                         if ((textoTienda=entrada.readLine()) != null) {
                            escribirAUno("Tienes un mensaje de "+nombreCliente+"> "+ textoTienda,Tienda);
                         }
                    }
                }
                else if ((textoUsuario.equals("3"))) {
                    salida.println("Escriba el texto a enviar:");
                    if ((textoUsuario=entrada.readLine()) != null )
                    escribirATodos(nombreCliente+"> "+ textoUsuario);
                    }

                else if ((textoUsuario.equals("4"))) {
                    salida.println("Escriba el Nombre del Grupo");
                    if ((Grupo=entrada.readLine()) != null)
                    {
                         salida.println("Escriba el texto a enviar:");
                         if ((textoGrupo=entrada.readLine()) != null) {
                            escribirAGrupo("Tienes un mensaje de "+nombreCliente+"> "+ textoGrupo,Grupo);
                         }
                    }
                }
                else if ((textoUsuario.equals("5"))) {
                    salida.println("> ***********HASTA LA VISTA****************");
                    break;
                    }

            salida.println(" --- MENU ---");
            salida.println("1. Consultar puntos de venta conectados");
            salida.println("2. Enviar mensaje a otra tienda");
            salida.println("3. Enviar mensaje a todas las tiendas");
            salida.println("4. Enviar mensaje a un grupo");
            salida.println("5. Salir");
            salida.println("Selecciona una opcion:");

                }
            }
        catch (java.io.InterruptedIOException e1) {
            escribirCliente(this, "> "+ "***************************************");
            escribirCliente(this, "> "+ "Se le pasó el tiempo: Conexión cerrada");
            escribirCliente(this, "> "+ "Si desea continuar, abra otra sesión");
            escribirCliente(this, "> "+ "*****************ADIOS*****************");
            // Se registra la desconexión por inactividad.
            }
        catch (IOException e2) {
            }
        finally {
            eliminarConexion(this);
            limpiar();
            } 
        }

    private void limpiar() {
        if ( entrada != null ) {
            try {
                entrada.close();
                }
            catch (IOException e1) {}
            entrada = null;
            }
        if ( salida != null ) {
            salida.close();
            salida = null;
            }
        if ( socket != null ) {
            try {
                socket.close();
                }
            catch (IOException e2) {}
            socket = null;
            }
        }

    private static synchronized void eliminarConexion(ThreadServidor threadServidor) {
        clientesActivos.remove(threadServidor);
        }

    private static synchronized void anyadirConexion(ThreadServidor threadServidor) {
        clientesActivos.add(threadServidor);
        }

    private synchronized void escribirATodos(String textoUsuario) {
        Iterator it = clientesActivos.iterator();
        while (it.hasNext()) {
            ThreadServidor tmp = (ThreadServidor) it.next();
            if ( !(tmp.equals(this)) )
                escribirCliente(tmp, textoUsuario);
            }
        }

    private synchronized void escribirAUno(String textoUsuario, String Tienda) {
        Iterator it = clientesActivos.iterator();
        while (it.hasNext()) {
            ThreadServidor tmp = (ThreadServidor) it.next();
            if ( Tienda.equals(tmp.nombreCliente)) {
                escribirCliente(tmp, textoUsuario);
                break;
            }
        }
    }
    
    private synchronized void Validar(String textoUsuario,String Tienda) {
        Iterator it = clientesActivos.iterator();
        while (it.hasNext()) {
            ThreadServidor tmp = (ThreadServidor) it.next();
            if ( Tienda.equals(tmp.nombreCliente)) {
                escribirCliente(tmp, textoUsuario);
                break;
            }
        }
        
    }


    private synchronized void escribirAGrupo(String textoUsuario, String Grupo) {
        Iterator it = clientesActivos.iterator();
        while (it.hasNext()) {
            ThreadServidor tmp = (ThreadServidor) it.next();
            if ( Grupo.equals(tmp.grupoCliente)) {
                escribirCliente(tmp, textoUsuario);
            }
        }
    }

    private synchronized void escribirCliente(ThreadServidor threadServidor, String textoUsuario) {
        (threadServidor.salida).println(textoUsuario);
        }

    private static synchronized StringBuffer listarClientesActivos() {
        StringBuffer cadena = new StringBuffer();
        for (int i = 0; i < clientesActivos.size(); i++) {
            ThreadServidor tmp = (ThreadServidor) (clientesActivos.get(i));
            cadena.append(i+1).append(".- ").append((((ThreadServidor) clientesActivos.get(i)).nombreCliente)).append("\n") ;
            }
        return cadena;
        }


    private static synchronized StringBuffer enumerarClientesActivos() {
        StringBuffer cadena = new StringBuffer();
        for (int i = 0; i < clientesActivos.size(); i++) {
            ThreadServidor tmp = (ThreadServidor) (clientesActivos.get(i));
            cadena.append(i+1).append(".-  ").append((((ThreadServidor) clientesActivos.get(i)).nombreCliente)).append("\n") ;
            }
        return cadena;
        }
    private static synchronized StringBuffer ClienteExistente() {
        StringBuffer cadena = new StringBuffer();
        for (int i = 0; i < clientesActivos.size(); i++) {
            ThreadServidor tmp = (ThreadServidor) (clientesActivos.get(i));
            cadena.append(i+1).append(".-  ").append((((ThreadServidor) clientesActivos.get(i)).nombreCliente)).append("\n") ;
            }
   
        return cadena;
    
    }
    
   
    
    }