import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/

public class Sarsa {
	
	private final Double EPSILON = 0.1;
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
    public Sarsa (Settings settings, int steps, Double threshold, String outputPath) {
		System.out.print("Running Sarsa algorithm .... ");
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zooms = this.cameras.get(0).zooms;
		this.steps = steps;
		this.threshold = threshold;
		this.outputPath = outputPath;
    	this.rand = new RandomNumberGenerator(9011);
		run();
		System.out.println("COMPLETE\n");
	}

    
    /**
     * Runs the Sarsa simulation
     */
    public void run(){
    	int[][][][] stats = new int[cameras.size()][zooms.length*(objects.size()+1)][zooms.length][2]; // (z*o)X(z)X(2) matrix for each camera
    	int[] minKCover = new int[steps];
    	int[][] zDo = new int[cameras.size()][2]; // index 0 keeps previous zoom, index 1 keeps previous number of detected objects (after step 0)
    	
    	for (int step=0 ; step<steps ; step++) {
        	int[] objCover = new int[objects.size()];

        	for (int n=0 ; n<cameras.size() ; n++) {
        		int crtZoom;
				int crtDO = 0;
        		crtZoom = getZoomIndex(n, step, stats[n][(zDo[n][0]*(objects.size()+1)+zDo[n][1])]);
				for (int m=0 ; m<objects.size() ; m++) {
					if (isDetectable(m, n, crtZoom)) {
						crtDO++;
						objCover[m]++;
					}
				}
				if (step!=0) {
					stats[n][(zDo[n][0]*(objects.size()+1)+zDo[n][1])][crtZoom][0]++;
					stats[n][(zDo[n][0]*(objects.size()+1)+zDo[n][1])][crtZoom][1] += crtDO;
				}
				zDo[n][0] = crtZoom;
				zDo[n][1] = crtDO;
	    	}
	    	
        	minKCover[step] = minimum(objCover);
//        	System.out.println("in step "+step+" objects are "+minKCover[step]+"-covered");        	
        	updateObjects();
    	}
    	
    	exportResult(minKCover);
    }

    
    /**
     * Generates a zoom index value for the camera according the time step and based on the
     * camera statistics so far.
     * @param n The index of the camera in the list of cameras.
     * @param step The current time step.
     * @param stats A two-dimensional array with the list of zoom level indexes on the first 
	 * dimension and a pair of integers on the second dimension. The first item in each pair
	 * is the number of times (steps) it was set as the zoom level of the camera. The second
	 * item is the sum of the number of objects the camera has ever detected with that zoom
	 * level. 
     * @return A zoom index from the zooms array.
     */
	private int getZoomIndex(int n, int step, int[][] stats) {
		int zIndex;
		if (step == 0) {
			zIndex = rand.nextInt(zooms.length);
		}
		else {
			int maxIndex = maxIndex(stats);
			Double p = rand.nextDouble();
			if (p > EPSILON)
				zIndex = maxIndex;
			else {
				do{
					zIndex = rand.nextInt(zooms.length);
				} while (zIndex == maxIndex);
			}
		}
//		System.out.println("step"+step+": zIndex of  "+cameras.get(n).id+" is "+zIndex);
		return zIndex;
	}

	
	/**
	 * Finds the index of a zoom level (FOV) with maximum average of detected objects so far
	 * @param stats A two-dimensional array with the list of zoom level indexes on the first 
	 * dimension and a pair of integers on the second dimension. The first item in each pair
	 * is the number of times (steps) it was set as the zoom level of the camera. The second
	 * item is the sum of the number of objects the camera has ever detected with that zoom
	 * level.
	 * @return The index of a zoom level with highest average object detection rate.
	 */
	private int maxIndex(int[][] stats) {
		int index = 0;
		Double maxAverage = 0.0;
		for (int i=0 ; i<stats.length ; i++) {
			Double average;
			if (stats[i][0]==0)
				average = 0.0;
			else
				average = (1.0 * stats[i][1]) / stats[i][0];
//			average = (1.0 * stats[i][1]) / stats[i][0];
			if (average >= maxAverage) {	// change this to '>=' if in equal situation the higher zoom is desired
				index = i;
				maxAverage = average;
			}
		}
		return index;
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
	 * Writes the result to '*-sarsa.csv' file.
	 * @param minKCover The array of minimum k-cover values
	 */
	private void exportResult(int[] minKCover) {
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-sarsa.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("Sarsa");
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
