package ab.demo.other;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;

import ab.demo.Solvability.Solvability;
import ab.vision.ABObject;
import ab.vision.Vision;
import ab.vision.ABType;

import javax.imageio.ImageIO;

public class Restarter {
    static int previousBirds = 0;
    static boolean disallowMiss = false;
    static Integer restartScore = 3000;
    static ABType currentBirdOnSling = null;
    static ABType previousBirdOnSling = null;
    static List<ABObject> oldBlocks = new ArrayList<>();
    static List<ABObject> currentBlocks = new ArrayList<>();

    static List<Integer> initialLevelStatistics = null;
    static List<Integer> levelStatisticsCurrent = null;
    static List<Integer> levelStatisticsPrevious = null;
    static int name = 0;
    /*
    0: Number of birds
    1: Number of pigs
    2: Score
    3: Number of blocks
    4: No. Wood blocks
    5: No. Ice Blocks
    6: No. Stone Blocks
     */

    public static boolean checkRestart(Vision vis, BufferedWriter writ, ActionRobot ourRobot, int currentLevel) {
        if (vis == null || writ == null || ourRobot == null) return false;

        try {

            extractLevelStatistics(vis, ourRobot);

            if (levelStatisticsPrevious == null) return false;

            if (levelStatisticsCurrent.get(2) > levelStatisticsPrevious.get(2)) disallowMiss = true;

            String restartReason = "";
            int currentScore = levelStatisticsCurrent.get(2);
            int oldScore = levelStatisticsPrevious.get(2);

            double restartScore = 0d;
            int scoreThresholdForRestart = 0;
            // If screenshot doesn't represent a new level state / bird being fired
            if (levelStatisticsCurrent == levelStatisticsPrevious
                    || levelStatisticsCurrent.get(1) == 0
                    || currentScore == -1
                    || oldScore > currentScore
                    || levelStatisticsCurrent.get(0) == 0
                    || (currentScore == oldScore
                    && currentScore == 0)) {
                //reload previous state
                levelStatisticsCurrent = levelStatisticsPrevious;

                try {
                    String b = (previousBirdOnSling == null) ? "" : previousBirdOnSling.toString();

                    // LEVEL, OLDSCORE, CURRENTSCORE, PREVIOUSBIRDS, CURRENTBIRDS, SOLVABILITY, CURRENTPIGS, RESTARTSCORE
                    writ.write(currentLevel
                            + "," + oldScore
                            + "," + currentScore
                            + "," + previousBirds
                            + "," + levelStatisticsCurrent.get(0)
                            + "," + ""
                            + "," + levelStatisticsCurrent.get(1)
                            + "," + b
                            + "," + scoreThresholdForRestart
                            + "," + restartScore
                            + "," + "N/A");
                    writ.newLine();
                    writ.flush();
                } catch (Exception j) {
                }

                return false;
            }
            calculateBirbs(vis);

            if (previousBirdOnSling != null) {
                if (previousBirdOnSling == ABType.YellowBird) {
                    scoreThresholdForRestart = 7000;
                } else if (previousBirdOnSling == ABType.BlueBird) {
                    scoreThresholdForRestart = 7000;
                } else if (previousBirdOnSling == ABType.RedBird) {
                    scoreThresholdForRestart = 5000;
                } else if (previousBirdOnSling == ABType.BlackBird) {
                    scoreThresholdForRestart = 10000;
                } else {
                    scoreThresholdForRestart = 5000;
                }

                double iceDelta = (double) levelStatisticsCurrent.get(5) / (double) levelStatisticsPrevious.get(5);
                double woodDelta = (double) levelStatisticsCurrent.get(4) / (double) levelStatisticsPrevious.get(4);
                double stoneDelta = (double) levelStatisticsCurrent.get(6) / (double) levelStatisticsPrevious.get(6);

                // IF SPECIFIC BIRD TYPE DESTROYED CORRESPONDING BLOCK TYPE
                if (previousBirdOnSling == ABType.BlueBird) {
                    if (iceDelta == 0) {
                        restartScore += 0.15;
                    }
                } else if (previousBirdOnSling == ABType.YellowBird) {
                    if (woodDelta == 0) {
                        restartScore += 0.15;
                    }
                } else if (previousBirdOnSling == ABType.BlackBird) {
                    if (stoneDelta == 0) {
                        restartScore += 0.15;
                    }
                }
            }

            //Solvability heuristic
            boolean levelSolvable = true;
            if (levelStatisticsCurrent.get(0) == 1) {
                levelSolvable = Solvability.solvable(vis);
                if (levelStatisticsCurrent.get(0) == 1 && !levelSolvable) {
                    restartScore += 0.49;
                }
            }

//            } else if (levelStatisticsCurrent.get(0) == 2 && !levelSolvable) {
//                restartScore += 0.3;
//            }

            if (currentScore != 0
                    && currentScore <= oldScore + scoreThresholdForRestart) {
                Double k = (double) currentScore;
                k -= oldScore;
                k = ((k) * ((0.6) / (scoreThresholdForRestart)));
                k = 0.6f - k;
                restartScore += k;
                restartReason += "Lack of score";

            } else if (currentScore == oldScore && currentScore != 0
                    && levelStatisticsCurrent.get(0) < levelStatisticsPrevious.get(0)) {
                restartScore += 0.5f;
                restartReason += "No score increase";
            }
            // NUMBRE OF PIGS HEURISTIC
            if (levelStatisticsCurrent.get(0) * 6 < (levelStatisticsCurrent.get(1))
                    && levelStatisticsCurrent.get(0) != 0
                    && levelStatisticsCurrent.get(1) != 0) {
                // CHANGE TO RATIO OF BIRDS:PIGS
                restartScore += 0.33f;
                restartReason += "Pigs more than 6 times birds";
            }


            List<ABObject> piggies = vis.findPigsMBR();
            // RULE : IF ONLY ONE BIRD LEFT AND >2 PIGS ARE FAR APART PROBABLY FINE TUNE THIS RULE MORE
            if (levelStatisticsCurrent.get(0) == 1 & piggies.size() > 1) {
                for (int pig1 = 0; pig1 < piggies.size(); pig1++) {
                    for (int pig2 = pig1 + 1; pig2 < piggies.size(); pig2++) {
                        double dis = distanceABObject(piggies.get(pig1), piggies.get(pig2));
                        if (dis > 200) {
                            restartScore += 0.6f;
                            restartReason += "PIgs too far apart";
                        }
                    }
                }
            }
            // CHECK IF SOME PERCENTAGE OF THE BLOCKS ARE THE SAME (POSITION?) AS BEFORE
            // TAKES COME PROPORTION OF ALL BLOCKS
            // TODO: IMPROVE AND UPDATE SIMILARITY MEASURE
            int sameBlock = 0;
            if (oldBlocks.size() != 0) {
                for (ABObject O : currentBlocks) {
                    for (ABObject N : oldBlocks) {
                        if (O.intersects(N) && N.intersects(O) &&
                                (O.contains(N) || N.contains(O) || N == O || N.equals(O))) {
                            sameBlock++;
                            break;
                        }
                    }
                }
            }
            if (((double) sameBlock / (double) oldBlocks.size()) > 0.96d) {
                restartScore += 0.25;
            } else if (((double) sameBlock / (double) oldBlocks.size()) > 0.92d) {
                restartReason += "Level too similar to previous";
                restartScore += 0.2;
            } else if (((double) sameBlock / (double) oldBlocks.size()) > 0.88d) {
                restartScore += 0.15;
            }

            boolean restart = (restartScore > 0.49);

            try {
                String k = (restart) ? "R" : "NR";
                String b = (previousBirdOnSling == null) ? "" : previousBirdOnSling.toString();

                // LEVEL, OLDSCORE, CURRENTSCORE, PREVIOUSBIRDS, CURRENTBIRDS, SOLVABILITY, CURRENTPIGS, RESTARTSCORE
                writ.write(currentLevel
                        + "," + oldScore
                        + "," + currentScore
                        + "," + previousBirds
                        + "," + levelStatisticsCurrent.get(0)
                        + "," + levelSolvable
                        + "," + piggies.size()
                        + "," + b
                        + "," + scoreThresholdForRestart
                        + "," + restartScore
                        + "," + k);
                writ.newLine();
                writ.flush();
            } catch (Exception j) {
            }


            return restart;
        }
        catch (Exception e) {
            try {
                writ.write(e.toString());
                writ.newLine();
                writ.flush();
            }
            catch (Exception f) {}
            e.printStackTrace();
        }
        return false;
    }

//    public static Boolean checkRestartTemp(Vision vis, ActionRobot arobot, BufferedWriter writ) {
//        extractLevelStatistics(vis, arobot);
//        //Solvability heuristic
//
//        Boolean thresholdFailed = null;
//        if (levelStatisticsPrevious == null) return false;
//
//        int scoreThresholdForRestart = 3000;
//        if (previousBirdOnSling != null) {
//            if (previousBirdOnSling == ABType.YellowBird) {
//                scoreThresholdForRestart = 2500;
//            } else if (previousBirdOnSling == ABType.BlueBird) {
//                scoreThresholdForRestart = 2500;
//            } else if (previousBirdOnSling == ABType.RedBird) {
//                scoreThresholdForRestart = 1500;
//            } else if (previousBirdOnSling == ABType.BlackBird) {
//                scoreThresholdForRestart = 4000;
//            } else {
//                scoreThresholdForRestart = 1500;
//            }
//        }
//
//        int currentScore = levelStatisticsCurrent.get(2);
//        int oldScore = levelStatisticsPrevious.get(2);
//        if (currentScore != 0
//                && currentScore <= oldScore + scoreThresholdForRestart) {
//            thresholdFailed = true;
//        }
//
//        return thresholdFailed;
//    }

//    public static Boolean checkRestartTemp(Vision vis, ActionRobot arobot, BufferedWriter writ) {
//        extractLevelStatistics(vis, arobot);
//        //Solvability heuristic
//
//        Boolean levelSolvable = null;
//
//        if (levelStatisticsCurrent.get(0) == 1) {
//            levelSolvable = Solvability.solvable(vis);
//        }
//        if (levelSolvable != null && !levelSolvable) {
//            BufferedImage im = ActionRobot.doScreenShot();
//            try {
//                File outputfile = new File("SC/" + name + ".jpg");
//                name++;
//                ImageIO.write(im, "jpg", outputfile);
//            }
//            catch (Exception e) {}
//        }
//
//        return levelSolvable;
//    }

        public static Boolean checkRestartTemp(Vision vis, ActionRobot arobot, BufferedWriter writ) {
        extractLevelStatistics(vis, arobot);
        //Solvability heuristic

        Boolean levelSolvable = null;

        if (levelStatisticsCurrent.get(0) == 1) {
            levelSolvable = Solvability.solvable(vis);
        }
        if (levelSolvable != null && !levelSolvable) {
            BufferedImage im = ActionRobot.doScreenShot();
            try {
                File outputfile = new File("SC1/" + name + ".jpg");
                name++;
                ImageIO.write(im, "jpg", outputfile);
            }
            catch (Exception e) {}
        }

        return levelSolvable;
    }

    public static void extractLevelStatistics(Vision vis, ActionRobot ourRobot) {
        List<Integer> tempLevelStatistics = new ArrayList<>();
        List<ABObject> blocks = vis.findBlocksMBR();
        List<ABObject> birds = vis.findBirdsMBR();
        List<ABObject> pigs = vis.findPigsMBR();
        List<ABObject> tnt = vis.findTNTs();

        List<ABObject> totalObjekts = new ArrayList<>();
        totalObjekts.addAll(blocks);
        totalObjekts.addAll(pigs);
        totalObjekts.addAll(tnt);
        int noIce = 0;
        int noStone = 0;
        int noWood = 0;
        for (ABObject ab : blocks) {
            if (ab.type == ABType.Wood) noWood++;
            else if (ab.type == ABType.Ice) noIce++;
            else if (ab.type == ABType.Stone) noStone++;
        }
        int score = ourRobot.getCurrScore();
        tempLevelStatistics.add(birds.size());
        tempLevelStatistics.add(pigs.size());
        tempLevelStatistics.add(score);
        tempLevelStatistics.add(blocks.size());
        tempLevelStatistics.add(noWood);
        tempLevelStatistics.add(noIce);
        tempLevelStatistics.add(noStone);

        if (levelStatisticsCurrent == null) {
            levelStatisticsCurrent = tempLevelStatistics;
            currentBlocks = totalObjekts;
        }
        else {
            levelStatisticsPrevious = levelStatisticsCurrent;
            levelStatisticsCurrent = tempLevelStatistics;
            oldBlocks = currentBlocks;
            currentBlocks = totalObjekts;
        }

        if (initialLevelStatistics == null) {
            initialLevelStatistics = tempLevelStatistics;
        }
    }

    public static void calculateBirbs(Vision vis) {
        List<ABObject> birds = vis.findBirdsMBR();
        Rectangle slin = vis.findSlingshotMBR();
        if (currentBirdOnSling != null) {
            previousBirdOnSling = currentBirdOnSling;
        }

        for (ABObject ab : birds) {
            if (slin.contains(ab)) {
                currentBirdOnSling = ab.type;
                break;
            }
        }
        if (currentBirdOnSling == null)
            currentBirdOnSling = ABType.RedBird;
    }

    public static double distanceABObject(ABObject a, ABObject b) {
        double ax = a.getX() + (a.getHeight() / 2);
        double ay = a.getX() + (a.getWidth() / 2);
        double bx = a.getX() + (a.getHeight() / 2);
        double by = a.getX() + (a.getWidth() / 2);
        return Math.sqrt(((ax + bx)*2) + ((ay + by) * 2));
    }

    public static void reset() {
        previousBirds = 0;
        disallowMiss = false;
        restartScore = 3000;
        currentBirdOnSling = null;
        previousBirdOnSling = null;
        oldBlocks = new ArrayList<>();
        currentBlocks = new ArrayList<>();

        initialLevelStatistics = null;
        levelStatisticsCurrent = null;
        levelStatisticsPrevious = null;
    }

}
