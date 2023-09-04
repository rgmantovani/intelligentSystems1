package pacotinho;

import java.awt.Color;
import java.lang.Object;
import robocode.*;
import robocode.util.Utils;
import robocode.BattleRules.*;
import java.awt.geom.*;     // for Point2D's
import java.util.ArrayList; // for collection of waves


public class Shadow_O_Ourico extends AdvancedRobot
{

	// Setando tamanho da arena e área para o wall smoothing
	public double WALL_STICK = 140;	
	public double _bfWidth;
	public double _bfHeight;
	public java.awt.geom.Rectangle2D.Double _fieldRect;

	public int BINS = 47;
    public double _surfStats[] = new double[BINS];
	
	
    public Point2D.Double _myLocation;     //localizacao do Shadow
    public Point2D.Double _enemyLocation;  //localizacao do inimigo que ta trocando tiro

    public ArrayList _enemyWaves;
    public ArrayList _surfDirections;
    public ArrayList _surfAbsBearings;
	
	public double lateralDirection;
	public double lastEnemyVelocity;

    public double _oppEnergy = 100.0;	

	public void run() {
	
		setColors(Color.black, Color.red, Color.black);
		setBulletColor(Color.yellow);
		lateralDirection = 1;
		lastEnemyVelocity = 0;
	    _enemyWaves = new ArrayList();
        _surfDirections = new ArrayList();
        _surfAbsBearings = new ArrayList();
		
		_bfWidth = getBattleFieldHeight();
		_bfHeight = getBattleFieldWidth();
		_fieldRect = new java.awt.geom.Rectangle2D.Double(18, 18, _bfWidth-36, _bfHeight-36);
		
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
		
        do {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }while(true);
	}


	public void onScannedRobot(ScannedRobotEvent e) {
		 _myLocation = new Point2D.Double(getX(), getY());//determina a localização do Shadow

        double lateralVelocity = getVelocity()*Math.sin(e.getBearingRadians()); 
        double absBearing = e.getBearingRadians() + getHeadingRadians();
		double enemyDistance = e.getDistance();
		double enemyVelocity = e.getVelocity();
		if(enemyVelocity != 0){
			lateralDirection = sign(enemyVelocity*Math.sin(e.getHeadingRadians()- absBearing));
		}
        
		Wave wave = new Wave(this);
		wave.gunLocation = new Point2D.Double(getX(),getY());
		wave.targetLocation = project(wave.gunLocation, absBearing, enemyDistance);
		wave.lateralDirection = lateralDirection;
		wave.bulletPower = 1.9;
		wave.setSegmentations(enemyDistance,enemyVelocity,lastEnemyVelocity);
		lastEnemyVelocity = enemyVelocity;
		wave.bearing = absBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()+
			wave.mostVisitedBearingOffset()));
			setFire(wave.bulletPower);
			if(getEnergy() >= 1.9){
				addCustomEvent(wave);
			}
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
            - getRadarHeadingRadians()) * 2);
        _surfDirections.add(0,
            new Integer(sign(lateralVelocity)));
        _surfAbsBearings.add(0, new Double(absBearing + Math.PI));


        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09 && _surfDirections.size() > 2) {
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 1;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = bulletVelocity(bulletPower);
            ew.direction = ((Integer)_surfDirections.get(2)).intValue();
            ew.directAngle = ((Double)_surfAbsBearings.get(2)).doubleValue();
            ew.fireLocation = (Point2D.Double)_enemyLocation.clone(); // last tick

            _enemyWaves.add(ew);
		}
	 	_oppEnergy = e.getEnergy();

        // update after EnemyWave detection, because that needs the previous
        // enemy location as the source of the wave
        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        updateWaves();
        doSurfing();

        // gun code would go here...
	}

		public void updateWaves() {
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);

            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled > _myLocation.distance(ew.fireLocation) + 50) {
                _enemyWaves.remove(x);
                x--;
            }
        }
    }

		public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50033; // I juse use some very big number here
        EnemyWave surfWave = null;

        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
            double distance = _myLocation.distance(ew.fireLocation)
                - ew.distanceTraveled;

            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }
        return surfWave;
    }
	
		public int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)- ew.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle) / maxEscapeAngle(ew.bulletVelocity) * ew.direction;
		
        return (int)limit(0,(factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2), BINS - 1);
    }
	
		public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        for (int x = 0; x < BINS; x++) {
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }


	public void onHitByBullet(HitByBulletEvent e) {
		// If the _enemyWaves collection is empty, we must have missed the
        // detection of this wave somehow.
        if (!_enemyWaves.isEmpty()){
            Point2D.Double hitBulletLocation = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            // look through the EnemyWaves, and find one that could've hit us.
            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave)_enemyWaves.get(x);

                if (Math.abs(ew.distanceTraveled -_myLocation.distance(ew.fireLocation)) < 50 && Math.abs(bulletVelocity(e.getBullet().getPower())- ew.bulletVelocity) < 0.001){
                    hitWave = ew;
                    break;
                }
            }

            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);

                // We can remove this wave now, of course.
                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
	}
	//---------------------------------------------------------------------------------------------
	 public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0; // number of ticks in the future
        boolean intercepted = false;

        do {    // the rest of these code comments are rozu's
            moveAngle =  wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation,predictedPosition) + (direction * (Math.PI/2)), direction, 1) - predictedHeading;
            moveDir = 1;

            if(Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // maxTurning is built in like this, you can't turn more then this in one tick
            maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading + limit(-maxTurning, moveAngle, maxTurning));

            // this one is nice ;). if predictedVelocity and moveDir have
            // different signs you want to breack down
            // otherwise you want to accelerate (look at the factor "2")
            predictedVelocity += (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
            predictedVelocity = limit(-8, predictedVelocity, 8);

            // calculate the new predicted position
            predictedPosition = project(predictedPosition, predictedHeading, predictedVelocity);
            counter++;

            if (predictedPosition.distance(surfWave.fireLocation) <
                surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)+ surfWave.bulletVelocity) {
                intercepted = true;
            }
        }while(!intercepted && counter < 500);

        return predictedPosition;
    }
//---------------------------------------------------------------------------------------------

	public double checkDanger(EnemyWave surfWave, int direction) {
        int index = getFactorIndex(surfWave,
            predictPosition(surfWave, direction));

        return _surfStats[index];
    }

    public void doSurfing() {
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) { return; }

        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1, -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1, 1);
        }

        setBackAsFront(this, goAngle);
		
    }
	
	//--------------------------------------------------------------------------------	




	//---------------------------------------------------------------------------------
	public void onHitWall(HitWallEvent e) {
		
		back(20);
	}	
	

	//---------------------------------------------------------------------------------
 class EnemyWave {
        Point2D.Double fireLocation;
        long fireTime;
        double bulletVelocity, directAngle, distanceTraveled;
        int direction;

        public EnemyWave() { }
    }

    public double wallSmoothing(Point2D.Double _myLocation, double startAngle, int orientation, int smoothTowardEnemy){
		
		double angle = startAngle + (4*Math.PI);
		double testX = _myLocation.x + (Math.sin(angle)*WALL_STICK);
		double testY = _myLocation.y + (Math.cos(angle)*WALL_STICK);
		double wallDistanceX = Math.min(_myLocation.x - 18, _bfWidth - _myLocation.x - 18);
		double wallDistanceY = Math.min(_myLocation.y - 18, _bfHeight - _myLocation.y - 18);
		double testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
		double testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);
	
		double adjacent = 0;
		//Previnindo loop infinito
		int g = 0;

		while(!_fieldRect.contains(testX, testY) && g++ < 25) {
			if (testDistanceY < 0 && testDistanceY < testDistanceX) {
				angle = ((int)((angle + (Math.PI/2)) / Math.PI))* Math.PI;
				adjacent = Math.abs(wallDistanceY); 
			} else if (testDistanceX < 0 && testDistanceX <= testDistanceY) {
				angle = (((int)(angle / Math.PI)) * Math.PI) + (Math.PI/2);
				adjacent = Math.abs(wallDistanceX);
			}//else

			angle += smoothTowardEnemy * orientation * (Math.abs(Math.acos(adjacent/WALL_STICK)) + 0.005);
			
			testX = _myLocation.x + (Math.sin(angle) * WALL_STICK);
			testY = _myLocation.y + (Math.cos(angle) * WALL_STICK);
			testDistanceX = Math.min(testX - 18, _bfWidth - testX - 18);
			testDistanceY = Math.min(testY - 18, _bfHeight - testY - 18);

		}//while

		return angle;

	}//wallSmoothing
	
    public Point2D.Double project(Point2D.Double sourceLocation,
        double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
            sourceLocation.y + Math.cos(angle) * length);
    }

    public double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }
	
	public int sign(double value){
		return value < 0 ? -1 : 1;
	}

    public double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    public double bulletVelocity(double power) {
        return (20.0 - (3.0*power));
    }

    public double maxEscapeAngle(double velocity) {
        return Math.asin(8.0/velocity);
    }

    public void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle =
            Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1*angle);
           } else {
                robot.setTurnRightRadians(angle);
           }
            robot.setAhead(100);
        }
    }
	//---------------------------------------------------------------------------------
	class Wave extends Condition {
	public Point2D.Double targetLocation;

	double bulletPower;
	Point2D.Double gunLocation;
	double bearing;
	double lateralDirection;

	public final double MAX_DISTANCE = 1000;
	public final int DISTANCE_INDEXES = 5;
	public final int VELOCITY_INDEXES = 5;
	public final int BINS = 25;
	public final int MIDDLE_BIN = (BINS - 1) / 2;
	public final double MAX_ESCAPE_ANGLE = 0.7;
	public final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;
	
	public int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

	public int[] buffer;
	public AdvancedRobot robot;
	public double distanceTraveled;
	
	Wave(AdvancedRobot _robot) {
		this.robot = _robot;
	}
	
	public boolean test() {
		advance();
		if (hasArrived()) {
			buffer[currentBin()]++;
			robot.removeCustomEvent(this);
		}
		return false;
	}

	double mostVisitedBearingOffset() {
		return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
	}
	
	void setSegmentations(double distance, double velocity, double lastVelocity) {
		int distanceIndex = (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES));
		int velocityIndex = (int)Math.abs(velocity / 2);
		int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
		buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
	}

	public void advance() {
		distanceTraveled += bulletVelocity(bulletPower);
	}

	public boolean hasArrived() {
		return distanceTraveled > gunLocation.distance(targetLocation) - 18;
	}
	
	public int currentBin() {
		int bin = (int)Math.round(((Utils.normalRelativeAngle(absoluteBearing(gunLocation, targetLocation) - bearing)) /
				(lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
		return (int) limit(bin, 0, BINS - 1);
	}
	
	public int mostVisitedBin() {
		int mostVisited = MIDDLE_BIN;
		for (int i = 0; i < BINS; i++) {
			if (buffer[i] > buffer[mostVisited]) {
				mostVisited = i;
			}
		}
		return mostVisited;
	}
	}
}


