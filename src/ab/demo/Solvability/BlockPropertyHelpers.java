package ab.demo.Solvability;

import ab.vision.ABObject;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by uilym on 1/03/2019.
 */
public class BlockPropertyHelpers {

    // Returns the center of mass of a group of objects, MUST USE REAL VISION MODULE
    public static Point centerOfMass(java.util.List<ABObject> whatBlocks) {
        if (whatBlocks.size() == 0) return new Point(0,0);
        int AiCix = 0;
        int AiCiy = 0;
        int totalArea = 0;
        for (ABObject ab : whatBlocks) {
            int area = (int)(ab.getWidth() * ab.getHeight());
            totalArea += area;
            AiCix += ab.getCenterX() * area;
            AiCiy += ab.getCenterY() * area;
        }
        AiCix /= totalArea;
        AiCiy /= totalArea;
        return new Point(AiCix, AiCiy);
    }

    public static boolean overlap(int a, int ah, int b, int bh) {
        for (int i = b; i <= bh; i++)
            if (i < a + ah && i > a)
                return true;
        return false;
    }

    public static boolean within(double xs, double xe, double p) {
        return ((p <= xe && p >= xs) || (p <= xs && p >= xe));
    }

    //A simple single right rotation calculation if a can fall onto b using two rectangles
    //Approximation using 6 rectangles
    public static boolean rightFallOnto(ABObject a, ABObject b) {
        if (a.x <= b.x) {
            double heightDistance = a.height / 7;

            for (int i = 6; i > 0; i--) {
                double myHeightDistance = i * heightDistance;
                Rectangle rotatedRet = new Rectangle(
                        (int)(a.x + a.getWidth()),
                        (int)(a.y + myHeightDistance),
                        (int)(Math.cos(myHeightDistance / a.getHeight()) * a.getHeight()),
                        (int)a.getWidth()
                );
                if (rotatedRet.intersects(b)) return true;
            }
        }
        return false;
    }

    //A simple single left rotation calculation if a can fall onto b
    public static boolean leftFallOnto(ABObject a, ABObject b) {
        if (a.x >= b.x) {
            double heightDistance = a.height / 7;

            for (int i = 6; i > 0; i--) {
                double myHeightDistance = i * heightDistance;
                Rectangle rotatedRet = new Rectangle(
                        (int)(a.x + a.getWidth()),
                        (int)(a.y + myHeightDistance),
                        (int)(Math.cos(myHeightDistance / a.getHeight()) * a.getHeight()),
                        (int)a.getWidth()
                );
                if (rotatedRet.intersects(b)) return true;
            }
        }
        return false;
    }

    public static boolean above(ABObject a, ABObject b) {
        Rectangle equalYAObject = new Rectangle(
                (int)(a.getX()),
                (int)(b.getY()),
                (int)(a.getWidth()),
                (int)(b.getHeight()));
        return (equalYAObject.intersects(b) && a.getY() > b.getY());
    }



    public static List<ABObject> aboBelow(ABObject a, HashMap<Integer, ABObject> blocks) {
        List<ABObject> currentBelows = new ArrayList<>();
        for (int i = 0; i < blocks.size(); i++) {
            if (i != a.id && above(a, blocks.get(i))) {
                currentBelows.add(blocks.get(i));
            }
        }
        return currentBelows;
    }

    public static List<ABObject> aboRightOf(ABObject a, HashMap<Integer, ABObject> blocks, int threshold) {
        List<ABObject> temp = new ArrayList<>();
        for (Map.Entry<Integer, ABObject> obf : blocks.entrySet()) {
            if (!(obf.getValue() == a)) {
                ABObject currObject = obf.getValue();
                if (currObject.getX() < a.getX() + threshold
                        && a.getY() >= currObject.getY() + (threshold / 10)) {
                    temp.add(currObject);
                }
            }
        }
        return temp;
    }

    public static List<Integer> findSupportedBlocks(int[][] supportGraph, int node) {
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < supportGraph[0].length; i++) {
            if (supportGraph[node][i] >= 1) {
                temp.add(i);
            }
        }
        return temp;
    }
}
