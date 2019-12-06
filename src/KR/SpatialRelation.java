package KR;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import reasoner.ReasonerUtils;

import ab.server.proxy.message.ProxyScreenshotMessage;
//import ab.utils.ShowDebuggingImage;
//import ab.vision.TestVision;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
//import ab.utils.ShowDebuggingImage;

import KR.util.KeyPair;
import KR.util.ObjectCollector;
import KR.util.VisionObject;

@SuppressWarnings("rawtypes")
public class SpatialRelation {
	public double refLength;
	public double maxWLratio;

	private double threshold;
	private HashMap<Integer, VisionObject> objsVision;

	private HashMap<KeyPair, ERA[]> RA;

	public enum ERA {
		BEFORE, AFTER, MEET, MEET_I, MOST_START, MOST_START_I, LESS_START, 
		LESS_START_I, LEFT_DURING, LEFT_DURING_I, RIGHT_DURING, RIGHT_DURING_I, 
		CENTRE_DURING, CENTRE_DURING_I, MOST_FINISH, MOST_FINISH_I, LESS_FINISH, 
		LESS_FINISH_I, EQUAL, MOST_OVERLAP_MOST, MOST_OVERLAP_MOST_I, 
		LESS_OVERLAP_MOST, LESS_OVERLAP_MOST_I, MOST_OVERLAP_LESS, 
		MOST_OVERLAP_LESS_I, LESS_OVERLAP_LESS, LESS_OVERLAP_LESS_I,UNKNOWN;
	}

	public SpatialRelation() {

	}

	public SpatialRelation(HashMap<Integer, VisionObject> objs, double refLength) {

		// InputType = this.ObjTypeCheck(objs);
		this.RA = new HashMap<KeyPair, ERA[]>();
		this.refLength = refLength;
		this.maxWLratio = 0d;
		this.threshold = 5/refLength;
		// if (InputType == INPUT_VISION) {
		this.objsVision = objs;
		this.buildRA();
		// }
		/*
		 * else if (InputType == INPUT_JSON) { this.objsJson = new
		 * HashMap<Double, ObjFromJson>(); this.objsJson = objs; this.buildRA();
		 * }
		 */
		/*
		 * else { System.out.print("Unknown input type!\n"); System.exit(0); }
		 */
	}
	
	/**
	 * @param objs
	 * @return object type: from vision of json file
	 */
	/*
	 * private int ObjTypeCheck(HashMap objs) { Iterator i =
	 * objs.keySet().iterator(); while (i.hasNext()) { Object ABO =
	 * objs.get(i.next()); if (ABO != null) { if (ABO instanceof ABObject)
	 * return INPUT_VISION; else if (ABO instanceof ObjFromJson) return
	 * INPUT_JSON; else return -1; } } return -1; }
	 */
	public HashMap<Integer, VisionObject> getObjs() {
		return this.objsVision;
	}

	public void SizeRel() {

	}

	/**
	 * build RA relations between each pair of the objects in the object list
	 */
	private void buildRA() {

		VisionObject vo1, vo2;
		Point start1, end1, start2, end2;
		KeyPair kp;
		ERA[] RArel;
		ERA IAx, IAy;
		double sx1, sy1, sx2, sy2, ex1, ey1, ex2, ey2;
		double wlratio;

		for (Integer k1 : this.objsVision.keySet()) {
			vo1 = this.objsVision.get(k1);
			// System.out.print(vo1.id + "\n");

			if (vo1.type == "Trajectory" || vo1.type == "Sky"
					|| vo1.type == "Red Bird"  || vo1.type == "Yellow Bird"
					|| vo1.type == "Blue Bird" || vo1.type == "White Bird"
					|| vo1.type == "Black Bird"|| vo1.type == "Unbreakable Wood")
				continue;
			// if (vo1.getBoundBox().height < CreateVision.noise_level
			// / this.refLength
			// || vo1.getBoundBox().width < CreateVision.noise_level
			// / this.refLength) {
			// // this.objsVision.remove(k1);
			// continue;
			// }

			start1 = vo1.startPoint;
			end1 = vo1.endPoint;
			sx1 = start1.getX();
			sy1 = start1.getY();
			ex1 = end1.getX();
			ey1 = end1.getY();

			wlratio = (double) vo1.shape.width / (double) vo1.shape.height;
			if (wlratio >= this.maxWLratio)
				this.maxWLratio = wlratio;

			for (Integer k2 : this.objsVision.keySet()) {
				if (k1 != k2) {
					vo2 = this.objsVision.get(k2);
					if (vo2.type == "Trajectory" || vo2.type == "Sky"
							|| vo2.type == "Red Bird"
							|| vo2.type == "Yellow Bird"
							|| vo2.type == "Blue Bird"
							|| vo2.type == "White Bird"
							|| vo2.type == "Black Bird"
							|| vo2.type == "Unbreakable Wood")
						continue;
					// if (vo2.Shape.height < this.threshold / this.refLength
					// || vo2.Shape.width < this.threshold
					// / this.refLength) {
					// // this.objsVision.remove(k1);
					// continue;
					// }

					start2 = vo2.startPoint;
					end2 = vo2.endPoint;

					sx2 = start2.getX();
					sy2 = start2.getY();
					ex2 = end2.getX();
					ey2 = end2.getY();

					kp = new KeyPair(vo1.id, vo2.id);
					RArel = new ERA[2];

					IAx = this.getRArel(sx1, sx2, ex1, ex2);
					RArel[0] = IAx;
					IAy = this.getRArel(sy1, sy2, ey1, ey2);
					RArel[1] = IAy;
					this.RA.put(kp, RArel);

				}
			}
		}

	}

	/**
	 * Get RA relation in one dimension
	 * 
	 * @param s1
	 *            start point of the first interval
	 * @param s2
	 *            start point of the second interval
	 * @param e1
	 *            end point of the first interval
	 * @param e2
	 *            end point of the second interval
	 * @return
	 */
	private ERA getRArel(double s1, double s2, double e1, double e2) {

		ERA RA;

		if (s2 - e1 >= threshold)
			RA = ERA.BEFORE;
		else if (s1 - e2 >= threshold)
			RA = ERA.AFTER;
		else if (s2 - e1 < threshold && s2 - e1 >= 0 && s1 < e2)
			RA = ERA.MEET;
		else if (s1 - e2 < threshold && s1 - e2 >= 0 && s2 < e1)
			RA = ERA.MEET_I;
		else if (s1 == s2 && e2 - e1 >= 0
				&& (e2 - s2) / 2 < e1 - s1)
			RA = ERA.MOST_START;
		else if (s1 == s2 && e1 - e2 > 0
				&& e2 - s2 > (e1 - s1) / 2)
			RA = ERA.MOST_START_I;
		else if (s1 == s2 && e2 - e1 > 0
				&& (e2 - s2) / 2 >= e1 - s1)
			RA = ERA.LESS_START;
		else if (s1 == s2 && e1 - e2 > 0
				&& e2 - s2 <= (e1 - s1) / 2)
			RA = ERA.LESS_START_I;
		else if (s1 - s2 > 0 && e2 - e1 > 0
				&& e1 <= (s2 + e2) / 2)
			RA = ERA.LEFT_DURING;
		else if (s2 - s1 > 0 && e1 - e2 > 0
				&& e2 <= (s1 + e1) / 2)
			RA = ERA.LEFT_DURING_I;
		else if (s1 - s2 > 0 && e2 - e1 > 0
				&& s1 >= (s2 + e2) / 2)
			RA = ERA.RIGHT_DURING;
		else if (s2 - s1 > 0 && e1 - e2 > 0
				&& s2 >= (s1 + e1) / 2)
			RA = ERA.RIGHT_DURING_I;
		else if (s1 - s2 > 0 && e2 - e1 > 0
				&& s1 < (s2 + e2) / 2 && e1 > (s2 + e2) / 2)
			RA = ERA.CENTRE_DURING;
		else if (s2 - s1 > 0 && e1 - e2 > 0
				&& s2 < (s1 + e1) / 2 && e2 > (s1 + e1) / 2)
			RA = ERA.CENTRE_DURING_I;
		else if (s1 - s2 > 0 && e1 == e2
				&& (e2 - s2) / 2 < e1 - s1)
			RA = ERA.MOST_FINISH;
		else if (s2 - s1 > 0 && e1 == e2
				&& e2 - s2 > (e1 - s1) / 2)
			RA = ERA.MOST_FINISH_I;
		else if (s1 - s2 > 0 && e1 == e2
				&& (e2 - s2) / 2 >= e1 - s1)
			RA = ERA.LESS_FINISH;
		else if (s2 - s1 > 0 && e1 == e2
				&& e2 - s2 <= (e1 - s1) / 2)
			RA = ERA.LESS_FINISH_I;
		else if (Math.abs(s1 - s2) < threshold && Math.abs(e1 - e2) < threshold)
			RA = ERA.EQUAL;
		else if (s2 - s1 > 0 && e2 - e1 > 0
				&& e1 - s2 > 0 && e1 - s2 >= s2 - s1
				&& e1 - s2 >= e2 - e1)
			RA = ERA.MOST_OVERLAP_MOST;
		else if (s2 - s1 > 0 && e2 - e1 > 0
				&& e1 - s2 > 0 && e1 - s2 < s2 - s1
				&& e1 - s2 >= e2 - e1)
			RA = ERA.LESS_OVERLAP_MOST;
		else if (s2 - s1 > 0 && e2 - e1 > 0
				&& e1 - s2 > 0 && e1 - s2 >= s2 - s1
				&& e1 - s2 < e2 - e1)
			RA = ERA.MOST_OVERLAP_LESS;
		else if (s2 - s1 > 0 && e2 - e1 > 0
				&& e1 - s2 > 0 && e1 - s2 < s2 - s1
				&& e1 - s2 < e2 - e1)
			RA = ERA.LESS_OVERLAP_LESS;
		else if (s1 - s2 > 0 && e1 - e2 > 0
				&& e1 - s2 > 0 && e2 - s1 >= s1 - s2
				&& e2 - s1 >= e1 - e2)
			RA = ERA.MOST_OVERLAP_MOST_I;
		else if (s1 - s2 > 0 && e1 - e2 > 0
				&& e2 - s1 > 0 && e2 - s1 < s1 - s2
				&& e2 - s1 >= e1 - e2)
			RA = ERA.LESS_OVERLAP_MOST_I;
		else if (s1 - s2 > 0 && e1 - e2 > 0
				&& e2 - s1 > 0 && e2 - s1 >= s1 - s2
				&& e2 - s1 < e1 - e2)
			RA = ERA.MOST_OVERLAP_LESS_I;
		else if (s1 - s2 > 0 && e1 - e2 > 0
				&& e2 - s1 > 0 && e2 - s1 < s1 - s2
				&& e2 - s1 < e1 - e2)
			RA = ERA.LESS_OVERLAP_LESS_I;
		else
			RA = ERA.UNKNOWN;

		return RA;
	}

	/**
	 * 
	 * @param vo
	 *            possible noise
	 * @return
	 */
	public boolean IgnoreNoise(VisionObject vo) {
		if (vo.type == "Trajectory" || vo.type == "Sky"
				|| vo.type == "Red Bird" || vo.type == "Yellow Bird"
				|| vo.type == "Blue Bird" || vo.type == "White Bird"
				|| vo.type == "Black Bird"|| vo.type == "SlingShot")
			return true;
		// if (vo.getBoundBox().height < CreateVision.noise_level /
		// this.refLength
		// || vo.getBoundBox().width < CreateVision.noise_level
		// / this.refLength)
		// return true;
		return false;
	}

	/*
	 * private void setAngle() { int rax, ray; double obj1, obj2;
	 * ArrayList<Integer> RArel = new ArrayList<Integer>();
	 * 
	 * for (KeyPair k : this.RA.keySet()) { obj1 = k.getFirst(); obj2 =
	 * k.getSecond(); RArel = this.RA.get(k); rax = (Integer) RArel.get(0); ray
	 * = (Integer) RArel.get(1); } }
	 */

	/**
	 * 
	 * @return RA relations
	 */
	public HashMap<KeyPair, ERA[]> getRArel() {
		return this.RA;
	}

	public void printRA(HashMap<KeyPair, ERA[]> RA) {
		System.out.println("Reference length " + this.refLength);
		System.out.println("RA RELATION: ");
		for (KeyPair k : RA.keySet()) {
			System.out.print(k.getFirst() + " " + objsVision.get(k.getFirst()).startPoint + " " + objsVision.get(k.getFirst()).endPoint + " and " + k.getSecond() + " : "
					+ " " + objsVision.get(k.getSecond()).startPoint + " " + objsVision.get(k.getSecond()).endPoint + " " + this.RA.get(k)[0].toString() + " " + this.RA.get(k)[1].toString() + "\n");
		}
		
	}

	public static void main(String args[]) {
//
//		BufferedImage screenshot = null;
//		ShowDebuggingImage frame = null;
//		Proxy game = TestVision.getGameConnection(9000);
//		HashMap<Integer, VisionObject> objs = null;
//
////		while (true) {
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
//			// System.out.println(objs.size() + " " +oc.refLength);
//			SpatialRelation sr = new SpatialRelation(objs, oc.refLength);
//			RA = sr.getRArel();
//			sr.printRA(RA);
//
//			// analyse and show image
//			// int[][] meta = computeMetaInformation(screenshot);
//			// screenshot = analyseScreenShot(screenshot);
//			// if (frame == null) {
//			// frame = new ShowDebuggingImage("TestVision", screenshot,
//			// meta);
//			// } else {
//			// frame.refresh(screenshot, meta);
//			// }
//
//			// sleep for 100ms
//			
//			// analyse and show image
//			int[][] meta = TestVision.computeMetaInformation(screenshot);
//			screenshot = ReasonerUtils.showBoundings(screenshot);
//			if (frame == null) {
//				frame = new ShowDebuggingImage("TestVision", screenshot, meta);
//			}
//
//			// sleep for 100ms
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
//			
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
////		}

	}
}
