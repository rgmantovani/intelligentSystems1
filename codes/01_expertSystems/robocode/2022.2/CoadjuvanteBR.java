package Kato;
import robocode.*;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import robocode.util.Utils;
// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * First - a robot by Kato
 */
public class First extends AdvancedRobot
{
	/**
	 * run: First's default behavior
	 */
	public int batidaParede = 0;
	public String alvo; // Robo para dar lock
	public double direcao = 1; // direcao do movimento
	public double ViraArma; // Radianos para virar a arma
	public void run() {
		// Initialization of the robot should be put here
		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// Coloca o radar independendo da arma, permitindo virar separado
		setAdjustRadarForGunTurn(true);
		// Coloca o arma independendo da robo, permitindo virar separado
		setAdjustGunForRobotTurn(true);
		setColors(Color.magenta,Color.orange,Color.red); // body,gun,radar
		setBulletColor(Color.red);
		while(true){
			turnRadarLeftRadians(Double.POSITIVE_INFINITY); //Sempre vira o radar para esquerda
		}

	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		// Pega o nome do alvo scaneado
		if(alvo == null) alvo = e.getName();

		// Pega posicao do robo inimigo
		double absBearing=e.getBearingRadians()+getHeadingRadians();
		//double radar = absBearing - getRadarHeadingRadians();
		//setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(radar));
		// Move radar para esquerda
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		double direcaoAlvo = absBearing-Math.PI/2*direcao;  // -90 graus para andar na perpendicular do inimigo

		// Cria um retangulo baseado no tamanho da arena
		// Com a intençao de evitar bater na parede.
		Rectangle2D field = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-50, getBattleFieldHeight()-50);

		// Vira enquanto X e Y estiver dentro do retangulo
		while (!field.contains(getX()+Math.sin(direcaoAlvo)*120, getY()+ Math.cos(direcaoAlvo)*120)){
			direcaoAlvo += direcao*.1;	//Vira um pouco
		}
		// Cria o angulo normalizado da curva
		double turn = robocode.util.Utils.normalRelativeAngle(direcaoAlvo-getHeadingRadians());

		// Se a curva for maior que 90
		if (Math.abs(turn) > Math.PI/2){
			turn = Utils.normalRelativeAngle(turn + Math.PI);
			setBack(100);
		} else{
			setAhead(100);
			setTurnRightRadians(turn);
		}

		// Se robo inimigo tiver mais que 300 px, pega o angulo para apontar para o robo inimigo e atira com
		// uma potencia de 1.5.
		// Se tiver entre 150 a 300 px, atira com potencia de 2.4
		// Se tiver abaixo de 150, atira com potencia maxima.
		ViraArma = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
		if(e.getDistance() > 300){
			setTurnGunRightRadians(ViraArma);
			setFire(1.5);
		} else if(e.getDistance() > 150 && e.getDistance() < 300) {
			setTurnGunRightRadians(ViraArma);
			setFire(2.4);
		} else {
			setTurnGunRightRadians(ViraArma);
			setFire(3);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		alvo = e.getName();  // Foca o robo que nos acertou
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		// Se bater na parede, resume o movimento em ré
		direcao=-direcao;
		// Caso fique preso no canto, vire 45 graus para sair
		batidaParede++;
		if(batidaParede == 4){
			setTurnRight(45);
			batidaParede = 0;
		}
	}

	public void onHitRobot(HitRobotEvent e) {
		// Replace the next line with any behavior you would like
		// Caso bata no em um robo, atira na potencia maxima
		ViraArma = Utils.normalRelativeAngle(e.getBearingRadians()+getHeadingRadians() - getGunHeadingRadians());
		setTurnGunRightRadians(ViraArma);
		setFire(3);
	}
}
