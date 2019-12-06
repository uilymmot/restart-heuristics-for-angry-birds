package ab.demo.Solvability;

import ab.vision.ABObject;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by uilym on 1/03/2019.
 */

public class Ground {

    public static List<GroundItem> groundProperties(Vision vis) {
        List<ABObject> hills = vis.findHills();
        List<List<GroundItem>> grounds = new ArrayList<>();
//        System.out.println(hills.size() + " WE HAVE THIS MANY HILLS");
        for(ABObject abo: hills){
            List<GroundItem> g = new ArrayList<>();
            //Use "instanceof" to check the actual shape of the abobject
            if(abo instanceof Poly){
                Poly aboPoly = (Poly)abo;
                Polygon polygon = aboPoly.polygon;
                //do something with the polygon

                //get points of polygon
                List<Point> pints = new ArrayList<>();
                for (int i = 0; i < polygon.xpoints.length; i++) {
                    Point p = new Point(polygon.xpoints[i], polygon.ypoints[i]);
                    if (!(polygon.xpoints[i] == 0 && polygon.ypoints[i] == 0)) {
                        pints.add(p);
                    }
                }
                if (pints.size() > 1) {
                    for (int i = 0; i <= pints.size() - 1; i++) {
                        Point p1 = pints.get(i);
                        Point p2 = (i != pints.size() - 1) ? pints.get(i + 1) : pints.get(0);
                        Point p3;
                        if (p1.getX() > p2.getX()) {
                            p3 = p1;
                            p1 = p2;
                            p2 = p3;
                        }
                        //System.out.println(p1.toString() + "|" + p2.toString());
                        if (p1.getX() != p2.getX()) {
                            Double angle = (p1.getY() - p2.getY()) / Math.abs(p1.getX() - p2.getX());
                            g.add(new GroundItem((int) p1.getX(), (int) p2.getX(), angle));
                        }
                    }
                }
            }
            grounds.add(g);
        }
        List<GroundItem> groundsFinal = new ArrayList<>();
        for (List<GroundItem> g : grounds)
            for (GroundItem gitem : g)
                if (Math.abs(gitem.a) >= 0.02d && Math.abs(gitem.a) <= 5) {
                    groundsFinal.add(gitem);
                }
        return groundsFinal;
    }
}
