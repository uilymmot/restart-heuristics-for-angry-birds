package KR.util;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import ab.vision.ABObject;
import ab.vision.VisionUtils;

public class VisionObject {
	public int id;
	public String type;
	public Rectangle shape;
	public Rectangle mbr;
	public Point startPoint;
	public Point endPoint;
	public Point centrePoint;
	public float angle;
	public DIRECTION direction;
	public ABObject abo;
	public boolean nonRec = false;
	public Polygon poly;

	public static enum DIRECTION {
		Vertical, Horizon, LeanToRight, LeanToLeft;
	}

	public VisionObject(int id, String Type, Rectangle realShape) {
		this.id = id;
		this.shape = realShape;
		this.mbr = realShape;
		this.startPoint = new Point((int) realShape.getMinX(),
				(int) realShape.getMinY());
		this.endPoint = new Point((int) realShape.getMaxX(),
				(int) realShape.getMaxY());
		this.centrePoint = new Point((int) realShape.getCenterX(),
				(int) realShape.getCenterY());
		this.type = Type;
		this.angle = 0;
		if (this.shape.height > this.shape.width)
			this.direction = DIRECTION.Vertical;
		else
			this.direction = DIRECTION.Horizon;
	}
	
	public VisionObject(int id, String Type, Rectangle realShape, Polygon poly) {
		this.nonRec = true;
		this.id = id;
		this.shape = realShape;
		this.mbr = realShape;
		this.startPoint = new Point((int) realShape.getMinX(),
				(int) realShape.getMinY());
		this.endPoint = new Point((int) realShape.getMaxX(),
				(int) realShape.getMaxY());
		this.centrePoint = new Point((int) realShape.getCenterX(),
				(int) realShape.getCenterY());
		this.type = Type;
		this.angle = 0;
		
		this.poly = poly; 
		if (this.shape.height > this.shape.width)
			this.direction = DIRECTION.Vertical;
		else
			this.direction = DIRECTION.Horizon;
	}
	

	public VisionObject(int id, String Type, Rectangle realShape, float angle) {
		this.id = id;
		this.shape = realShape;
		AffineTransform rotateRec = new AffineTransform();
		rotateRec.rotate(Math.toRadians(-angle), realShape.getCenterX(),
				realShape.getCenterY());
		Shape rotatedRec = rotateRec.createTransformedShape(realShape);
		this.mbr = rotatedRec.getBounds();
		this.startPoint = new Point((int) this.mbr.getMinX(),
				(int) this.mbr.getMinY());
		this.endPoint = new Point((int) this.mbr.getMaxX(),
				(int) this.mbr.getMaxY());
		this.centrePoint = new Point((int) realShape.getCenterX(),
				(int) realShape.getCenterY());
		this.type = Type;
		this.angle = angle;
		if (this.shape.height > this.shape.width) {
			if (angle < 5) {
				this.direction = DIRECTION.Vertical;
			} else {
				this.direction = DIRECTION.LeanToLeft;
			}
		} else {
			if (angle < 5) {
				this.direction = DIRECTION.Horizon;
			} else {
				this.direction = DIRECTION.LeanToRight;
			}
		}
	}

	public void setObjectID(int ID) {
		this.id = ID;
	}

	public void setABO(ABObject abo){
		this.abo = abo;
	}
	
	public Area realShapeIntersect(VisionObject vo) {
		AffineTransform rotate1 = new AffineTransform();
		rotate1.rotate(Math.toRadians(-this.angle), this.shape.getCenterX(),
				this.shape.getCenterY());
		Rectangle enlargedShape1 = VisionUtils.dialateRectangle(this.shape, 3,
				3);
		Shape shapeA = rotate1.createTransformedShape(enlargedShape1);

		AffineTransform rotate2 = new AffineTransform();
		rotate2.rotate(Math.toRadians(-vo.angle), vo.shape.getCenterX(),
				vo.shape.getCenterY());
		Rectangle enlargedShape2 = VisionUtils.dialateRectangle(vo.shape,3, 3);
		Shape shapeB = rotate2.createTransformedShape(enlargedShape2);

		Area area = new Area(shapeA);
		area.intersect(new Area(shapeB));
		return area;
	}
}
