package reasoner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;

import KR.util.ObjectCollector;
import KR.util.VisionObject;
import ab.vision.GameStateExtractor;
import ab.vision.VisionUtils;

public class ReasonerUtils {

	public static boolean checkConsequence() {
		return true;
	}

	// draw the bounding box for one object
	public static BufferedImage drawBoundingBox(BufferedImage canvas,
			Rectangle r, Color fgColour, Color bgColour, String id) {
		Graphics2D g2d = canvas.createGraphics();
		g2d.setColor(bgColour);
		g2d.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
		g2d.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
		g2d.setColor(fgColour);
		g2d.drawRect(r.x, r.y, r.width, r.height);
		g2d.setColor(Color.red);
		g2d.drawString(id, (int) r.getCenterX(), (int) r.getCenterY());
		return canvas;
	}

	public static BufferedImage drawRealShape(BufferedImage canvas,
			VisionObject vo, Color fgColour, Color bgColour, String id) {
		Graphics2D g2d = canvas.createGraphics();
		AffineTransform rotateRec = new AffineTransform();
		rotateRec.rotate(Math.toRadians(-vo.angle), vo.shape.getCenterX(), vo.shape.getCenterY());
		Shape rotatedRec = rotateRec.createTransformedShape(vo.shape);
		Rectangle bkgRec = new Rectangle(vo.shape.x - 1, vo.shape.y - 1, vo.shape.width + 2, vo.shape.height + 2);
		Shape bkgRotatedRec = rotateRec.createTransformedShape(bkgRec);
		// g2d.rotate(Math.PI/8,r.getCenterX(),r.getCenterY());
		g2d.setColor(fgColour);
		g2d.draw(rotatedRec);
		// g2d.drawRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
		// g2d.drawRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
		g2d.setColor(bgColour);
		g2d.draw(bkgRotatedRec);
		bkgRec = new Rectangle(vo.shape.x + 1, vo.shape.y + 1, vo.shape.width - 2, vo.shape.height - 2);
		bkgRotatedRec = rotateRec.createTransformedShape(bkgRec);
		g2d.draw(bkgRotatedRec);
		
		//draw ids of objects
		g2d.setColor(Color.black);
		g2d.drawString(id, (int) bkgRec.getCenterX(), (int) bkgRec.getCenterY());
		return canvas;
	}
	
	public static BufferedImage drawTarget(BufferedImage screenshot,
			Rectangle target) {
		return drawBoundingBox(screenshot, target, Color.RED, Color.BLUE,
				"target");
	}

	public static BufferedImage drawBoundingBox(BufferedImage canvas,
			Rectangle r, Color fgColour, Color bgColour) {
		return drawBoundingBox(canvas, r, fgColour, bgColour, null);
	}


}
