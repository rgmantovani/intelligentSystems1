package Catatau;

import robocode.*;
import robocode.util.Utils;
import java.lang.*; // for Double and Integer objects
import java.awt.geom.*; // for Point2D's
//import java.awt.Color;

public class WaveBullet {
	private double startX, startY, startBearing, power;
	private long fireTime;
	private int direction;
	private int[] returnSegment;

	public WaveBullet(double x, double y, double bearing, double power, int direction, long time, int[] segment) {
		this.startX = x;
		this.startY = y;
		this.startBearing = bearing;
		this.power = power;
		this.direction = direction;
		this.fireTime = time;
		this.returnSegment = segment;
	}

	public double getBulletSpeed() {
		return 20 - this.power * 3;
	}

	public double maxEscapeAngle() {
		return Math.asin(8 / getBulletSpeed()); // furthest angle our enemy can be
	}

	public boolean checkHit(double enemyX, double enemyY, long currentTime) {
		// if the distance from the wave origin to our enemy has passed
		// the distance the bullet would have traveled...
		if (Point2D.distance(this.startX, this.startY, enemyX, enemyY) <= (currentTime - this.fireTime) * getBulletSpeed()) {
			double desiredDirection = Math.atan2(enemyX - this.startX, enemyY - this.startY);
			double angleOffset = Utils.normalRelativeAngle(desiredDirection - this.startBearing);
			double guessFactor = Math.max(-1, Math.min(1, angleOffset / maxEscapeAngle())) * this.direction;
			int index = (int) Math.round((this.returnSegment.length - 1) / 2 * (guessFactor + 1));
			this.returnSegment[index]++;
			return true;
		}
		return false;
	}
}
