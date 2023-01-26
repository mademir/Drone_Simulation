/**
 * 
 */
package drone;

/** Explorer drone that travels around the arena
 * @author Mustafa
 *
 */
public class Explorer extends Drone{
	private static final long serialVersionUID = 1L;
	public static int explorerCt = 0;	//Counter for the explorer drones
	public int explorerId;				//Id of the explorer drone

	Explorer(int x, int y, int rad, int dir, int lifespan) {
		super(x, y, rad, dir, lifespan);
		explorerId = explorerCt++;
		speed = 2;
	}

	/** Display explorer info
	 *
	 */
	@Override
	public String toString() {
		return "Explorer " + String.format("%03d", explorerId) + " is at X: " + format(x) + ", Y: " + format(y) + ". Going " + dir + "°";
	}
	
	/** Display the drone
	 *
	 */
	@Override
	public void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'b');
		c.drawImageWithRotation(MyCanvas.droneOrange, dir+90%360, x*k, y*k, rad*2.8*k, rad*2.8*k);	//Display the explorer drone image
		if (showIds) c.showText(x*k, y*k, String.format("%d", explorerId));
	}
	
	/**Update the drone
	 *
	 */
	@Override
	public void update(DroneArena a) {
		super.update(a);
	}
}
