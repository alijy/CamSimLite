import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class OneHopOptimal {
	private ArrayList<Camera> cameras;
	private ArrayList<Object> objects;
	private Double[] zooms;
	private int steps;
	private Double threshold;
    private String outputPath;
    private int[] step0CamConfig;
    private int tempMinK; //for internal use with recursive function
    private int[] tempCamConfig; //for internal use with recursive function

 
	/**
	 * Constructor
	 * @param settings An instance of Settings class that contains all scenario settings.
	 * @param steps Number of time steps the simulation will run for.
	 * @param threshold The selected confidence threshold to determine whether an object
	 * is detectable or not.
	 * @param outputPath The path to output folder.
	 */
    public OneHopOptimal(Settings settings, int steps, Double threshold, String outputPath) {
		System.out.println("Running Optimal algorithm ....\n");
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zooms = this.cameras.get(0).zooms;
		this.steps = steps;
		this.threshold = threshold;
		this.outputPath = outputPath;
		this.step0CamConfig = new int[cameras.size()];
		Arrays.fill(step0CamConfig, 0);
		run();
    }

    
    /**
     * Runs the optimal algorithm simulation
     */
    private void run() {
		int[] minKCover = new int[steps];
		int[] z = new int[cameras.size()];
    	for (int n=0 ; n<cameras.size() ; n++)
    		z[n] = -1;

		for (int step=0 ; step<steps ; step++) {
			tempMinK = 0;
			tempCamConfig = new int[cameras.size()];
			
			System.out.print("step "+step+" .... ");
			computeMinKCover(cameras.size(), new int[cameras.size()], z);
			z = tempCamConfig.clone();
			minKCover[step] = tempMinK;
			if (step==0)
				step0CamConfig = tempCamConfig.clone();
			
			updateObjects();
			System.out.println("COMPLETE");
		}
		long tableCount = (long)Math.pow(zooms.length, cameras.size());
		System.out.println("Table Count = "+tableCount+"\n");		
    	exportResult(minKCover);
	}

	
	/**
	 * Computes the minimum k-covers for a given step by finding the table with maximum min k and
	 * saves the value in a global variable (tempMinK) to be used in run().
	 * @param size The number of cameras in each (recursive) run.
	 * @param zoomList An (initially empty) auxiliary list for keeping the configuration indexes.
	 * @param step The current time step.
	 */
	private void computeMinKCover(int size, int[] zoomList, int[] zIndex) {
		if (size==0) {
			int tableResult = getMinK(zoomList);			
			if (tableResult > tempMinK) {
				tempMinK = tableResult;
				tempCamConfig = zoomList.clone();
			}
		}
		else {
			if (zIndex[0]==-1) {
				for (int z=0 ; z<zooms.length ; z++) {
					zoomList[size-1] = z;
					computeMinKCover(size-1, zoomList, zIndex);
				}
			}
			else {
				for (int z=0 ; z<zooms.length ; z++) {
					if (Math.abs(z-zIndex[size-1])<2) {
						zoomList[size-1] = z;
						computeMinKCover(size-1, zoomList, zIndex);
					}
				}				
			}
		}
	}

	
	/**
	 * Returns the minimum number of cameras that detect each object.
	 * @param zoomList List of selected cameras' zoom indexes.
	 * @return the k value of k-cover with a specific (given) camera configuration.
	 */
	private int getMinK(int[] zoomList) {
    	int[] objCover = new int[objects.size()];

    	for (int n=0 ; n<cameras.size() ; n++) {
    		int z = zoomList[n];
			for (int m=0 ; m<objects.size() ; m++) {
				if (isDetectable(m, n, z))
					objCover[m]++;
			}
    	}
    	
    	return minimum(objCover);
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

	
	/**
	 * Updates all objects one time step.
	 */
	private void updateObjects() {
		
		for (Object obj : objects)
			obj.update();
	}

	
	/**
	 * Writes the result to '*-oneHopOptimal.csv' file.
	 * @param minKCover The array of minimum k-cover values
	 */
	private void exportResult(int[] minKCover) {
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-oneHopOptimal.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("1-hop optimal");
	        for (int k : minKCover)
	        	out.println(k);        
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Gives read access to the camera configurations that is selected in step 0 of the runtime 
	 * @return A list camera configurations that is optimised for step 0
	 */
	public int[] getStep0CamConfig() {
		return step0CamConfig;
	}
}
