package KR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import KR.util.KeyPair;
import KR.util.VisionObject;
import ab.vision.ABType;
import ab.vision.Vision;
//import ab.utils.ShowDebuggingImage;
//import ab.vision.TestVision;
//import KR.util.ShowStructure;

public class StructureAnalyse extends PhysicalRelation {

	public HashMap<KeyPair, ERA[]> RA;
	public HashMap<Integer, VisionObject> objsVision;
	private HashMap<Integer, ArrayList<Entry<Integer, Integer>>> SupportStructure;
	private HashMap<Integer, ArrayList<Integer>> ShelteringStructure;
	private HashMap<Integer, Double> Stability;
	private HashMap<KeyPair, SUPPORT_RELATION> SupportRel;

	public ArrayList<Integer> pigs;
	public int[][] SupportGraph;
	public HashMap<Integer, Integer> graphDoneCheck;

	public StructureAnalyse() {

	}

	/**
	 * 
	 * @param objs
	 * @param refLength
	 *            for normalization
	 */
	public StructureAnalyse(HashMap<Integer, VisionObject> objs,
			double refLength) {
		super(objs, refLength);

		System.out.println("AFTER SUPER, THE SIZE OF OBJS IS " + objs.size());

		this.SupportGraph = new int[objs.size()+1][objs.size()+1];
		this.graphDoneCheck = new HashMap<>();
		for (Map.Entry<Integer, VisionObject> vb : objs.entrySet()) {
			graphDoneCheck.put(vb.getValue().abo.id, 0);
//			this.SupportGraph = new int[vb.getValue().abo.id + 1][vb.getValue().abo.id + 1];
		}

		this.SupportStructure = new HashMap<Integer, ArrayList<Entry<Integer, Integer>>>();
		this.ShelteringStructure = new HashMap<Integer, ArrayList<Integer>>();
		this.Stability = new HashMap<Integer, Double>();
		// this.SupportRel = new HashMap<KeyPair,Integer>();
		// this.RA = new HashMap<KeyPair,ArrayList<Integer>>();
		this.SupportRel = super.getSupportRel();
		this.RA = super.getRArel();
		this.objsVision = objs;
		this.setSupportAndShelteringStructure();
		this.setStability();
	}

	private void setSupportAndShelteringStructure() {
		TreeMap<Integer, Integer> SupportMap;
		ArrayList<Integer> ShelteringList;
		ABType objClass = null;
		ArrayList<Entry<Integer, Integer>> SupportList = null;
		this.pigs = new ArrayList<Integer>();

		VisionObject vo;
		//System.out.println("SIZE OF OBJECTS: " + this.objsVision.size());
		for (Integer k : this.objsVision.keySet()) {
//			for (int k = 0; k <= this.objsVision.size(); k++) {
			vo = this.objsVision.get(k);
		//	System.out.println("INTEGER IN SUPPORTSHELTERING: " + k + "," + vo.abo.id);
			if (this.graphDoneCheck.get(k) != 1
							&& vo.abo.type != ABType.BlackBird
							&& vo.abo.type != ABType.YellowBird
							&& vo.abo.type != ABType.RedBird
							&& vo.abo.type != ABType.WhiteBird
							&& vo.abo.type != ABType.BlueBird
							&& vo.abo.type != ABType.Sling) {
				//System.out.println("PASSED CHECK");
				getSupportGraph(vo, 1, 100);
			}
			//System.out.println("GRAPH CHECK PASSED");

			objClass = vo.abo.type;
			if (IgnoreNoise(vo))
				continue;
			if (objClass == ABType.Pig) {
				//System.out.println("THIS THING IS A PIG");

				this.pigs.add(k);
				SupportMap = new TreeMap<Integer, Integer>();
				ShelteringList = new ArrayList<Integer>();
				getSupportor(vo, 1, SupportMap);
				//System.out.println("GOT SUPPORTER");

				/** Convert EntrySet to List **/
				SupportList = new ArrayList<Map.Entry<Integer, Integer>>(
						SupportMap.entrySet());

				/** Sort by value **/
				Collections.sort(SupportList,
						new Comparator<Map.Entry<Integer, Integer>>() {
							@Override
							public int compare(
									Map.Entry<Integer, Integer> mapping1,
									Map.Entry<Integer, Integer> mapping2) {
								return mapping1.getValue().compareTo(
										mapping2.getValue());
							}
						});

				this.SupportStructure.put(k, SupportList);

				this.getSheltering(vo, ShelteringList);
				this.ShelteringStructure.put(k, ShelteringList);
			}
		}

	}

	public void getSupportGraph(VisionObject vo, int depth, int maxDepth) {
		this.graphDoneCheck.put(vo.id,1);
		int d = depth + 1;

		for (KeyPair k : this.SupportRel.keySet()) {
			if (vo.id == k.getSecond() && d <= maxDepth) {
//				System.out.println("PASSED IF STATEMENT CHECK");
				this.SupportGraph[k.getFirst()][k.getSecond()] = 1;
//				System.out.println("PASSED ARRAY ACCESS");
				getSupportGraph(this.objsVision.get(k.getFirst()), d, maxDepth);
//				System.out.println("PASSED SUPPGRAPH MODIFICATION");
			}
		}
	}

	/*
	 * private void setSheltingStructure() { String objClass = null;
	 * 
	 * if(this.InputType == INPUT_VISION) { VisionObject vo; for (Double k :
	 * this.objsVision.keySet()) { vo = new VisionObject(); vo =
	 * this.objsVision.get(k); objClass = vo.getVision_name();
	 * if(IgnoreNoise(vo)) continue;
	 * 
	 * 
	 * }
	 * 
	 * } }
	 */

	/**
	 * stability of single object that consider the height and width ratio and
	 * the vertical level of the object
	 */
	private void setStability() {

		VisionObject vo = null;
		for (Integer k : this.objsVision.keySet()) {
			vo = this.objsVision.get(k);
			if (IgnoreNoise(vo))
				continue;
			if (this.Stability.keySet().contains(k))
				continue;
			calStability(vo);
		}

	}

	/**
	 * default depth is 0 and default maxDepth is 100
	 * 
	 * @param vo
	 * @param supporteelist
	 */
	public void getSupportor(VisionObject vo,
			TreeMap<Integer, Integer> supporteelist) {
		getSupportor(vo, 0, supporteelist, 100);
	}

	/**
	 * default maxDepth is 100
	 * 
	 * @param vo
	 * @param depth
	 * @param supporteelist
	 */
	public void getSupportor(VisionObject vo, int depth,
			TreeMap<Integer, Integer> supporteelist) {
		getSupportor(vo, depth, supporteelist, 100);
	}

	/**
	 * default depth is 0
	 * 
	 * @param vo
	 * @param supporteelist
	 * @param maxDepth
	 */
	public void getSupportor(VisionObject vo,
			TreeMap<Integer, Integer> supporteelist, int maxDepth) {
		getSupportor(vo, 0, supporteelist, maxDepth);
	}

	/**
	 * get supportors within maxDepth of a queried object with depth
	 * 
	 * @param vo
	 * @param depth
	 * @param supportlist
	 * @param maxDepth
	 */
	public void getSupportor(VisionObject vo, int depth,
			TreeMap<Integer, Integer> supportlist, int maxDepth) {
		int d = depth + 1;

		for (KeyPair k : this.SupportRel.keySet()) {
			if (vo.id == k.getSecond() && d <= maxDepth) {
				supportlist.put(k.getFirst(), depth);
				getSupportor(this.objsVision.get(k.getFirst()), d, supportlist,
						maxDepth);
			}
		}
	}

	/** Get supportees methods, similar **/
	public void getSupportee(VisionObject vo,
			TreeMap<Integer, Integer> supporteelist) {
		
		getSupportee(vo, 0, supporteelist, 100);
	}

	public void getSupportee(VisionObject vo, int depth,
			TreeMap<Integer, Integer> supporteelist) {
		getSupportee(vo, depth, supporteelist, 100);
	}

	public void getSupportee(VisionObject vo,
			TreeMap<Integer, Integer> supporteelist, int maxDepth) {
		getSupportee(vo, 0, supporteelist, maxDepth);
	}

	public void getSupportee(VisionObject vo, int depth,
			TreeMap<Integer, Integer> supporteelist, int maxDepth) {
		int d = depth + 1;

		for (KeyPair k : this.SupportRel.keySet()) {
			if (vo.id == k.getFirst() && d <= maxDepth) {
				supporteelist.put(k.getSecond(), depth);
				getSupportee(this.objsVision.get(k.getSecond()), d,
						supporteelist, maxDepth);
			}
		}
	}


	public void getSheltering(VisionObject vo, ArrayList<Integer> ShelteringList) {
		ERA RArel = ERA.UNKNOWN;
		int RoofDepthL = 100;
		int RoofDepthR = 100;
		int RoofID = -1;
		int Left = -1;
		int Right = -1;
		TreeMap<Integer, Integer> supporteelistLeft, supporteelistRight, supportlistLeft, supportlistRight;

		supporteelistLeft = new TreeMap<Integer, Integer>();
		supporteelistRight = new TreeMap<Integer, Integer>();
		supportlistLeft = new TreeMap<Integer, Integer>();
		supportlistRight = new TreeMap<Integer, Integer>();

		for (KeyPair k : this.RA.keySet()) {
			if (vo.id == k.getFirst() && vo.id != k.getSecond()) {
				RArel = this.RA.get(k)[1];

				// get left sheltering objs
				if (RArel != ERA.BEFORE && RArel != ERA.AFTER
						&& RArel != ERA.MEET && RArel != ERA.MEET_I) {
					RArel = this.RA.get(k)[0];
					if (RArel == ERA.BEFORE || RArel == ERA.MEET
							|| RArel == ERA.LESS_OVERLAP_MOST
							|| RArel == ERA.LESS_OVERLAP_LESS
							|| RArel == ERA.MOST_OVERLAP_MOST
							|| RArel == ERA.MOST_OVERLAP_LESS
							|| RArel == ERA.MOST_FINISH_I
							|| RArel == ERA.LESS_FINISH_I
							|| RArel == ERA.LEFT_DURING
							|| RArel == ERA.RIGHT_DURING
							|| RArel == ERA.CENTRE_DURING
							|| RArel == ERA.LEFT_DURING_I
							|| RArel == ERA.RIGHT_DURING_I
							|| RArel == ERA.CENTRE_DURING_I) {
						if (Left < 0)
							Left = k.getSecond();
						else if (DistanceCompare(this.objsVision.get(Left), // previous
																			// obj
								this.objsVision.get(k.getSecond()), // current
																	// obj
								this.objsVision.get(k.getFirst())) // pig
						== 1) {
							Left = k.getSecond();
						}
					}
				}

				// get right sheltering objs
				RArel = this.RA.get(k)[1];
				if (RArel != ERA.BEFORE && RArel != ERA.AFTER
						&& RArel != ERA.MEET && RArel != ERA.MEET_I) {
					RArel = this.RA.get(k)[0];
					if (RArel == ERA.AFTER || RArel == ERA.MEET_I
							|| RArel == ERA.LESS_OVERLAP_MOST_I
							|| RArel == ERA.LESS_OVERLAP_LESS_I
							|| RArel == ERA.MOST_OVERLAP_MOST_I
							|| RArel == ERA.MOST_OVERLAP_LESS_I
							|| RArel == ERA.MOST_START_I
							|| RArel == ERA.LESS_START_I
							|| RArel == ERA.LEFT_DURING
							|| RArel == ERA.RIGHT_DURING
							|| RArel == ERA.CENTRE_DURING
							|| RArel == ERA.LEFT_DURING_I
							|| RArel == ERA.RIGHT_DURING_I
							|| RArel == ERA.CENTRE_DURING_I) {
						if (Right < 0)
							Right = k.getSecond();
						else if (DistanceCompare(this.objsVision.get(Right), // previous
																				// obj
								this.objsVision.get(k.getSecond()), // current
																	// obj
								this.objsVision.get(k.getFirst())) // pig
						== 1) {
							Right = k.getSecond();
						}
					}
				}

			}
		}

		if (Left >= 0) {
			getSupportee(this.objsVision.get(Left), 1, supporteelistLeft);
			getSupportor(this.objsVision.get(Left), 1, supportlistLeft);
		}
		if (Right >= 0) {
			getSupportee(this.objsVision.get(Right), 1, supporteelistRight);
			getSupportor(this.objsVision.get(Right), 1, supportlistRight);
		}

		for (int kl : supporteelistLeft.keySet()) {
			for (int kr : supporteelistRight.keySet()) {
				if (kl == kr) {
					if (RoofDepthL > supporteelistLeft.get(kl)) {
						RoofDepthL = supporteelistLeft.get(kl);
						RoofDepthR = supporteelistRight.get(kr);
						RoofID = kl;
					}
				}
			}
		}

		if (RoofID >= 0) {
			ShelteringList.add(RoofID);
			ShelteringList.add(Left);
			ShelteringList.add(Right);
			for (int kl : supporteelistLeft.keySet()) {
				if (supporteelistLeft.get(kl) < RoofDepthL)
					ShelteringList.add(kl);
			}

			for (int kr : supporteelistRight.keySet()) {
				if (supporteelistRight.get(kr) < RoofDepthR)
					ShelteringList.add(kr);
			}

			for (int kl : supportlistLeft.keySet()) {

				ERA RAy = ERA.UNKNOWN;
				for (KeyPair kp : this.RA.keySet()) {
					if (kp.getFirst() == kl && kp.getSecond() == vo.id)
						RAy = this.RA.get(kp)[1];
				}

				if (RAy != ERA.BEFORE && RAy != ERA.AFTER && RAy != ERA.MEET
						&& RAy != ERA.MEET_I)
					ShelteringList.add(kl);
			}

			for (int kr : supportlistRight.keySet()) {

				ERA RAy = ERA.UNKNOWN;

				for (KeyPair kp : this.RA.keySet()) {
					if (kp.getFirst() == kr && kp.getSecond() == vo.id)
						RAy = this.RA.get(kp)[1];
				}

				if (RAy != ERA.BEFORE && RAy != ERA.AFTER && RAy != ERA.MEET
						&& RAy != ERA.MEET_I)
					ShelteringList.add(kr);
			}

		}

		else {

		}
	}



	private void calStability(VisionObject vo) {
		double stability;
		double suppstab = 0;
		TreeMap<Integer, Integer> SupportMap;
		String objClass = null;

		objClass = vo.type;
		SupportMap = new TreeMap<Integer, Integer>();

		for (KeyPair k : this.SupportRel.keySet()) {
			if (vo.id == k.getSecond()) {
				SupportMap.put(k.getFirst(), 1);
			}
		}

		stability = (double) vo.shape.width / (double) vo.shape.height
				/ this.maxWLratio;

		for (int k : SupportMap.keySet()) {

			if (this.Stability.keySet().contains(k)) {
				suppstab += this.Stability.get(k);
			} else {
				calStability(this.objsVision.get(k));
				suppstab += this.Stability.get(k);
			}

		}

		if (SupportMap.size() > 0)
			stability = stability * suppstab / SupportMap.size();
		else
			stability *= this.maxWLratio;
		this.Stability.put(vo.id, stability);
	}

	/**
	 * 
	 * @return Support structure for pigs
	 */
	public HashMap<Integer, ArrayList<Entry<Integer, Integer>>> getSupportStructure() {
		return this.SupportStructure;
	}
	/**  
	 * 
	 * @return Sheltering structure for pigs
	 */
	public HashMap<Integer, ArrayList<Integer>> getShelteringStructure() {
		return this.ShelteringStructure;
	}

	public HashMap<Integer, Double> getStability() {
		return this.Stability;
	}

	public void printSupportStructure(
			HashMap<Integer, ArrayList<Entry<Integer, Integer>>> SupportStructure,
			HashMap<Double, VisionObject> SupportObjs) {
		for (int k : SupportStructure.keySet()) {
			ArrayList<Entry<Integer, Integer>> SupportList = SupportStructure
					.get(k);
			System.out.println("Support Structures for PigID" + k + ":");
			for (int i = 0; i < SupportList.size(); i++) {
				double supportorID = SupportList.get(i).getKey();
				int depth = SupportList.get(i).getValue();

				System.out.println("	SupportorID : " + supportorID
						+ "Support Depth : " + depth);
				this.getStructureObjsVision(supportorID, SupportObjs);
			}
		}
	}

	public void printStability(HashMap<Double, Double> Stability) {

		System.out.println("\nStability:");
		for (Double k : Stability.keySet()) {
			System.out.println("	objID : " + k + " Stability : "
					+ Stability.get(k));
		}
	}

	public void printShelteringStructure(
			HashMap<Double, ArrayList<Double>> ShelteringStructure,
			HashMap<Double, VisionObject> ShelteringObjs) {
		System.out.print("\nSheltering Structure:\n");

		for (double k : ShelteringStructure.keySet()) {
			System.out.print("	PigID : " + k + "\n");
			for (int i = 0; i < ShelteringStructure.get(k).size(); i++) {
				System.out.print("		objID: "
						+ ShelteringStructure.get(k).get(i) + "\n");
				this.getStructureObjsVision(ShelteringStructure.get(k).get(i),
						ShelteringObjs);
			}
		}
	}

	public void getStructureObjsVision(Double key,
			HashMap<Double, VisionObject> StrucutureObjs) {
		StrucutureObjs.put(key, this.objsVision.get(key));
	}

	/**
	 * 
	 * @param vo1
	 *            first obj
	 * @param vo2
	 *            second obj
	 * @param vo
	 *            target obj
	 * @return 1 if dis(vo1,vo) > dis(vo2,vo), -1 if dis(vo1,vo) < dis(vo2,vo),
	 *         0 if dis(vo1,vo) = dis(vo2,vo)
	 * 
	 */
	public int DistanceCompare(VisionObject vo1, VisionObject vo2,
			VisionObject vo) {
		// System.out.print("\n" + vo1.getVision_id() + " " + vo2.getVision_id()
		// +" " +vo.getVision_id() + "\n");
		double sqd1 = Math.pow(vo1.centrePoint.getX() - vo.centrePoint.getX(),
				2);
		double sqd2 = Math.pow(vo2.centrePoint.getX() - vo.centrePoint.getX(),
				2);

		if (sqd1 > sqd2)
			return 1;
		else if (sqd1 < sqd2)
			return -1;
		else
			return 0;
	}

	public static void main(String[] args) {
//		BufferedImage screenshot = null;
//		ShowDebuggingImage frame = null;
//		Proxy game = TestVision.getGameConnection(9000);
//		HashMap<Integer, VisionObject> objs = null;
//
//		//while (true) {
//			// capture an image
//			byte[] imageBytes = game.send(new ProxyScreenshotMessage());
//			try {
//				screenshot = ImageIO.read(new ByteArrayInputStream(imageBytes));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//			ObjectCollector oc = new ObjectCollector(screenshot);
//
//			HashMap<KeyPair, ERA[]> RA = new HashMap<KeyPair, ERA[]>();
//			// System.out.println(oc.refLength);
//			objs = oc.getObjects();
//			StructureAnalyse sa = new StructureAnalyse(objs, oc.refLength);
//			HashMap<Integer, ArrayList<Entry<Integer, Integer>>> SupportStructure = sa
//					.getSupportStructure();
//			HashMap<Integer, ArrayList<Integer>> ShelteringStructure = sa
//					.getShelteringStructure();
//
//			HashMap<Double, VisionObject> SupportObjs = new HashMap<Double, VisionObject>();
//			HashMap<Double, VisionObject> ShelteringObjs = new HashMap<Double, VisionObject>();
//
//			sa.printSupportStructure(SupportStructure, SupportObjs);
////			sa.printStability(Stability);
////			sa.printShelteringStructure(ShelteringStructure, ShelteringObjs);
//			 //analyse and show image
//			int[][] meta = TestVision.computeMetaInformation(screenshot);
//			screenshot = ReasonerUtils.showBoundings(screenshot);
//			if (frame == null) {
//				frame = new ShowDebuggingImage("TestVision", screenshot,
//						meta);
//			}
//			// sleep for 100ms
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
//		//}
//		// oc.collect(cv);
//		// String image_path = Env.getMatlabDir() + Env.getSystemSeparator()
//		// + "im.png";
//		//
//		// StructureAnalyse sa = new StructureAnalyse(oc.getObjs(),
//		// oc.refLength);
//		//
//		// /*
//		// * HashMap<Double PigID,ArrayList<Entry<Double SupportorID, Integer
//		// * depth>>>
//		// */
//		// HashMap<Double, ArrayList<Entry<Integer, Integer>>> SupportStructure
//		// = sa
//		// .getSupportStructure();
//		// HashMap<Double, Double> Stability = sa.getStability();
//		// HashMap<Double, ArrayList<Double>> ShelteringStructure = sa
//		// .getShelteringStructure();
//		//
//		// HashMap<Double, VisionObject> SupportObjs = new HashMap<Double,
//		// VisionObject>();
//		// HashMap<Double, VisionObject> ShelteringObjs = new HashMap<Double,
//		// VisionObject>();
//		//
//		// sa.printSupportStructure(SupportStructure, SupportObjs);
//		// sa.printStability(Stability);
//		// sa.printShelteringStructure(ShelteringStructure, ShelteringObjs);
//		//
//		// try {
//		// new ShowStructure(image_path, ShelteringObjs, oc);
//		// new ShowStructure(image_path, SupportObjs, oc);
//		// } catch (Exception e) {
//		// e.printStackTrace();
//		// }

	}
}
