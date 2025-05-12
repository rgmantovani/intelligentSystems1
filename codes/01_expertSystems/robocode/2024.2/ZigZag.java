package Atividade01;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.ScannedRobotEvent;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * ZigZag - a robot by (your name here)
 */

// Se alguém tiver atacando, desvia/foge com mínimo de moviementos
// Scanner a vida e ataca quem tiver com menos vida
// Caso, não estiver acontecendo isso, movimentos pequenos.

// Método para scannear inimigos (localização e vida)
// Método para atacar o que tiver menos vida 
// Método para scannear se está sendo atacado
// Método para fugir
// Método para sair de uma parede 

// Se ele estiver atacando e tomar um hit, começar a fugir 
// Se ele acertar o hit no adversário, ir em ZigZag 

public class ZigZag extends AdvancedRobot
{
	/**
	 * run: ZigZag's default behavior
	 */

	public void run() {
		
		//definicao das carcteristicas do zigzag
		setBodyColor(new Color(178, 24, 255));
		setGunColor(new Color(255, 255, 0));
		setBulletColor(new Color(102,51,0));

		//O radar vai ser dependente da arma, ira rodar junto com a arma
		setAdjustRadarForGunTurn(false); 
		
		//loop para deixar a arma caçar novos inimigos
		while (true) {
			turnGunRight(30);
		}
 	
	}//run

	public void onScannedRobot(ScannedRobotEvent radar) {
		
		//fazer com que a arma e o radar fiquem alinhados 
		double turnGunAmt = normalRelativeAngleDegrees(radar.getBearing() + getHeading() - getGunHeading());
		
		//fazendo a varredura e atacando o inimigo
		smartFire(radar.getDistance());
		setTurnRight(90);
		ahead(20);
		setTurnLeft(80);
		ahead(20);
		stop();
		scan();
		resume();

	}//onScannedRobot

	public void onHitByBullet(HitByBulletEvent radar) {
		// Quando ele toma um hit		
		setTurnRight(60);
		ahead(30);
		setTurnLeft(80);
		ahead(30);	
			
	}//onHitByBullet
	
	public void onHitWall(HitWallEvent e) {
		// Quando cruza com uma paredes
		// Da um giro de 180º para seguir o caminho contrário
		setTurnRight(180);
	}	
	
	public void smartFire(double robotDistance) {
		//alvejando alvos com base na energia e distancia
		if (robotDistance > 200 || getEnergy() < 15) {
			fire(1);
		} else if (robotDistance > 50) {
			fire(2);
		} else {
			fire(3);
		}
	}

}