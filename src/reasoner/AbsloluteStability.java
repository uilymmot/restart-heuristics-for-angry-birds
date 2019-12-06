package reasoner;

import java.util.HashMap;
import java.util.TreeMap;

import KR.PhysicalRelation.SUPPORT_RELATION;
import KR.SpatialRelation.ERA;
import KR.StructureAnalyse;
import KR.util.KeyPair;
import KR.util.VisionObject;

/**
 * check the structure stability of a affected structure
 * 
 * @author ZP
 * 
 */
public class AbsloluteStability {
	private VisionObject obj;
	private StructureAnalyse sa;
	private HashMap<KeyPair, ERA[]> RA;
	private HashMap<Integer, AffectedObject> affectObjs;

	public AbsloluteStability(VisionObject obj, StructureAnalyse sa,
			HashMap<KeyPair, ERA[]> RA,
			HashMap<Integer, AffectedObject> affectObjs) {
		this.obj = obj;
		this.sa = sa;
		this.RA = RA;
		this.affectObjs = affectObjs;
	}

	public AbsloluteStability(VisionObject obj, StructureAnalyse sa,
			HashMap<Integer, AffectedObject> affectObjs) {
		this.obj = obj;
		this.sa = sa;
		this.RA = sa.RA;
		this.affectObjs = affectObjs;
	}

	public boolean checkStability() {
		boolean rightSupporter = false;
		boolean leftSupporter = false;
		boolean centreSupporter = false;
		int numRightSupportee = 0;
		int numLeftSupportee = 0;
		ERA RAx;
		KeyPair p = null;
		TreeMap<Integer, Integer> supporteelist = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> supporterlist = new TreeMap<Integer, Integer>();
		
		this.sa.getSupportee(obj, supporteelist);
		this.sa.getSupportor(obj, supporterlist, 1);

		for (int id : supporteelist.keySet()) {
			if (this.affectObjs != null) {
				if (this.affectObjs.containsKey(id)) {
					continue;
				}
			}

			p = new KeyPair(id, obj.id);
			RAx = RA.get(p)[0];
			if (RAx == ERA.LESS_OVERLAP_MOST || RAx == ERA.LESS_OVERLAP_LESS
					|| RAx == ERA.MOST_OVERLAP_MOST
					|| RAx == ERA.MOST_OVERLAP_LESS || RAx == ERA.MOST_START
					|| RAx == ERA.LESS_START || RAx == ERA.LEFT_DURING) {
				numLeftSupportee++;
			}

			else if (RAx == ERA.LESS_OVERLAP_MOST_I
					|| RAx == ERA.LESS_OVERLAP_LESS_I
					|| RAx == ERA.MOST_OVERLAP_MOST_I
					|| RAx == ERA.MOST_OVERLAP_LESS_I || RAx == ERA.MOST_FINISH
					|| RAx == ERA.LESS_FINISH || RAx == ERA.RIGHT_DURING) {
				numRightSupportee++;
			}
		}

		for (int id : supporterlist.keySet()) {
			if (this.affectObjs != null) {
				if (this.affectObjs.containsKey(id)) {
					continue;
				}
			}

			if (sa.objsVision.get(id).nonRec) {
				//System.out.println("non poly id : " + id);
				if (sa.objsVision.get(id).poly.contains(obj.shape.x,
						obj.shape.y + obj.shape.height, obj.shape.width, 10)) {
					
			//		System.out.println("non poly support");
					
					return true;
				}
			}

			p = new KeyPair(id, obj.id);
			RAx = RA.get(p)[0];
			if (RAx == ERA.LESS_OVERLAP_MOST || RAx == ERA.LESS_OVERLAP_LESS
					|| RAx == ERA.MOST_OVERLAP_MOST
					|| RAx == ERA.MOST_OVERLAP_LESS || RAx == ERA.MOST_START
					|| RAx == ERA.LESS_START || RAx == ERA.LEFT_DURING) {

			//	System.out.println("left supported by " + id);

				leftSupporter = true;
			}

			else if (RAx == ERA.LESS_OVERLAP_MOST_I
					|| RAx == ERA.LESS_OVERLAP_LESS_I
					|| RAx == ERA.MOST_OVERLAP_MOST_I
					|| RAx == ERA.MOST_OVERLAP_LESS_I || RAx == ERA.MOST_FINISH
					|| RAx == ERA.LESS_FINISH || RAx == ERA.RIGHT_DURING) {
			//	System.out.println("right supported by " + id);
				rightSupporter = true;
			}

			else if (RAx == ERA.MOST_START_I || RAx == ERA.LESS_START_I
					|| RAx == ERA.MOST_FINISH_I || RAx == ERA.LESS_FINISH_I
					|| RAx == ERA.CENTRE_DURING || RAx == ERA.CENTRE_DURING_I
					|| RAx == ERA.LEFT_DURING_I || RAx == ERA.RIGHT_DURING_I
					|| RAx == ERA.MOST_START || RAx == ERA.MOST_FINISH
					|| RAx == ERA.MOST_OVERLAP_MOST
					|| RAx == ERA.LESS_OVERLAP_MOST
					|| RAx == ERA.MOST_OVERLAP_MOST_I
					|| RAx == ERA.MOST_OVERLAP_LESS_I || RAx == ERA.EQUAL) {
			//	System.out.println("centre supported by " + id);
				centreSupporter = true;
			}
		}
		if ((leftSupporter && rightSupporter) || centreSupporter) {
			return true;
		}

		else if (leftSupporter) {
			if (numLeftSupportee > numRightSupportee + 2) {
				return true;
			}
		}

		// else if (rightSupporter){
		// if(numLeftSupportee +2 < numRightSupportee){
		// return true;
		// }
		// }

		return false;
	}
}
