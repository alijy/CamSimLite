import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class OneHopArbitrary {

	static final int SEED = 0;
	private ArrayList<Camera> cameras;
	private ArrayList<Object> objects;
	private Double[] zooms;
	private int steps;
	private Double threshold;
    private String outputPath;
    private RandomNumberGenerator rand;

    
	/**
	 * Constructor
	 * @param settings An instance of Settings class that contains all scenario settings.
	 * @param steps Number of time steps the simulation will run for.
	 * @param threshold The selected confidence threshold to determine whether an object
	 * is detectable or not.
	 * @param outputPath The path to output folder.
	 */
    public OneHopArbitrary (Settings settings, int steps, Double threshold, String outputPath) {
		System.out.print("Running Arbitrary algorithm .... ");
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zooms = this.cameras.get(0).zooms;
		this.steps = steps;
		this.threshold = threshold;
		this.outputPath = outputPath;
    	this.rand = new RandomNumberGenerator(SEED);
		run();
		System.out.println("COMPLETE\n");
	}

    
    /**
     * Runs the arbitrary simulation
     */
    public void run(){
    	int[] minKCover = new int[steps];
    	int[] z = new int[cameras.size()];
    	for (int n=0 ; n<cameras.size() ; n++)
    		z[n] = -1;
    	
    	for (int step=0 ; step<steps ; step++) {
        	int[] objCover = new int[objects.size()];

        	for (int n=0 ; n<cameras.size() ; n++) {
//	    		int z = rand.nextInt(zooms.length);
	    		z[n] = getNextZoom(z[n]);
				for (int m=0 ; m<objects.size() ; m++) {
					if (isDetectable(m, n, z[n])) {
						objCover[m]++;
					}
				}
	    	}
	    	
        	minKCover[step] = minimum(objCover);
//        	System.out.println("in step "+step+" objects are "+minKCover[step]+"-covered");        	
        	updateObjects();
    	}
    	
    	exportResult(minKCover);
    }
    
    
	private int getNextZoom(int z) {
		int zIndex;
		if (zooms.length < 3 || z==-1)
			zIndex = rand.nextInt(zooms.length);
		else if (z==0)
			zIndex = rand.nextInt(2);
		else if (z==zooms.length-1) {
			zIndex = rand.nextInt(2) + zooms.length - 2;
		}
		else
			zIndex = rand.nextInt(3) + z - 1;
		return zIndex;
	}


	/**
	 * Checked whether an object is detectable by a camera with a specified zoom (FOV).
	 * @param m The index of the object in the list of objects
	 * @param n The index of the camera in the list of cameras
	 * @param z The index of the zoom level in the list of zoom values
	 * @return True if the object is within FOV (zoom range) AND the camera can see it 
	 * with a confidence above threshold. False otherwise.
	 */
	private boolean isDetectable(int m, int n, int z) {
		Double distance = Math.sqrt(Math.pow((cameras.get(n).x-objects.get(m).x), 2) + Math.pow((cameras.get(n).y-objects.get(m).y), 2));
		if (distance > cameras.get(n).zooms[z])
			return false;
		else {
			double b = 15;
			Double conf = 0.95 * (b / (cameras.get(n).zooms[z] * distance)) - 0.15;
			return (conf >= threshold);
		}
	}

	
	/**
	 * Updates all objects one time step.
	 */
	private void updateObjects() {
		
		for (Object obj : objects)
			obj.update();
	}

	
	/**
	 * Writes the result to '*-oneHopRandom.csv' file.
	 * @param minKCover The array of minimum k-cover values
	 */
	private void exportResult(int[] minKCover) {
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-oneHopRandom.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("1-Hop Random");
	        for (int k : minKCover)
	        	out.println(k);        
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Returns the minimum value of a list of integers < 10000.
	 * @param list The list of integer
	 * @return The minimum integer value in the list
	 */
	private static int minimum(int[] list) {
		int min = 10000;
		for (int i : list){
			if (i < min)
				min = i;
		}
		return min;
	}

}
