package robozao;
import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;


public class Igris extends AdvancedRobot{

	private int moveDirection = 1; // controla o movimento do robo. para frente ou para trás
	private int control = 1; // controla o movimento do robo. para direita ou para esquerda
	private boolean change = false; // flag para controlar se houve mudança de movimento
	private double quant; // quantidade para andar perpendicularmente a outro robo
	private long contador = 0; // conta quantos ticks desde a ativação da flag 


	public void run() {
		// Define que o radar e a arma devem ajustar suas posições de acordo com a movimentação do robô
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true); 

		// Define as cores do robô
		setBodyColor(new Color(182, 0, 238));
		setGunColor(new Color(182, 0, 238));
		setRadarColor(new Color(180, 0, 0));
		setScanColor(new Color(0, 0, 0));
		setBulletColor(new Color(182, 0, 238));

		// Faz o radar girar infinitamente para detectar inimigos
		turnRadarRightRadians(Double.POSITIVE_INFINITY);
	}
	
	// quando o Radar escanear um robo
	public void onScannedRobot(ScannedRobotEvent e) {

		// condição para mudar de direção após 35 ticks
		if (change && getTime() - contador  >= 35 ){
			// direção retorna ao normal, contador zerado e flag desativada
			moveDirection = 1;
			contador = 0;
			change = false;
		}
		
		// define a direção do movimento do robô
		quant =  -90-e.getBearing();

		// calcula a direção absoluta do robô inimigo
		double absBearing = e.getBearingRadians() + getHeadingRadians();	
		
		// calcula a velocidade lateral do robô inimigo
		double latVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing);
		
		// atualiza o movimento do radar
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
		
		// altera aleatoriamente a velocidade do robô
		if (Math.random() > 0.9){
			setMaxVelocity((12 * Math.random()) + 12);
		}
		
		// define a potência da bala
		double bulletPower = Math.min(3.0, getEnergy()/3); 
		
		// calcula as coordenadas do robô e do inimigo
		double myX = getX();
		double myY = getY();
		double enemyX = getX() + e.getDistance() * Math.sin(absBearing);
		double enemyY = getY() + e.getDistance() * Math.cos(absBearing);
		
		// define a direção do robô inimigo
		double enemyHeading = e.getHeadingRadians();
		double enemyVelocity = e.getVelocity();
		double deltaTime = 0;
		double battleFieldHeight = getBattleFieldHeight();
		double battleFieldWidth = getBattleFieldWidth();

		// calcula o tempo que leva para a bala atingir o inimigo
		while ((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(myX, myY, enemyX, enemyY)) {
			enemyX += Math.sin(enemyHeading) * enemyVelocity;
			enemyY += Math.cos(enemyHeading) * enemyVelocity;
			
			// se o robô inimigo estiver perto das bordas do campo de batalha, ajusta as coordenadas
			if (enemyX < 18.0 || enemyY < 18.0 || enemyX > battleFieldWidth - 18.0 || enemyY > battleFieldHeight - 18.0) {
				enemyX = Math.min(Math.max(18.0, enemyX), battleFieldWidth - 18.0);
				enemyY = Math.min(Math.max(18.0, enemyY), battleFieldHeight - 18.0);
				break;
			}
		}

		// calcula a direção da arma para apontar para o inimigo
		double futureBearing = Math.atan2(enemyX - getX(), enemyY - getY());
		double gunTurn = robocode.util.Utils.normalRelativeAngle(futureBearing - getGunHeadingRadians());
		setTurnGunRightRadians(gunTurn);

		// Verifica se a distância do robô para o inimigo é maior que 150 unidades.
		if (e.getDistance() > 150) {

		// Define a direção para a qual o robô deve se virar para enfrentar o inimigo
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()
				+latVel/getVelocity()));

			// move para frente ou para trás dependendo do valor de moveDirection
			setAhead((e.getDistance() - 140)*moveDirection);
			setFire(bulletPower);		
		}
		else{// caso contrario

			// vira perpendicularmente ao inimigo
			setTurnLeft(quant); 
			setBack(100*moveDirection*control);		
		    setFire(bulletPower);
		}
	}

	public void	onHitByBullet(HitByBulletEvent e){

		// caso seja atingido por um tiro, control é invertido
		control *=-1;
	}
	public void onHitWall(HitWallEvent e){

		// muda a direção do movimento
		moveDirection *=-1;

		// a flag change é ativada
		change = true;

		// salva o valor do tempo naquele instante
		contador = getTime();

		// pega a coordenada do robo e o tamanho da arena
		double robotX = getX();
    	double arenaWidth = getBattleFieldWidth();

		if (robotX < arenaWidth / 2) {
			// o robô bateu na parede esquerda
			setTurnRight(quant*control);
		} else {
			// o robô bateu na parede direita
			setTurnLeft(quant*control);
		}
		execute();
	}
	public void onHitRobot(HitRobotEvent e){

		// vira a arma em direção a ameaça e extermina
		setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle( e.getBearingRadians()+getHeadingRadians() - getGunHeadingRadians()));
		setFire(3);
	}


}
