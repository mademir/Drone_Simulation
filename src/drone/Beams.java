/**
 * 
 */
package drone;

import java.io.Serializable;

/** Beams that travels a fixed distance and dies. Destroys explorer drones on hit
 * @author Mustafa
 *
 */
public class Beams implements Serializable{
	private static final long serialVersionUID = 1L;
	private double x;		//Current x pos
	private double sx;		//Start x pos
	private double y;		//Current y
	private double sy;		//Start y
	private double dist;	//Max distance to travel before dying
	private int rad = 5;	//Radius of the beam
	private double speed = 5;	//Speed of the beam
	private double dir;		//Fixed direction of travel
	public boolean destroyed = false;	//Flag to check if the beams are dead
	
	Beams(double sx, double sy, double dist, double dir){
		this.sx = sx;
		this.sy = sy;
		x = sx;
		y = sy;
		this.dir = dir;
		this.dist = dist;
	}
	
	/** Update the beams
	 * @param a Arena the beams are in
	 */
	public void update(DroneArena a) {
		double speedMultiplier = JavaFX.speed.getValue();	//Get the speed multiplier value from the speed slider
		double nextX = x + Math.cos(Math.toRadians(dir)) * speed * speedMultiplier;		//Calculate the next position from the direction and the speed
		double nextY = y + Math.sin(Math.toRadians(dir)) * speed * speedMultiplier;
		if (Math.pow(sx - x, 2) + Math.pow(sy - y, 2) < Math.pow(dist, 2)) {	//if the distance travelled is less than max distance
			x = nextX;
			y = nextY;
			Entity hit = a.getEntityAt(-1, x, y, rad);	//Check hit
			if (hit != null) {							//If hit
				if (hit.getClass() == Planet.class || hit.getClass() == Obstacle.class) destroyed = true;	//If hit a planet or an obstacle, kill the beams
				else if(hit.getClass() == Explorer.class) {		//If hit an explorer
					((Explorer) hit).lifespan = 1;	//Kill the explorer by setting its lifespan to 1
					destroyed = true;	//Destroy the beams
				}
			}
		}
		else {	//if reached max distance
			destroyed = true;	//Kill the beams
		}
		
	}
	
	/** Display the beams on the canvas
	 * @param c Canvas to display on
	 */
	public void display(MyCanvas c) {
		double k = JavaFX.zoom.getValue();	//Zoom value from the zoom slider
		c.drawImageWithRotation(MyCanvas.beams, (dir+90)%360, x*k, y*k, rad*2.8*k, rad*2.8*k);		//Draw beams image on the canvas
	}
}
