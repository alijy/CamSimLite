import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class OldOptimal {
	private ArrayList<Camera> cameras;
	private ArrayList<Object> objects;
	private Double[] zooms;
	private int steps;
	private Double threshold;
    private String outputPath;
    private int[] step0CamConfig;

    public OldOptimal(Settings settings, int steps, Double threshold, String outputPath) {
		System.out.println("Running Optimal algorithm ....\n");
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.zooms = this.cameras.get(0).zooms;
		this.steps = steps;
		this.threshold = threshold;
		this.outputPath = outputPath;
		run();
    }

    
    /**
     * Runs the optimal algorithm simulation
     */
    private void run() {
		int tableCount = (int)Math.pow(zooms.length, cameras.size());
		int[][] minKCover = new int[2][steps];
		System.out.println("Table Count = "+tableCount);

		for (int step=0 ; step<steps ; step++) {
			int[] minKs = new int[tableCount];
			System.out.println("************");
			
			System.out.print("step "+step+":  ");
			populateTable(minKs, cameras.size(), new int[cameras.size()]);
			
			int[] kCover = getMaximum(minKs);
			minKCover[0][step] = kCover[0];
			minKCover[1][step] = kCover[1];

			if (step==0) {
				step0CamConfig = getConfiguration(kCover[1]);
				System.out.println("Table Number: "+kCover[1]);
			}
			
			updateObjects();
//			System.out.println("COMPLETE");
		}
		
    	exportResult(minKCover[0]);
	}

	
	/**
	 * Returns a pair (m,i) where m is the maximum value in a list and i is
	 * its corresponding index.
	 * @param list A list of integer values.
	 * @return The maximum value together with its index in the list.
	 */
	private int[] getMaximum(int[] list) {
		int[] max = {0,0};
		for (int i=0 ; i<list.length ; i++) {
			if (list[i] > max[0]) {
				max[0] = list[i];
				max[1] = i;
			}
		}
		System.out.println("Table "+max[1]+" with k="+max[0]);
		return max;
	}
	
	

	/**
	 * populate a given list by computing the minimum k-cover of corresponding camera configurations.
	 * Each item correspond to a unique combination of cameras configurations.
	 * @param tables The list to be populated.
	 * @param size The number of cameras in each (recursive) run.
	 * @param zoomList An (initially empty) auxiliary list for keeping the configuration indexes.
	 */
	private void populateTable(int[] tables, int size, int[] zoomList) {
		if (size==0) {
//			int count = zoomList[0] + 3*zoomList[1] + 9*zoomList[2] + 27*zoomList[3];
			int count = 0;
			for (int p = 0 ; p < zoomList.length ; p++)
				count += zoomList[p] * (int)Math.pow(zooms.length, p);
//			System.out.println("["+zoomList[0]+","+zoomList[1]+","+zoomList[2]+","+zoomList[3]+"]");
			tables[count] = getMinK(zoomList);
//			System.out.println("table "+count+":\tmin-k: "+tables[count]);

		}
		else {
			for (int z=0 ; z<zooms.length ; z++) {
				zoomList[size-1] = z;
				populateTable(tables, size-1, zoomList);
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
	 * Returns the respective cameras zoom (FOV) configuration.
	 * @param number The number assigned to the configuration table.
	 * @return The list of camera configurations that corresponds to that table.
	 */
	private int[] getConfiguration(int number) {
		int[] configuration = new int[cameras.size()];
		for (int n=0 ; n<cameras.size() ; n++) {
			configuration[cameras.size()-n-1] = number / (int)(Math.pow(zooms.length, cameras.size()-n-1));
			number = number % (int)(Math.pow(zooms.length, cameras.size()-n-1));
		}		
		return configuration;
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
	 * Writes the result to '*-egreedy.csv' file.
	 * @param minKCover The array of minimum k-cover values
	 */
	private void exportResult(int[] minKCover) {
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-optimal.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("optimal");
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
