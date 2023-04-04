package EC;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 * Coadjuvante - Robozinho de X1 dos Eternos Coadjuvantes
 */
public class Coadjuvante extends AdvancedRobot{

	String nome = null;

	public void run() {		

		setAdjustRadarForGunTurn(true); // separa o movimento do radar do movimento da arma
		setAdjustGunForRobotTurn(true); // separa o movimento da arma do movimento do corpo
		setColors(Color.black,Color.green,Color.green); // Corpo preto, radar e armas verdes
		
		double graus = 0; //quando não escaneando um robô, armazena a quantidade de graus a ser virada
		
		while (true) {
			if(nome == null) //caso não tenha um alvo
				turnRadarLeftRadians(Double.POSITIVE_INFINITY); //Sempre vira o radar para esquerda
			graus = Math.random()*30 + 30;
			ahead(100);
			turnRight(graus);
			ahead(100);
			graus = Math.random()*30 + 30;
			turnLeft(graus);
		}
	}

	/**
	 * Caso escaneie um robô:
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		//encontra a localização do robô escaneado
		
		if(nome == null){
			nome = e.getName();
		}		

		if (Math.abs(bearingFromGun) <= 3) {
			//confere se está próximo o suficiente do robô alvo e atira
			if (getGunHeat() == 0) {
				fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
			}
		} 
		turnGunRight(bearingFromGun);

		// Se movimenta de forma que o robô alvo fique sempre entre 300 e 400 unidades de distância
		if(e.getDistance() > 400)
			setAhead(e.getDistance()-400);
		else if(e.getDistance() < 300)
			setBack(300-e.getDistance());
		

	}

	//Quando bate na parede, recua e vira para uma direção aleatória
	public void onHitWall(HitWallEvent e){
		back(10);
		double graus = Math.random()*30 + 30;
		setTurnLeft(graus);
	}

	//Quando bate em um robô, se for minha culpa atira com grande força, porque eu estarei virado para ele
	public void onHitRobot(HitRobotEvent e){
		if(e.isMyFault())
			fire(8);
		back(10);
	}
	
}	