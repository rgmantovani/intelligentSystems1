
/**
 * NOME DO ROBO: COBAIA05
 * Guilherme
 * Julio
 * Rafael 
 */
package c05;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.HitWallEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.*;

public class Cobaia05 extends Robot {
    int dist = 50;
    String targetName = null;
    double targetEnergy = Double.MAX_VALUE;
    boolean nearWall = false;
    boolean peek = false;
    double moveAmount;

    public void run() {
        // Configurando as cores do robô para preto e branco
        setBodyColor(Color.black);     // Corpo preto
        setGunColor(Color.white);      // Arma branca
        setRadarColor(Color.white);    // Radar branco
        setScanColor(Color.white);     // Scanner branco
        setBulletColor(Color.white);   // Bala branca

        // Configuração inicial para movimento ao longo das bordas
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
        turnLeft(getHeading() % 90);
        ahead(moveAmount);
        peek = true;
        turnGunRight(90);
        turnRight(90);

        while (true) {
            peek = true;
            ahead(moveAmount);
            peek = false;
            turnRight(90);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Atualiza o alvo se o robô escaneado tiver menor energia
        if (e.getEnergy() < targetEnergy) {
            targetName = e.getName();
            targetEnergy = e.getEnergy();
        }

        if (e.getName().equals(targetName)) {
            double angleToTarget = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
            turnGunRight(angleToTarget);

            // Atira estrategicamente com base na própria energia
            if (getEnergy() > 50) {
                fire(3);
            } else if (getEnergy() > 20) {
                fire(2);
            } else {
                fire(1); // Conserva energia ao usar tiros de potência baixa
            }

            if (!nearWall) {
                turnRight(e.getBearing());
                ahead(e.getDistance() - 50);
            }
        }
    }

    public void onHitWall(HitWallEvent e) {
        nearWall = true;
        // Permanece nas bordas
    }

    public void onHitByBullet(HitByBulletEvent e) {
        turnRight(normalRelativeAngleDegrees(90 - (getHeading() - e.getHeading())));
        ahead(dist);
        dist *= -1;
        scan();
    }

    public void onHitRobot(HitRobotEvent e) {
        double turnGunAmt = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
        turnGunRight(turnGunAmt);
        fire(3);
    }
}
