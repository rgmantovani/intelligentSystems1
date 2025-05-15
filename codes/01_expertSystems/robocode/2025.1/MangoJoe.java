package stone;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class MangoJoe extends AdvancedRobot {

    private int closeEnemies = 0;

    public void run() {
        setColors(Color.orange, Color.black, Color.yellow, Color.cyan, Color.red);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            closeEnemies = 0;
            turnRadarRight(360);  
            decideAndMove();
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        
        if (e.getDistance() < 200) {
            closeEnemies++;
        }

       
        if (closeEnemies <= 1) {
            double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
            double bearingFromGun = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians());
            setTurnGunRightRadians(bearingFromGun);

            if (Math.abs(bearingFromGun) < Math.toRadians(10)) {
                setFire(Math.min(400 / e.getDistance(), 3));
            }
        }
    }

    private void decideAndMove() {
        if (closeEnemies > 1) {
            
            setTurnRight(90);
            setAhead(150);
        } else {
           
            setTurnRight(30);
            setAhead(100);
        }
    }

    public void onHitWall(HitWallEvent e) {
        
        setBack(100);
        setTurnRight(90);
    }

    public void onHitRobot(HitRobotEvent e) {
       
        if (e.isMyFault()) {
            setBack(50);
            setTurnRight(60);
        }
    }
}
