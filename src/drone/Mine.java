package drone;

/**
 * @author Mustafa
 *
 */
public class Mine extends Entity{
	private static final long serialVersionUID = 1L;
	private int rot = 0;				//Rotation angle
	public static int mineCt = 0;	//Counter for the mines
	private int mineId;				//ID of the mine
	
	Mine(int x, int y, int rad) {
		super(x, y, rad);
		mineId = mineCt++;
	}
	
	/**Displays the mine
	 * @param c Canvas to display the mine on
	 * @param a Arena the mine is in
	 */
	@Override
	void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'r');
		c.drawImageWithRotation(MyCanvas.mine, rot, x*k, y*k, rad*2*k, rad*2*k);	//Draw mine image
		if (showIds) c.showText(x*k, y*k, String.format("%d", mineId));
	}

	/**Update the mine
	 *
	 */
	@Override
	void update(DroneArena a) {
		rot = (++rot%360);		//Iterate the rotation
	}

	/** Return mine info
	 *
	 */
	@Override
	public String toString() {
		return "Mine " + String.format("%03d", mineId) + " is at X: " + format(x) + ", Y: " + format(y) + ".";
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
