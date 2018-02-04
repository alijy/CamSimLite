import java.util.ArrayList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/

public class Density {

	Field field;
	ArrayList<Camera> cameras;
	ArrayList<Object> objects;
	int zoomCount;
	Double density1;
	Double density2;
	Double density3;
	Double density4_0;
	Double density4_1;
	
	/**
	 * Constructor
	 * @param settings
	 */
	public Density(Settings settings) {
		this.field = settings.field;
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zoomCount = settings.zoomCount;
		
		density1 = computeDensity1();
		density2 = computeDensity2();
		density3 = computeDensity3(1000000);
		density4_0 = computeDensity4_0();
		density4_1 = computeDensity4_1(1000000);
		
	}

	
	/**
	 * (Same as computeDensity4 with an algorithm similar to computeDensity3)
	 * Computes the percentage of the field that is covered by cameras' largest FOV.
	 * In this calculation the overlapping areas of n circles are calculated n time.
	 * @param count is the number of randomly generated points used to compute the density.
	 * @return the ratio of points inside circles to the total number of points.
	 */
	private Double computeDensity4_1(int count) {
		int inPoints = 0;
		RandomNumberGenerator rand = new RandomNumberGenerator(731);
		Double x,y;
		for (int i=0 ; i<count ; i++) {
			x = rand.nextDouble() * (field.maxX - field.minX) + field.minX;
			y = rand.nextDouble() * (field.maxY - field.minY) + field.minY;
			for (int n=0 ; n<cameras.size() ; n++) {
				Camera cam = cameras.get(n);
				if (distance(x,y,cam.x,cam.y) < cam.zooms[zoomCount-1]) {
					inPoints++;
				}
			}
		}
		return (double)inPoints/count;
	}

	
	/**
	 * Computes the percentage of the field that is covered by cameras' largest FOV.
	 * In this calculation the overlapping areas of n circles are calculated just 1 time.
	 * @param count is the number of randomly generated points used to compute the density.
	 * @return the ratio of points inside circles to the total number of points.
	 */
	private Double computeDensity3(int count) {
		int inPoints = 0;
		RandomNumberGenerator rand = new RandomNumberGenerator(731);
		Double x,y;
		for (int i=0 ; i<count ; i++) {
			x = rand.nextDouble() * (field.maxX - field.minX) + field.minX;
			y = rand.nextDouble() * (field.maxY - field.minY) + field.minY;
			for (int n=0 ; n<cameras.size() ; n++) {
				Camera cam = cameras.get(n);
				if (distance(x,y,cam.x,cam.y) < cam.zooms[zoomCount-1]) {
					inPoints++;
					break;
				}
			}
		}
		return (double)inPoints/count;
	}
	
	
	/**
	 * Calculates the Euclidean distance between 2 points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */


	private Double distance(Double x1, Double y1, Double x2, Double y2) {
		return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
	}
	
	


	/**
	 * Computes the percentage of the field that is covered by cameras' largest FOV.
	 * In this calculation the overlapping areas of n circles are calculated n times.
	 */
	private Double computeDensity4_0() {
		Double coveredArea = 0.0;
		for (int n=0 ; n<cameras.size() ; n++) {
			coveredArea += calculateIntersection2(cameras.get(n));
		}		
		return coveredArea/((field.maxX - field.minX) * (field.maxY - field.minY));
	}
		

	private Double calculateIntersection2(Camera cam) {
		Double radius = cam.zooms[zoomCount-1];
		Double p = cam.x;
		Double q = cam.y;
		
		Double cutOffArea = 0.0;
		int flag = 0;
		Double x1 = 0.0;
		Double x2 = 0.0;
		Double y1 = 0.0;
		Double y2 = 0.0;
		
		/**
		 * (x-p)^2 + (y-q)^2 = r^2
		 * (1) y = (+-)sqrt(r^2 - (x-p)^2) + q
		 * (2) x = (+-)sqrt(r^2 - (y-q)^2) + p
		 */
		
		//line1: x = field.minX
		if (radius > Math.abs(field.minX-p)) {
			flag += 1;
			y1 = -Math.sqrt(Math.pow(radius, 2) - Math.pow(field.minX-p, 2)) + q;
			y2 = Math.sqrt(Math.pow(radius, 2) - Math.pow(field.minX-p, 2)) + q;
			cutOffArea += calculateCutOffArea(radius, Math.abs(y1 - y2));
		}
		
		//line2: y = field.maxY
		if (radius > Math.abs(field.maxY-q)) {
			flag += 2;
			x1 = -Math.sqrt(Math.pow(radius, 2) - Math.pow(field.maxY-q, 2)) + p;
			x2 = Math.sqrt(Math.pow(radius, 2) - Math.pow(field.maxY-q, 2)) + p;
			cutOffArea += calculateCutOffArea(radius, Math.abs(x1 - x2));
		}
		
		//line3: x = field.maxX
		if (radius > Math.abs(field.maxX-p)) {
			flag += 4;
			y1 = -Math.sqrt(Math.pow(radius, 2) - Math.pow(field.maxX-p, 2)) + q;
			y2 = Math.sqrt(Math.pow(radius, 2) - Math.pow(field.maxX-p, 2)) + q;
			cutOffArea += calculateCutOffArea(radius, Math.abs(y1 - y2));
		}
		
		//line4: y = field.minY
		if (radius > Math.abs(field.minY-q)) {
			flag += 8;
			x1 = -Math.sqrt(Math.pow(radius, 2) - Math.pow(field.minY-q, 2)) + p;
			x2 = Math.sqrt(Math.pow(radius, 2) - Math.pow(field.minY-q, 2)) + p;
			cutOffArea += calculateCutOffArea(radius, Math.abs(x1 - x2));
		}
		
		
		// taking out the double calculated overlap
		switch (flag) {
			case 3:
				if (Math.sqrt(Math.pow(field.minX - p, 2)+Math.pow(field.maxY - q, 2)) < radius) {
					Double x = (x1<x2)? x1 : x2; // take the smaller x intersection (which is the one outside the field)
					Double y = (y1>y2)? y1 : y2; // take the larger y intersection
					cutOffArea -= Math.abs(y - field.maxY) * Math.abs(x - field.minX) / 2;
				}
				break;
			case 6:
				if (Math.sqrt(Math.pow(field.maxX - p, 2)+Math.pow(field.maxY - q, 2)) < radius) {
					Double x = (x1>x2)? x1 : x2;
					Double y = (y1>y2)? y1 : y2;
					cutOffArea -= Math.abs(y - field.maxY) * Math.abs(x - field.maxX) / 2;
				}
				break;
			case 12:
				if (Math.sqrt(Math.pow(field.maxX - p, 2)+Math.pow(field.minY - q, 2)) < radius) {
					Double x = (x1>x2)? x1 : x2;
					Double y = (y1<y2)? y1 : y2;
					cutOffArea -= Math.abs(y - field.minY) * Math.abs(x - field.maxX) / 2;
				}
				break;
			case 9:
				if (Math.sqrt(Math.pow(field.minX - p, 2)+Math.pow(field.minY - q, 2)) < radius) {
					Double x = (x1<x2)? x1 : x2;
					Double y = (y1<y2)? y1 : y2;
					cutOffArea -= Math.abs(y - field.minY) * Math.abs(x - field.minX) / 2;
				}
				break;
			default: break;
		}
		
		return (Math.PI*Math.pow(radius, 2)) - cutOffArea;
	}


	private Double calculateCutOffArea(Double r, Double c) {
		Double O = 2 * Math.asin(c / (2 * r));
		Double A = (Math.pow(r, 2) / 2) * (O - Math.sin(O));
		return A;
	}


	/**
	 * Computes the percentage of cameras per square unit
	 */
	private Double computeDensity2() {
		return (double)cameras.size()/((field.maxX - field.minX) * (field.maxY - field.minY));
	}

	
	/**
	 * Computes the camera to object ratio (i.e. how many cmeras per object are available)
	 */
	private Double computeDensity1() {
		return (double)cameras.size()/objects.size();
	}
}
