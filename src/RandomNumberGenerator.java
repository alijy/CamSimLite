import java.util.Random;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class RandomNumberGenerator {

	private long _seed;
	private Random random = null;
	
	
    /**
     * Constructor for RandomNumberGenerator
     * @param seed initial seed
     */
	public RandomNumberGenerator(long seed) {
		this._seed = seed;
		this.random = new Random(seed);
	}
	
    /**
     * Constructor for RandomNumberGenerator
     * @param seed initial seed
     */
	public RandomNumberGenerator() {
		this.random = new Random();
	}

	
    /**
     * get a random double number
     * @return
     */	
	public double nextDouble() {
		return random.nextDouble();
	}
	
	
    /**
     * get a random integer number
     * @return
     */	
	public int nextInt(int bound) {
		return random.nextInt(bound);
	}
	
	
    /**
     * return initial seed
     * @return
     */
    public long getSeed() {
        return _seed;
    }

	
}
