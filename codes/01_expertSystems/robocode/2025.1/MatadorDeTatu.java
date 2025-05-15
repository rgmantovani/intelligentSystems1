package stone;

import robocode.*;
import java.awt.Color;

/**
 * MatadorDeTatu - Robô com mira preditiva, radar travado e controle de distância.
 */
public class MatadorDeTatu extends AdvancedRobot {

    public void run() {
        setBodyColor(Color.black);
        setRadarColor(Color.green);
        setGunColor(Color.black);
        setBulletColor(Color.orange);

        // Movimentos independentes para radar, arma e corpo
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Radar girando para detectar inimigos
        while (true) {
            turnRadarRight(360);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyBearing = e.getBearing();
        double enemyDistance = e.getDistance();
        double enemyHeading = e.getHeadingRadians();
        double enemyVelocity = e.getVelocity();

        // Mira preditiva
        double bulletPower = 2;
        double myX = getX();
        double myY = getY();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyX = myX + Math.sin(absoluteBearing) * enemyDistance;
        double enemyY = myY + Math.cos(absoluteBearing) * enemyDistance;

        double bulletSpeed = 20 - 3 * bulletPower;
        double time = enemyDistance / bulletSpeed;

        double futureX = enemyX + Math.sin(enemyHeading) * enemyVelocity * time;
        double futureY = enemyY + Math.cos(enemyHeading) * enemyVelocity * time;

        double dx = futureX - myX;
        double dy = futureY - myY;
        double theta = Math.atan2(dx, dy);

        double gunTurn = robocode.util.Utils.normalRelativeAngle(theta - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);
        setFire(bulletPower);

        // Radar travado no inimigo
        double radarTurn = robocode.util.Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(2 * radarTurn);

        // Controle de distância
        if (enemyDistance < 150) {
            back(50);
        } else if (enemyDistance > 400) {
            ahead(50);
        } else {
            setTurnRight(enemyBearing + 90); // Movimento lateral
            ahead(50);
        }
    }

    public void onHitWall(HitWallEvent e) {
        back(50);
        turnRight(90);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        back(30);
        turnRight(45);
    }
}
