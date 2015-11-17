/* 	Violeta Macaya 70822226-M	
 	Sara Rodriguez 70909142-G
 */


package services;

import java.io.*;

public class Log {
	FileWriter fichero = null;
	PrintWriter pw = null;

	void escribir(String mensaje, int idLog) {
		try {
			fichero = new FileWriter(System.getProperty("user.home") + "/Escritorio/log_"+idLog+".txt",true);
			pw = new PrintWriter(fichero);
			// Escribimos el mensaje y la hora a la que se ha mandado. El mensaje ya contiene el n�mero de proceso y la iteraci�n
			java.util.Date date = new java.util.Date();  
			pw.println(mensaje+"\t "+date);
			pw.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				// Nos aseguramos de que se cierra el fichero
				if (null != fichero)
					fichero.close();
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
