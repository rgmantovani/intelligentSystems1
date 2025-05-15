package stone;

import robocode.*;
import java.util.Random;

public class BattleRoyale extends AdvancedRobot {
    private String targetName = null;           // nome do alvo atual
    private double targetDistance = Double.MAX_VALUE; // distancia ate o alvo
    private int moveDir = 1;                    // direcao de strafe: +1 ou -1
    private final Random rnd = new Random();

    @Override
    public void run() {
        // permite girar canhao e radar independentemente do corpo
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);

        // faz uma varredura inicial 
        turnRadarRight(360);

        // mantem o radar girando sem parar
        while (true) {
            setTurnRadarRight(360);
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double dist = e.getDistance();
        double vel  = e.getVelocity();

        // escolhe o oponente mais proximo como alvo
        if (targetName == null || dist < targetDistance) {
            targetName     = e.getName();
            targetDistance = dist;
        }
        // so reage ao alvo atual
        if (!e.getName().equals(targetName)) {
            return;
        }

        // mantem o radar focado nesse alvo
        double radarTurn = getHeading() + e.getBearing() - getRadarHeading();
        setTurnRadarRight(normalize(radarTurn));

        // define força do tiro: mais forte se estiver perto
        double firePower = dist < 150 ? 3.0 : Math.min(400 / dist, 3.0);
        double bulletSpeed = 20 - firePower * 3;

        // calcula onde o inimigo estara com base em sua velocidade
        double angleToEnemy = Math.toRadians(getHeading() + e.getBearing());
        double travelTime  = dist / bulletSpeed;
        double futureX     = getX() + dist * Math.sin(angleToEnemy)
                           + vel * travelTime * Math.sin(e.getHeadingRadians());
        double futureY     = getY() + dist * Math.cos(angleToEnemy)
                           + vel * travelTime * Math.cos(e.getHeadingRadians());
        double aimAngle    = Math.toDegrees(Math.atan2(futureX - getX(), futureY - getY()));
        setTurnGunRight(normalize(aimAngle - getGunHeading()));
        if (getGunHeat() == 0) {
            setFire(firePower);
        }

        // decide movimento: avança se estiver longe, recua se estiver perto
        if (dist > 300) {
            setAhead(dist - 250);
        } else if (dist < 100) {
            setBack(120 - dist);
        } else {
            // faz um strafe com pequena variacao aleatória
            double jitter    = rnd.nextDouble() * 40 - 20;  
            double strafeAng = e.getBearing() + 90 * moveDir + jitter;

            // se for bater na parede, inverte o strafe e afasta
            if (willHitWall(100)) {
                moveDir *= -1;
                setBack(50);
            } else {
                setTurnRight(normalize(strafeAng));
                setAhead(100 * moveDir);
            }
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // quando atingido, inverte a direcao e recua um pouco
        moveDir *= -1;
        setBack(60);
    }

    @Override
    public void onHitWall(HitWallEvent e) {
        // ao bater na parede, inverte a direcao e recua
        moveDir *= -1;
        setBack(60);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        // se o alvo morreu, escolhe outro 
        if (e.getName().equals(targetName)) {
            targetName     = null;
            targetDistance = Double.MAX_VALUE;
        }
    }

    //Ajusta um ângulo para ficar entre -180 e +180 graus
     
    private double normalize(double angle) {
        while (angle > 180)  angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    //Verifica se andar 'dist' pixels na direção atual bate em alguma parede
     
    private boolean willHitWall(double dist) {
        double x = getX() + Math.sin(Math.toRadians(getHeading())) * dist;
        double y = getY() + Math.cos(Math.toRadians(getHeading())) * dist;
        return x < 50 || x > getBattleFieldWidth() - 50
            || y < 50 || y > getBattleFieldHeight() - 50;
    }
}
