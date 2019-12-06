package ab.demo.Solvability;

import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by uilym on 1/03/2019.
 */
public class Reachablility {

    //  TODO: Fix this up, not exactly fully working, issues with pig IDs maybe. Some blocks being left out, also different pig types and multiple blocks being hit?

    // TODO: AREA DEPENDS ON ZOOM LEVEL OF LEVEL SO HARD AREA CONSTRAINTS GONNA BE SORTA TOUGH
    public static List<List<Integer>> reachableBlocks(List<ABObject> blocks, Vision vision, ABType birdTypeFired) {
        Set<List<Integer>> reachByThisBird = new HashSet<>();
        TrajectoryPlanner tp = new TrajectoryPlanner();
        List<List<Point>> releasePointsForBlocks = new ArrayList<List<Point>>();
        Rectangle sling = vision.findSlingshotMBR();

        List<ABObject> hills = vision.findHills();

        for (int i = 0; i < blocks.size(); i++) {
            //System.out.println("THIS THING LOOKING FOR POINTS " + i);
            ABObject b = blocks.get(i);
            List<Point> pt = tp.estimateLaunchPoint(sling, b.getCenter());
            // Mid and bottom points
//            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x,
//                    (int) (b.y + b.getHeight()))));
//            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x,
//                    (int) (b.y + (b.getHeight() / 2)))));
            // Two thirds points
//            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x,
//                    (int) (b.y + (2 * (b.getHeight() / 3))))));
//            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x,
//                    (int) (b.y + (b.getHeight()/ 3)))));

            // Center left and center top points,
            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x, (int) (b.y + (b.getHeight() / 2)))));
            pt.addAll(tp.estimateLaunchPoint(sling, new Point((int)(b.x + (b.getWidth() / 2)), b.y)));
            //top left point
            pt.addAll(tp.estimateLaunchPoint(sling, new Point(b.x, b.y)));

            releasePointsForBlocks.add(pt);
        }

        for (int i = 0; i < releasePointsForBlocks.size(); i++){
            boolean bre = false;
            Set<Integer> reachBlocks = new HashSet<>();

            for (int j = 0; j < releasePointsForBlocks.get(i).size(); j++){

                //test whether the trajectory can pass the target without considering obstructions
                Point releasePoint = releasePointsForBlocks.get(i).get(j);
                int traY = tp.getYCoordinate(sling, releasePoint, blocks.get(i).x);
//                if (Math.abs(traY - blocks.get(i).y) < 100)
//                {
//                    reachable = 1;
//                }
                List<Point> points = tp.predictTrajectory(sling, releasePoint);
                for(Point point: points) {
                    if (point.x < 840 && point.y < 480 && point.y > 100 && point.x > 400) {
                        for (ABObject ab : blocks) {
                            if (!reachBlocks.contains(ab.id)){
                                if (((ab.contains(point)
                                        && !ab.contains(blocks.get(i)))
                                        || Math.abs(vision.getMBRVision()._scene[point.y][point.x] - 48) < 10)
                                        && point.x < blocks.get(i).x) {

                                    if (!(ab.type == ABType.BlueBird
                                            || ab.type == ABType.RedBird
                                            || ab.type == ABType.YellowBird
                                            || ab.type == ABType.Sling)) {

                                        if (ab.type == ABType.Pig) {
                                            reachBlocks.add(ab.id);
//                                            System.out.println("added a piggy");
                                            continue;
                                        }

                                        // TODO: Work with different types of birds fired and block types
                                        if (birdTypeFired == ABType.YellowBird
                                                && reachBlocks.size() < 4
                                                && ab.type == ABType.Wood) {
                                            reachBlocks.add(ab.id);
                                            continue;
                                        } else if (birdTypeFired == ABType.YellowBird) {
                                            reachBlocks.add(ab.id);
                                            bre = true;
                                        }

                                        if (birdTypeFired == ABType.BlueBird
                                                && ab.type == ABType.Ice
                                                && reachBlocks.size() < 4) {
                                            reachBlocks.add(ab.id);
                                            continue;
                                        }
                                        else if (birdTypeFired == ABType.BlueBird) {
                                            reachBlocks.add(ab.id);
                                            bre = true;
                                        }

                                        if (reachBlocks.size() == 0
                                                && birdTypeFired == ABType.RedBird
                                                && (ab.type == ABType.Ice
                                                && (ab.getWidth() * ab.getHeight() < 500))
                                                || (ab.type == ABType.Wood
                                                && (ab.getWidth() * ab.getHeight() < 25))) {
                                            reachBlocks.add(ab.id);
//                                            System.out.println("added an ice");
                                            continue;
                                        } else if (birdTypeFired == ABType.RedBird) {
                                            reachBlocks.add(ab.id);
//                                            System.out.println("Added an ice but bird stoppin");
//                                            System.out.println(ab.getType()
//                                                    + "," + (ab.getHeight() * ab.getWidth())
//                                                    + "," + reachBlocks.size()
//                                                    + "," + ab.getHeight()
//                                                    + "," + ab.getWidth());
                                            bre = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (bre) break;
                }
            }

            List<Integer> ret = new ArrayList<>();
            for (Integer in : reachBlocks)
                ret.add(in);
            if (ret.size() > 0) reachByThisBird.add(ret);
        }

        List<List<Integer>> returnList = new ArrayList<>();
        returnList.addAll(reachByThisBird);
//        for (List<Integer> se : reachByThisBird) {
//            returnList.add(se);
//        }
        return returnList;
    }

}
