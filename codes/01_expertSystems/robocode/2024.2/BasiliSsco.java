import robocode.*;
import robocode.util.Utils;
import java.awt.Color;

public class BasiliSsco extends AdvancedRobot {
    private double moveDirection = 1;
    private double previousEnemyEnergy = 100;
    private double preferredDistance = 300;

    public void run() {
        // Configurações iniciais
        setBodyColor(Color.BLUE);
        setGunColor(Color.DARK_GRAY);
        setRadarColor(Color.GREEN);
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        // Movimenta o radar continuamente para encontrar o inimigo
        turnRadarRight(Double.POSITIVE_INFINITY);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double enemyBearing = e.getBearingRadians();
        double enemyDistance = e.getDistance();
        double enemyEnergy = e.getEnergy();
        double radarTurn = getHeadingRadians() + enemyBearing - getRadarHeadingRadians();

        // Controle de radar para manter o inimigo sempre detectado
        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn * 2));

        // Detecta disparos do inimigo com base na queda de energia
        if (previousEnemyEnergy > enemyEnergy) {
            moveDirection = -moveDirection;
            setAhead(150 * moveDirection);
        }
        previousEnemyEnergy = enemyEnergy;

        // Movimentos laterais aleatórios
        double lateralMovement = Utils.normalRelativeAngle(enemyBearing + Math.PI / 2 - (Math.random() * Math.PI / 4 * moveDirection));
        setTurnRightRadians(lateralMovement);
        setAhead(100 * moveDirection);

        // Ajuste de distância
        if (enemyDistance < preferredDistance) {
            setBack(100 * moveDirection);
        } else if (enemyDistance > preferredDistance + 50) {
            setAhead(100 * moveDirection);
        }

        // Cálculo preditivo para a mira
        double bulletPower = Math.min(3, Math.max(1, (400 / enemyDistance)));
        double enemyVelocity = e.getVelocity();
        double enemyHeading = e.getHeadingRadians();
        double bulletSpeed = 20 - 3 * bulletPower;
        double lead = enemyVelocity * Math.sin(enemyHeading - (getHeadingRadians() + enemyBearing)) / bulletSpeed;
        double gunTurn = Utils.normalRelativeAngle((getHeadingRadians() + enemyBearing + lead) - getGunHeadingRadians());

        setTurnGunRightRadians(gunTurn);
        if (getGunHeat() == 0 && getEnergy() > bulletPower) {
            setFire(bulletPower);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // Reação ao ser atingido: mudar a direção
        moveDirection = -moveDirection;
        setAhead(100 * moveDirection);
    }

    public void onHitRobot(HitRobotEvent e) {
        // Controle de colisão e tiro próximo
        double gunTurn = Utils.normalRelativeAngle(e.getBearingRadians() + getHeadingRadians() - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);
        setFire(3);
    }

    public void onWin(WinEvent e) {
        // Dança da vitória
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}
