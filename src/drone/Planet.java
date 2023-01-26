/**
 * 
 */
package drone;

import java.util.Random;

/** Planet that stays in a fixed position and spawns Explorer drones
 * @author Mustafa
 *
 */
public class Planet extends Entity{
	private static final long serialVersionUID = 1L;
	private int frame = 0;	//Frame counter of the image
	private int rot;		//Fixed rotation of the planet
	public static int planetCt = 0;	//Counter for the planets
	private int planetId;		//Id of the planet
	
	Planet(int x, int y, int rad, int rot) {
		super(x, y, rad);
		planetId = planetCt++;
		this.rot = rot;
	}

	/**Display the planet
	 *
	 */
	@Override
	void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'g');
		c.drawImageWithRotation(MyCanvas.planet1Images.get(frame % 93 / 3), rot, x*k, y*k, rad*1.8*k, rad*1.8*k); //93 because (max frame - 1) * 3
		if (showIds) c.showText(x*k, y*k, String.format("%d", planetId));
	}

	/** Update the planet.
	 *	Spawn explorer drones every interval
	 */
	@Override
	void update(DroneArena a) {
		frame++;
		if ((double)frame/JavaFX.FPS % 5 == 0) {	//Every 5 seconds
			int randomAngOffset = new Random().nextInt(360);	//Random initial angle to start trying to spawn a drone from
			for (int ang = 0; ang<360; ang+=30) {		//Start trying to spawn a drone around the planet
				ang = (ang + randomAngOffset) % 360;
				double dist = 25;						//Distance of the spawn from the planet centre
				int dx = (int)(x+Math.cos(ang)*dist);
				int dy = (int)(y+Math.sin(ang)*dist);
				int dir = (int) (Math.toDegrees(Math.atan2(dy - y, dx - x))+360)%360;	//Direction for the drone. The angle from the planet to spawn location
				if (a.addDrone(dx, dy, dir, 15, Drone.Types.EXPLORER)) break;	//Try spawning a drone with 15 second lifespan. If spawned successfully, stop
			}
		}
	}

	/**Display planet information
	 *
	 */
	@Override
	public String toString() {
		return "Planet " + String.format("%03d", planetId) + " is at X: " + format(x) + ", Y: " + format(y) + ".";
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
