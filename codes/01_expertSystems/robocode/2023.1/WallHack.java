package sample;

import robocode.*;
import robocode.util.*;
import java.awt.*;

/**
 * WallHack - é baseado no robo SuperWalls criado por CrazyBassoonist, a partir do robo original Walls
 * Se move envolta da arena com dois tipos de ataque
 */
public class WallHack extends AdvancedRobot {
    static int HGShots;     //Numero de tiros com Head-on Targeting
    static int LGShots;     //Numero de tiros com Lienar Targeting
    static int HGHits;      //Numero de acertos com Head-on Targeting
    static int LGHits;      //Numero de acertos com Lienar Targeting
    boolean gunIdent;       //Define a estrategia de tiro que sera usada
    int dir = 1;
    double energy;
    static int enemyFireCount = 0;

    public void run() {
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setBodyColor(Color.gray);
        setGunColor(Color.black);
        setRadarColor(Color.orange);
        setBulletColor(Color.orange);
        setScanColor(Color.red);

        setTurnRadarRight(Double.POSITIVE_INFINITY);
		
		int contador = 0;

        turnLeft(getHeading() % 90);//fica em um angulo reto com as paredes

        while (true) {
		
            if (Utils.isNear(getHeadingRadians(), 15D) || Utils.isNear(getHeadingRadians(), Math.PI)) {
                ahead((Math.max(getBattleFieldHeight() - getY(), getY()) - 28) * dir);
            } else {
                ahead((Math.max(getBattleFieldWidth() - getX(), getX()) - 28) * dir);
            }
			
            turnRight(90 * dir);
			
			double andarRandomico = Math.random();
			
	        if(andarRandomico > .8){
	            setMaxVelocity(12 * andarRandomico); 
	        }
			
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
        }
    }

	//O que fazer ao Scannear um robo
	
    public void onScannedRobot(ScannedRobotEvent e) {
	
        double absBearing = e.getBearingRadians() + getHeadingRadians();                // Localizcao do inimigo em relacao ao robo
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing); // Velocidade lateral do inimigo
        double radarTurn = absBearing - getRadarHeadingRadians();                       // Quando deve mover o radar

        double HGRating = (double) HGHits / HGShots;
        double LGRating = (double) LGHits / LGShots;

        if (energy > (energy = e.getEnergy())) {
            enemyFireCount++;
            if (enemyFireCount % 1 == 0) {
                dir = -dir;
                if (Utils.isNear(getHeadingRadians(), 60D) || Utils.isNear(getHeadingRadians(), Math.PI)) {
                    setAhead((Math.max(getBattleFieldHeight() - getY(), getY()) - 28) * dir);
                } else {
                    setAhead((Math.max(getBattleFieldWidth() - getX(), getX()) - 28) * dir);
                }
            }
        }
		

        if ((getRoundNum() == 0 || LGRating > HGRating) && getRoundNum() != 1){ // No primeiro round ou quando a Linear Gun acertou mais
            double bulletPower = Math.min(3, e.getEnergy() / 4);
			if(absBearing < 65){
				bulletPower = 3;
			}
			setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + Math.asin(latVel / 22)));
            LGShots++;
            gunIdent = true;
            setFire(bulletPower); // Atira o minumo de energia para derrotar o inimigo ou o maximo caso ele esteja perto
        } else { // segundo round ou quando a Head-on gun acertou mais
			double bulletPower = Math.min(3, e.getEnergy() / 4);
			if(absBearing < 55){
				bulletPower = 3;
			}
            setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians()));
            HGShots++;
            gunIdent = false;
            setFire(bulletPower); // Atira o minumo de energia para derrotar o inimigo ou o maximo caso ele esteja perto
        }
        setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn) * 2.5); // Fazer o radar travar
    }
	
	//O que fazer ao levar um tiro

    public void onBulletHit(BulletHitEvent e) {
        if(gunIdent) {
            LGHits = LGHits+1;
        } else {
            HGHits = HGHits+1;
        }
		setMaxVelocity(12);
    }
}
