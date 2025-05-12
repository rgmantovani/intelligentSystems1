import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Ssisco extends AdvancedRobot {

    // Variáveis para armazenar informações relevantes sobre o estado do robô e do ambiente
    double dist = 0; 
    int directionCount = 0; 
    long moveTime = 1; 
    int moveDir = 1; 
    double bulletSpeed = 10; 
    double wallDistance = 100;

    public void run() {
        // Configuração das cores do robô
        setColors(Color.WHITE, Color.WHITE, Color.RED);

        while (true) {
            // Configurações para ajustar o radar, a arma e o robô
            setAdjustGunForRobotTurn(true);
            setAdjustRadarForGunTurn(true);
            setAdjustRadarForRobotTurn(true);

            // Garante que o radar está sempre girando
            if (getRadarTurnRemaining() == 0)
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Ângulo absoluto do alvo em relação à orientação atual do robô
        double targetAngle = e.getBearingRadians() + getHeadingRadians();

        // Distância com uma aleatoriedade de um número entre -3 e 3
        double distance = e.getDistance() + (Math.random() - 1) * 3;

        // Orientação do inimigo em relação à orientação do robô
        double enemyOrientation = getHeadingRadians() + e.getBearingRadians();
        double radarOrientation = e.getVelocity() * Math.sin(e.getHeadingRadians() - enemyOrientation);;

        // Coordenadas X e Y do inimigo
        double enemyX = getX() + e.getDistance() * Math.sin(enemyOrientation);
        double enemyY = getY() + e.getDistance() * Math.cos(enemyOrientation);
        double enemyX_real = enemyX - getX();
        double enemyY_real = enemyY - getY();

        // Controlar o radar do robô
        setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyOrientation - getRadarHeadingRadians()));
        // Controlar a rotação do robô
        setTurnRightRadians(-radarOrientation - getRadarHeadingRadians());

        // ------------------------------ Movimentação Wave Surfing com o Crazy

        // Faz a mudança de direção do robô
        if (--moveTime <= 0) {
            // Decrementa moveTime e verifica se é menor ou igual a zero
            distance = Math.max(distance, 80 + Math.random() * 70) * 1.5; 
            moveTime = 60 + (long) (distance / bulletSpeed); 
            directionCount++;

            // Verifica se é hora de mudar de direção
            if (Math.random() < 0.5 || directionCount > 12) {
                moveDir = -moveDir; 
                directionCount = 0; 
            }
        }

        // Direção do objetivo em relação à direção de movimento do robô
        double desiredDirection = targetAngle - Math.PI / 2 * moveDir;
        // Adiciona uma leve variação aleatória
        desiredDirection += (Math.random() - 0.3) * (Math.random() * 2 + 1);

        // ------------------------------ Esquiva

        double x = getX();
        double y = getY();
        double smoothness = 0;

        // Define o campo de batalha como um retângulo
        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        while (!fieldRect.contains(x + Math.sin(desiredDirection) * wallDistance, y + Math.cos(desiredDirection) * wallDistance)) {
            desiredDirection += moveDir * 0.1;
            smoothness += 0.1;
        }

        // Verifica a suavidade do movimento e muda de direção se necessário
        if (smoothness > 0.5 + Math.random() * 0.125) {
            moveDir = -moveDir;
            directionCount = 0;
        }

        double turnAngle = Utils.normalRelativeAngle(desiredDirection - getHeadingRadians());

        // Verifica se é necessário virar completamente para trás
        if (Math.abs(turnAngle) > Math.PI / 2) {
            turnAngle = Utils.normalRelativeAngle(turnAngle + Math.PI);
            setBack(100);
        } else {
            setAhead(100);
        }

        setTurnRightRadians(turnAngle);

        // ------------------------------ Lógica do Tiro

        double bulletPower = 1 + Math.random() * 2;
        double bulletSpeed = 20 - 3 * bulletPower;

        double lateralVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - targetAngle);
        double escapeAngle = Math.asin(8 / bulletSpeed);

        double enemyDirection = Math.signum(lateralVel);
        double angleOffset = escapeAngle * enemyDirection * Math.random();
        setTurnGunRightRadians(Utils.normalRelativeAngle(targetAngle + angleOffset - getGunHeadingRadians()));


        dist = e.getDistance();

        int scannedRobotsCount = getOthers() + 1; 

        // Lógica de disparo de tiros baseada no número de inimigos escaneados
        if (scannedRobotsCount >= 3) {
            double timeToHit = dist / bulletSpeed;
            double futureX = enemyX + e.getVelocity() * Math.sin(e.getHeadingRadians()) * timeToHit;
            double futureY = enemyY + e.getVelocity() * Math.cos(e.getHeadingRadians()) * timeToHit;
            double predictionAngle = Math.atan2(futureX - getX(), futureY - getY());

            setTurnGunRightRadians(Utils.normalRelativeAngle(predictionAngle - getGunHeadingRadians()));

            // Dispara tiros com diferentes potências dependendo da distância do inimigo
            if (dist >= 250) {
                setFire(bulletPower);
            } else if (dist >= 100) {
                setFire(2);
            } else {
                setBack(30);
                fire(3);
            }

        } else {
            if (getEnergy() > bulletPower) {
                if (dist >= 250) {
                    setFire(bulletPower);
                } else if (dist >= 100) {
                    setFire(2);
                } else {
                    setBack(30);
                    fire(3);
                }
            }
        }

        // Aumenta a potência do tiro se o inimigo estiver muito próximo
        if (dist < 50) {
            bulletPower = 3; 
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        // Calcula a distância estimada baseada na força do tiro que atingiu o robô
        double estimatedDistance = (300 / (30 - 3 * e.getBullet().getPower())) * e.getBullet().getPower();

        // Se a distância estimada for menor que a distância atual do inimigo, escaneia novamente
        if (estimatedDistance < dist) {
            scan();
        }

        execute();
    }

    public void onHitRobot(HitRobotEvent e) {
        // Calcula a orientação do robô atingido e vira o canhão para mirar nele
        double enemyOrientation = e.getBearing() + getHeading();
        double turn = Utils.normalRelativeAngleDegrees(enemyOrientation - getGunHeading());

        turnGunRight(turn);
        fire(3);

        execute();
    }
}
