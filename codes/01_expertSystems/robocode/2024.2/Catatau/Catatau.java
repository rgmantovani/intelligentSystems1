package Catatau;

import robocode.*;
import robocode.util.Utils;
import java.lang.*; // for Double and Integer objects
import java.awt.geom.*;
import java.util.ArrayList; // for collection of waves
import java.util.List;

import java.awt.Color;

/**
 * Catatau (patrocinado pelo chefe)
 */
public class Catatau extends AdvancedRobot {
    class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;

        public EnemyWave() {
        }
    }

    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!_fieldRect.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation * 0.05;
        }
        return angle;
    }

    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length, sourceLocation.y + Math.cos(angle) * length);
    }

    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public static double bulletVelocity(double power) {
        return (20.0 - (3.0 * power));
    }

    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0 / velocity);
    }

    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle = Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI / 2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1 * angle);
            } else {
                robot.setTurnRightRadians(angle);
            }
            robot.setAhead(100);
        }
    }

    List<WaveBullet> waves = new ArrayList<WaveBullet>();
    static int[] stats = new int[31]; // 31 is the number of unique GuessFactors we're using
    // this must be odd number so we can get GuessFactor 0 at middle.
    int direction = 1;

    public static int BINS = 47;
    public static double surfStats[] = new double[BINS];
    public Point2D.Double myLocation; // our bot's location
    public Point2D.Double enemyLocation; // enemy bot's location

    public ArrayList enemyWaves;
    public ArrayList surfDirections;
    public ArrayList surfAbsBearings;

    public static double oppEnergy = 100.0;

    /**
     * This is a rectangle that represents an 800x600 battle field,
     * used for a simple, iterative WallSmoothing method (by PEZ).
     * WallSmoothing: the wall stick indicates
     * the amount of space we try to always have on either end of the tank
     * (extending straight out the front or back) before touching a wall.
     */
    public static Rectangle2D.Double _fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    public static double WALL_STICK = 160;

    /**
     * run: Catatau's default behavior
     */
    public void run() {
        // Initialization of the robot should be put here
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        enemyWaves = new ArrayList();
        surfDirections = new ArrayList();
        surfAbsBearings = new ArrayList();

        // Robot main loop
        while (true) {
            setBodyColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
            setGunColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
            setRadarColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
            setBulletColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
            setScanColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
            // Replace the next 4 lines with any behavior you would like
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

    public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50000;
        EnemyWave surfWave = null;

        for (int x = 0; x < enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) enemyWaves.get(x);
            double distance = myLocation.distance(ew.fireLocation) - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    public void updateWaves() {
        for (int x = 0; x < enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave) enemyWaves.get(x);

            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled > myLocation.distance(ew.fireLocation) + 50) { // extra 50 is just to give some extra space to track the onHitByBullet event
                enemyWaves.remove(x);
                x--;
            }
        }
    }

    /**
     * Given the EnemyWave that the bullet was on, and the point where we
     * were hit, calculate the index into our stat array for that factor.
     * @param ew
     * @param targetLocation
     * @return
     */
    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle) / maxEscapeAngle(ew.bulletVelocity) * ew.direction;

        return (int) limit(0, (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2), BINS - 1); // Dont know how this math works, thanks lords of the robocode wiki
    }

    /**
     * Given the EnemyWave that the bullet was on, and the point where we
     * were hit, update our stat array to reflect the danger in that area.
     * @param ew
     * @param targetLocation
     */
    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            // for the spot bin that we were hit on, add 1;
            // for the bins next to it, add 1 / 2;
            // the next one, add 1 / 5; and so on...
            surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    /**
     * onScannedRobot: What to do when you see another robot
     * @param e
     */
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        myLocation = new Point2D.Double(getX(), getY());

        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());

        // Enemy absolute bearing, you can use your one if you already declare it.
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);

        surfDirections.add(0, new Integer((lateralVelocity >= 0) ? 1 : -1));
        surfAbsBearings.add(0, new Double(absBearing + Math.PI));

        double bulletPower = oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09 && surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 1;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = bulletVelocity(bulletPower);
            ew.direction = ((Integer) surfDirections.get(2)).intValue();
            ew.directAngle = ((Double) surfAbsBearings.get(2)).doubleValue();
            ew.fireLocation = (Point2D.Double) enemyLocation.clone(); // last tick

            enemyWaves.add(ew);
        }

        oppEnergy = e.getEnergy();

        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        enemyLocation = project(myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();

        // find our enemy's location:
        double ex = getX() + Math.sin(absBearing) * e.getDistance();
        double ey = getY() + Math.cos(absBearing) * e.getDistance();

        // Let's process the waves now:
        for (int i = 0; i < waves.size(); i++) {
            WaveBullet currentWave = (WaveBullet) waves.get(i);
            if (currentWave.checkHit(ex, ey, getTime())) {
                waves.remove(currentWave);
                i--;
            }
        }

        double power = Math.min(2, Math.max(.1, 500 / e.getDistance()));
        // don't try to figure out the direction they're moving
        // they're not moving, just use the direction we had before
        if (e.getVelocity() != 0) {
            if (Math.sin(e.getHeadingRadians() - absBearing) * e.getVelocity() < 0)
                direction = -1;
            else
                direction = 1;
        }
        int[] currentStats = stats; // This seems silly, but I'm using it to show something else later
        WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, power, direction, getTime(), currentStats);

        int bestindex = 15; // initialize it to be in the middle, guessfactor 0.
        for (int i = 0; i < 31; i++)
            if (currentStats[bestindex] < currentStats[i])
                bestindex = i;

        // this should do the opposite of the math in the WaveBullet:
        double guessfactor = (double) (bestindex - (stats.length - 1) / 2) / ((stats.length - 1) / 2);
        double angleOffset = direction * guessfactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + angleOffset);
        setTurnGunRightRadians(gunAdjust);

        if (getEnergy() > power) {
            if (getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && setFireBullet(power) != null) {
                waves.add(newWave);
            }
        }
    }

    /**
     * onHitByBullet: What to do when you're hit by a bullet
     * @param e
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // If the enemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave) enemyWaves.get(x);

                if (Math.abs(ew.distanceTraveled - myLocation.distance(ew.fireLocation)) < 50 && Math.abs(bulletVelocity(e.getBullet().getPower()) - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                // We can remove this wave now
                enemyWaves.remove(enemyWaves.lastIndexOf(hitWave));
            }
        }
    }

    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double) myLocation.clone();
        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {
            moveAngle = wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation, predictedPosition) + (direction * (Math.PI / 2)), direction) - predictedHeading;
            moveDir = 1;

            if (Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this
            maxTurning = Math.PI / 720d * (40d - 3d * Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading + limit(-maxTurning, moveAngle, maxTurning));

            // if predictedVelocity and moveDir have
            // different signs you want to breack down
            // otherwise you want to accelerate (look at the factor "2")
            predictedVelocity += (predictedVelocity * moveDir < 0 ? 2 * moveDir : moveDir);
            predictedVelocity = limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);

            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) < surfWave.distanceTraveled + (counter * surfWave.bulletVelocity) + surfWave.bulletVelocity) {
                intercepted = true;
            }
        } while (!intercepted && counter < 500);

        return predictedPosition;
    }

    /**
      * Checks the danger level of a given direction based on the predicted position.
      * @param surfWave The wave to check against.
      * @param direction The direction to check (1 for right, -1 for left).
      * @return The danger level for the given direction.
      */
    public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave, predictPosition(surfWave, direction));

        return surfStats[index];
    }

    
    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) {
            return;
        }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = absoluteBearing(surfWave.fireLocation, myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(myLocation, goAngle - (Math.PI / 2), -1);
        } else {
            goAngle = wallSmoothing(myLocation, goAngle + (Math.PI / 2), 1);
        }

        setBackAsFront(this, goAngle);
    }

    /**
     * onHitWall: What to do when you hit a wall
     */
    public void onHitWall(HitWallEvent e) {
        // Replace the next line with any behavior you would like
        double turnAngle = Math.random() * 90 + 45; // Random turn between 45 and 135 degrees
        setBack(50 + Math.random() * 50); // Random back distance between 50 and 100 units
        setTurnRight(turnAngle);
        execute();
    }
}