package reasoner;

import java.util.HashMap;

/**
 * Data structure to store the evaluation information of a shot
 * @author ZP
 *
 */
public class AShot {
	public int ID;
	public int score;
	public int direction;
	public HashMap<Integer, AffectedObject> affectedObjs;
	public int highOrLow = 0;
	public boolean highReachability;
	public boolean lowReachability;
	
	public AShot(int ID){
		this.ID = ID;
		this.highReachability = false;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	public void setHighReachability(boolean reachability){
		this.highReachability = reachability;
	}
	
	public void setLowReachability(boolean reachability){
		this.lowReachability = reachability;
	}
	public void setDirection(int direction){
		this.direction = direction;
	}
	
	public void setConsequence(HashMap<Integer, AffectedObject> affectedObjs){
		this.affectedObjs = affectedObjs;
	}
	
	@Override
	public int hashCode() {
		return 37;
	}
	
	@Override
	public boolean equals(Object o){
		AShot shot = (AShot) o;
		if(this.ID == shot.ID && this.direction == shot.direction){
			return true;
		}
		
		else return false;
	}

	@Override 
	public String toString(){
		String result = "";
		String direction;
		String consequence;
		AffectedObject ao;
		if(this.direction == AffectedObject.FROM_LEFT){
			direction = "From Left";
		}
		
		else{
			direction = "From Right";
		}
	
		result += ("Target ID: " + this.ID + "\n");
		result += ("Reachability for high traj: " + this.highReachability+ "\n");
		result += ("Reachability for low traj: " + this.lowReachability+ "\n");
		result += ("score: " + this.score + "\n");
		result += (direction + "\n" + "Affected objects: \n");
		for(int id : this.affectedObjs.keySet()){
			ao = this.affectedObjs.get(id);
			if(ao.Consequence == AffectedObject.DESTROY){
				consequence = "Destroied";
			}
			
			else {
				consequence = "Fall";
			}
			result += (ao.ID+ " Consequence: "  + consequence + " ");
			result += ("affecter: " + ao.affecter + "\n"); 
		}
		return result;
	}
}
