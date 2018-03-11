package Servidor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Esta clase implementa el servidor que atiende a los clientes. El servidor 
 * esta implemntado como un pool de threads. Cada vez que un cliente crea
 * una conexion al servidor, un thread se encarga de atenderlo el tiempo que
 * dure la sesion. 
 * Infraestructura Computacional Universidad de los Andes. 
 * Las tildes han sido eliminadas por cuestiones de compatibilidad.
 * 
 * @author Sebastian Sanchez y Luis Mesa
 */
public class Servidor 
{
	private static final int numTransacciones = 1;//TODO: Establecer numero max de conexiones
	private static final int TIME_OUT = 100000;//TODO: Establecer timeout

	/**
	 * Constante que especifica el numero de threads que se usan en el pool de conexiones.
	 */

	//Escoger numero de threads (No es necesario cambiar)

	//Escenario 1
	public static final int N_THREADS = 1;

	//Escenario 2
	//public static final int N_THREADS = 2;

	//Escenario 3
	//public static final int N_THREADS = 8;

	/**
	 * Puerto en el cual escucha el servidor. 
	 |*/
	public static final int PUERTO = 1337;

	/**
	 * El socket que permite recibir requerimientos por parte de clientes.
	 */
	private static ServerSocket elSocket;
	private static Servidor elServidor;

	/**
	 * Metodo main del servidor con seguridad que inicializa un 
	 * pool de threads determinado por la constante nThreads.
	 * @param args Los argumentos del metodo main (vacios para este ejemplo).
	 * @throws IOException Si el socket no pudo ser creado.
	 */
	private ExecutorService executor = Executors.newFixedThreadPool(N_THREADS);
	
	
	
	public static void main(String[] args) throws IOException {
		elServidor = new Servidor();
		elServidor.runServidor();
	}

	

	private void runServidor() {

		int num = 0;
		try {
			// Crea el socket que escucha en el puerto seleccionado.
			elSocket = new ServerSocket(PUERTO);
			System.out.println("Servidor Coordinador escuchando en puerto: " + PUERTO);
			System.out.println("Numero maximo de conexiones: "+ numTransacciones);

			while (num!=numTransacciones) 
			{
				Socket sThread = null;
				// ////////////////////////////////////////////////////////////////////////
				// Recibe conexiones de los clientes
				// ////////////////////////////////////////////////////////////////////////
				sThread = elSocket.accept();
				sThread.setSoTimeout(TIME_OUT);
				System.out.println("Thread " + num + " recibe a un cliente.");

				Worker w = new Worker(num,sThread);
				executor.submit(w);

				num++;
			}
		} 
		catch (Exception e) 
		{
			// No deberia alcanzarse en condiciones normales de ejecucion.
			e.printStackTrace();
		}
	}



}

