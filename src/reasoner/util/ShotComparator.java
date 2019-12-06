package reasoner.util;

import java.util.Comparator;

import reasoner.AShot;

public class ShotComparator implements Comparator<AShot> {

	@Override
	public int compare(AShot shot1, AShot shot2) {
		if(shot1.score > shot2.score){
			return -1;
		} 
		
		else if (shot1.score < shot2.score){
			return 1;
		}
		
		else return 0;
	}
}
