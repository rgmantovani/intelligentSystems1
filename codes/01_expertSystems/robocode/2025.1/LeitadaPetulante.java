package stone;
import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.Random;

/*
 * LeitadaPetulante - a robot by (Hirata)
 * Robô de comportamento 1 x 1
 */

public class LeitadaPetulante extends AdvancedRobot {

	Random random = new Random();
	int direction = 1;

	public void run() {
		setColors(Color.black, Color.orange, Color.yellow);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		while (true) {
			setTurnRadarRight(360);
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// Lock no radar
		double radarTurn = Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading());
		setTurnRadarRight(radarTurn);

		// Mira
		double absBearing = getHeading() + e.getBearing();
		double gunTurn = Utils.normalRelativeAngleDegrees(absBearing - getGunHeading());
		setTurnGunRight(gunTurn);

		// Movimento lateral com mudança aleatória de direção
		double moveAngle = Utils.normalRelativeAngleDegrees(e.getBearing() + 90 - (15 * direction));
		setTurnRight(moveAngle);
		setAhead(150 * direction);
		if (random.nextDouble() < 0.1) direction *= -1;

		// Tiro só se estiver bem alinhado
		if (Math.abs(gunTurn) < 10) {
			if (e.getDistance() < 150) {
				setFire(3);
			} else if (e.getDistance() < 400) {
				setFire(2);
			} else {
				setFire(1);
			}
		}

		execute(); // executa tudo junto
	}

	public void onHitByBullet(HitByBulletEvent e) {
		direction *= -1; // troca direção se for atingido
	}

	public void onHitWall(HitWallEvent e) {
		back(100);
		turnRight(90 + random.nextInt(90)); // Gira em um ângulo aleatório
		ahead(150);
	}


	// "T-bag" (não dá para fazer no corpo do adversário pois não dá tempo mas dá para girar no lugar)
	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		// Gira tudo ao mesmo tempo pra comemorar a vitória com um 720°
		setTurnRight(720);
		setTurnGunRight(720);
		setTurnRadarRight(720);
		waitFor(new TurnCompleteCondition(this));
	}

}
