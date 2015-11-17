/* 	Violeta Macaya 70822226-M	
 	Sara Rodriguez 70909142-G
 */


package services;

public class Mensaje implements Comparable<Mensaje>{

	String id;
	String contenido;
	float orden;
	String estado;
	int numPropuestas;

	public Mensaje (String _contenido, String _id, float _orden, String _estado, int _numPropuestas) {		
		id = _id;
		contenido = _contenido;
		orden = _orden;
		estado = _estado;
		numPropuestas = _numPropuestas;
	}


	public void setId(String _id){
		id=_id;
	}
	public void setContenido(String _contenido){
		contenido=_contenido;
	}
	public void setOrden(float _orden){
		orden=_orden;
	}
	public void setEstado(String _estado){
		estado=_estado;
	}
	public void setNumPropuestas(int _numPropuestas){
		numPropuestas = _numPropuestas;
	}

	public String getId(){
		return id;
	}
	public String getContenido(){
		return contenido;
	}
	public float getOrden(){
		return orden;
	}
	public String getEstado(){
		return estado;
	}
	public int getNumPropuestas(){
		return numPropuestas;
	}

	@Override
	public int compareTo(Mensaje o) {
		if (orden < o.orden) {
			return -1;
		}
		if (orden > o.orden) {
			return 1;
		}
		return 0;
	}
}
