/**
 * 
 */
package drone;

import java.io.Serializable;

/** Abstract class for all the entities
 * @author Mustafa
 *
 */
public abstract class Entity implements Serializable{
	private static final long serialVersionUID = 1L;
	public double x;		//X pos of the entity
	public double y;		//Y pos of the entity
	public int id;			//ID of the entity
	public int rad;			//Radius of the entity
	public static int ct = 0;	//Counter for all the entities
	public static boolean showIds = false;	//Flag to show ID while displaying the entity
	
	Entity(int x, int y, int rad) {
		this.x = x;
		this.y = y;
		this.rad= rad;
		this.id = ct++;
	}
	
	abstract void display(MyCanvas c, DroneArena a);	//Display the entity
	abstract void update(DroneArena a);					//Update the entity logic
	public abstract String toString();					//Get entity information
}
