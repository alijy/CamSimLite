import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.FileWriter;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class Run {

//	private Field field;
	private ArrayList<Camera> cameras;
	private ArrayList<Object> objects;
	private int steps;
	private Double[] thresholds;
	Double[][] dist;
	Double[][][] conf;
    Double[][][][] fullconf;
    int[][][][] fullTable;
    int[] initialCamConfig;
    int[][][] fullMinCovers;
    String outputPath;
    int initialThresholdIndex;
    int[][] baseLines;
    int[][] allLines;
    
    /*
     * Constructor
     */
    public Run (Settings settings, int steps, Double[] thresholds, String outputPath, int[] camPreset, 
    		int[][] Lines, int thresholdIndex, String lineID) {
    	
		initialise(settings, steps, thresholds, outputPath);
		this.initialCamConfig = camPreset;
		this.allLines = Lines;
		int[][] idLines = new int[steps][this.thresholds.length];
		this.initialThresholdIndex = thresholdIndex;

    	if (isSetValid(camPreset)){
    		this.conf = new Double[steps][cameras.size()][objects.size()];
    		for (int step=0 ; step<this.steps ; step++) {

    			System.out.print("step "+step+" .... ");
    			calculateDistances();
    			calculatePresetConfidences(step);

    			for (int t=0 ; t<this.thresholds.length ; t++) {
					int[][] covers = filterConf(step, this.thresholds[t]);
					int minimum = 10000;
					for (int m=0 ; m<objects.size() ; m++) {
						int sum = 0;
						for (int n=0 ; n<cameras.size() ; n++) {
							sum += covers[n][m];
						}
		    	        if (sum < minimum) {
		    	        	minimum = sum;
		    	        }
					}
					idLines[step][t] = minimum;
				}

    			updateObjects();
    			System.out.println("COMPLETE");
    		}

    		switch (lineID) {
    		case "baseline":
    			baseLines = idLines;
    			copyColumn(idLines, initialThresholdIndex, this.allLines, 1);
    			exportConfs();
    			exportbaseLines();
    			break;
    		case "zoomoutline":
    			copyColumn(idLines, initialThresholdIndex, this.allLines, 2);
    			break;
    		case "randomline":
    			copyColumn(idLines, initialThresholdIndex, this.allLines, 3);
    			exportfinalLines();
    			break;
    		}
    		
    		int tableCount = (int)Math.pow(cameras.get(0).zooms.length, cameras.size());
    		System.out.println("\n>>>  Computed Tables: "+tableCount*thresholds.length*steps+"  <<<");
    	}
    	else {
    		System.err.println("ERROR: Initial camera configurations are invalid!");
    	}
    	    	
    }

    
    /**
     * Copies a column from a 2D array to a column of another 2D array
     * @param arr1 The array to copy from
     * @param c1 The column to be copied
     * @param arr2 The array to copy to
     * @param c2 The column to copy to
     */
    private void copyColumn(int[][] arr1, int c1, int[][] arr2, int c2) {
		for (int i=0 ; i<arr1.length ; i++)
			arr2[i][c2] = arr1[i][c1];
	}


	private void exportfinalLines() {

    	System.out.print("\nCreating final.csv file .... ");
        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"-final.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.println("Baseline\tOptimal Line\tzoomOutLine\trandomLine");
	        for (int step=0 ; step<steps ; step++) {
	        	out.println(allLines[step][1]+"\t"+allLines[step][0]+"\t"+allLines[step][2]+"\t"+allLines[step][3]);
	        }	        
	        out.close();        
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("COMPLETE");
	}


	/**
     * Writes confidence values into 'confs.csv' file.
     */
    private void exportConfs() {

        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"confs.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        for (int step=0 ; step<steps ; step++) {
	        	out.println("step:"+step);
	        	for (int n=0 ; n<cameras.size() ; n++) {
	        		out.print(cameras.get(n).id+":\t");
	        		for (int m=0 ; m<objects.size() ; m++) {
	        			out.print(conf[step][n][m]+"\t");
	        		}
	        		out.println();
	        	}
	        	out.println();
	        }	        
	        out.close();        
		}
		catch (IOException e) {
			e.printStackTrace();
		}    	
	}


	/**
     * Writes the minimum k-cover values into 'blueLine.csv' file.
     */
	private void exportbaseLines() {

        FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"blueLine.csv");
	        PrintWriter out = new PrintWriter(outFile);
	        
	        out.print("threshold\t");
	        for (int t=0; t<thresholds.length ; t++) {
	        	out.print(thresholds[t]+"\t");
	        }
	        out.println();
	        
	        for (int step=0 ; step<steps ; step++) {
	        	out.print("step:"+step+"\t");
	        	for (int t=0 ; t<thresholds.length ; t++) {
	        		out.print(baseLines[step][t]+"\t");
	        	}
	        	out.println();
	        }	        
	        out.close();        
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Filters all confidences in 'conf' based on threshold t.
	 * If a confidence is greater than or equal to t it is filtered
	 * as 1. It is 0 otherwise.
	 */
	private int[][] filterConf(int step, Double threshold) {
		int[][] cover = new int[cameras.size()][objects.size()];
		for (int n=0 ; n<cameras.size() ; n++) {
			for (int m=0 ; m<objects.size() ; m++) {
				if (conf[step][n][m] >= threshold) {
					cover[n][m] = 1;
				} else {
					cover[n][m] = 0;
				}
			}
		}

		return cover;
    	}
    
    


	/**
     * Calculates the confidences of all camera-object pairs for
     * a specific (preset) zoom level of each camera
     */
    private void calculatePresetConfidences(int step) {

		for (int n=0 ; n<cameras.size() ; n++) {
				for (int m=0 ; m<objects.size() ; m++) {
					if (dist[n][m] <= cameras.get(n).zooms[initialCamConfig[n]-1]) {
		                double b = 15;	                
		                conf[step][n][m] = 0.95*(b / (cameras.get(n).zooms[initialCamConfig[n]-1] * dist[n][m]))-0.15;
		                
		                if(conf[step][n][m] < 0.0)
		                	conf[step][n][m] = 0.0;		                	
					}
					else {
						conf[step][n][m] = 0.0;
					}
				}
		}		
		
	}
    
    


	/**
     * Checks whether a set of camera FOV (zoom level) configurations is valid or not.
     * A set is valid if there is exactly 1 zoom level for each camera, and each zoom
     * level is among the possible values.
     */
    private boolean isSetValid(int[] camPreset) {

    	if (camPreset.length != cameras.size())
    		return false;
    	
    	int maxZoomLevel = cameras.get(0).zooms.length;
		for (int zoomLevel : camPreset) {
			if (zoomLevel<=0 || zoomLevel > maxZoomLevel)
				return false;
		}
		
		return true;
	}
    
    


	/**
     * Constructor
     */
	public Run (Settings settings, int steps, Double[] thresholds, String outputPath, boolean stepTables) {
		
		initialise(settings, steps, thresholds, outputPath);

		this.fullconf = new Double[steps][cameras.size()][cameras.get(0).zooms.length][objects.size()];
		int tableCount = (int)Math.pow(cameras.get(0).zooms.length, cameras.size());
		this.fullTable = new int[thresholds.length][tableCount][cameras.size()+1][objects.size()];
		this.fullMinCovers = new int[steps][thresholds.length][tableCount];
		
		for (int step=0 ; step<this.steps ; step++) {
						
			System.out.print("step "+step+" .... ");
			calculateDistances();			
			calculateConfidences(step);

			if (stepTables) {
		        FileWriter outFile;
				try {
					outFile = new FileWriter(outputPath+"mincovers-step"+step+".csv");
			        PrintWriter out = new PrintWriter(outFile);
					
					for (int t=0 ; t<this.thresholds.length ; t++) {
	
						int[][][] covers = filterFullConf(step, this.thresholds[t]);
						populateCoverTable(fullTable[t], covers, fullMinCovers[step][t], cameras.size(), objects.size(), cameras.get(0).zooms.length, new int[cameras.size()]);
		
						for (int i = 0 ; i < tableCount ; i++)
				        	out.print(fullMinCovers[step][t][i]+ "\t");
						out.println();
					}
					
			        out.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        
	        else {
				for (int t=0 ; t<this.thresholds.length ; t++) {

					int[][][] covers = filterFullConf(step, this.thresholds[t]);
					populateCoverTable(fullTable[t], covers, fullMinCovers[step][t], cameras.size(), objects.size(), cameras.get(0).zooms.length, new int[cameras.size()]);
				}
	        }
			
			updateObjects();
			System.out.println("COMPLETE");

		}
		
		exportFullConf(outputPath+"fullconfs.csv");
		
		System.out.print("\nCalculating optimal configurations .... ");
		calculateOptimals();		
		System.out.println("COMPLETE");
		
		System.out.println("\ntableCount: "+tableCount+"\tallTables: "+tableCount*thresholds.length*steps+"\n\n");
	}

	
	

	private void calculateOptimals() {
		
		allLines = new int[steps][4];

		FileWriter outFile;
		try {
			outFile = new FileWriter(outputPath+"blackLine.csv");
			PrintWriter out = new PrintWriter(outFile);
			out.print("step\t\tk\tthreshold\t\t");
			for (int n=0 ; n<cameras.size() ; n++) {
				out.print(cameras.get(n).id+"\t");
			}
			out.println();
			
			for (int step=0 ; step<steps ; step++) {

				int[] optimalTable = optimalTable(step);
				int[] camConfig = camConfiguration(optimalTable);

				allLines[step][0] = optimalTable[0];
				
				out.print(step+"\t\t"+optimalTable[0]+"\t"+thresholds[optimalTable[3]]+"\t\t");
				for (int n=0 ; n<camConfig.length ; n++) {
					out.print(camConfig[n]+"\t");
				}
				out.println();
				
				if (step == 0) {
					initialCamConfig = camConfig;
					initialThresholdIndex = optimalTable[3];
				}
			}
			out.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Returns the respective cameras zoom (FOV) configuration
	 */
	private int[] camConfiguration(int[] optimalTable) {

		int number = optimalTable[2];
		int zoom = cameras.get(0).zooms.length;
		int[] configuration = new int[cameras.size()];
		for (int n=0 ; n<cameras.size() ; n++) {
			configuration[cameras.size()-n-1] = number / (int)(Math.pow(zoom, cameras.size()-n-1)) + 1;
			number = number % (int)(Math.pow(zoom, cameras.size()-n-1));
		}		

		return configuration;
	}


	/**
	 * finds highest k-cover in each threshold and returns the optimal
	 * together with its count, table number and respective threshold
	 */
	private int[] optimalTable(int step) {

		// finds highest k-cover in each threshold
		int[][] list = fullMinCovers[step];
		int[][] results = new int[list.length][3];
		
		for (int t=0 ; t<list.length ; t++) {
			for (int c=0; c<list[0].length ; c++) {
				if (list[t][c] > results[t][0]) {
					results[t][0] = list[t][c];
					results[t][1] = 1;
					results[t][2] = c;
				}
				else if (list[t][c] == results[t][0]) {
					results[t][1]++;
					//results[t][2] = c; //uncomment this if you want the highest table number (i.e. widest optimal FOV)
				}
			}
		}
		
		// finds optimal k-cover (plus its relevant threshold) among all thresholds
		int[] optimal = new int[4];
		for (int t=0 ; t<results.length ; t++) {
			if (results[t][0] > optimal[0] || 
					((results[t][0] == optimal[0]) && (results[t][1] < optimal[1]))) {
				optimal[0] = results[t][0];
				optimal[1] = results[t][1];
				optimal[2] = results[t][2];
				optimal[3] = t;
			}
		}
//		System.out.println("Optimal: (k: "+optimal[0]+"  count:"+optimal[1]+"  table:"+optimal[2]+"  threshold:"+thresholds[optimal[3]]+")");
		
		return optimal;
	}


	private void initialise(Settings settings, int steps, Double[] thresholds, String outputPath) {

		System.out.println("\nRunning the simulation ....\n");
//		this.field = settings.field;
		this.cameras = settings.cameras;
		this.objects = settings.objects;
		this.steps = steps;
		this.thresholds = thresholds;
		this.outputPath = outputPath;
		this.dist = new Double[cameras.size()][objects.size()];

	}
	
	


	/**
	 * Filters all confidences in 'fullconf' based on threshold t.
	 * If a confidence is greater than or equal to t it is filtered
	 * as 1. It is 0 otherwise.
	 */
	private int[][][] filterFullConf(int step, Double threshold) {
		
		int[][][] cover = new int[cameras.size()][cameras.get(0).zooms.length][objects.size()];
		for (int n=0 ; n<cameras.size() ; n++) {
			for (int z=0 ; z<cameras.get(n).zooms.length ; z++) {
				for (int m=0 ; m<objects.size() ; m++) {
					if (fullconf[step][n][z][m] >= threshold) {
						cover[n][z][m] = 1;
					} else {
						cover[n][z][m] = 0;
					}
				}
			}
		}

		return cover;
	}
	
	
	/**
	 * Exports the confidence values for all steps to conf.csv
	 */
	private void exportFullConf(String outputFilePath) {

		try {
	        FileWriter outFile;
			outFile = new FileWriter(outputFilePath);
	        PrintWriter out = new PrintWriter(outFile);
	        
	        for (int step=0 ; step<this.steps ; step++) {
	        	out.println("step:"+step);
				for (int n=0 ; n<cameras.size() ; n++) {
					for (int z=0 ; z<cameras.get(n).zooms.length ; z++) {
						out.print(cameras.get(n).id+"(z"+z+"):\t");
						for (int m=0 ; m<objects.size() ; m++) {
							out.print(fullconf[step][n][z][m]+ "\t");
						}
						out.println();
					}
					out.println();
				}
				out.println("\n");
	        }
	        out.close();
	        
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	
	/**
	 * Calculates the confidences of all camera-object pairs for
	 * every zoom level of every camera
	 */
	private void calculateConfidences(int step) {

		for (int n=0 ; n<cameras.size() ; n++) {
			for (int z=0 ; z<cameras.get(n).zooms.length ; z++) {
				for (int m=0 ; m<objects.size() ; m++) {
					if (dist[n][m] <= cameras.get(n).zooms[z]) {
		                double b = 15;	                
		                fullconf[step][n][z][m] = 0.95*(b / (cameras.get(n).zooms[z] * dist[n][m]))-0.15;
		                
		                if(fullconf[step][n][z][m] < 0.0)
		                	fullconf[step][n][z][m] = 0.0;		                	
					}
					else {
						fullconf[step][n][z][m] = 0.0;
					}
				}

//				System.out.println(cameras.get(n).id+"-z"+z+": "+fullconf[step][n][z][0]+" , "+fullconf[step][n][z][1]+" , "+fullconf[step][n][z][2]);
			}
		}		
		
	}
	
	
	/**
	 * Updating all objects one step
	 */
	private void updateObjects() {
		
		for (Object obj : objects)
			obj.update();
	}

	
	/**
	 * Calculates the distances between all camera-object pairs
	 */
	private void calculateDistances() {

		dist = new Double[cameras.size()][objects.size()];
		for (int n=0 ; n<cameras.size() ; n++) {
			for (int m=0 ; m<objects.size() ; m++) {
				dist[n][m] = Math.sqrt(Math.pow((cameras.get(n).x-objects.get(m).x), 2) + Math.pow((cameras.get(n).y-objects.get(m).y), 2));
//				System.out.println("dist "+cameras.get(n).id+" to object "+objects.get(m).id+" is: "+dist[n][m]);
			}
		}
	}

	/**
	 * Recursively populates the k-cover tables and calculates the minimum k cover for each table
	 */
	private static void populateCoverTable(int[][][] tables, int[][][] camTables, int[] minCovers, int camCount, int objCount, int zoomCount, int[] zoomList) {
		if (camCount == 0){
			
//			int count = zoomList[0] + 3*zoomList[1] + 9*zoomList[2] + 27*zoomList[3];
			int count = 0;
			for (int p = 0 ; p < zoomList.length ; p++){
				count += zoomList[p] * (int)Math.pow(zoomCount, p);
			}
			
			for (int m = 0 ; m < objCount ; m++){
				tables[count][zoomList.length][m] = 0;
				for (int n = 0 ; n < zoomList.length ; n++){
					
					tables[count][n][m] = camTables[n][zoomList[n]][m];
					tables[count][zoomList.length][m] += tables[count][n][m];
				}					        					
			}
			        				
			minCovers[count] = minimum(tables[count][zoomList.length]);
		}
		else{
			
			for (int i = 0 ; i < zoomCount ; i++){
				zoomList[camCount-1] = i;
				populateCoverTable(tables, camTables, minCovers, camCount-1, objCount, zoomCount, zoomList);
			}
		}
	}

	/**
	 * Returns the minimum value of a list of integers
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
