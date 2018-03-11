package Servidor;


import java.awt.FontFormatException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
//import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * Esta clase implementa el protocolo que se realiza al recibir una conexión de un cliente.
 * Infraestructura Computacional Universidad de los Andes. 
 * Las tildes han sido eliminadas por cuestiones de compatibilidad.
 * 
 * @author Sebastian Sanchez y Luis Mesa
 */
public class Worker implements Runnable
{

	// ----------------------------------------------------
	// CONSTANTES DE CONTROL DE IMPRESION EN CONSOLA
	// ----------------------------------------------------
	public static final boolean SHOW_ERROR = true;
	public static final boolean SHOW_S_TRACE = true;
	public static final boolean SHOW_IN = true;
	public static final boolean SHOW_OUT = true;
	// ----------------------------------------------------
	// CONSTANTES PARA LA DEFINICION DEL PROTOCOLO
	// ----------------------------------------------------
	public static final String OK = "OK";
	public static final String SEPARADOR = ":";
	public static final String HOLA = "HOLA";
	public static final String INIT = "INIT";
	public static final String RTA = "RTA";
	public static final String INFO = "INFO";
	public static final String ERROR = "ERROR";
	public static final String ERROR_FORMATO = "Error en el formato. Cerrando conexion";

	private int id;
	private Socket ss;

	//tamano original: 146.988
	private int bufferSize = 146988;//TODO Establecer tamano del buffer

	//tamano original: 100.000
	private int tamanoFragmento = 200000; //TODO Establecer tamano fragmento

	private String numArchivo;

	public Worker(int pId, Socket pSocket)
	{
		id = pId;
		ss = pSocket;
		// Adiciona la libreria como un proveedor de seguridad.
		// Necesario para crear llaves.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	/**
	 * Metodo que se encarga de imprimir en consola todos los errores que se 
	 * producen durante la ejecuación del protocolo. 
	 * Ayuda a controlar de forma rapida el cambio entre imprimir y no imprimir este tipo de mensaje
	 */
	private static void printError(Exception e)
	{
		if (SHOW_ERROR)
			System.out.println(e.getMessage());
		if (SHOW_S_TRACE)
			e.printStackTrace();
	}

	/**
	 * Metodo que se encarga de leer los datos que envia el punto de atencion.
	 *  Ayuda a controlar de forma rapida el cambio entre imprimir y no imprimir este tipo de mensaje
	 */
	private String read(BufferedReader reader) throws IOException
	{
		String linea = reader.readLine();
		if (SHOW_IN)
			System.out.println("Thread " + id + "<<CLNT: (recibe) " + linea);
		return linea;

	}

	/**
	 * Metodo que se encarga de escribir los datos que el servidor envia el punto de atencion.
	 *  Ayuda a controlar de forma rapida el cambio entre imprimir y no imprimir este tipo de mensaje
	 */
	private void write(PrintWriter writer, String msg)
	{
		writer.println(msg);
		if (SHOW_OUT)
			System.out.println("Srv " + id + ">>SERV (envia): " + msg);
	}

	public void run()
	{

		//System.out.println("Inicio de nuevo thread." + id);
		try
		{
			PrintWriter escritor = new PrintWriter(ss.getOutputStream(), true);

			BufferedReader lector = new BufferedReader(new InputStreamReader(ss.getInputStream()));

			procesar(lector, escritor);

			escritor.close();

			lector.close();

			ss.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void procesar(BufferedReader pIn, PrintWriter pOut) throws IOException
	{
		String inputLine, outputLine;

		int estado = 0;

		while (estado < 3)
		{
			switch (estado)
			{
			case 0:
				//Enviar CONECTADO AL SERVIDOR.
				outputLine = "CONECTADO AL SERVIDOR - Envie HOLA";
				pOut.println(outputLine);

				//Recibir HOLA.
				inputLine = pIn.readLine();
				System.out.println("Cliente " + id + ": " + inputLine);

				//Enviar Lista.

//				outputLine = "Por favor escriba el numero del archivo que quiere descargar:\n"+
//						"1. Pequeno 5MB\n"+
//						"2. Mediano 20MB\n"+
//						"3. Grande 50MB\n"+
//						"ya";

				outputLine = "Por favor escriba el numero del archivo que quiere descargar:\n";
				File data = new File("./data");
				int i = 1;
				for (File archivo : data.listFiles())
				{
					if (!archivo.isDirectory())
					{
						String cadena =i+"%%%"+archivo.getName()+"%%%"+((((double)archivo.length())/1048576L)+"").substring(0,5)+"MB\n";
						i++;
						outputLine+=cadena;
					}
				}
				outputLine+="ya";
				pOut.println(outputLine);

				//Enviar BUFFERSIZE
				ss.setSendBufferSize(bufferSize);
				//System.out.println("Tamano del buffer: "+bufferSize);
				pOut.println(Integer.toString(bufferSize));

				//Enviar BUFFERSIZE
				//System.out.println("Tamano de fragmento: "+tamanoFragmento);
				pOut.println(Integer.toString(tamanoFragmento));

				estado++;

				break;

			case 1:
				//Recibir Numero

				inputLine = pIn.readLine();
				System.out.println("Cliente " + id + ": Quiero archivo " + inputLine);
				int numArchivo=Integer.parseInt(inputLine);
//				if (inputLine.equalsIgnoreCase("1"))
//				{
//					numArchivo = "1";
//				}
//				else if (inputLine.equalsIgnoreCase("2"))
//				{
//					numArchivo = "2";
//				}
//				else if (inputLine.equalsIgnoreCase("3"))
//				{
//					numArchivo = "3";
//				}

				//////////////////////////////////////////////////////////////////////////
				//ENVIAR ARCHIVO - INICIO
				//////////////////////////////////////////////////////////////////////////
				data = new File("./data");
				
//				String ruta = "./data/Archivo" + numArchivo + ".pdf";
				
				//Specify the file
				File file = data.listFiles()[numArchivo];
				FileInputStream fis = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(fis);

				//Get socket's output stream
				OutputStream os = ss.getOutputStream();

				//Read File Contents into contents array 
				byte[] contents;

				long fileLength = file.length();
				long current = 0;

				while (current != fileLength)
				{
					int size = tamanoFragmento;
					if (fileLength - current >= size)
					{
						current += size;
					}
					else
					{
						size = (int) (fileLength - current);
						current = fileLength;
					}
					contents = new byte[size];
					bis.read(contents, 0, size);
					os.write(contents);
					//System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
				}

				os.flush();

				System.out.println("Adios Cliente " + id);

				//////////////////////////////////////////////////////////////////////////  
				//ENVIAR ARCHIVO - FIN	
				//////////////////////////////////////////////////////////////////////////

				estado = 1;

				break;

			default:
				outputLine = "ERROR";
				pOut.println(outputLine);
				estado = 0;
				break;
			}
		}
	}
}

