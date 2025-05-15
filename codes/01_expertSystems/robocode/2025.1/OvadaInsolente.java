package stone;

import robocode.*;
import java.awt.Color;

/*
 * OvadaInsolente - a robot by (Thiagao)
 * Robô de comportamento all x all
*/

public class OvadaInsolente extends AdvancedRobot {
	boolean movingForward;
	
	public void run() {
		while (getEnergy() > 5) {
			setColors(Color.white, Color.yellow, Color.orange); // Cor do robô
			setMaxTurnRate(8); // Taxa máxima de rotação
			setMaxVelocity(4); // Velocidade máxima
			setAhead(50000); // Move-se para frente indefinidamente
			setTurnGunRight(50000); // Gira o canhão indefinidamente
			movingForward = true; // Marca que está indo para frente
			// Realiza giros em ângulo reto (movimento em zig-zag)
			setTurnRight(90);
			waitFor(new TurnCompleteCondition(this));
			setTurnLeft(180);
			waitFor(new TurnCompleteCondition(this));
			setTurnRight(180);
			waitFor(new TurnCompleteCondition(this));
		}

		while (getEnergy() <= 5) {
			setColors(Color.blue, Color.blue, Color.white); // Muda cor indicando estado crítico
			setMaxTurnRate(10);
			setMaxVelocity(8); // Move-se mais rápido para fugir
			// Calcula posição central do campo
			double centerX = getBattleFieldWidth() / 2;
			double centerY = getBattleFieldHeight() / 2;
			double dx = centerX - getX();
			double dy = centerY - getY();
			// Calcula ângulo para o centro
			double angleToCenter = Math.toDegrees(Math.atan2(dx, dy));
			double turnAngle = angleToCenter - getHeading();
			turnAngle = normalizeBearing(turnAngle);
			// Gira para o centro e anda até lá
			turnRight(turnAngle);
			ahead(Math.hypot(dx, dy));
			// Continua girando indefinidamente e movendo
			setTurnRight(10000);
			ahead(10000);
			break; // Sai do loop
		}
	}
	
	public void onScannedRobot(ScannedRobotEvent e) {
		if (getEnergy() > 5) {
			fire(2); // Atira com potência 2 se tiver energia suficiente
		}
	}

	
	public void onHitWall(HitWallEvent e) {
		reverseDirection(); // Reverte direção ao bater na parede
	}

	public void onHitRobot(HitRobotEvent e) {
		reverseDirection(); // Reverte direção ao colidir com outro robô
	}

	
	public void reverseDirection() {
		if (movingForward) {
			setBack(40000); // Anda para trás se estava indo para frente
			movingForward = false;
		} else {
			setAhead(40000); // Anda para frente se estava indo para trás
			movingForward = true;
		}
	}

	public double normalizeBearing(double angle) {
	    while (angle > 180) angle -= 360;
	    while (angle < -180) angle += 360;
	    return angle;
	}
}
