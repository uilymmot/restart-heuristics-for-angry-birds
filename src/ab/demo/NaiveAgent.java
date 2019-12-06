/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.*;

import ab.demo.Solvability.Solvability;
import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import ab.demo.other.Restarter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class NaiveAgent implements Runnable {

	private Restarter res = new Restarter();
	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 1;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	static Boolean toldToRestart = null;
	int birdsUsed = 0;
	private BufferedWriter writer;
	private BufferedWriter writer1;
	long startLevelTime = 0;

	// a standalone implementation of the Naive Agent
	public NaiveAgent() {

		try {
			writer = new BufferedWriter(new FileWriter("Outputs.txt", true));
			writer1 = new BufferedWriter(new FileWriter("RestartOutputs.txt", true));
		}
		catch (Exception e) { }

		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}


	// run the client
	public void run() {
		aRobot.loadLevel(currentLevel);
		startLevelTime = 0;
		long endLevelTime = 0;

		boolean startLevel = true;
		while (true) {
			//aRobot.restartLevel()
//			GameState state = solve();

			aRobot.fullyZoomOut();


			long solvabilityTime = System.currentTimeMillis();
			BufferedImage b = aRobot.doScreenShot();
			Vision v = new Vision(b);
			try {
				Solvability s = new Solvability();
				System.out.println("Level is solvable" + Solvability.solvable(v));
				System.out.println("Reachable blocks" + Solvability.rBlocks);

			}
			catch (Exception e) {
				try {
					System.out.println("Level is solvable" + Solvability.solvable(v));
					System.out.println("Reachable blocks" + Solvability.rBlocks);
				}
				catch (Exception f) {
					System.out.println("Double failure of solvability module");
				}
			}

			long solvabilityTimeEnd = System.currentTimeMillis();
			System.out.println("SOLVABILITY MODULE TOOK: " + (((double)solvabilityTimeEnd - (double)solvabilityTime) / 1000f) + "s");

			if (startLevel) {
				startLevelTime = System.currentTimeMillis();
				startLevel = false;
			}

			GameState state = solve();
//			GameState state = GameState.WON;
			
			System.out.println("CURRENT LEVEL IS: " + currentLevel);

//			aRobot.loadLevel(++currentLevel);

			if (state == GameState.WON) {
				endLevelTime = System.currentTimeMillis();
				String r = "NA";
				if (toldToRestart == null) {
					r = "NA";
				}
				else {
					r = toldToRestart ? "FP" : "TN";
				}
				try {
					writer.write(currentLevel + "," + (endLevelTime - startLevelTime) + "," + aRobot.getScore() + "," + r);
					writer.newLine();
					writer.flush();
				}
				catch (Exception e) {}

				if (currentLevel >= 21) {
					currentLevel = 0;
					aRobot.loadLevel(++currentLevel);
				}
				else {
					aRobot.loadLevel(++currentLevel);
				}
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
				startLevel = true;
				res.reset();
				toldToRestart = null;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				String r = "NA";
				if (toldToRestart == null) {
					r = "NA";
				}
				else {
					r = toldToRestart ? "TP" : "FN";
				}
				long currentTime = System.currentTimeMillis() - startLevelTime;
//				if (currentTime > 600000) {
//					if (currentTime >= 21) {
//						currentLevel = 0;
//					}
//					aRobot.loadLevel(++currentLevel);
//					startLevel = true;
//				}

				try {
					writer.write(currentLevel + "," + currentTime + "," + "-1" + "," + r);
					writer.newLine();
					writer.flush();
				}
				catch (Exception e) {}

				aRobot.restartLevel();

				res.reset();
				toldToRestart = null;
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}
		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}

//		if (res.checkRestart(vision, writer1, aRobot, currentLevel)) {
//			long currentTime = System.currentTimeMillis();
//			try {
//				writer.write(currentLevel + "," + (currentTime - startLevelTime) + "," + "-2");
//				writer.newLine();
//				writer.flush();
//			}
//			catch (Exception e) {}
//			aRobot.restartLevel();
//			res.reset();
//		}

//		try {
//			Boolean solvable = Restarter.checkRestartTemp(vision, aRobot, writer1);
//			if (toldToRestart == null && solvable != null) {
//				toldToRestart = !solvable;
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}

		Boolean solvable = Restarter.checkRestartTemp(vision, aRobot, writer1);
		if (solvable != null && solvable == false){
			aRobot.restartLevel();
		}

        // get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();

		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point releasePoint = null;
				Shot shot = new Shot();
				birdsUsed++;
				int dx,dy;
				{
					// random pick up a pig
					ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));

					Point _tpt = pig.getCenter();// if the target is very close to before, randomly choose a
					// point near it
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = randomGenerator.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// estimate the trajectory
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);

					// do a high shot when entering a level to find an accurate velocity
					if (firstShot && pts.size() > 1)
					{
						releasePoint = pts.get(1);
					}
					else if (pts.size() == 1)
						releasePoint = pts.get(0);
					else if (pts.size() == 2)
					{
						// randomly choose between the trajectories, with a 1 in
						// 6 chance of choosing the high one
						if (randomGenerator.nextInt(6) == 0)
							releasePoint = pts.get(1);
						else
							releasePoint = pts.get(0);
					}
					else
						if(pts.isEmpty())
						{
							System.out.println("No release point found for the target");
							System.out.println("Try a shot with 45 degree");
							releasePoint = tp.findReleasePoint(sling, Math.PI/4);
						}

					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);


					//Calculate the tapping time according the bird type
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						int tapInterval = 0;
						switch (aRobot.getBirdTypeOnSling())
						{

						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlackBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlueBird:
							tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
						default:
							tapInterval =  60;
						}

						int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}

	public static void main(String args[]) {

		NaiveAgent na = new NaiveAgent();
		if (args.length > 0)
			na.currentLevel = Integer.parseInt(args[0]);
		na.run();

	}
}
