// File: Pobrema.java
package stone;

import robocode.*;
import java.awt.*;

/**
 * Pobrema - robô avançado que tranca no inimigo, se aproxima e atira conforme a distância
 */
public class Pobrema extends AdvancedRobot {
    private int moveDirection = 1; // direção de movimento (1 para frente, -1 para trás)

    /**
     * Função principal: configurações iniciais e escaneamento contínuo
     */
    public void run() {
        setAdjustRadarForRobotTurn(true);    // manter radar estático ao girar
        setAdjustGunForRobotTurn(true);      // manter canhão estático ao girar
        setBodyColor(new Color(0, 153, 0));  
        setGunColor(new Color(255, 0, 0));
        setRadarColor(new Color(255, 255, 255));
        setScanColor(Color.white);
        setBulletColor(Color.blue);

        while (true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY); // gira radar sem parar
        }
    }

    /**
     * Chamado ao escanear um robô inimigo
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians()); // travar radar no alvo

        if (Math.random() > 0.9) {
            setMaxVelocity(12 * Math.random() + 15); // varia velocidade aleatoriamente
        }

        double distance = e.getDistance();
        double gunTurnAmt;

        if (distance > 450) {
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 37);
            setTurnGunRightRadians(gunTurnAmt);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));
            setAhead((distance - 100) * moveDirection);
        } else if (distance > 100) {
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);
            setTurnGunRightRadians(gunTurnAmt);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));
            setAhead((distance - 100) * moveDirection);
            setFire(7);
        } else {
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 15);
            setTurnGunRightRadians(gunTurnAmt);
            setTurnLeft(-90 - e.getBearing()); // strafing perpendicular
            setAhead((distance - 100) * -moveDirection);
            setFire(20);
        }
    }

    /**
     * Chamado ao colidir com uma parede: inverte direção
     */
    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection*3;
    }

    /**
     * Chamado ao colidir com outro robô: recupera ou avança e inverte direção
     */
    public void onHitRobot(HitRobotEvent e) {
        if (e.getBearing() > -90 && e.getBearing() <= 90) {
            back(100); // inimigo à frente, recua
        } else {
            ahead(100); // inimigo atrás, avança
        }
        moveDirection = -moveDirection; // evita colisões repetidas
    }

    /**
     * Dança da vitória quando vence
     */
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnLeft(130);
        }
    }
}
