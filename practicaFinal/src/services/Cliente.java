/* 	Violeta Macaya 70822226-M	
 	Sara Rodriguez 70909142-G
 */


package services;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
public class Cliente {

	public static void main(String[] args) {
		int modoEjecucion;
		String[] servidores = null; 
		String serverList = null;
		int numMensajes = 100;

		try {
			BufferedReader br = new BufferedReader(new FileReader("urls.txt"));
			String contenidoFichero;
			while ((contenidoFichero = br.readLine()) != null){
				if (serverList == null)
					serverList=contenidoFichero + "-";
				else
					serverList += contenidoFichero + "-";
			}
			br.close();
			servidores = serverList.split("-");
		}
		catch (Exception e){
			e.printStackTrace();
		}
		int argumento = Integer.parseInt(args[0]);
		switch(argumento) {
			case 1:
				System.out.println("Se ha elegido el modo desordenado");
				modoEjecucion = 1;
				break;
			case 0:
				System.out.println("Se ha elegido el modo ordenado");
				modoEjecucion = 0;
				break;
			default: {
				System.out.println("Parametro incorrecto, se ejecutara desordenado");
				modoEjecucion = 1;
				break;
			}
		}	
		URI uri;

		Client client=ClientBuilder.newClient();
		for (int i=0; i<servidores.length; i++){
			uri=UriBuilder.fromUri("http://"+servidores[i]).build();
			WebTarget target = client.target(uri);

			System.out.println(target.path("rest").path("server").path("inicializar").queryParam("numMensajes", numMensajes).queryParam("modoEjecucion", modoEjecucion).queryParam("serverList", serverList).queryParam("numServidor", i).request(MediaType.TEXT_PLAIN).get
					(String.class));

		}
		for (int i=0; i<servidores.length; i++){
			uri=UriBuilder.fromUri("http://"+servidores[i]).build();
			WebTarget target = client.target(uri);

			System.out.println(target.path("rest").path("server").path("arrancar").request(MediaType.TEXT_PLAIN).get
					(String.class));
		}		


	}

}
