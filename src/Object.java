import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
*
* @author Arezoo Vejdanparast <vejdanpa@aston.ac.uk> & Ali Karami <ali.karami@alumni.york.ac.uk>
*/
public class Object {
	
	String id;
	Double x,y;
	Double heading;
	Double speed;
	Field field;
	RandomNumberGenerator randomGen;
	int timestep;
	ArrayList<Point2D> waypoints;
	int nextWaypointIndex;
	
	/**
	 * Constructor
	 */
	public Object (String id, Double x, Double y, Double heading, Double speed, Field field, RandomNumberGenerator rand) {
		this.id = id;
		this.timestep = 0;
		this.x = x;
		this.y = y;
		this.heading = heading;
		this.speed = speed;
		this.field = field;
		this.randomGen = rand;
	}
	
	
	/**
	 * Constructor
	 */
	public Object (String id, int timestep, Double speed, ArrayList<Point2D> waypoints, Field field) {
		this.id = id;
		this.timestep = timestep;
		this.x = waypoints.get(0).getX();
		this.y = waypoints.get(0).getY();
		this.waypoints = waypoints;
		this.nextWaypointIndex = 1;
		this.heading = 0.0;
		this.speed = speed;
		this.field = field;
	}
	
	
	/**
	 * Updates the position of the object based on current position, heading and speed 
	 */
	public void update() {
		if (waypoints == null) 
		{
	        double x_move = 0;
	        double y_move = 0;
	        
	        x_move = Math.sin(heading) * speed;
	        y_move = Math.cos(heading) * speed;
	        
	        this.x += x_move;
	        this.y += y_move;
	        
	        checkBoundaryCollision(x_move, y_move);
		}
		else 
		{
			Point2D nextPoint = waypoints.get(nextWaypointIndex);
			double distance = nextPoint.distance(this.x, this.y);

			if (distance <= speed)
			{
				this.x = nextPoint.getX();
				this.y = nextPoint.getY();
				updateWayPointIndex();
			}
			else
			{
				this.x += speed * (nextPoint.getX() - this.x) / distance;
				this.y += speed * (nextPoint.getY() - this.y) / distance;
			}
		}
	}
	
	
	/**
     * checks if a boundary collision will occur after the next move and makes the accoring bounce if necessary
     * @param x_move future move towards x
     * @param y_move future move towards y
     */
    public void checkBoundaryCollision(double x_move, double y_move){
     // If we breach any boundary, bounce off at a slightly randomised angle
        if (this.x > field.maxX || this.x < field.minX ||
                this.y > field.maxY || this.y < field.minY){
            this.heading += getTurnaroundAngle(); 
            
            // Undo move across boundary
            this.x -= x_move; 
            this.y -= y_move;
        }
    }
    

    /**
     * Gets an angle to turn around by with some partial added randomness  
     * @return angle to bounce of wall
     */
    public double getTurnaroundAngle() {
        // Turn around 180 degrees, add a bit of angle for randomness
        double angle = Math.PI; // Turn 180 degrees
        angle += (randomGen.nextDouble()*2-1.0) * Math.PI / 6.0; // adds -30.0 to +30.0 degrees (at random) to bounced angle
        return angle;
    }
	

	/**
	 * Updates the index(pointer) of the next waypoint target.
	 */
    private void updateWayPointIndex() {
    	nextWaypointIndex++;
		if (nextWaypointIndex == waypoints.size())
			nextWaypointIndex = 0;
	}

}
