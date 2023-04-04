//import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Freeza - a robot by (your name here)
 */
package Freeza;
import robocode.*;
import java.awt.*;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Freeza extends AdvancedRobot {
	//Variaveis utilizadas
	String robotName = null;
	boolean peek;
	double moveAmount;
	
	public void run() {
		// Set colors
		setBodyColor(new Color(255, 255, 255));
		setGunColor(new Color(136, 86, 167));
		setRadarColor(new Color(136, 86, 167));
		setBulletColor(new Color(227, 74, 51));
		setScanColor(new Color(255, 200, 200));
		
		// Inicializa moveAmount com o maximo do campo de batalha
		moveAmount = Math.max((getBattleFieldWidth() - 20), (getBattleFieldHeight()-20));
		// inicializa o peek com falso
		peek = false;
		//Vira a esquerda
		turnLeft((getHeading() % 90)- 0);
		//Vai o maximo possivel para frente
		ahead(moveAmount);
		//vira a arma 90 graus a esquerda
		peek = true;
		//vira a direita
		turnRight(90);
		while (true) {		
			// quando completar o ahead entrar aqui e mover novamente
			peek = true;
			// mover para frente
			ahead(moveAmount);
			// alterar o peek para falso
			peek = false;
			// virar a direita 90 graus
			turnRight(90);
			turnGunRight(360);		
		}
	}
	
	//o que fazer apos scanear um inimigo 
	public void onScannedRobot(ScannedRobotEvent e){
		//Calcular a localizacao do inimigo
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		turnGunRight(bearingFromGun);
		//Foraca do tiro 
		//Caso menor de 300 de distancia fogo nivel 3
		if(e.getDistance() < 300){
			fire(3);
		//Caso ele estiver menor q 500 de distancia fogo nivel 2
		}else if(e.getDistance() >= 300 && e.getDistance() < 500){
			fire(2);
		//Caso distancia maior que 500 fogo nivel 1
		}else{
			fire(2);
		}
	}

	//Evento ao tomar dano de um inimigo
	public void onHitByBullet(HitByBulletEvent e){	
		//Calcular a localizacao do inimigo
		double absoluteBearing = getHeading() + e.getBearing();
		double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
		//Para onde a arma deve apontar
		turnGunRight(bearingFromGun);
		fire(3);
	}
	
	//o que fazer apos colisao com outro robo
	public void onHitRobot(HitRobotEvent e) {
		//isMyFault() informa se o robo que acertei com o tiro esta vindo em minha direcao
		boolean dirHitRobot = e.isMyFault();
		//turnGunAmt: coleta a direcao que robo colidiu e abre fogo
		if(dirHitRobot){
			//Calcular a localizacao do inimigo
			double absoluteBearing = getHeading() + e.getBearing();
			double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
			//Para onde a arma deve apontar
			turnGunRight(bearingFromGun);
			fire(3);
		}
	}
}