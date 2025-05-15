package stone;

import robocode.*;
import robocode.util.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * LuciusTcl
 * Based on:
 * [1] https://robowiki.net/wiki/GuessFactor_Targeting_Tutorial
*/
public class LuciusTcl extends AdvancedRobot {
	private class WaveBullet {
		private double startX;
		private double startY;
		private double startBearing;
		private double power;
		private long fireTime;
		private int direction;
		private int[] returnSegment;
		
		public WaveBullet(double x, double y, double bearing, double power, int direction, long time, int[] segment) {
			startX = x;
			startY = y;
			startBearing = bearing;
			this.power = power;
			fireTime = time;
			this.direction = direction;
			returnSegment = segment;
		}

		public double getBulletSpeed() {
			return 20 - power * 3;
		}
		
		public double maxEscapeAngle() {
			return Math.asin(8 / getBulletSpeed());
		}
		
		public double getBulletTraveledDistance(long currentTime) {
			return (currentTime -fireTime) * getBulletSpeed();
		}
		
		public boolean checkHit(double enemyX, double enemyY, long currentTime) {
			if(Point2D.distance(startX, startY, enemyX, enemyY) > getBulletTraveledDistance(currentTime)) {
				return false;
			}
			
			double desiredDirection = Math.atan2(enemyX - startX, enemyY - startY);
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - startBearing);
			double guessFactor = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * direction;
			int index = (int) Math.round((returnSegment.length - 1) /2 * (guessFactor + 1));
			returnSegment[index]++;
			return true;
		}
	}

	private static final Color tclPink = new Color(219,60,107);

	/* Gun targeting system */
	private ArrayList<WaveBullet> waves = new ArrayList<WaveBullet>();
	private static final int STATS_SIZE = 31;
	private static final int SEGMENT_COUNT = 13;
	private static final int PIXELS_PER_SEGMENT = 100;
	private static int[][] stats = new int[SEGMENT_COUNT][STATS_SIZE];
	private int direction = 1;

	public void run() {
		setAllColors(tclPink);

		while(true) {
			ahead(20);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double ex = getX() + Math.sin(absBearing) * e.getDistance();
		double ey = getY() + Math.cos(absBearing) * e.getDistance();
		
		for(int i=0; i < waves.size(); i++) {
			WaveBullet currentWave = (WaveBullet)waves.get(i);

			if(currentWave.checkHit(ex, ey, getTime())) {
				waves.remove(currentWave);
				i--;
			}
		}
		
		double power = Rules.MAX_BULLET_POWER * 0.8;

		if(e.getVelocity() != 0) {
			if(Math.sin(e.getHeadingRadians()-absBearing)*e.getVelocity() < 0) {
				direction = -1;
			} else {
				direction = 1;
			}
		}

		int[] currentStats = stats[Math.min((int)(e.getDistance() / PIXELS_PER_SEGMENT), SEGMENT_COUNT - 1)];
		WaveBullet newWave = new WaveBullet(getX(), getY(), absBearing, power, direction, getTime(), currentStats);
						
		int bestindex = STATS_SIZE / 2;

		for(int i=0; i<STATS_SIZE; i++) {
			if(currentStats[bestindex] < currentStats[i]) {
				bestindex = i;
			}
		}

		double guessfactor = (double)(bestindex - (stats.length - 1) / 2) / ((stats.length - 1) / 2);
		double angleOffset = direction * guessfactor * newWave.maxEscapeAngle();
        double gunAdjust = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + angleOffset);
        setTurnGunRightRadians(gunAdjust);
				
		if(getGunHeat() == 0 && gunAdjust < Math.atan2(9, e.getDistance()) && setFireBullet(power) != null) {
			waves.add(newWave);
		}
	}
	
	public void onHitWall(HitWallEvent e) {
		back(20);
		turnRight(45);
	}

	public void onHitByBullet(HitByBulletEvent e) {
        int direction = (int)(Math.random() * 2);

        if(direction == 0) {
            turnLeft(90);
        } else {
            turnRight(90);
        }

        ahead(100);
    }	
}