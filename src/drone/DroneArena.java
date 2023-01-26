package drone;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class DroneArena implements Serializable{
	private static final long serialVersionUID = 1L;
	private int w;	//Width of the arena
	private int h;	//Height of the arena
	ArrayList<Entity> entities;		//All the entities in the arena
	ArrayList<Entity> toRemove = new ArrayList<Entity>();	//Entities to remove from the arena
	Random randomiser = new Random();	//Randomiser
	
	public int getW() { return w;}	//Width getter
	public int getH() { return h;}	//Height getter
	
	DroneArena(int w, int h){
		this.w = w;
		this.h = h;
		entities = new ArrayList<Entity>();
	}
	
	/** Adds a drone of type at a random position
	 * @param type Type of the drone
	 */
	public void addDrone(Drone.Types type) {
		if (entities.size() >= w*h) return;
		int x, y, rad = 10, ct = 0;
		switch (type) {			//Random radius according to the type
		case EXPLORER:
			rad = randomiser.nextInt(3)+10;
			break;
		case ATTACKER:
			rad = randomiser.nextInt(5)+12;
			break;
		}
		
		//Get a random position and check if the position is free for spawning. Up to 100 tries
		do {
			x = randomiser.nextInt(w);
			y = randomiser.nextInt(h);
			ct++;
		}
		while(!canMoveHere(-1, x, y, rad) && ct < 100);//Used -1 as the entity to be added does not have an id and -1 will never be assigned to any other entities
		
		if (ct >= 100) {
			System.out.println("Too crowded!");		//If tried 100 times and cannot find a position to place the drone
			return;
		}
		
		//Spawn the relative drone
		switch (type) {
		case EXPLORER:
			this.entities.add(new Explorer(x, y, rad, randomiser.nextInt(360), -1));
			break;
		case ATTACKER:
			this.entities.add(new Attacker(x, y, rad, randomiser.nextInt(360), -1));
			break;
		}
		
	}
	
	/** Add a drone of type at a specified position and direction with lifespan value
	 * @param x
	 * @param y
	 * @param dir
	 * @param lifespan
	 * @param type
	 * @return
	 */
	public Boolean addDrone(int x, int y, int dir, int lifespan, Drone.Types type) {
		int rad = 10;
		if (canMoveHere(-1, x ,y, rad)) {
			switch (type) {
			case EXPLORER:
				this.entities.add(new Explorer(x, y, rad, dir, lifespan));
				break;
			case ATTACKER:
				this.entities.add(new Attacker(x, y, rad, dir, lifespan));
				break;
			}
			return true;
		}
		else return false;
	}
	
	/** Add an obstacle in the arena, at a random position
	 * Same logic is used as adding drones
	 */
	public void addObstacle() {
		int x, y, rad = randomiser.nextInt(5) + 5, ct = 0;
				
		do {
			x = randomiser.nextInt(w);
			y = randomiser.nextInt(h);
			ct++;
		}
		while(!canMoveHere(-1, x, y, rad) && ct < 100);//Used -1 as the entity to be added does not have an id and -1 will never be assigned to any other entities
		
		if (ct >= 100) {
			System.out.println("Too crowded!");
			return;
		}
		this.entities.add(new Obstacle(x, y, rad));
	}
	
	/** Add a mine in the arena, at a random position
	 * Same logic is used as adding drones
	 */
	public void addMine() {
		int x, y, rad = randomiser.nextInt(5) + 5, ct = 0;
		
		do {
			x = randomiser.nextInt(w);
			y = randomiser.nextInt(h);
			ct++;
		}
		while(!canMoveHere(-1, x, y, rad) && ct < 100);//Used -1 as the entity to be added does not have an id and -1 will never be assigned to any other entities
		
		if (ct >= 100) {
			System.out.println("Too crowded!");
			return;
		}
		this.entities.add(new Mine(x, y, rad));
	}
	
	/** Add a planet in the arena, at a random position
	 * Same logic is used as adding drones
	 */
	public void addPlanet() {
		int x, y, rad = randomiser.nextInt(5) + 8, ct = 0;
		
		do {
			x = randomiser.nextInt(w);
			y = randomiser.nextInt(h);
			ct++;
		}
		while(!canMoveHere(-1, x, y, rad) && ct < 100);//Used -1 as the entity to be added does not have an id and -1 will never be assigned to any other entities
		
		if (ct >= 100) {
			System.out.println("Too crowded!");
			return;
		}
		this.entities.add(new Planet(x, y, rad, randomiser.nextInt(360)));
	}
	
	/** Get the arena information and the entity information of each entity in the arena in a string
	 *
	 */
	public String toString(){
		String str = "Size: " + w + "x" + h + ".\n";
		for (Entity e: entities) str += e.toString() + '\n';
		return str;
	}
	
	/** Checks if an entity is at a position + radius
	 * @param id
	 * @param x
	 * @param y
	 * @param rad
	 * @return null if no entity found, entity if found
	 */
	public Entity getEntityAt(int id, double x, double y, int rad) {
		for(Entity e : entities) {
			if (e.id == id) continue;
			double dist = Math.sqrt(Math.pow(Math.abs(e.x - x), 2) + Math.pow(Math.abs(e.y - y), 2));
			if (dist <= e.rad + rad) return e;
		}
		return null;
	}
	
	/** Display every entity in the arena
	 * @param c Canvas to display on
	 */
	public void showEntites(MyCanvas c) {
		for (Entity e : entities) e.display(c, this);
	}
	
	/** Check if an entity can move at the given position + radius
	 * @param id ID of the entity trying to move
	 * @param x
	 * @param y
	 * @param rad
	 * @return false if cannot move, true if can
	 */
	public boolean canMoveHere(int id, double x, double y, int rad) {
		if (x + rad >= w || y + rad >= h || x - rad < 0 || y - rad < 0) return false;	//Wall detection
		return getEntityAt(id, x, y, rad) == null;
	}
	
	/** Changes the direction of the entity according to collisions
	 * @param id ID of the entity changing direction
	 * @param x
	 * @param y
	 * @param rad
	 * @param dir
	 * @return The new direction
	 */
	public int changeDir(int id, double x, double y, int rad, int dir, Entity e) {
		int res = dir;
		if (x < rad || x > w - rad) res =  180 - dir; 	//if hitting left or right walls, return mirrored angle
		if (y < rad || y > h - rad) res =  -dir; 		//if hitting top or bottom, return mirrored  angle
		
		Entity o = getEntityAt(id, x, y, rad);
		if (o != null && o instanceof Mine) {	//If hit a mine
			toRemove.add(e);	//Remove drone
			toRemove.add(o);	//Remove mine
		}
		if (o != null) res = (int) Math.toDegrees(Math.atan2(y - o.y, x - o.x)); //if hitting another entity, return the angle between		  
		 
		while (res < 0) res += 360;	//Put the angle back in 0-360 range if outside
		res %= 360;
		
		return res;
	}
	
	/** Updates every entity in the arena
	 *  Removes the entities in the toRemove list from the arena
	 */
	public void updateEntities(MyCanvas m) {
		for (Entity e : toRemove){
			if (entities.contains(e)) {
				double k = JavaFX.zoom.getValue();
				m.displayBlast(e.x*k, e.y*k, 40*k);
				entities.remove(e);
			}
		}
		for (int i = 0; i<entities.size(); i++) entities.get(i).update(this);
	}
	
	/** Counts the entities of each type and sets the counter for each of them
	 * 
	 */
	public void recalculateCts() {
		Explorer.explorerCt = 0;
		Attacker.attackerCt = 0;
		Obstacle.obstacleCt = 0;
		Planet.planetCt = 0;
		for (Entity e : entities) {
			if (e.getClass() == Explorer.class) Explorer.explorerCt++;
			if (e.getClass() == Attacker.class) Attacker.attackerCt++;
			if (e.getClass() == Planet.class) Planet.planetCt++;
			if (e.getClass() == Obstacle.class) Obstacle.obstacleCt++;
		}
	}
	
	/** Check if any explorer drones are in the given sight cone
	 * @param e The attacker doing the check
	 * @param x
	 * @param y
	 * @param vision Length of the vision
	 * @param dir	Centre direction of the vision cone
	 * @param angle	Angle range of the vision cone
	 * @return
	 */
	public boolean checkSight(Attacker e, double x, double y, double vision, double dir, double angle) {
		Entity t = null;
		double dist = 0, ang = 0, diff = 0;
		for(Entity o : entities) {
			if (o.getClass() != Explorer.class) continue;	//Filter to explorer type only
			dist = Math.sqrt(Math.pow(Math.abs(o.x - x), 2) + Math.pow(Math.abs(o.y - y), 2));	//Distance between
			ang = (Math.toDegrees(Math.atan2(y - o.y, x - o.x))+540)%360;	//Angle between the attacker and the other
			diff = Math.abs(ang - dir);
			diff = diff > 180 ? 360 - diff : diff;	//angle difference
			if (dist <= o.rad + vision && diff < angle/2) {	//If in range and in sight
				t = o;	//Select as target
				break;
			}
		}
		
		if (t != null) {	//If detected explorer
			double moveAmount = Math.min(diff, 10);
			if (diff > 1) e.dir += ((ang - dir) > 0 ? 1 : -1) * moveAmount;	//Rotate towards the explorer drone
			return true;
		}
		else return false;
	}
}
