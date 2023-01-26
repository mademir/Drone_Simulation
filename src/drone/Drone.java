/**
 * 
 */
package drone;

/**
 * @author Mustafa
 *
 */
public class Drone extends Entity{
	private static final long serialVersionUID = 1L;
	public static int droneCt = 0;		//Counter for all the drones of all types
	public int droneId;					//Id of the drone
	protected double speed = 1;			//Default speed
	protected int dir;					//Direction of the drone
	public int lifespan;
	protected int frame = 0;
	
	Drone(int x, int y, int rad, int dir, int lifespan) {
		super(x, y, rad);
		this.dir = dir;
		this.lifespan = lifespan;
		droneId = droneCt++;
	}
	
	public static enum Types{EXPLORER, ATTACKER};	//Enum for the type of the drone
	
	@Override
	public String toString() {
		return "Drone " + String.format("%03d", droneId) + " is at X: " + format(x) + ", Y: " + format(y) + ". Going " + dir + "°";
	}
	
	/**To format the numeric value used in toString()
	 * @param x the numeric value to be formatted
	 * @return the formatted string
	 */
	protected static String format(double x) {
		int beforeP = (int)Math.log10(x);
		return String.format("%."+ (3 - beforeP) +"f", x);
	}
	
	/**Displays the drone
	 * @param c Canvas to display the drone on
	 * @param a Arena the drone is in
	 */
	@Override
	public void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'b');
		c.drawImageWithRotation(MyCanvas.droneOrange, dir+90%360, x*k, y*k, rad*2.8*k, rad*2.8*k);	//Display the image of the drone
	}
	
	/** To update and adjust the drone
	 * @param a Arena the drone is in
	 */
	@Override
	public void update(DroneArena a) {
		if (dir%30 == 0) dir += (dir+1) %360;
		frame++;
		if ((frame/JavaFX.FPS)/lifespan >= 1) {	//If reached its life span, flag the drone to be removed from the arena
			a.toRemove.add(this);
			return;
		}
		
		double dx = 0, dy = 0;
		
		double radAngle = Math.toRadians(dir); 	//Angle in radians
		dx = speed * Math.cos(radAngle);		//Delta x and y calculated from the speed and the direction the drone is going
		dy = speed * Math.sin(radAngle);
		
		double speedMult = JavaFX.speed.getValue();		//Get the speed multiplier from the speed slider
		
		//Apply the speed multiplier
		dx *= speedMult;
		dy *= speedMult;
		
		if (a.canMoveHere(id, x + dx, y + dy, rad)) {	//If the drone can move in the next position, add the delta values to the current position
			x += dx;
			y += dy;
		}
		else dir = a.changeDir(id, x+dx, y+dy, rad, dir, this); //Adjust the direction if cannot move
	}
}