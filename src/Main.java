import java.io.IOException;
import java.text.DecimalFormat;

/*
 * 1-hop Random and 1-hop optimal algorithms added. Also eGreedyActual and eGreedyIntended removed (15)
 * 4 e-greedy algorithms with gradual zoom changes (eGreedyActual, eGreedyIntended, eGreedyActualDO, eGreedyIntendedDO) added (14)
 * Sarsa algorithm added (13.2)
 * Q-Learning with e-Greedy selection during each cycle added (13.1)
 * Q-Learning algorithm (with random zoom selection during training) implemented (13)
 * Distance metric (in 12.1) is replaced with ratios and 3 other ratios (baseline,zoomout,random) added as well (12.2) 
 * Calculation of distance metric between optimal and epsilon-greedy added. (12.1)
 * Event-shaped objects (with waypoints) are added to enable defining trajectories. (12)
 * Each algorithm has its own class now and re-written to increase efficiency and speed. (11.2)
 * New (corrected) version of optimal algorithm implemented. Also, individual algorithm running times added. (v11.1)
 * This version is extended from v10.1 not v10.2. The epsilon-greedy is now implemented. (v11)
 * The optimal threshold selection bug is now fixed. (v10)
 * The zoomOutLine and randomLine computation added. (v10.1)
 */

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class Main {
	
  static final String XmlFile = "random2s";
  static final int STEPS = 300;
  static final Double THRESHOLD = 0.3;
  static final String[] FILES = {"baseline","optimal","zoomout","random","egreedy","qlearning","qlearning-eg","sarsa","egreedy-act-do","egreedy-int-do","oneHopRandom","oneHopOptimal"}; //don't change this order or distance metric won't work!
  static final int QL_CYCLES = 1000;
  
//  static String MAINPATH = "C:\\Users\\vejdanpa\\Desktop\\CamSim 15\\CamSimLite"; 
//  static String XmlFilePath = MAINPATH + "\\scenarios\\"+XmlFile+".xml";
//  static String OutputFilePath = MAINPATH + "\\output\\"+XmlFile+"-";
  static String MAINPATH = "/Users/ali/Desktop/CamSim 15/CamSimLite"; 
  static String XmlFilePath = MAINPATH + "/scenarios/"+XmlFile+".xml";
  static String OutputFilePath = MAINPATH + "/output/"+XmlFile+"-";
  static long seed = 0;
 
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		
		Settings settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		Density density = new Density(settings);
		long densityTime = System.currentTimeMillis();		

		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		Optimal optimal = new Optimal(settings, STEPS, THRESHOLD, OutputFilePath);
		long optimalTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new Baseline(settings, STEPS, THRESHOLD, OutputFilePath, optimal.getStep0CamConfig());
		long baseTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new ZoomOut(settings, STEPS, THRESHOLD, OutputFilePath);
		long zoomoutTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new Arbitrary(settings, STEPS, THRESHOLD, OutputFilePath);
		long arbitraryTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new EpsilonGreedy(settings, STEPS, THRESHOLD, OutputFilePath);
		long egreedyTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		Settings settingsCopy = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new QLearning(settings, STEPS, THRESHOLD, OutputFilePath, QL_CYCLES, settingsCopy);
		long qlearningTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		settingsCopy = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new QLearningEG(settings, STEPS, THRESHOLD, OutputFilePath, QL_CYCLES, settingsCopy);
		long qlearningegTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new Sarsa(settings, STEPS, THRESHOLD, OutputFilePath);
		long sarsaTime = System.currentTimeMillis();

		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new EpsilonGreedyActualDO(settings, STEPS, THRESHOLD, OutputFilePath);
		long egreedyActualDOTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new EpsilonGreedyIntendedDO(settings, STEPS, THRESHOLD, OutputFilePath);
		long egreedyIntendedDOTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new OneHopArbitrary(settings, STEPS, THRESHOLD, OutputFilePath);
		long oneHopArbitraryTime = System.currentTimeMillis();
		
		settings = new Settings(XmlFilePath, new RandomNumberGenerator(seed));
		new OneHopOptimal(settings, STEPS, THRESHOLD, OutputFilePath);
		long oneHopOptimalTime = System.currentTimeMillis();
		
		new ResultWriter("result",OutputFilePath,FILES,STEPS);

		
		
		DecimalFormat df = new DecimalFormat("#.####");
		System.out.println("==========================================");
		System.out.println("Density 1 (camera to object ratio): " + df.format(density.density1));
		System.out.println("Density 2 (cameras per square unit): " + df.format(density.density2));
		System.out.println("Density 3 (covered area excluding multiple overlaps): " + df.format(density.density3));
//		System.out.println("Density 4.0 (covered area including multiple overlaps): " + df.format(density.density4_0));
//		System.out.println("Density 4.1 (covered area including multiple overlaps): " + df.format(density.density4_1));
		
		System.out.println("==========================================");
		System.out.println("Computing Densities "+(densityTime-startTime)+" ms");
		System.out.println("Computing Optimal algorithm   "+(optimalTime-densityTime)+" ms");
		System.out.println("Computing BaseLine algorithm  "+(baseTime-optimalTime)+" ms");
		System.out.println("Computing Zoom-out algorithm  "+(zoomoutTime-baseTime)+" ms");
		System.out.println("Computing Random  algorithm   "+(arbitraryTime-zoomoutTime)+" ms");
		System.out.println("Computing \u03B5-greedy algorithm  "+(egreedyTime-arbitraryTime)+" ms");
		System.out.println("Computing Q-Learning algorithm\t   "+(qlearningTime-egreedyTime)+" ms");
		System.out.println("Computing \u03B5g-Q-Learning algorithm  "+(qlearningegTime-qlearningTime)+" ms");
		System.out.println("Computing Sarsa algorithm  "+(sarsaTime-qlearningegTime)+" ms");
		System.out.println("Computing \u03B5-greedy-actual-do algorithm  "+(egreedyActualDOTime-sarsaTime)+" ms");
		System.out.println("Computing \u03B5-greedy-intended-do algorithm  "+(egreedyIntendedDOTime-egreedyActualDOTime)+" ms");
		System.out.println("Computing 1-hop Random algorithm  "+(oneHopArbitraryTime-egreedyIntendedDOTime)+" ms");
		System.out.println("Computing 1-hop Optimal algorithm  "+(oneHopOptimalTime-oneHopArbitraryTime)+" ms");
		System.out.println("==========================================");
		System.out.println("Total Running time  = "+(System.currentTimeMillis()-startTime)+" ms");
	}
	
}