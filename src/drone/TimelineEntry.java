/**
 * 
 */
package drone;

/**	A Timeline Entry
 * @author Mustafa
 *
 */
public class TimelineEntry {
	public byte[] save;			//Byte array for the saved arena instance
	public double timestamp;	//The timestamp in seconds
	
	TimelineEntry(byte[] save, double timestamp){
		this.save = save;
		this.timestamp = timestamp;
	}
}
