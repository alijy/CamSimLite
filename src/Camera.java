
/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class Camera {
	
	String id;
	Double x,y;
	Double[] zooms;
	Double fixedZoom;
	
	/*
	 * Constructor
	 */
	public Camera (String id, Double x, Double y, Double[] zooms) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.zooms = zooms;
	}
		
}
