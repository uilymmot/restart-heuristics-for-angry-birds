package KR;

import java.util.ArrayList;
import java.util.HashMap;

import KR.util.KeyPair;

/**
 * Integrate Gary's algorithm into the framework
 * @author ZP
 *
 */
public class AngleReasoner extends SpatialRelation {

	public static final int Regular = 1;
	public static final int Angular = 2;

	private ArrayList<ArrayList<Double>> ConnectObj;
	private HashMap<KeyPair, ArrayList<Integer>> RA;
	private HashMap objs;
	private HashMap<Double, Integer> ObjAngle;
	private ArrayList<Double> HandledList;


	/*public AngleReasoner(HashMap objs, double refLength) {
		super(objs, refLength);
		this.ConnectObj = new ArrayList<ArrayList<Double>>();
		this.HandledList = new ArrayList<Double>();
		this.RA = new HashMap<KeyPair, ArrayList<Integer>>();
		this.objs = (HashMap) objs.clone();
	}

	
	*//**
	 * Get connected MBRs
	 * @param objs 
	 *//*
	public void getConnectStructure(HashMap objs) {
		ArrayList<Double> connectlist;
		double ID1 = -1;
		

		if (super.InputType == INPUT_VISION) {
			HashMap<Double, ABObject> abobjs = objs;
			Iterator<Entry<Double, ABObject>> it1 = abobjs.entrySet()
					.iterator();
			while (it1.hasNext()) {
				Map.Entry<Double, ABObject> obj1 = it1
						.next();
				ID1 = obj1.getKey();
				if (this.HandledList.contains(ID1)) {
					continue;
				}
				if (IgnoreNoise(obj1.getValue())) {
					continue;
				}
				connectlist = new ArrayList<Double>();
				this.HandledList.add(ID1);
				connectlist.add(ID1);
				// it1.remove();

				ConnectDetect(connectlist, abobjs, ID1);
				this.ConnectObj.add(connectlist);
			}
		}

	}

	public void ConnectDetect(ArrayList<Double> connectlist,
			HashMap<Double, ABObject> abobjs, Double ID1) {

		int rax, ray;
		int inputType = super.InputType;
		ArrayList<Integer> RArel = new ArrayList<Integer>();
		this.RA = super.getRArel();
		Iterator<Entry<Double, ABObject>> it2 = abobjs.entrySet().iterator();
		while (it2.hasNext()) {
			Map.Entry<Double, ABObject> obj2 = it2
					.next();
			double ID2 = obj2.getKey();
			if (this.HandledList.contains(ID2)) {
				continue;
			}
			if (IgnoreNoise(obj2.getValue())) {
				continue;
			}

			KeyPair k = new KeyPair(ID1, ID2);
			// super.printRA(this.RA);
			RArel = this.RA.get(k);

			// System.out.print(k.getFirst() + " " + k.getSecond() + "\n");

			rax = RArel.get(0);
			ray = RArel.get(1);

			if (ray != BEFORE && ray != AFTER) {
				if (rax != MEET && rax != MEET_I && rax != AFTER
						&& rax != BEFORE) {
					if (!connectlist.contains(ID2)) {
						connectlist.add(ID2);
						this.HandledList.add(ID2);
						ConnectDetect(connectlist, abobjs, ID2);
						// it2.remove();
					}
				}
			}
		}
	}

	public void outputObjs() throws IOException {

		Rectangle rec;

		if (super.InputType == INPUT_VISION) {
			HashMap<Double, ABObject> abobjs = this.objs;

			for (ArrayList<Double> connectlist : this.ConnectObj) {
				PrintStream ps = new PrintStream(new FileOutputStream(
						"connectobjs"));
				for (double id : connectlist) {
					try {
						rec = abobjs.get(id).getBoundBox();
						ps.print(id + " " + (double) rec.x + " "
								+ (double) rec.y + " " + (double) rec.width
								+ " " + (double) rec.height + "\n");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					ps.print("$");
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.setAngle();
			}
		}
	}

	public void setAngle() throws IOException {
		ScenarioIO sio = new ScenarioIO("connectobjs");
		LinkedList<LinkedList<MBR>> scenarios = sio.load("connectobjs");
		for (LinkedList<MBR> scenario : scenarios) {
			Debug.echo(null, scenario);
		}

		LinkedList<MBR> s1 = scenarios.get(0);

		int count = 0;
		for (MBR mbr : s1) {

			if (count == 0) {
				MBRRegisterWithFuzzyShape.registerMBR(mbr, true);
				count = 1;
			} else
				MBRRegisterWithFuzzyShape.registerMBR(mbr, false);
		}
		Node node = MBRRegisterWithFuzzyShape.constructNode();

		MBRReasoner MBRR = new MBRReasoner();

		MBRR.reason(node);
		ScenarioPanel sp = new ScenarioPanel();
		sp.run(s1);
	}

	public static void main(String[] args) throws MatlabConnectionException,
			MatlabInvocationException {
		CreateVision cv = new CreateVision(Env.getMatlabDir());
		ObjectCollector oc = new ObjectCollector();

		oc.collect(cv);

		AngleReasoner ar = new AngleReasoner(oc.getObjs(), oc.refLength);
		ar.getConnectStructure((HashMap) ar.objs.clone());
		try {
			ar.outputObjs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/
}
