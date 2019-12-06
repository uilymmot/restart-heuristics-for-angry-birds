package KR;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import reasoner.ReasonerUtils;

import ab.server.Proxy;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.real.shape.Poly;
//import ab.utils.ShowDebuggingImage;
//import ab.vision.TestVision;

import KR.PhysicalRelation.SUPPORT_RELATION;
import KR.SpatialRelation.ERA;
import KR.util.KeyPair;
import KR.util.ObjectCollector;
import KR.util.VisionObject;
import KR.util.VisionObject.DIRECTION;

public class PhysicalRelation extends SpatialRelation {

	/** Support relations **/

	public enum SUPPORT_RELATION {
		SURFACE_TO_SURFACE, SURFACE_TO_POINT, POINT_TO_SURFACE, NO_DIRECT_SUPPORT, SURFACE_TO_SURFACE_I, SURFACE_TO_POINT_I, POINT_TO_SURFACE_I, INCONSISTANCE;
	};

	private HashMap<KeyPair, SUPPORT_RELATION> SupportRel;
	private HashMap<KeyPair, Area> contactAreas;
	private HashMap<KeyPair, ERA[]> RA;
	private ArrayList<Integer> AngularLeft;
	private ArrayList<Integer> AngularRight;

	public PhysicalRelation() {

	}

	public PhysicalRelation(HashMap/* Type will be checked later */
	objs, double refLength) {
		super(objs, refLength);
		// this.sr = new SpatialRelation(objs);
		this.SupportRel = new HashMap<KeyPair, SUPPORT_RELATION>();
		this.contactAreas = new HashMap<KeyPair, Area>();
		this.RA = new HashMap<KeyPair, ERA[]>();
		AngularLeft = new ArrayList<Integer>();
		AngularRight = new ArrayList<Integer>();
		this.SupportDetect();
	}

	/**
	 * Naive method which treat two objects with overlapped or vertically
	 * touched MBRs as support
	 */
	private void SupportDetect() {
		ERA[] RArel = new ERA[2];
		ERA rax, ray;
		SUPPORT_RELATION supportrel;

		this.RA = super.getRArel();
		for (KeyPair k : this.RA.keySet()) {
			VisionObject vo1, vo2;
			vo1 = super.getObjs().get(k.getFirst());
			vo2 = super.getObjs().get(k.getSecond());
			DIRECTION direction1 = vo1.direction;
			DIRECTION direction2 = vo2.direction;
//			System.out.print("DIR1 : " + direction1 + " DIR2 : " + direction2 + "\n");
			Area intersectArea = vo1.realShapeIntersect(vo2);
			RArel = this.RA.get(k);
			rax = RArel[0];
			ray = RArel[1];

			if(vo1.nonRec){
				if(vo1.poly.contains(vo2.shape.x, vo2.shape.y + vo2.shape.height + 5, vo2.shape.width, 10)){
					supportrel = SUPPORT_RELATION.SURFACE_TO_SURFACE;
					this.SupportRel.put(k, supportrel);
					this.contactAreas.put(k, intersectArea);
					continue;
				}
			}
			
			if (intersectArea.isEmpty()) {
				supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
				continue;
			}
			
			if (ray == ERA.MEET_I) {
				if (rax != ERA.MEET && rax != ERA.MEET_I && rax != ERA.AFTER
						&& rax != ERA.BEFORE) {
					supportrel = SUPPORT_RELATION.SURFACE_TO_SURFACE;

				/*	if ((direction1.equals(DIRECTION.Horizon) || direction1
							.equals(DIRECTION.Vertical))
							&& (direction2.equals(DIRECTION.Horizon) || direction2
									.equals(DIRECTION.Vertical))) {
						supportrel = SUPPORT_RELATION.SURFACE_TO_SURFACE;
					} else if (!intersectArea.isEmpty()) {
						if (direction1.equals(DIRECTION.Horizon)
								|| direction1.equals(DIRECTION.Vertical))
							supportrel = SUPPORT_RELATION.SURFACE_TO_POINT;
						else if (direction2.equals(DIRECTION.Horizon)
								|| direction2.equals(DIRECTION.Vertical))
							supportrel = SUPPORT_RELATION.POINT_TO_SURFACE;
						else
							supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
					} else {
						supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
					}*/
				}

				else {
					supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
				}
				
			}

//			else if (rax != ERA.MEET && rax != ERA.MEET_I && rax != ERA.AFTER
//						&& rax != ERA.BEFORE) {
//				if (ray == ERA.MOST_START_I || ray == ERA.LESS_START_I
//					|| ray == ERA.MOST_FINISH || ray == ERA.LESS_FINISH
//					|| ray == ERA.MOST_OVERLAP_MOST_I
//					|| ray == ERA.MOST_OVERLAP_LESS_I
//					|| ray == ERA.LESS_OVERLAP_MOST_I
//					|| ray == ERA.LESS_OVERLAP_LESS_I || ray == ERA.LEFT_DURING
//					|| ray == ERA.RIGHT_DURING || ray == ERA.CENTRE_DURING) {
//					supportrel = SUPPORT_RELATION.POINT_TO_SURFACE;				
//				}
//				else {
//					supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
//				}
//
//			}

			else {
				supportrel = SUPPORT_RELATION.NO_DIRECT_SUPPORT;
			}

			if (supportrel != SUPPORT_RELATION.NO_DIRECT_SUPPORT) {
				this.SupportRel.put(k, supportrel);
				this.contactAreas.put(k, intersectArea);
			}
		}
	}

	public HashMap<KeyPair, SUPPORT_RELATION> getSupportRel() {
		return this.SupportRel;
	}

	/**
	 * output naive support relations
	 * 
	 * @param suppr
	 */
	public void printSupportRelation(HashMap<KeyPair, SUPPORT_RELATION> suppr) {

		System.out.print("SUPPORT RELATION: \n");
		for (KeyPair k : suppr.keySet()) {
			switch (suppr.get(k)) {
			case SURFACE_TO_SURFACE:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " SURFACE_TO_SURFACE\n");
				break;
			case SURFACE_TO_POINT:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " POINT_TO_SURFACE\n");
				break;
			case POINT_TO_SURFACE:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " POINT_TO_SURFACE\n");
				break;
			case NO_DIRECT_SUPPORT:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " NO_DIRECT_SUPPORT\n");
				break;
			case SURFACE_TO_SURFACE_I:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " SURFACE_TO_SURFACE_INVERSE\n");
				break;
			case SURFACE_TO_POINT_I:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " SURFACE_TO_POINT_INVERSE\n");
				break;
			case POINT_TO_SURFACE_I:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " POINT_TO_SURFACE_INVERSE\n");
				break;
			case INCONSISTANCE:
				System.out.print(k.getFirst() + " and " + k.getSecond() + " :"
						+ " INCONSISTANCE\n");
				break;
			default:
				System.out.print("UNKNOWN!\n");
				break;
			}
		}
	}
	
	public BufferedImage drawContactPoints(BufferedImage canvas){
		Graphics2D g2d = canvas.createGraphics();
		g2d.setColor(Color.RED);
		for(Entry<KeyPair, Area> e : this.contactAreas.entrySet()){
			g2d.draw(e.getValue());
		}
		
		return canvas;
	}
	
	public static void main(String args[]) {
//
//		BufferedImage screenshot = null;
//		ShowDebuggingImage frame = null;
//		Proxy game = TestVision.getGameConnection(9000);
//		HashMap<Integer, VisionObject> objs = null;
//
//		// while (true) {
//		// capture an image
//		byte[] imageBytes = game.send(new ProxyScreenshotMessage());
//		try {
//			screenshot = ImageIO.read(new ByteArrayInputStream(imageBytes));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		ObjectCollector oc = new ObjectCollector(screenshot);
//
//		HashMap<KeyPair, ERA[]> RA = new HashMap<KeyPair, ERA[]>();
//		// System.out.println(oc.refLength);
//		objs = oc.getObjects();
//		PhysicalRelation pr = new PhysicalRelation(objs, oc.refLength);
//		HashMap<KeyPair, SUPPORT_RELATION> suppr = pr.getSupportRel();
//		pr.printSupportRelation(suppr);
//		imageBytes = game.send(new ProxyScreenshotMessage());
//		try {
//			screenshot = ImageIO.read(new ByteArrayInputStream(imageBytes));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		// analyse and show image
//		int[][] meta = TestVision.computeMetaInformation(screenshot);
//		screenshot = ReasonerUtils.showBoundings(screenshot);
//		screenshot = pr.drawContactPoints(screenshot);
//		if (frame == null) {
//			frame = new ShowDebuggingImage("TestVision", screenshot, meta);
//		}
//		/*
//		 * CreateVision cv = new CreateVision(Env.getMatlabDir());
//		 * ObjectCollector oc = new ObjectCollector(); oc.collect(cv); String
//		 * image_path = Env.getMatlabDir() + Env.getSystemSeparator() +
//		 * "im.png";
//		 * 
//		 * try { new Picture_Panel(image_path,oc); } catch (Exception e1) {
//		 * System.out.print("Draw Pic Failed! \n"); // TODO Auto-generated catch
//		 * block e1.printStackTrace(); } PhysicalRelation pr = new
//		 * PhysicalRelation(oc.getObjs(), oc.refLength); HashMap<KeyPair,
//		 * Integer> suppr = pr.getSupportRel(); pr.printSupportRelation(suppr);
//		 */
	}

}
