package stone;

import robocode.*;
import java.util.Random;

public class OneVOne extends AdvancedRobot {
    private int moveDir;
    private int tickCount;
    private int switchTick;
    private final Random rnd = new Random();

    @Override
    public void run() {
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        moveDir = 1;
        tickCount = 0;
        switchTick = 20 + rnd.nextInt(41); // troca de direcao entre 20 e 60 ticks

        turnRadarRight(360);

        while (true) {
            setTurnRadarRight(360);
            execute();

            tickCount++;
            if (tickCount >= switchTick) {
                moveDir *= -1;
                tickCount = 0;
                switchTick = 20 + rnd.nextInt(41);
            }
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double dist = e.getDistance();
        double vel  = e.getVelocity();

        // Mantem o radar travado no oponente
        double radarTurn = getHeading() + e.getBearing() - getRadarHeading();
        setTurnRadarRight(normalize(radarTurn));

        // Ajusta a potencia do tiro para equilibrar alcance e velocidade do projetil
        double firePower = (vel > 2 && dist < 300) ? 2.0 : Math.min(400 / dist, 3.0);
        double bulletSpeed = 20 - firePower * 3;

        // Calcula onde o inimigo vai estar e mira nessa posicao
        double angleToEnemy = Math.toRadians(getHeading() + e.getBearing());
        double time = dist / bulletSpeed;
        double futureX = getX() + dist * Math.sin(angleToEnemy)
                       + vel * time * Math.sin(e.getHeadingRadians());
        double futureY = getY() + dist * Math.cos(angleToEnemy)
                       + vel * time * Math.cos(e.getHeadingRadians());
        double targetAngle = Math.toDegrees(Math.atan2(futureX - getX(), futureY - getY()));
        setTurnGunRight(normalize(targetAngle - getGunHeading()));
        if (getGunHeat() == 0) {
            setFire(firePower);
        }

        // Decide a distancia e o angulo de desvio
        double orbit = 100 + rnd.nextDouble() * 50;    // entre 100 e 150 pixels
        double jitter = rnd.nextDouble() * 90 - 45;     // ate 45° de variacao
        double strafeAng = e.getBearing() + 90 * moveDir + jitter;

        // Se estiver muito perto, faz um avanco rapido
        if (dist < 120) {
            setAhead((dist - 80) * moveDir);
        }
        // Se a proxima posicao bater na parede, gira para dentro
        else if (willHitWall(orbit)) {
            setTurnRight(45 * moveDir);
        }
        // Caso contrario, segue no desvio definido
        else {
            setTurnRight(normalize(strafeAng));
            setAhead(orbit * moveDir);
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Quando atingido, inverte a direcao e se afasta
        moveDir *= -1;
        setTurnRight(normalize(90 - e.getBearing()));
        setAhead(100 * moveDir);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // Rebate ao encostar na parede
        moveDir *= -1;
        setBack(50);
    }

    // Ajusta qualquer angulo para ficar entre -180° e +180°
    private double normalize(double ang) {
        while (ang > 180) ang -= 360;
        while (ang < -180) ang += 360;
        return ang;
    }

    // Verifica se, ao andar a certa distancia, bateremos na parede
    private boolean willHitWall(double dist) {
        double x = getX() + Math.sin(Math.toRadians(getHeading())) * dist;
        double y = getY() + Math.cos(Math.toRadians(getHeading())) * dist;
        return x < 50 || x > getBattleFieldWidth() - 50
            || y < 50 || y > getBattleFieldHeight() - 50;
    }
}
