package reasoner;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import KR.StructureAnalyse;
import KR.util.ObjectCollector;
import KR.util.VisionObject;
import ab.vision.ABObject;

public class StabilityModule {
	public List<ABObject> getUnstableObjs(List<ABObject> abos, Rectangle Ground) {
		 //System.out.println("Start to get global stability " + abos.size());
		List<ABObject> unstableObjs = new ArrayList<ABObject>();

		ObjectCollector oc = new ObjectCollector(abos);
		HashMap<Integer, VisionObject> vos = oc.getObjects();
		VisionObject voGround = new VisionObject(-1, "Ground", Ground);
		vos.put(voGround.id, voGround);
		StructureAnalyse sa = new StructureAnalyse(vos, 1);
		for (int key : vos.keySet()) {
			AbsloluteStability as = new AbsloluteStability(vos.get(key), sa,
					null);
			if (as.checkStability()) {
				// System.out.println("GT:: Object " + key + " is stable");

			} else {
				if (!vos.get(key).type.equals("Ground")) {
					unstableObjs.add(vos.get(key).abo);
					// System.out.println("GT::  Object " + key +
					// " is not stable");
				} else {
					// System.out.println("GT:: Ground " + key + " is stable");

				}
			}
		}

		// System.out.println("echo4");
		return unstableObjs;
	}

	public boolean isLocallyStable(ABObject abo, List<ABObject> abos,
			Rectangle Ground) {
		//System.out.println("Start to check local stable; " + abos.size());
		ObjectCollector oc = new ObjectCollector(abos);

		HashMap<Integer, VisionObject> vos = oc.getObjects();
		VisionObject voGround = new VisionObject(-1, "Ground", Ground);
		vos.put(voGround.id, voGround);

		StructureAnalyse sa = new StructureAnalyse(vos, 1);

		AbsloluteStability as = new AbsloluteStability(vos.get(abo.id), sa,
				null);
		if (as.checkStability()) {
			// System.out.println("Object " + abo.id + " is stable");
			//System.out.println("finish local stable;");
			return true;
		} else {
			//System.out.println("finish local stable;");
			// System.out.println("Object " + abo.id + " is not stable");
			return false;
		}
	}

	public List<List<Rectangle>> getPossibleSupportingAreas(ABObject abo,
			List<ABObject> abos) {
		List<List<Rectangle>> setOfSupportingAreas = new ArrayList<List<Rectangle>>();

		int areaHeight = 10;
		int height = abo.getBounds().height;
		int width = abo.getBounds().width;

		// TODO deal with angular objects

		List<Rectangle> supportAreas = new ArrayList<Rectangle>();
		supportAreas.add(new Rectangle(abo.getBounds().x + width / 3, abo
				.getBounds().y + height, width / 3, areaHeight));
		supportAreas.add(new Rectangle(abo.getBounds().x, abo.getBounds().y
				+ height, width / 3, areaHeight));
		supportAreas.add(new Rectangle(abo.getBounds().x + 2 * width / 3, abo
				.getBounds().y + height, width / 3, areaHeight));
		setOfSupportingAreas.add(supportAreas);

		return setOfSupportingAreas;
	}

	public List<List<Area>> getSupportingAreas(ABObject abo,
			List<ABObject> abos) {
		List<List<Area>> setOfSupportingAreas = new ArrayList<List<Area>>();

		int areaHeight = 10;
		int areaWidth = 10;
		int height = abo.getBounds().height;
		int width = abo.getBounds().width;

		Path2D centrePath = new Path2D.Double();
		centrePath.moveTo(abo.x+width/2 - width/6, abo.y - 2);
		centrePath.lineTo(abo.x+width/2 + width/6, abo.y - 2);
		centrePath.lineTo(abo.x+width/2 + width/6, abo.y + height + 2);
		centrePath.lineTo(abo.x+width/2 - width/6, abo.y + height + 2);
		centrePath.closePath();

		Area centreArea = new Area(centrePath);
		Area intersectShape = new Area(centrePath);
		intersectShape.intersect(new Area(abo));
		centreArea.exclusiveOr(intersectShape);

		List<Area> supportAreas = new ArrayList<Area>();

		setOfSupportingAreas.add(supportAreas);

		return setOfSupportingAreas;
	}

	class PolygonUtilities {

		/**
		 * @author Christopher Fuhrman (christopher.fuhrman@gmail.com)
		 * @version 2006-09-27
		 * Function to calculate the area of a polygon, according to the
		 * algorithm defined at
		 * http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
		 *
		 * @param polyPoints
		 *            array of points in the polygon
		 * @return area of the polygon defined by pgPoints
		 */
		public double area(Point2D[] polyPoints) {
			int i, j, n = polyPoints.length;
			double area = 0;

			for (i = 0; i < n; i++) {
				j = (i + 1) % n;
				area += polyPoints[i].getX() * polyPoints[j].getY();
				area -= polyPoints[j].getX() * polyPoints[i].getY();
			}
			area /= 2.0;
			return (area);
		}

		/**
		 * Function to calculate the center of mass for a given polygon,
		 * according ot the algorithm defined at
		 * http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/
		 ***
		 * @author Christopher Fuhrman (christopher.fuhrman@gmail.com)
		 * @version 2006-09-27
		 *
		 * @param polyPoints
		 *            array of points in the polygon
		 * @return point that is the center of mass
		 */
		public Point2D centerOfMass(Point2D[] polyPoints) {
			double cx = 0, cy = 0;
			double area = area(polyPoints);
			// could change this to Point2D.Float if you want to use less memory
			Point2D res = new Point2D.Double();
			int i, j, n = polyPoints.length;

			double factor = 0;
			for (i = 0; i < n; i++) {
				j = (i + 1) % n;
				factor = (polyPoints[i].getX() * polyPoints[j].getY() - polyPoints[j]
						.getX() * polyPoints[i].getY());
				cx += (polyPoints[i].getX() + polyPoints[j].getX()) * factor;
				cy += (polyPoints[i].getY() + polyPoints[j].getY()) * factor;
			}
			area *= 6.0f;
			factor = 1 / area;
			cx *= factor;
			cy *= factor;
			res.setLocation(cx, cy);
			return res;
		}

	}

}
