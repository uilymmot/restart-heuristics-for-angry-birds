package reasoner;

/**
 * Data structure to store affected object information
 * @author ZP
 *
 */
public class AffectedObject {
	//Consequence
	public final static int PENDING = 0;
	public final static int DESTROY = 1;
	public final static int FALL = 2;
	public final static int HIT = 3;
	public final static int EXPLODE = 4;
	
	//Affected direction
	public final static int FROM_LEFT = 1;
	public final static int FROM_RIGHT = 2;
	
	//Affected method
	public final static int FROM_HIT = 1;
	public final static int FROM_SUPPORTER_DESTROY = 2;
	public final static int FROM_SUPPORTER_FALL = 3;
	public final static int FROM_SUPPORTEE = 4;
	
	public int ID;
	public int affecter;
	public int affectedDirection;
	public int Consequence;
	public int affectedMethod;
	public int possibleAffectedTime;
	public boolean highTrajReachability;
	public boolean lowTrajReachability;
	
	public AffectedObject(int ID){
		this.ID = ID;
		this.possibleAffectedTime = 0;
		this.affectedDirection = -1;
		this.affectedMethod = -1;
		this.Consequence = -1;
		this.affecter = -1;
		this.highTrajReachability = false;
		this.lowTrajReachability = false;
	}
	
	public void setID(int ID){
		this.ID = ID;
	}
	
	public void setHighTrajReachability(boolean reachability){
		this.highTrajReachability = reachability;
	}
	
	public void setLowTrajReachability(boolean reachability){
		this.lowTrajReachability = reachability;
	}
	
	public void setDirection(int direction){
		this.affectedDirection = direction;
	}
	
	public void setConsquence(int cons){
		this.Consequence = cons;
	}
	
	/*Affected by supporter, supportee, or direct hit*/
	public void setAffectedMethod(int method){
		this.affectedMethod = method;
	}
	
	public void setAffecter(int affecter){
		this.affecter = affecter;
	}
	
	public static int OppositeDirection(int direction){
		if(direction == FROM_LEFT){
			return FROM_RIGHT;
		}
		
		else{
			return FROM_LEFT;
		}
	}
	
}
