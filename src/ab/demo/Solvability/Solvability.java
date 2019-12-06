package ab.demo.Solvability;

import KR.StructureAnalyse;
import KR.util.ObjectCollector;
import ab.vision.ABObject;
import ab.vision.ABShape;
import ab.vision.ABType;
import ab.vision.Vision;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.List;

import static ab.demo.Solvability.BlockPropertyHelpers.*;
import static ab.demo.Solvability.GraphGen.fillStabilityGraphOut;
import static ab.demo.Solvability.Ground.groundProperties;
import static ab.demo.Solvability.PropagateForces.propagateDownForce;
import static ab.demo.Solvability.PropagateForces.propagateRightFoce;
import static ab.demo.Solvability.Reachablility.reachableBlocks;

public class Solvability {
    // TODO: MAKE THIS MODULE TAKE A LEVEL AND RETURN WHETHER THE LEVEL IS SOLVABLE IN ONE SHOT OR NOT

    public static int[][] supportGraph = new int[0][0];
    public static double[][] rForceGraph = new double[0][0];
    public static List<Integer> sBlocks = new ArrayList<>();
    public static List<List<Integer>> rBlocks = new ArrayList<>();
    public static Set<ABObject> tumblocks = new HashSet<>();
    public static int nodeToHit;
    public static Point tumblePoint;
    public static List<ABObject> recurseTumbleBlocks = new ArrayList<>();
    public static HashMap<Integer, ABObject> blocks = new HashMap<>();
    public static int[][] downForceGraph = new int[0][0];

    //Given some nodes, find blocks that support all of them
    public static List<Integer> sharesSupportBlocks(List<Integer> pigNodes, int[][] graph) {
        List<Integer> supportingNodes = new ArrayList<>();
        if (pigNodes.size() == 0) {
            supportingNodes.add(1000000);
            return supportingNodes;
        }
        boolean nodeWorks = true;
        for (int node = 0; node < graph[0].length; node++) {
            for (int pig : pigNodes) {
                if (graph[pig][node] == 0) {
                    nodeWorks = false;
                    break;
                }
            }
            if (nodeWorks)
                supportingNodes.add(node);
            nodeWorks = true;
        }
        return supportingNodes;
    }

    // checks the effect of destroying a block on the overall structure
    public static HashMap<Integer, Force> destroyBlockStructureEffect(HashMap<Integer, Force> fs, int node) {
        // First round of direct structure fallings, need to propagate this
        tumblocks = new HashSet<>();
        int structureToppes = supportStructureDestruction(node, supportGraph);

        //System.out.println("CODE FOR STRUCTURE IS: " + structureToppes);
        if (structureToppes == 0) {
            // Nothing happens because the structure doesn't fall
            return fs;
        }
        //blocks receives torque/forces relative to the tumblepoint
        for (ABObject ab : tumblocks) {
            ab.area = (int)(ab.getWidth() * ab.getHeight());
            //System.out.println("AREA OF THIS BLOCK IS: " + ab.area + " which is of type " + ab.type.toString());
            Force newFoce;

            //no tumblepoint
            if (structureToppes == 1)
                newFoce = new Force(new double[]{0,0,ab.getY(),0});
            else if (structureToppes == 2) {
                if (ab.getX() >= tumblePoint.getX())
                    newFoce = new Force(new double[]{0, 0, ab.getY(), 0});
                else {
                    double xChange = ab.getCenterX() - tumblePoint.getX();
                    newFoce = new Force(new double[]{0, ab.getY(), xChange, 0});
                }
            }
            else if (structureToppes == 3) {
                if (ab.getX() <= tumblePoint.getX())
                    newFoce = new Force(new double[]{0, 0, ab.getY(), 0});
                else {
                    double xChange = ab.getCenterX() - tumblePoint.getX();
                    newFoce = new Force(new double[]{0, 0, ab.getY(), xChange});
                }
            }
            else
                newFoce = new Force(new double[]{0,0,0,0});

            if (structureToppes == 1 || structureToppes == 2 || structureToppes == 3) {
                if (!(recurseTumbleBlocks.contains(ab)))
                    recurseTumbleBlocks.add(ab);
            }

            // Add force onto current existing forces
            fs.put(ab.id, newFoce);
        }
        return fs;
    }


    // 0 : STRUCTURE DOES NOT FALL
    // 1 : ENTIRE STRUCTURE FALLS DOWN
    // 2 : STRUCTURE TUMBLES RIGHT
    // 3 : STRUCTURE TUMBLES LEFT
    public static int supportStructureDestruction(int node, int[][] graph) {
        // Check if if the blocks are destroyed, if the supported blocks fall
        //The blocks that the current block supports
        tumblePoint = null;
        List<ABObject> blocksSupported = new ArrayList<>();
        List<ABObject> directSupport = new ArrayList<>();
        List<ABObject> supportsOfDirectSupports = new ArrayList<>();

        //cant destroy stone blocks - so sad
        try {
            if (blocks.get(node) != null && blocks.get(node).type == ABType.Stone) return 0;
        }
        catch (Exception e) {}

        for (int i = 0; i < graph[0].length-1; i++) {
//            System.out.println("Max size is" + graph[0].length + ", currently: " + i);
            if (graph[node][i] > 0)
                blocksSupported.add(blocks.get(i));
            if (graph[node][i] == 1) {
                directSupport.add(blocks.get(i));
                for (int j = 0; j < graph[0].length-1; j++) {
                    if (graph[j][i] == 1 && j != node)
                        supportsOfDirectSupports.add(blocks.get(j));
                }
            }

        }
        // TODO: DEAL WITH BUG WHERE SOME BLOCKS NOT DETECTED HERE

        //System.out.println(blocksSupported.size() + " bleeeergh");

        if (blocksSupported.size() == 0) return 0;
        tumblocks.addAll(directSupport);
        Point centerMass = centerOfMass(blocksSupported);

        // cases: 1 supports x
        // then the supporting blocks will fall without a pivot point
        if (supportsOfDirectSupports.size() == 0) return 1;

        // cases: x supports 1
        if (supportsOfDirectSupports.size() > 0 && directSupport.size() == 1) {
            boolean leftSupped = false;
            boolean rightSupped = false;
            for (ABObject ab : supportsOfDirectSupports) {
                if (ab.getX() < centerMass.getX()) leftSupped = true;
                else if (ab.getX() > centerMass.getX()) rightSupped = true;
            }
            if (!leftSupped || !rightSupped) {
                // structure will tumble around rotation point
                supportsOfDirectSupports.remove(blocks.get(node));
                Point tumblePointLeft = new Point(0,0);
                Point tumblePointRight = new Point(100000, 0);
                for (ABObject ab : supportsOfDirectSupports) {
                    if (leftSupped) {
                        if (ab.getX() <= centerMass.getX() && ab.getX() > tumblePointLeft.getX()) {
                            tumblePoint = new Point(
                                    (int) (ab.getX() + ab.getWidth()),
                                    (int) (ab.getY()));
                            return 2;
                        }
                    }
                    if (rightSupped) {
                        if (ab.getX() >= centerMass.getX() && ab.getX() <= tumblePointRight.getX()) {
                            tumblePoint = new Point(
                                    (int) (ab.getX()),
                                    (int) (ab.getY()));
                            return 3;
                        }
                    }
                }
            }

        }

        // TODO: cases: x supports y, just assume it doesn't fall?
        // yeah idk atm

        tumblocks.addAll(directSupport);

        tumblePoint = null;
        return 0;
    }

    //
    // Note: int[x][y] denotes that node x propagates force to node y
    /*
    FORCEGRAPH KEY:
    4: BLOCKS THAT SUPPORT EACH OTHER - IN DIRECT CONTACT
    1: BLOCKS THAT ARE BESIDE EACH OTHER - A WILL GET PUSHED INTO B
    2: BLOCKS THAT FALL ONTO EACH OTHER - A TOPPLES ONTO B
    3: BLOCKS THAT FALL ONTO EACH OTHER BASED ON THEIR STRUCTURE - POSSIBLE DOWN FORCE FROM THIS
     */
    public static double[][] generateRightForceGraph(int[][] supportGraph, List<GroundItem> groundProps) {
        double[][] rightForceGraph = new double[supportGraph[0].length][supportGraph[0].length];
        downForceGraph = new int[supportGraph[0].length][supportGraph[0].length];

        //finds adjacent blocks that can be pushed into another
        for (int i = 0; i < supportGraph[0].length-1; i++) {

            ABObject currentBlock = blocks.get(i);
            currentBlock.area = (int)(currentBlock.getHeight() * currentBlock.getWidth());

            try {
                if (blocks.get(i).type == ABType.Pig) continue;
            }
            catch (Exception e) {}

            for (int j = 0; j < supportGraph[0].length-1; j++) {

                // make arrows bidirectional on support graph
                if (supportGraph[i][j] == 1) {
                    rightForceGraph[i][j] = 4;
                    rightForceGraph[j][i] = 4;
                }

                if (i == j) continue;

                ABObject leftBlock = blocks.get(i);
                ABObject rightBlock = blocks.get(j);
                if (leftBlock == null || rightBlock == null) continue;

                // Blocks touching on the right
                if (leftBlock.x <= rightBlock.x
                        // threshold is 3 pixels atm
                        && rightBlock.x <= leftBlock.x + 3
                        && overlap(leftBlock.y, (int) leftBlock.getHeight(), rightBlock.y, (int) rightBlock.getHeight())) {
                    rightForceGraph[i][j] = 1;
                    continue;
                }

                //blocks that can fall over onto each other, but only if they're not already touching
                //this method might detect too many blocks to fall onto because we're considering all blocks within a full falling arc
                if (rightFallOnto(leftBlock, rightBlock)
                        && rightForceGraph[i][j] == 0) {
                    // Can tip over and fall onto the next object ignoring obstructions
                    // TODO: Find proper value for this
                    downForceGraph[i][j] = 1;
                    rightForceGraph[i][j] = 0.7;
                }
            }

            // TODO: SUBSTRUCTUREs, ENTIRE STRUCTURES MIGHT NOT WORK BECAUSE IT FUCKED

            //finds structures that can be pushed onto each other
            boolean noRightBlocks = true;
            for (int j = 0; j < supportGraph[i].length-1; j++) {
                if (rightForceGraph[i][j] > 0) noRightBlocks = false;
            }

            // what could happen if entire support structure falls
            if (noRightBlocks) {
                List<Integer> suppStructure = findSupportedBlocks(supportGraph, i);
                List<ABObject> suppsStruct = new ArrayList<>();
                for (Integer su : suppStructure) {
                    suppsStruct.add(blocks.get(su));
                }
                //Point centerMass = centerOfMass(suppsStruct);

                // TODO: FIND BETTER SOLUTION THAN JUST DISALLOWING SUPPORTING BLOCKS FURTHER APART
                ABObject lowestRightestBlock = null;
                for (ABObject ab : suppsStruct) {
                    if (lowestRightestBlock == null ||
                            (ab.x >= lowestRightestBlock.x
                             && ab.y >= lowestRightestBlock.y
                             && (ab.getX() + ab.getWidth()) < (blocks.get(i).getX() + blocks.get(i).getWidth()) + 33)) {
                        lowestRightestBlock = ab;
                    }
                }
                if (lowestRightestBlock == null) {
                    continue;
                }

                Rectangle totalStructure = new Rectangle(
                        blocks.get(i).x,
                        blocks.get(i).y,
                        blocks.get(i).x - lowestRightestBlock.x,
                        blocks.get(i).y - lowestRightestBlock.y
                );

                ABObject structureObject = new ABObject(totalStructure, ABType.Wood, 1000);
                ABObject modifiedStructureObject = new ABObject((new Rectangle(
                        structureObject.x,
                        structureObject.y,
                        (int)(structureObject.getHeight() * 1.25),
                        (int)structureObject.getWidth())
                ), ABType.Wood, 1001);

                for (int j = 0; j < supportGraph[0].length - 1; j++) {
                    if (!(suppStructure.contains(j))) {
                        if (rightFallOnto(modifiedStructureObject, blocks.get(j))) {
                            // We calculate the height difference and consider the potential gain in force from gravity
                            // Not necessarily linear increase though
                            // Probably bullshit though
                            double fallAmount = Math.abs(blocks.get(i).getY() - blocks.get(j).getY());
                            double numberHeights = fallAmount / blocks.get(i).getHeight();
                            double angleDiff = Math.tan(fallAmount / (Math.abs(blocks.get(i).getX() - blocks.get(j).getX())));
                            rightForceGraph[i][j] = numberHeights * angleDiff;
                            downForceGraph[i][j] = 1;
                        }
                    }
                }
            }

            // TODO: take into account the ground angles
            // TODO: blocks can be 'thrown' into others (ie level 2)

            if (currentBlock.shape == ABShape.Circle) {
//                System.out.println(currentBlock.id + " THIS BLOCK IS ROUND");
                ABObject rightclosest = null;
                for (Map.Entry<Integer, ABObject> ab : blocks.entrySet()) {
                    if (rightclosest == null ||
                            (rightclosest.getX() >= ab.getValue().getX()
                                    && ab.getValue().getY() <= currentBlock.getX() + 3))  {
                        rightclosest = ab.getValue();
                    }
                }
                if (!(rightclosest == null)) {
                    // can roll into that rightclosest
                    if (rightForceGraph[i][rightclosest.id] == 0) {
                        // TODO: GET PROPER ROLLY VALUE HERE
                        rightForceGraph[i][rightclosest.id] = 1;
                    }

                    // can roll down hills
                    for (GroundItem g : groundProps) {
                        // this block maaay roll down hill
                        if (currentBlock.getCenter().getX() >= g.s - 15 &&
                                g.a < 0) {
                            rightForceGraph[i][rightclosest.id] = 5;
                        }
                    }
                }
            }

            // blocks can be thrown onto others
            if (currentBlock.area < 500
                    && currentBlock.type != ABType.Stone
                    || (currentBlock.type == ABType.Stone
                        && currentBlock.area < 200)) {
                List<ABObject> rightOf = aboRightOf(currentBlock, blocks, currentBlock.area);
                for (ABObject ab : rightOf) {
                    if (rightForceGraph[i][ab.id] != 0) {
                        // TODO: Find proper values here
                        System.out.println("Block: " + i + " can be thrown onto " + ab.id );
                        rightForceGraph[i][ab.id] = 0.8;
                    }
                }
            }

            double ratio = currentBlock.getHeight() / currentBlock.getWidth();
            // These blocks can sorta 'roll' along the ground
            if (ratio <= 1.4 && ratio >= 0.6) {

            }


        }


        for (int i = 0; i < supportGraph[0].length; i++) {
            double numberOfTouchingNodes = 0;
            for (int j = 0; j < supportGraph[0].length; j++) {
                if (rightForceGraph[i][j] == 4 || rightForceGraph[i][j] == 1)
                    numberOfTouchingNodes += 1;
            }
            if (!(numberOfTouchingNodes == 0)) {
                for (int j = 0; j < supportGraph[0].length; j++)
                    if (rightForceGraph[i][j] == 4 || rightForceGraph[i][j] == 1)
                        rightForceGraph[i][j] = 1d / numberOfTouchingNodes;
            }
        }

        return rightForceGraph;
    }

    // blocks that fall onto the ground may behave differencely. ie round balls roll

    public static int[][] generateLeftForceGraph(int[][] supportGraph, Vision vis) {
        List<GroundItem> g = groundProperties(vis);
        double[][] leftForceGraph = generateRightForceGraph(supportGraph, g);

        for (int i = 0; i < supportGraph[0].length; i++) {
            for (int j = 0; j < supportGraph[0].length; j++) {
                if (supportGraph[i][j] == 2) {
                    supportGraph[i][j] = 0;
                }
                ABObject a = blocks.get(i);
                ABObject b = blocks.get(j);

                if (leftFallOnto(a, b) &&
                        supportGraph[i][j] == 0) {
                    supportGraph[i][j] = 2;
                }
            }
        }
        return new int[0][0];
    }

    //Checks if a level is solvable
    public static boolean solvable(Vision vision) {
        List<ABObject> recurseTumbleBlocks = new ArrayList<>();

        // TESTING
        ArrayList<ABObject> objs = new ArrayList<>();
        List<ABObject> k = vision.findBirdsRealShape();
        if (k == null) {
            return true;
        }
        Rectangle s = vision.findSlingshotMBR();
        if (s == null) {
            return true;
        }
        objs.addAll(k);
        objs.addAll(vision.findPigsRealShape());
//        objs.addAll(vision.findTNTs());
        objs.addAll(vision.findBlocksRealShape());

        //Reassign id'd because realshap apparently doesn't do sequential id's properly
        int id = 0;
        for (ABObject ab : objs) {
            ab.id = id;
            id++;
        }

//        for (ABObject ab : objs) {
//            System.out.println(ab.type + "," + ab.id + "," + ab.getX());
//        }

        // analyses stability and structure of level
        ObjectCollector oc = new ObjectCollector(objs);
        StructureAnalyse sa = new StructureAnalyse(oc.objs, oc.refLength);

//         prints block, type and support
        for (int i = 0; i < sa.SupportGraph.length; i++) {
            for (int j = 0; j < sa.SupportGraph.length; j++) {
                if (sa.SupportGraph[i][j] == 1) {
                    System.out.println(j + " is a " + oc.objs.get(j).abo.type + ", supported " + i);
                }
            }
        }

        if (sa.pigs.size() == 0) return true;

        // adds every object into a global hashmap matching it with its id
        HashMap<Integer, ABObject> bl = new HashMap<>();
        List<ABObject> objts = new ArrayList<>();
        for (ABObject ab : objs) {
            bl.put(ab.id, ab);
            objts.add(ab);
        }
        blocks = bl;

        // TODO: ALL THE Y COORDINATES ARE REVERSED IN CALCULATIONS BECAUSE COMPUTER VALUES FUCK

        System.out.println("The pig nodes are: ");
        for (Integer p : sa.pigs) {
            ABObject pig = blocks.get(p);
            System.out.println(p + ":" + pig.getX() + "," + pig.getY() + "," + pig.area);
        }

        List<GroundItem> grnd = groundProperties(vision);

//        System.out.println("Number of ground regions: " + grnd.size());
//        for (groundItem g1 : grnd){
//            System.out.println(g1.toString());
//        }

        int[][] supgraph = sa.SupportGraph;
        supgraph = fillStabilityGraphOut(supgraph);
        supportGraph = supgraph;
        sBlocks = sharesSupportBlocks(sa.pigs, supgraph);

        Rectangle sling = vision.findSlingshotMBR();
        ABType currentBirdType = ABType.RedBird;
        for (ABObject ab : objs) {
            if (sling.contains(ab)){
                currentBirdType = ab.getType();
            }
        }
        if (!(currentBirdType == ABType.RedBird
                || currentBirdType == ABType.BlueBird
                || currentBirdType == ABType.YellowBird)) {
            currentBirdType = ABType.RedBird;
        }

        rBlocks = reachableBlocks(objts, vision, currentBirdType);
        System.out.println("Reachable blocks from sling: " + rBlocks);
        rForceGraph = generateRightForceGraph(supportGraph, grnd);

//        System.out.println(Arrays.deepToString(rForceGraph).replace("], ","]\n"));
        boolean levelSolva = true;
        try {
            levelSolva = doAllForcesAndStuff(sa.pigs);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return levelSolva;
    }

    public static ABObject blockWithinRightDistance(int node, double distance) {
        ABObject currentObject = blocks.get(node);
        ABObject rightClosestObject = null;
        for (Map.Entry<Integer, ABObject> abE : blocks.entrySet()) {
            if (abE.getValue().getX() >= currentObject.getX() + distance
                    && within(currentObject.getY() + 50, currentObject.getY() - 50, abE.getValue().getY())){
                if (rightClosestObject == null) {
                    rightClosestObject = abE.getValue();
                }
                else {
                    if (rightClosestObject.getX() >= abE.getValue().getX()) {
                        rightClosestObject = abE.getValue();
                    }
                }
            }
        }
        return rightClosestObject;
    }

    public static boolean doAllForcesAndStuff(List<Integer> pignodes) {
//        System.out.println(Arrays.deepToString(rForceGraph).replace("], ","]\n"));
//        try {
//            System.out.println("Okay checking forces now then");
            for (List<Integer> iList : rBlocks) {
                System.out.println("Hitting initial node: " + iList.get(0));
                ABObject hit = blocks.get(iList.get(0));
                System.out.println(hit.type + "," + hit.getX() + "," + hit.getY() + "," + (hit.getWidth() * hit.getHeight()));
                boolean levelSuccess = false;
                HashMap<Integer, Force> objectsNForces = new HashMap<>();

                // If our bird pierces through many blocks
                if (iList.size() > 1) {
                    System.out.println("block is destroyed, force doesn't propagate");

                    HashMap<Integer, Force> totalForces = new HashMap<>();
                    Integer lastVal = iList.lastIndexOf(iList);
                    for (Integer i : iList) {
                        if (!i.equals(lastVal)) {
                            HashMap<Integer, Force> tumbleForces = destroyBlockStructureEffect(totalForces, i);
                            for (Map.Entry<Integer, Force> ent : tumbleForces.entrySet()) {
                                totalForces.put(ent.getKey(), ent.getValue());
                            }
                        }
                        else {
                            // TODO: Find momentum value properly
                            double mom = 100 / iList.size();
                            for (Map.Entry<Integer, Force> rForce : propagateRightFoce(rForceGraph, i, mom).entrySet()) {
                                if (totalForces.containsKey(rForce.getKey())) {
                                    Force f = totalForces.get(rForce.getKey());
                                    f.ForceSum(rForce.getValue());
                                    totalForces.put(rForce.getKey(), f);
                                }
                                else {
                                    totalForces.put(rForce.getKey(), rForce.getValue());
                                }
                            }
                        }
                    }

                    if (recurseTumbleBlocks.size() > 0) {
                        Set<ABObject> recurseBlocks = new HashSet<>();
                        do {
                            recurseBlocks.addAll(recurseTumbleBlocks);
                            if (recurseBlocks.size() > 0) {
                                for (ABObject ab : recurseBlocks)
                                    totalForces = destroyBlockStructureEffect(totalForces, ab.id);
                            }
                            System.out.println(recurseBlocks.size() + "," + recurseTumbleBlocks.size());
                        } while (recurseBlocks.size() != recurseTumbleBlocks.size());
                    }
                }
                // Birds hits something and does't destroy it
                else if (iList.size() == 1) {
                    int momentum = 100;
                    if (blocks.get(iList.get(0)).type == ABType.Ice) {
                        momentum = 100 - (blocks.get(iList.get(0)).area);
                        if (momentum < 10) momentum = 10;
                    }
                    objectsNForces = propagateRightFoce(rForceGraph, iList.get(0), momentum);

                    objectsNForces = propagateDownForce(objectsNForces, blocks, downForceGraph);
                } else {
                    System.out.println("Shits fucked");
                    throw new NullPointerException();
                }

                // Convert stuff from a hashmap to a list containing the affected blocks
                Set<Integer> blocksAffected = new HashSet<>();
                for (Map.Entry<Integer, Force> ety : objectsNForces.entrySet()) {
                    blocksAffected.add(ety.getKey());
                }

                List<Integer> forcesNegligible = new ArrayList<>();
                System.out.println("If we hit block: " + iList + ":");
                for (Integer i : iList) {
                    System.out.println(blocks.get(i).type + "," + blocks.get(i).area);
                }
                for (Map.Entry<Integer, Force> obf : objectsNForces.entrySet()) {
                    Force f = obf.getValue();
                    if (f != null) {
                        if (obf.getValue().direction[1] < 0.1 && obf.getValue().direction[2] < 0.1) {
                            forcesNegligible.add(obf.getKey());
                        }
                        else {
                            System.out.println(blocks.get(obf.getKey()).type.toString() + "," + obf.getKey() + ":" + obf.getValue().toString());
                        }
                    }
                }

                for (Integer i : forcesNegligible) {
                    objectsNForces.remove(i);
                }

                int numPigsKilled = 0;
                for (Integer pig : pignodes) {
                    if (blocksAffected.contains(pig) && objectsNForces.get(pig) != null) {
                        if (objectsNForces.get(pig).direction[2] > 0) {
                            numPigsKilled++;
                            System.out.println("Pig number: " + pig + " killed via falling or crushing");
                        } else if (objectsNForces.get(pig).direction[1] >= 25) {
                            numPigsKilled++;
                            System.out.println("Pig number: " + pig + " killed via brute force");
                        }
                        //check if pig can die using some other method
                        else if (objectsNForces.get(pig).direction[1] > 0) {
                            ABObject rightObject = blockWithinRightDistance(pig,
                                    objectsNForces.get(pig).direction[1] + blocks.get(pig).getWidth());
                            if (!(rightObject == null)) {
                                numPigsKilled++;
                            }
                            System.out.println("Pig number: " + pig + " maybe killed via pushed somewhere");
                        }
                    }
                }

                System.out.println("");
                if (numPigsKilled >= pignodes.size()) levelSuccess = true;

                if (levelSuccess) {
                    nodeToHit = iList.get(0);
                    return true;
                }
            }
            return false;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
    }


}
