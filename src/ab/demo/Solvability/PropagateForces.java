package ab.demo.Solvability;

import ab.vision.ABObject;
import ab.vision.ABType;

import java.util.*;

import static ab.demo.Solvability.BlockPropertyHelpers.aboBelow;

/**
 * Created by uilym on 3/03/2019.
 */
public class PropagateForces {
    //Returns a hashmap of affected blocks and the amount of energy they will receive via propagation
    public static HashMap<Integer, Force> propagateRightFoce(double[][] fGraph, int hitNode, double momentum) {
        HashMap<Integer, Force> blocksMoved = new HashMap<>();
        Set<Integer> affectedNodes = new HashSet<>();
        affectedNodes.add(hitNode);
        Set<Integer> nextNodes = new HashSet<>();
        nextNodes.add(hitNode);
        blocksMoved.put(hitNode, new Force(new double[]{0,momentum,0,0}));

        try {
            //int distance = 0;
            while (true) {
                List<Integer> itemsToRemove = new ArrayList<>();
                List<Integer> itemsToAdd = new ArrayList<>();
                for (Integer propagateBlocks : nextNodes) {
                    for (int i = 0; i < fGraph[0].length; i++) {
                        if (fGraph[propagateBlocks][i] > 0 && !affectedNodes.contains(i)) {
                            if (blocksMoved.containsKey(i)) {
                                Force propaForce = blocksMoved.get(i);
                                Force newFoce = propaForce.NewForceSum(blocksMoved.get(propagateBlocks));
                                newFoce.Mult(fGraph[propagateBlocks][i]*0.9, 1);
                                blocksMoved.put(i, propaForce);
                            }
                            else {
                                Force newForce = new Force(new double[]{0,0,0,0});
                                newForce.ForceSum(blocksMoved.get(propagateBlocks));
                                newForce.Mult(fGraph[propagateBlocks][i] * 0.9, 1);
                                blocksMoved.put(i, newForce);
                            }
                            affectedNodes.add(i);
                            itemsToAdd.add(i);
                        }
                    }
                    itemsToRemove.add(propagateBlocks);
                }

                for (Integer rem : itemsToRemove)
                    nextNodes.remove(rem);
                for (Integer ade : itemsToAdd)
                    nextNodes.add(ade);
                //distance++;
                if (nextNodes.size() == 0) break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return blocksMoved;
    }


    public static HashMap<Integer, Force> propagateDownForce(HashMap<Integer, Force> fs, HashMap<Integer, ABObject> blocks, int[][] downForceGraph) {
        HashMap<Integer, Force> newForces = new HashMap<>();
        for (Map.Entry<Integer, Force> forc : fs.entrySet()) {
            //System.out.println("Down entry here for: " + forc.getKey());
            for (int i = 0; i < downForceGraph[0].length; i++) {
                if (downForceGraph[forc.getKey()][i] == 1 || downForceGraph[i][forc.getKey()] == 1) {
                    if (fs.containsKey(i) && fs.get(i).direction[1] > 5) {
                        Force newForce = new Force(new double[]{0,0,blocks.get(forc.getKey()).getY(),0});
                        newForce.ForceSum(fs.get(i));
                        newForces.put(i, newForce);
                    }
                }
            }
            if (!(newForces.containsKey(forc.getKey())))
                newForces.put(forc.getKey(), forc.getValue());
        }
        return newForces;
    }

    public static HashMap<Integer, Force> overallForceEffect(HashMap<Integer, Force> fs, HashMap<Integer, ABObject> blocks, int[][] supportGraph) {
        if (fs.size() == 0) return fs;
        // Deal with all the forces on blocks and how they propagate

        HashMap<Integer, Force> tempForces = new HashMap<>();

        for (Map.Entry<Integer, Force> ofe : fs.entrySet()) {
            if (ofe.getValue().direction[2] > 0) {
                //this block falls
                //TODO: CHECK PROPERLY WHICH BLOCKS THIS BLOCK FALLS ONTO

                // the blocks currently below this tumbling object
                List<ABObject> belowBlocks = aboBelow(blocks.get(ofe.getKey()), blocks);
//                Collections.sort(belowBlocks, (p1, p2) -> (int)(p2.getY() - p1.getY()));
                belowBlocks.sort((p1, p2) -> (int) (p2.getY() - p1.getY()));

                double lastY = -9999;
                int solidBlocksPassed = 0;
                for (ABObject ab : belowBlocks) {
                    if (!(supportGraph[ofe.getKey()][ab.id] == 1)) {
                        if (!(ab.type == ABType.Pig
                                || (ab.type == ABType.Ice
                                && (ab.getHeight() * ab.getWidth() < 100)
                        ))) {
                            solidBlocksPassed++;
                        }
                        Force newFoce = new Force(new double[]{0, 0, 0, 0});
                        newFoce.ForceSum(ofe.getValue());
                        if (fs.containsKey(ab.id)) {
                            newFoce.ForceSum(fs.get(ab.id));
                        }
                        tempForces.put(ab.id, newFoce);
                    }
                    lastY = ab.getY();
                    // TODO: RELATE NUMBER OF BLOCKS PASSED TO MASS OF FALLING OBJECT?
                    if (solidBlocksPassed >= 2) break;
                }
            }
        }
        for (Map.Entry<Integer, Force> af : tempForces.entrySet()) {
            fs.put(af.getKey(), af.getValue());
        }

        return fs;
    }

}
