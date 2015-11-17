/* 	Violeta Macaya 70822226-M	
 	Sara Rodriguez 70909142-G
 */

package services;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.inject.Singleton;

@Path("server")
@Singleton
public class Server extends Thread implements Runnable 
{

	public ArrayList<Mensaje> cola;
	int numServidor;
	int numMensajes;
	String[] procesos;
	float orden;
	Log log;
	String recibirMensaje, recibirAcuerdo;
	int modoEjecucion;
	URI uri;
	Client client=ClientBuilder.newClient();
	WebTarget target;
	int iniciados = 0;

	public Server(int numServidor){
		this.numServidor=numServidor;
	}

	public Server(){
		this.numServidor = -1; //Si no se le pasan argumentos, peta
	}

	@GET
	@Path("/inicializar")
	@Produces(MediaType.TEXT_PLAIN)
	public String inicializar(@DefaultValue("0") @QueryParam(value="numServidor") int numServidor,@DefaultValue("1") @QueryParam(value="modoEjecucion") int modoEjecucion, @DefaultValue("0") @QueryParam(value="numMensajes")int numMensajes, @DefaultValue("0") @QueryParam(value="serverList")String serverList ) {

		this.modoEjecucion = modoEjecucion;
		this.numServidor=numServidor;
		this.numMensajes=numMensajes;
		procesos = serverList.split("-");
		cola = new ArrayList<Mensaje>();

		this.orden =(float)numServidor/(float)10;

		log = new Log();
		log.escribir("Nueva ejecución del programa", numServidor);

		return "Proceso inicializado";
	}

	@GET
	@Path("/arrancar")
	@Produces(MediaType.TEXT_PLAIN)
	public String arrancar(){
		this.start();
		return "Proceso arrancado";
	}
	public void run(){

		Mensaje mensaje;
		for(int i = 0; i<numMensajes; i++){
			String contenido = "P"+this.numServidor+" "+i;
			String idMensaje = this.numServidor+"-"+i;

			mensaje = new Mensaje(contenido,idMensaje,this.orden,"PROVISIONAL",1);

			multidifundir(mensaje);

		}
	}
	public  void multidifundir(Mensaje mensaje){

		if(modoEjecucion == 0){
			cola.add(mensaje);
			//			for(Mensaje aux : cola){
			//				System.out.print("P"+numServidor+": La cola al a�adir mensajes propios: "+aux.id+" con orden " +aux.orden + " " +aux.estado +"\n ");
			//			}
			//			System.out.println(" ");
		}
		esperarTiempoAleatorio(100,150);
		//En la primera fase multidifunde el primer mensaje
		for(int i=0; i<procesos.length; i++){
			uri=UriBuilder.fromUri("http://" + procesos[i]+"/rest/server/mensaje").build();
			target =  client.target(uri);

			target.queryParam("contenido", mensaje.contenido).queryParam("idMensaje", mensaje.id).queryParam("orden", mensaje.orden).request(MediaType.TEXT_PLAIN).get(String.class);
			esperarTiempoAleatorio(20,50);
		}
	}


	@GET
	@Path("mensaje")
	@Produces(MediaType.TEXT_PLAIN)
	public String mensaje(@DefaultValue("0") @QueryParam(value="contenido") String contenido, @DefaultValue("0") @QueryParam(value="idMensaje") String idMensaje, @DefaultValue("0") @QueryParam(value="orden") float orden) {

		if(this.modoEjecucion == 0){
			this.orden ++;
			//System.out.println("P"+numServidor+" El nuevo orden propio al recibir el mensaje "+idMensaje+ " ser�: "+this.orden+"");
			synchronized(cola){
				String id[] = idMensaje.split("-");
				int emisor = Integer.parseInt(id[0]);
				if (emisor != this.numServidor){

					System.out.println("P"+numServidor+" Mensaje a añadir a la cola, emitido por P"+emisor +" hacia P"+this.numServidor+" mensaje con contenido: "+contenido +" y orden "+this.orden);
					cola.add(new Mensaje(contenido, idMensaje, this.orden, "PROVISIONAL", 1));


					//					for(Mensaje aux : cola){
					//						System.out.print("P"+numServidor+": La cola cuando se a�aden otros mensajes: "+aux.id +" con orden "+aux.orden +" "+ aux.estado +"\n ");
					//					}
					//					System.out.println(" ");
				}
			}

			enviarPropuesta(idMensaje, orden);
			return "Propuesta enviada";
		}
		else {
			Mensaje auxMensaje = new Mensaje(contenido, idMensaje, orden, "PROVISIONAL", 1);
			int idLog = this.numServidor;
			log.escribir(auxMensaje.getContenido(), idLog);
			return "Mensaje enviado";
		}
	}


	public void enviarPropuesta(String idMensaje, float ordenORI){

		String aux[] = idMensaje.split("-"); 
		int emisor=Integer.parseInt(aux[0]);
		if (emisor != this.numServidor){
			String direccion = "http://" + procesos[emisor];
			uri=UriBuilder.fromUri(direccion).build();
			target =  client.target(uri);

			System.out.println("P"+numServidor+": Envía la propuesta a P" +emisor +" con el contenido: P"+ idMensaje +" orden: "+ this.orden);
			target.path("rest/server").path("acuerdo").queryParam("orden", this.orden).queryParam("idMensaje", idMensaje).request(MediaType.TEXT_PLAIN).get(String.class);
		}
	}

	@GET
	@Path("acuerdo")
	@Produces(MediaType.TEXT_PLAIN)
	public String acuerdo(@DefaultValue("0") @QueryParam(value="orden") float orden, @DefaultValue("0") @QueryParam(value="idMensaje") String idMensaje) {

		Boolean acuerdo=false;
		Mensaje auxMensaje; 

		String aux[] = idMensaje.split("-"); 
		int emisor=Integer.parseInt(aux[0]);
		for(int i=0; i<cola.size()&&!acuerdo;i++){ //Busca en la cola el mensaje sobre el que se va a hacer el acuerdo.
			synchronized(cola){
				auxMensaje = cola.get(i);
			}
			if(auxMensaje.getId().equals(idMensaje)){

				this.orden +=  1.0;
				float ordenAux = auxMensaje.getOrden();
				ordenAux =(int)Math.max(this.orden, orden);
				ordenAux += (float)numServidor/10;
				auxMensaje.setOrden(ordenAux);
				//System.out.println("P"+numServidor+" El orden del mensaje auxiliar en propuesta es "+auxMensaje.id+ " a a�adir ser�: "+ordenAux+"");

				auxMensaje.setNumPropuestas((auxMensaje.getNumPropuestas())+1);

				if(auxMensaje.getNumPropuestas()==procesos.length){ //Si han llegado todas las propuestas, quiere decir que ya hay acuerdo, que ser� el orden m�s alto que ha llegado. 
					//Hay acuerdo. Solo tiene que multidifundir el acuerdo el proceso que gener� el mensaje original.
					if(emisor == this.numServidor){
						for(int j=0; j<procesos.length; j++){ //Para todos los procesos, multidifunde el acuerdo
							acuerdo=true; //Para salir del for indicando que ya hay acuerdo
							String direccion = "http://" + procesos[j];
							System.out.println("P"+numServidor+" Multidifusion del acuerdo hacia P" +j + " del mensaje " + idMensaje+" con orden "+auxMensaje.getOrden());
							uri=UriBuilder.fromUri(direccion).build();
							target =  client.target(uri);
							target.path("rest/server").path("entregar").queryParam("orden", ordenAux).queryParam("idMensaje", idMensaje).request(MediaType.TEXT_PLAIN).get(String.class);
						}	
					}
				}
			}
		}

		return "Propuesta recibida";
	}


	@GET
	@Path("entregar")
	@Produces(MediaType.TEXT_PLAIN)
	public String entregar(@DefaultValue("0") @QueryParam(value="orden") float orden, @DefaultValue("0") @QueryParam(value="idMensaje") String idMensaje) {
		String aux[] = idMensaje.split("-"); 
		int emisor=Integer.parseInt(aux[0]);
		System.out.println("P"+numServidor+" Recibir acuerdo prodecente de P"+emisor+" con mensaje "+idMensaje+" y orden " +orden);
		boolean encontrado = false;

		for(int i=0; i < cola.size() && !encontrado; i++){
			Mensaje auxMensaje;
			Mensaje auxMensaje2;
			synchronized (cola){
				auxMensaje = cola.get(i);
			}

			if (auxMensaje.getId().equals(idMensaje)){
				encontrado = true;
				auxMensaje.setOrden(orden);
				auxMensaje.setEstado("DEFINITIVO");
				System.out.println("P"+numServidor+" Se ha marcado definitivo el mensaje: "+auxMensaje.id +" con orden "+ auxMensaje.orden);
			}


			synchronized(cola){
				Collections.sort(cola);
			}

			synchronized(cola){
				auxMensaje2=cola.get(0);
			}

			while(auxMensaje2.estado.equals("DEFINITIVO" ) && cola.size() != 0){
				log.escribir(auxMensaje2.contenido +" "+auxMensaje2.orden, this.numServidor);
				synchronized(cola){
					cola.remove(0);
					System.out.println("P"+numServidor+" Se ha eliminado el mensaje: "+auxMensaje2.id +" con orden "+ auxMensaje2.orden);

				}
				if(cola.size() != 0){
					synchronized(cola){
						auxMensaje2 = cola.get(0);
					}
				}
			}
		}

		/*
			//Accediendo a un mensaje
		synchronized(cola){
			for (Mensaje auxMensaje : cola) {
				if (auxMensaje.getId().equals(idMensaje)){
					auxMensaje.setOrden(orden);
					auxMensaje.setEstado("DEFINITIVO");
					System.out.println("P"+numServidor+" El orden del mensaje en acuerdo "+idMensaje+ " a a�adir ser�: "+orden+"");
				}
			}
		}
		synchronized(cola){

			Collections.sort(cola);
		}
		//Realizar entrega
		synchronized(cola){

		for (Mensaje auxMensaje : cola){
				if (auxMensaje.estado.equals("DEFINITIVO")){
					log.escribir(auxMensaje.contenido +" "+auxMensaje.orden, this.numServidor);
					cola.remove(auxMensaje);
					System.out.println("P"+numServidor+" Se ha eliminado el mensaje: "+auxMensaje.id +" con orden "+ auxMensaje.orden);
				}
				break;
			}
		}

			for(Mensaje auxMensaje : cola){
				System.out.print("P"+numServidor+": La cola al eliminar mensajes: "+auxMensaje.id +" con orden "+auxMensaje.orden +" "+ auxMensaje.estado +"\n ");
			}
			System.out.println(" ");

		 */
		return "Mensaje entregado";
	}		




	public void esperarTiempoAleatorio(int inferior, int superior) {
		long tiempo;

		tiempo = (long)(inferior + (Math.random() * (superior - inferior)));
		try {
			Thread.sleep(tiempo);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}