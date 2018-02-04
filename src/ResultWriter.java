import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class ResultWriter {

	public ResultWriter(String destination, String outputPath, String[] input, int steps) throws IOException {
//      int difference = 0;
		int baselineSum = 0;
		int optimalSum = 0;
		int zoomoutSum = 0;
		int randomSum = 0;
		int eGreedySum = 0;
		int qLearningSum = 0;
		int qLearningegSum = 0;
		int sarsaSum = 0;
		int eGreedyActualDOSum = 0;
		int eGreedyIntendedDOSum = 0;
		int oneHopRandomSum = 0;
		int oneHopOptimalSum = 0;

		DecimalFormat df = new DecimalFormat("#.##");

        FileWriter outFile = new FileWriter(outputPath+"-"+destination+".csv");
        PrintWriter out = new PrintWriter(outFile);
        
        File file[] = new File[input.length];
        Scanner sc[] =  new Scanner[input.length];
        
        for (int i=0 ; i<input.length ; i++) {
        	file[i] = new File(outputPath+"-"+input[i]+".csv");
        	sc[i] = new Scanner(file[i]);
        }
        
        for (int step=0 ; step<steps+1 ; step++) {
        	out.print(step+"\t");	// out.print((step+1)+"\t");
            for (int i=0 ; i<input.length ; i++) {
            	String value = sc[i].nextLine();
                out.print(value+"\t");
                
                if (step > 0) {
//	                if (i == 1)
//	                	difference += Integer.parseInt(value);
//	                if (i == 4)
//	                	difference -= Integer.parseInt(value);
                	switch (i) {
                		case 0:
                			baselineSum += Integer.parseInt(value);
                			break;
                		case 1:
                			optimalSum += Integer.parseInt(value);
                			break;
                		case 2:
                			zoomoutSum += Integer.parseInt(value);
                			break;
                		case 3:
                			randomSum += Integer.parseInt(value);
                			break;
                		case 4:
                			eGreedySum += Integer.parseInt(value);
                			break;
                		case 5:
                			qLearningSum += Integer.parseInt(value);
                			break;
                		case 6:
                			qLearningegSum += Integer.parseInt(value);
                			break;
                		case 7:
                			sarsaSum += Integer.parseInt(value);
                			break;
                		case 8:
                			eGreedyActualDOSum += Integer.parseInt(value);
                			break;
                		case 9:
                			eGreedyIntendedDOSum += Integer.parseInt(value);
                			break;
                		case 10:
                			oneHopRandomSum += Integer.parseInt(value);
                			break;
                		case 11:
                			oneHopOptimalSum += Integer.parseInt(value);
                			break;
                	}
                }
            }
            out.println();
        }
        
        for (int i=0 ; i<input.length ; i++)
        	sc[i].close();
        out.close();
        
        System.out.println("====================================");
        System.out.println("Baseline--Optimal ratio = " + df.format(1.0 * baselineSum / optimalSum));
        System.out.println("ZoomOut--Optimal ratio = " + df.format(1.0 * zoomoutSum / optimalSum));
        System.out.println("Random--Optimal ratio = " + df.format(1.0 * randomSum / optimalSum));
        System.out.println("\u03B5greedy--Optimal ratio = " + df.format(1.0 * eGreedySum / optimalSum));
        System.out.println("QLearning--Optimal ratio = " + df.format(1.0 * qLearningSum / optimalSum));
        System.out.println("\u03B5GQLearning--Optimal ratio = " + df.format(1.0 * qLearningegSum / optimalSum));
        System.out.println("Sarsa--Optimal ratio = " + df.format(1.0 * sarsaSum / optimalSum));
        System.out.println("\u03B5greedyActualDO--Optimal ratio = " + df.format(1.0 * eGreedyActualDOSum / optimalSum));
        System.out.println("\u03B5greedyIntendedDO--Optimal ratio = " + df.format(1.0 * eGreedyIntendedDOSum / optimalSum));
        System.out.println("1-HopRandom--Optimal ratio = " + df.format(1.0 * oneHopRandomSum / optimalSum));
        System.out.println("1-HopOptimal--Optimal ratio = " + df.format(1.0 * oneHopOptimalSum / optimalSum));
        
        // deleting redundant files
        for (int i=0 ; i<input.length ; i++) {
        	if(!file[i].delete()){
    			System.out.println("Delete operation failed.");
    		}
        }
	}
}
