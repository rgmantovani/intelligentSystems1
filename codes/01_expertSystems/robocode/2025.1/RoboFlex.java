package stone;

import robocode.*;
import java.awt.*;
import java.util.Random;

/**
 * ATV 01 - ROBOCODE 
 * Maria Eduarda Soares e Nicolas Romano
 */

public class RoboFlex extends AdvancedRobot {
    private final Random aleatorio = new Random();
    private int inimigosVivos; // contador de inimigos ainda vivos

    public void run() {
        // define as cores do robo
        setBodyColor(Color.BLACK);
        setGunColor(Color.RED);
        setRadarColor(Color.BLACK);

        // permite que o canhao e o radar se movam independente do corpo
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // gira o radar sem parar para procurar inimigos
        setTurnRadarRight(Double.POSITIVE_INFINITY);

        // armazena o numero total de inimigos na batalha
        inimigosVivos = getOthers();

        while (true) {
            // se houver mais de um inimigo, executa movimento continuo aleatorio
            if (inimigosVivos > 1) {
                movimentoContinuo();
            }
            execute(); // executa todos os outros comandos 
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double distancia = e.getDistance();
        double absoluteBearing = getHeading() + e.getBearing();
        double anguloCanhao = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        double anguloRadar = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());

        if (inimigosVivos == 1) {  // modo com apenas um inimigo (duelo 1x1)
           
            setTurnRadarRight(anguloRadar * 2);  // trava o radar no inimigo
            movimento1adv(e);  // movimentacao lateral (desvia dos tiros)
            setTurnGunRight(anguloCanhao); // gira o canhao em direcao ao inimigo
			
            // atira somente se o canhao estiver quase alinhado
            if (Math.abs(anguloCanhao) < 5 && getGunHeat() == 0) {
                if (distancia < 200 && getEnergy() > 15) fire(3); // tiro forte de perto
                else fire(1.5); // tiro fraco se estiver longe ou com pouca energia
            }

        } else { // modo com varios inimigos 
            
            setTurnRadarRight(anguloRadar + (anguloRadar < 0 ? -20 : 20)); // radar gira levemente para os lados procurando alvos
            setTurnGunRight(anguloCanhao);   // gira o canhao em direcao ao inimigo
            // atira imediatamente apos mirar
            if (distancia < 200 && getEnergy() > 15) fire(3); // tiro forte de perto
            else fire(1.5); // tiro padrao
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        inimigosVivos--;  // diminui o contador de inimigos vivos
        setTurnRadarRight(Double.POSITIVE_INFINITY); // volta a progrurar novos inimigos 
    }

    public void onHitByBullet(HitByBulletEvent e) {
       
        double bulletAngle = e.getBearing() + getHeading();  // quando for atingido por uma bala, tenta se afastar da direcao do tiro
        setTurnRight(normalRelativeAngleDegrees(bulletAngle + 180)); // gira para o lado oposto
        setAhead(150); // avanca
    }

    public void onHitWall(HitWallEvent e) {  // se bater na parede, volta e gira para outro lado
        setBack(100);
        setTurnRight(90);
    }

    public void onHitRobot(HitRobotEvent e) {
        if (!e.isMyFault()) {  // se a colisao nao for culpa do roboFlex, se afasta
            setBack(50);
            setTurnRight(90);
        } else {
            // se colidir atacando, tenta mirar e atirar
            double absoluteBearing = getHeading() + e.getBearing();
            double angulo = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
            setTurnGunRight(angulo);
            fire(2.5);
        }
    }

    // movimentacao aleatoria para batalhas com varios inimigos
    private void movimentoContinuo() {
        int distancia = 100 + aleatorio.nextInt(100); // distancia aleatoria entre 100 e 200
        int angulo = 30 + aleatorio.nextInt(60); // angulo aleatorio entre 30 e 90

        if (aleatorio.nextBoolean()) {
            setAhead(distancia); // anda para frente
            setTurnRight(angulo); // gira para a direita
        } else {
            setBack(distancia); // anda para tras
            setTurnLeft(angulo); // gira para a esquerda
        }
    }

    // movimentacao lateral para duelos 1x1
    private void movimento1adv(ScannedRobotEvent e) {
        double bearing = e.getBearing(); // angulo do inimigo em relacao ao robo
        setTurnRight(bearing + 90 - 30); // gira para um angulo lateral 
        setAhead(150 * (Math.random() > 0.5 ? 1 : -1)); // anda para frente ou tras aleatoriamente
    }

    // normaliza um angulo para o intervalo entre -180 e 180 graus
    private double normalRelativeAngleDegrees(double angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
