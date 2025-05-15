package stone;

import robocode.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;

// powered by hugostoso e ttzintt

public class PuroSangueFortnite extends AdvancedRobot {
    //constantes gerais
    static final double WALL_MARGIN = 36; //margem para evitar colisão na parede
    static final double BULLET_POWER_MAX = 3.0;
    static final double BULLET_POWER_MIN = 0.1;
    static final double DIST_SAFE = 150;   //distancia segura do alvo

    //variaveis de estado
    boolean movingForward = true;   //indicador de movimento
    double moveDirection = 1;       //direçao de movimento (+1 frente, -1 trás)
    Random rand = new Random();

    //estado do inimigo
    double enemyAbsBearing = 0;
    double enemyDistance = 0;
    double enemyEnergy = 100;

    public void run() {
        //definindo as cores
        setBodyColor(new Color(255, 0, 0));       //vermelho
        setGunColor(new Color(255, 127, 0));      //laranja
        setRadarColor(new Color(255, 255, 0));    //amarelo
        setBulletColor(new Color(0, 255, 0));     //verde
        setScanColor(new Color(0, 0, 255));       //azul

        //ajusta tiros e radar para não girarem junto ao corpo/gun
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        //gira radar constantemente para achar inimigos
        setTurnRadarRight(Double.POSITIVE_INFINITY);

        //vai até a parede mais próxima e alinha
        goToWall();

        //loop: mantém o robô patrulhando e executando comandos
        while (true) {
            wallSmoothMove();
            execute();
        }
    }

    //vai até a parede mais próxima e alinha paralelo a ela.
    private void goToWall() {
        double heading = getHeading() % 90;  //alinha com eixo X ou Y
        turnLeft(heading);
        ahead(getDistanceToWall());
        turnRight(90); //fica paralelo à parede
    }

    //calcula distância até a parede mais próxima(com margem)
    private double getDistanceToWall() {
        double x = getX(), y = getY();
        double width = getBattleFieldWidth(), height = getBattleFieldHeight();
        double toLeft   = x - WALL_MARGIN;
        double toRight  = width - x - WALL_MARGIN;
        double toTop    = height - y - WALL_MARGIN;
        double toBottom = y - WALL_MARGIN;
        return Math.min(Math.min(toLeft, toRight), Math.min(toTop, toBottom));
    }

    //moimento de patrulha, evita parede e cria imprevisibilidade
     
    private void wallSmoothMove() {
        //reversao aleatoia de direçao
        if (rand.nextDouble() < 0.015) {
            moveDirection *= -1;
            setBack(60 * moveDirection);
        }
        //se estiver perto de uma parede/canto, gira suavemente
        if (getX() < WALL_MARGIN || getY() < WALL_MARGIN
            || getBattleFieldWidth() - getX() < WALL_MARGIN
            || getBattleFieldHeight() - getY() < WALL_MARGIN) {
            setTurnRight(90);
            setAhead(50 * moveDirection);
        } else {
            setAhead(120 * moveDirection);
            setTurnRight(rand.nextGaussian() * 7); //pequeno desvio aleatorio
        }
    }

    //Quando detecta um robô, mira usando previsão linear e atira.

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        //radar Loc, mantém radar fixo no inimigo
        double absBearing = getHeadingRadians() + e.getBearingRadians();
        double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
        setTurnRadarRightRadians(1.9 * radarTurn); // Over-scan

        //previsao da posição inimiga
        double bulletPower = chooseBulletPower(e.getDistance(), e.getEnergy());
        double bulletSpeed = Rules.getBulletSpeed(bulletPower);

        double deltaTime = 0;
        double predictedX = getX() + e.getDistance() * Math.sin(absBearing);
        double predictedY = getY() + e.getDistance() * Math.cos(absBearing);

        //simula projeção linear até o tiro alcançar possível ponto futuro
        while ((++deltaTime) * bulletSpeed < Point2D.distance(getX(), getY(), predictedX, predictedY)) {
            predictedX += Math.sin(e.getHeadingRadians()) * e.getVelocity();
            predictedY += Math.cos(e.getHeadingRadians()) * e.getVelocity();
            //evita previsão fora do campo
            if (predictedX < WALL_MARGIN || predictedY < WALL_MARGIN ||
                predictedX > getBattleFieldWidth() - WALL_MARGIN ||
                predictedY > getBattleFieldHeight() - WALL_MARGIN) {
                break;
            }
        }
        //calcula angulo do canhão até ponto previsto
        double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));

        //atira somente se o canhão estiver quase alinhado
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(bulletPower);
        }

        //se o inimigo está muito perto, faz manobra evasiva especial
        if (e.getDistance() < DIST_SAFE) {
            setTurnRight(60);
            setBack(90);
            moveDirection *= -1; //troca direção
        }

        enemyEnergy = e.getEnergy();
    }

    //Ajusta potência do tiro conforme energia e distância do alvo.
    private double chooseBulletPower(double distance, double eEnergy) {
        if (getEnergy() < 15) return BULLET_POWER_MIN; //poupa energia no fim
        if (distance < 100) return BULLET_POWER_MAX;
        if (distance < 250) return 2.0;
        if (distance < 400) return 1.5;
        return 1.0;
    }

    //quando bate em parede recua e gira para evitar travar e ficar vulneravel

    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(60 * moveDirection);
        setTurnRight(55 + rand.nextInt(30)); //pqqueno giro
        moveDirection *= -1;
    }

    //quando bate em outro robô manobra evasiva para não ficar preso
    @Override
    public void onHitRobot(HitRobotEvent e) {
        double bearing = e.getBearing();
        if (bearing > -90 && bearing < 90) {
            setBack(70);
        } else {
            setAhead(70);
        }
        setTurnRight(35 + rand.nextInt(50));
        moveDirection *= -1;
    }

    //quando é atingido por um tiro faz manobra evasiva para dificultar tiros seguintes
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRight(60 + rand.nextInt(40));
        moveDirection *= -1;
        setAhead(90 * moveDirection);
    }

    //faz uma dancinha comemorando se vencer
    @Override
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}

/**
 * Utils - Funções auxiliares para normalização de ângulos:
 *  - normalRelativeAngle(): normaliza para [-pi, pi]
 *  - normalAbsoluteAngle(): normaliza para [0, 2*pi]
 */
class Utils {
    public static double normalRelativeAngle(double angle) {
        while (angle > Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }
    public static double normalAbsoluteAngle(double angle) {
        while (angle < 0) angle += 2 * Math.PI;
        while (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
        return angle;
    }
}
