package KR.util;

import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;
import java.util.List;

import ab.vision.ABObject;
import ab.vision.real.shape.Poly;

public class ObjectCollector {
	
	public HashMap<Integer, VisionObject> objs;
	public HashMap<Integer, VisionObject> hills;
	public double refLength = 1.;
	public Point refPoint = new Point(0, 0);
	public Point focusPoint = new Point(0, 0);
	public int birdCount = 0;
	public int pigCount = 0;
	public int currentBird = -1;
	public int score = -1;


	public ObjectCollector(List<ABObject> abos) {
		transferABO(abos);
		// this.hills = this.collectHill();
	}

	public HashMap<Integer, VisionObject> getObjects() {
		return this.objs;
	}

	public HashMap<Integer, VisionObject> getHills() {
		return this.hills;
	}

	// private HashMap<Integer, VisionObject> collectHill() {
	// int hillIDCount = 0;
	// this.hills = new HashMap<Integer,VisionObject>();
	// List<Rectangle> temObjs = new ArrayList<Rectangle>();
	// temObjs.clear();
	// temObjs = this.vision.findHill();
	// if (!temObjs.isEmpty()) {
	// for (Rectangle o : temObjs) {
	// VisionObject vo = new VisionObject(hillIDCount, "Hill", o);
	// this.hills.put(hillIDCount, vo);
	// hillIDCount++;
	// }
	// }
	//
	// return this.hills;
	// }

	private HashMap<Integer, VisionObject> transferABO(List<ABObject> abos) {
		this.objs = new HashMap<Integer, VisionObject>();

		for (ABObject abo : abos) {
			String type;

			type = "Unknown";
			
			
			VisionObject vo;
			
			if (abo instanceof Poly) {
				Poly _abo = (Poly) abo;
				Polygon a = _abo.polygon;
				vo = new VisionObject(abo.id, type,
						abo.getBounds(), a);
			}

			else {
				vo = new VisionObject(abo.id, type,
						abo.getBounds());
			}
			vo.setABO(abo);
			this.objs.put(abo.id, vo);

		}

		return this.objs;
	}



}
