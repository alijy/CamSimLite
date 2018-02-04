import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/

public class QLearning {
	
    private final double alpha = 0.1; // Learning rate
    private final double gamma = 0.9; // Eagerness - 0 looks in the near future, 1 looks in the distant future
    private ArrayList<Camera> cameras;
	private ArrayList<Object> objects;
	private Double[] zooms;
	private int steps;
	private Double threshold;
    private String outputPath;
    private RandomNumberGenerator rand;
    private int cycles;
    private Double[][][] Q; // (z*o)X(z*o) matrix for each camera
    private Double[][][] QF; // (z*o)X(z) matrix for each camera
    private int[][] action; // an array of length (z*o) for each camera
    private Settings settingsCopy; // to keep a fresh untouched copy of the settings object
    
	/**
	 * Constructor
	 * @param settings An instance of Settings class that contains all scenario settings.
	 * @param steps Number of time steps the simulation will run for.
	 * @param threshold The selected confidence threshold to determine whether an object
	 * is detectable or not.
	 * @param outputPath The path to output folder.
	 */
    public QLearning (Settings settings, int steps, Double threshold, String outputPath, int cycles, Settings settingsCopy) {
		System.out.print("Running Q-Learning algorithm .... ");
		this.settingsCopy = settingsCopy;
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zooms = this.cameras.get(0).zooms;
		this.steps = steps;
		this.threshold = threshold;
		this.outputPath = outputPath;
    	this.rand = new RandomNumberGenerator(4112017);
    	this.cycles = cycles;
    	Q = new Double[cameras.size()][zooms.length*(objects.size()+1)][zooms.length*(objects.size()+1)];
    	for (Double[][] square : Q)
            for (Double[] line : square)
                Arrays.fill(line, 0.0); // initialising all elements of Q matices
    	QF = new Double[cameras.size()][zooms.length*(objects.size()+1)][zooms.length];
    	action = new int[cameras.size()][zooms.length*(objects.size()+1)];

    	calculateQ();
    	inferAction();
		run();

		System.out.println("COMPLETE\n");
	}

    
	/**
	 * Resets all Object objects to their initial state
	 */
	private void resetObjects() {
		this.objects = settingsCopy.objects;
	}


	/**
	 * Calculates the Q matrices of individual cameras and transforms the final Q matrices
	 * into QF matrices which only have zoom index as their columns (as opposed to z*(obj+1)
	 * in Q matrices). 
	 */
	private void calculateQ() {
    	for (int i=0 ; i<cycles ; i++) {
    		int[][][] stats = new int[steps+1][cameras.size()][2];
    		
    		// generating random zooms and calculating detected objects for all steps
    		for (int step=0 ; step<steps+1 ; step++) {    			
        		for (int n=0 ; n<cameras.size() ; n++) {
        			int crtZoom = rand.nextInt(zooms.length);
        			int detectedObj = 0;
    				for (int m=0 ; m<objects.size() ; m++) {
    					if (isDetectable(m, n, crtZoom)) {
    						detectedObj++;
						}
    				}
        			stats[step][n][0] = crtZoom; // saves the current zoom index
        			stats[step][n][1] = detectedObj; // saves the number of detected object by this camera with this zoom at this step
        		}        		
        		updateObjects();
    		}
    		
    		// Modifying Q of each camera for all steps of the cycle
    		for (int step=0 ; step<steps ; step++) {
        		for (int n=0 ; n<cameras.size() ; n++) {
        			int crtZoom = stats[step][n][0];
        			int crtDO = stats[step][n][1];
        			int nxtZoom = stats[(step+1)][n][0];
        			int nxtDO = stats[(step+1)][n][1];
        			double r = nxtDO - crtDO; // define reward function
//        			r = r * Math.abs(r);
//        			if (r > 0) r = r * r;
//        			if (r == 0) r = crtZoom - nxtZoom;
	    			Double q = Q[n][(crtZoom*(objects.size()+1)+crtDO)][(nxtZoom*(objects.size()+1)+nxtDO)];
	    			Double maxQ = getMax(Q[n][(nxtZoom*(objects.size()+1)+nxtDO)]);
	    			Double value = q + alpha * (r + gamma * maxQ - q);
	    			Q[n][(crtZoom*(objects.size()+1)+crtDO)][(nxtZoom*(objects.size()+1)+nxtDO)] = value;
        		}
    		}    		
    	}
    	
    	// calculating QF
    	for (int n=0 ; n<cameras.size() ; n++) {
			for (int z1=0 ; z1<zooms.length ; z1++) {
				for (int m1=0 ; m1<objects.size() ; m1++) {
		    		for (int z2=0 ; z2<zooms.length ; z2++) {
		    			Double sum = 0.0;
		    			for (int m2=0 ; m2<objects.size() ; m2++) {
		    				sum += Q[n][z1*(objects.size()+1)+m1][z2*(objects.size()+1)+m2];
		    			}
		    			QF[n][z1*(objects.size()+1)+m1][z2] = sum;
		    		}
				}
			}
		}    	
	}


	/**
	 * Infers actions (next zooms) for each zoom-detectedObj combination by finding the
	 * zoom with maximum QF value.
	 */
    private void inferAction() {
    	for (int n=0 ; n<cameras.size() ; n++) {
			for (int z1=0 ; z1<zooms.length ; z1++) {
				for (int m=0 ; m<objects.size() ; m++) {
					int z2 = getMaxIndex(QF[n][z1*(objects.size()+1)+m]);
					action[n][z1*(objects.size()+1)+m] = z2;
				}
			}
    	}
	}

	
	/**
	 * Finds the index of the maximum value in a list of integers
	 * @param list A list of integers
	 * @return A integer value (index) between 0 and list.length-1
	 */
	private int getMaxIndex(Double[] list) {
		int index = 0;
		Double value = list[0];
		for (int i=1 ; i<list.length ; i++) {
			if (list[i]>=value) {
				value = list[i];
				index = i;
			}
		}
		return index;
	}


	/**
	 * Finds the maximum value in a list of integers
	 * @param list A list of integer values
	 * @return The highest value in the list
	 */
	private Double getMax(Double[] list) {
		Double max = list[0];
		for (int i=1 ; i<list.length ; i++)
			if (list[i] > max)
				max = list[i];
		return max;
	}


	/**
     * Runs the Q-Learning simulation
     */
    public void run(){
    	resetObjects();

    	int[] crtZoom = new int[cameras.size()];
    	int[] minKCover = new int[steps];

    	for (int step=0 ; step<steps ; step++) {
        	int[] objCover = new int[objects.size()];
        	if (step==0) {
//        		System.out.println("***** STEP 0 *****"); 
        		for(int i=0 ; i<cameras.size() ; i++) {
        			crtZoom[i] = rand.nextInt(zooms.length); // the initial zoom level of each camera at step 0
//        			System.out.println("z: "+crtZoom[i]);
        		}
        	}
        	
    		for (int n=0 ; n<cameras.size() ; n++) {
    			int detectedObj = 0;
    			for (int m=0 ; m<objects.size() ; m++) {
					if (isDetectable(m, n, crtZoom[n])) {
						detectedObj++;
						objCover[m]++;
					}
				}
//    			System.out.println(cameras.get(n).id+": zoom="+crtZoom[n]+"  DO="+detectedObj);
    			int nxtZoom = action[n][crtZoom[n]*(objects.size()+1)+detectedObj]; // selects action based on learnt results
    			crtZoom[n] = nxtZoom;
    		}

        	minKCover[step] = minimum(objCover);
//	    	System.out.println("in step "+step+" objects are "+minKCover[step]+"-covered");        	
	    	updateObjects();
    	}
    	exportResult(minKCover);
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
	 * Writes the result to '*-qlearning.csv' file.
	 * @param minKCover The array of minimum k-cover values
	 */
	private void exportResult(int[] minKCover) {
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-qlearning.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("Q-Learning");
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
