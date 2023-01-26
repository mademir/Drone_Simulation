/**
 * 
 */
package drone;

import java.util.ArrayList;

/** Attacker drone that detects and shoots beams at the explorer drones to destroy them
 * @author Mustafa
 *
 */
public class Attacker extends Drone{
	private static final long serialVersionUID = 1L;
	public static int attackerCt = 0;		//Attacker counter
	public int attackerId;					//Id of the attacker drone
	private double vision = 10;				//Sight distance of the drone
	private boolean isAttacking = false;	//Flag to represent if the drone is currently attacking
	private ArrayList<Beams> beams = new ArrayList<Beams>();	//To store the beams shot

	Attacker(int x, int y, int rad, int dir, int lifespan) {
		super(x, y, rad, dir, lifespan);
		attackerId = attackerCt++;
	}

	/** Display attacker drone information
	 *
	 */
	@Override
	public String toString() {
		return "Attacker " + String.format("%03d", attackerId) + " is at X: " + format(x) + ", Y: " + format(y) + ". Going " + dir + "°";
	}
	
	/** Display the attacker drone, its vision cone and the beams shot
	 *
	 */
	@Override
	public void display(MyCanvas c, DroneArena a) {
		double k = JavaFX.zoom.getValue();
		c.showCircle(x*k, y*k, rad*k, 'y');
		c.drawCone(x*k, y*k, rad*k*vision, dir, 60, isAttacking ? 'r':'y');	//Display vision cone
		c.drawImageWithRotation(MyCanvas.droneAttacker, dir+90%360, x*k, y*k, rad*2.8*k, rad*2.8*k);	//Display the attacker drone image
		if (showIds) c.showText(x*k, y*k, String.format("%d", attackerId));
		
		for (Beams b : beams) {		//For each beams shot
			if (!b.destroyed) {		//If the beams are still alive
				b.display(c);		//Display the beams
			}
		}
	}
	
	/** Update the attacker drone
	 *  Update the beams 
	 */
	@Override
	public void update(DroneArena a) {
		super.update(a);
		isAttacking = a.checkSight(this, x, y, (rad*vision)/2, dir, 60);	//Check if explorer drone in sight
		if (!isAttacking) vision = 10 + Math.sin(frame/10.0) * 1.5;	//shrink/grow effect for the vision cone while searching
		
		if (isAttacking && frame % (int)(JavaFX.FPS / (4 * JavaFX.speed.getValue())) == 0) {		//Every interval, if currently attacking
			beams.add(new Beams(x, y, vision * rad/2, dir));	//Generate beams
		}
		
		for (Beams b : beams) {		//For each beams
			if (!b.destroyed) {		//If the beam is still alive
				b.update(a);		//Call beams update
			}
		}
	}
}
