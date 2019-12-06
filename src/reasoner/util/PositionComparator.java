package reasoner.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import KR.SpatialRelation.ERA;
import KR.util.KeyPair;

public class PositionComparator implements Comparator<Integer> {

	private HashMap<KeyPair, ERA[]> RA;
	private int coordinate;

	public PositionComparator(HashMap<KeyPair, ERA[]> RA,
			int coordinate) {
		this.RA = RA;
		if (coordinate != 0 && coordinate != 1) {
			System.out
					.print("Class PositionComparator: coordinate must be 0 (x) or 1 (y)\n");
			System.exit(0);
		}
		this.coordinate = coordinate;
	}

	@Override
	public int compare(Integer ID1, Integer ID2) {
		KeyPair k = new KeyPair(ID1, ID2);
		if(this.RA.get(k) == null)
			return -1;
		ERA RArel = this.RA.get(k)[this.coordinate];
		if (RArel == ERA.BEFORE
				|| RArel == ERA.MOST_START
				|| RArel == ERA.LESS_START
				|| RArel == ERA.MEET
				|| RArel == ERA.LEFT_DURING
				|| RArel == ERA.RIGHT_DURING_I
				|| RArel == ERA.LESS_OVERLAP_MOST
				|| RArel == ERA.LESS_OVERLAP_LESS
				|| RArel == ERA.MOST_OVERLAP_MOST
				|| RArel == ERA.MOST_OVERLAP_LESS
				|| RArel == ERA.MOST_FINISH_I
				|| RArel == ERA.LESS_FINISH_I) {
			return 1;
		}

		else if (RArel == ERA.AFTER
				|| RArel == ERA.MOST_START_I
				|| RArel == ERA.LESS_START_I
				|| RArel == ERA.MEET_I
				|| RArel == ERA.LEFT_DURING_I
				|| RArel == ERA.RIGHT_DURING
				|| RArel == ERA.LESS_OVERLAP_MOST_I
				|| RArel == ERA.LESS_OVERLAP_LESS_I
				|| RArel == ERA.MOST_OVERLAP_MOST_I
				|| RArel == ERA.MOST_OVERLAP_LESS_I
				|| RArel == ERA.MOST_FINISH
				|| RArel == ERA.LESS_FINISH) {
			return -1;
		}

		else {
			return 0;
		}
	}
}
