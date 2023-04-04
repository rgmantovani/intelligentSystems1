package SInteligentes;
import robocode.*;
import java.awt.Color;

public class Ussop extends AdvancedRobot {
    int moveD=1;

    public void run() {
        setAdjustRadarForRobotTurn(true);//mantém o radar parado quando o robô está virando
		setAdjustGunForRobotTurn(true); //mantém a arma parada quando o robô está virando
		
		// CORES DO USSOP
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(new Color(200, 200, 70));
		
        setScanColor(Color.blue);
        setBulletColor(Color.red);
		
        turnRadarRightRadians(Double.POSITIVE_INFINITY); //mantém o radar sempre virando para a direita
    }

	// Se escanear um robô faça...
    public void onScannedRobot(ScannedRobotEvent e) {
        double absAngle = e.getBearingRadians() + getHeadingRadians(); //enemies absolute angle
		
        double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absAngle);//velocidade posterior dos inimigos
		
        double gunTurnAmt;
		
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//trava o radar
		
        if (e.getDistance() > 200) {//se a distância para o inimigo for maior que 200
			setMaxVelocity(0); //fica parado
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absAngle- getGunHeadingRadians()+latVel/22); //quantidade que a arma será deslocada
            setTurnGunRightRadians(gunTurnAmt); //vira a arma
            //setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absAngle - getHeadingRadians()+latVel/getVelocity()));
            setAhead((e.getDistance() - 140)*moveD);//se move para frente
            setFire(1);//atira
        } else{
			setMaxVelocity(500); //Se a distancia para o inimigo for menor que 200, diminua a velocidade
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absAngle - getGunHeadingRadians()+latVel/15); //quantidade que a arma será deslocada
            setTurnGunRightRadians(gunTurnAmt); //vira a arma
            setTurnLeft(-90-e.getBearing()); //se mantém na perpendicular ao inimigo
            setAhead((e.getDistance() - 140)*moveD);//se move para frente
            setFire(5);//atira
		}
    }
	
    public void onHitWall(HitWallEvent e){
        moveD=-moveD;//reverte a direção do robô caso ele colida com uma parede
    }
    

	// DANÇA DA VITÓRIA!!!!!
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
			turnRight(60);
            turnLeft(60);
        }
    }
}
