/**
 * 
 */
package drone;

/**	Obstacle that stay in fixed positions and collides with other entities
 * @author Mustafa
 *
 */
public class Obstacle extends Entity{
	private static final long serialVersionUID = 1L;
	private int rot = 0;				//Rotation angle
	public static int obstacleCt = 0;	//Counter for the obstacles
	private int obstacleId;				//ID of the obstacle
	
	Obstacle(int x, int y, int rad) {
		super(x, y, rad);
		obstacleId = obstacleCt++;
	}
	
	/**Displays the obstacle
	 * @param c Canvas to display the obstacle on
	 * @param a Arena the obstacle is in
	 */
	@Override
	void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'o');
		c.drawImageWithRotation(MyCanvas.obstacle1, rot, x*k, y*k, rad*2.6*k, rad*2.6*k);	//Draw obstacle image
		if (showIds) c.showText(x*k, y*k, String.format("%d", obstacleId));
	}

	/**Update the obstacle
	 *
	 */
	@Override
	void update(DroneArena a) {
		rot = (++rot%360);		//Iterate the rotation
	}

	/** Return obstacle info
	 *
	 */
	@Override
	public String toString() {
		return "Obstacle " + String.format("%03d", obstacleId) + " is at X: " + format(x) + ", Y: " + format(y) + ".";
	}
	
	/**To format the numeric value used in toString()
	 * @param x the numeric value to be formatted
	 * @return the formatted string
	 */
	private String format(double x) {
		int beforeP = (int)Math.log10(x);
		return String.format("%."+ (3 - beforeP) +"f", x);
	}

}
