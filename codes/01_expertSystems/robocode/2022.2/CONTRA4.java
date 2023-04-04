package CONTRA4;

import robocode.*;
import java.awt.*;

//Equipe CONTRA4 - Gustavo Lázaro, Camila Beatriz e Hevellyn Paz.

/**
 * Baseado no Tracker, o CONTRA4 é uma MAQUINA de matar robos alheios. Ele se baseia em chegar perto de outros robos e descer bala neles.
 * Como a pontuação geral também leva em conta o dano causado, este robo se aproveita dessa métrica para fazer o maior número de pontos possíveis, mesmo que n ganhe a partida.
 * 
 */
public class CONTRA4 extends AdvancedRobot {
    int moveDirection = 1;// Define a direção que ele irá se mover

    //Função principal - Ela vai ficar em loop chamando os métodos ANIQUILADORES DE ROBOS.
    public void run() {

        setAdjustRadarForRobotTurn(true); //Garante que o radar vai ficar parada enquanto o robo faz a curva - Evita que ative desnecessáriamente e gaste energia.

        //Cor do robo e do scan
        setBodyColor(new Color(215, 130, 186));
        setGunColor(new Color(225, 138, 212));
        setRadarColor(new Color(238, 177, 213));
        setScanColor(Color.white);
        setBulletColor(Color.blue);

        setAdjustGunForRobotTurn(true); //Garante que a arma vai ficar parada enquanto o robo faz a curva - Evita perder a mira de outro robo.

        turnRadarRightRadians(Double.POSITIVE_INFINITY); //Roda o radar o máximo para a direita sempre
    }

    public void onScannedRobot(ScannedRobotEvent e) {

        double absBearing = e.getBearingRadians() + getHeadingRadians();//Comportamento dos inimigos (Para onde irão?)
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);//Velocidade dos inimgios calculada
        double gunTurnAmt;//Direção da pistola

        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//Olha o radar

        if(Math.random() > .9){
            setMaxVelocity((12 * Math.random()) + 12); //Muda a velocidade aleatoriamente (Faz o passinho para confundir os inimigos)
        }

        if (e.getDistance() > 150) {//Se a distancia estiver maior que 150

            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + latVel / 22);//Vira a arma para a direção "correta"

            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing - getHeadingRadians() + latVel / getVelocity()));//Vai para onde o inimigo provavelmente está indo também
            setAhead((e.getDistance() - 140) * moveDirection);//Segue em frente 
            setFire(3);//🔥🔥🔥🔥🔥
        }
        else{//Se estiver perto o suficiente do inimigo (Desce o chumbo)
            
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians() + latVel / 15);//Vira a arma para a direção "correta"

            setTurnGunRightRadians(gunTurnAmt);//Mira a pistola
            setTurnLeft(-90  -e.getBearing()); //Vira perpendicularmente para o inimigo (Diminiu distancia sem ir reto como se fosse um bobão).
            setAhead((e.getDistance() - 140) * moveDirection);//Vai para frente
            setFire(3);// 🔥🔥🔥🔥🔥
        }
    }
    public void onHitWall(HitWallEvent e){

        moveDirection=-moveDirection;// Quando bater na parede, ele vai para o lado oposto.
    }
    
    //  A DANÇA DO CAMPEÃO (OU NÃO)
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}