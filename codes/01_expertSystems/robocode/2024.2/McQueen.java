package fazuele;

/*
Membros: Mariana Pedroso, Lucas Santana, Filipe Augusto
*/

import robocode.*;
import java.awt.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class McQueen extends AdvancedRobot {
	private String inimigo;
	private boolean movimentoCircular = true; // Controle de direção sentido horario

	public void run() {
		setColors(Color.red, Color.black, Color.yellow, Color.orange, Color.yellow);
		
		// Não identificou nenhum inimigo
		inimigo = null;
		setAdjustGunForRobotTurn(true);      // Canhão independente do robô
		setAdjustRadarForGunTurn(true);      // Radar independente do canhão
		setAdjustRadarForRobotTurn(true);    // Radar independente do robô

		while (true) {
			if (inimigo == null) {
				turnRadarRight(360);  // Varre o campo para encontrar inimigos
			}
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		inimigo = e.getName();
		
		// Calcular a posição absoluta do inimigo e ajustar o radar
		double posicaoInimigo = getHeading() + e.getBearing();
		double ganhoRadar = normalRelativeAngleDegrees(posicaoInimigo - getRadarHeading());
		setTurnRadarRight(ganhoRadar + (ganhoRadar > 0 ? 30 : -30));  // "Bloqueio" no inimigo

		// Potência do tiro e velocidade da bala
		double potenciaBala = Math.min(3, getEnergy());
		double velocidadeBala = 20 - 3 * potenciaBala;  // Velocidade do projétil no Robocode

		// Caso o inimigo esteja parado, atira diretamente na posição atual
		if (e.getVelocity() == 0) {
			double ganhoCanhao = normalRelativeAngleDegrees(posicaoInimigo - getGunHeading());
			setTurnGunRight(ganhoCanhao);
			if (getGunHeat() == 0) {
				fire(potenciaBala);
			}
		} else {
			// Se o inimigo estiver em movimento, calcular a posição futura
			double direcaoInimigo = e.getHeading();
			double velocidadeInimigo = e.getVelocity();
			double distanciaInimigo = e.getDistance();

			// Estima o tempo para o projétil alcançar o inimigo
			double tempoBala = distanciaInimigo / velocidadeBala;

			// Calcula a posição futura com base na direção e velocidade do inimigo
			double futureX = getX() + distanciaInimigo * Math.sin(Math.toRadians(posicaoInimigo)) 
					+ tempoBala * velocidadeInimigo * Math.sin(Math.toRadians(direcaoInimigo));
			double futureY = getY() + distanciaInimigo * Math.cos(Math.toRadians(posicaoInimigo)) 
					+ tempoBala * velocidadeInimigo * Math.cos(Math.toRadians(direcaoInimigo));

			// Calcula o ângulo do canhão para a posição futura
			double anguloFuturoInimigo = normalRelativeAngleDegrees(Math.toDegrees(Math.atan2(futureX - getX(), futureY - getY())) - getGunHeading());
			setTurnGunRight(anguloFuturoInimigo);

			if (getGunHeat() == 0) {
				fire(potenciaBala);
			}
		}

		// Movimento do robô para se aproximar, recuar ou circular
		if (e.getDistance() > 150) {
			setTurnRight(e.getBearing());
			setAhead(e.getDistance() - 140);
		} else if (e.getDistance() < 100) {
			if (e.getBearing() > -90 && e.getBearing() <= 90) {
				setBack(40);
			} else {
				setAhead(40);
			}
		} else {
			// Movimento circular ao redor do inimigo
			if (movimentoCircular) {
				setTurnRight(e.getBearing() + 90);  // Mover em sentido horário
			} else {
				setTurnRight(e.getBearing() - 90);  // Mover em sentido anti-horário
			}
			setAhead(50);
		}
		execute();
	}

	// Método chamado quando o robô bate em uma parede
	public void onHitWall(HitWallEvent e) {
		// Muda a direção do movimento ao colidir com a parede
		setTurnRight(90);  // Gira 90 graus
		setBack(100);     // Move-se para longe da parede
		movimentoCircular = !movimentoCircular; // Inverte a direção ao redor do inimigo
	}

	// Método chamado quando o robô é atingido por uma bala
	public void onHitByBullet(HitByBulletEvent e) {
		movimentoCircular = !movimentoCircular;  // Muda a direção ao redor do inimigo
	}

	// Método chamado quando o inimigo é destruído
	public void onRobotDeath(RobotDeathEvent e) {
		if (e.getName().equals(inimigo)) {
			inimigo = null;  // Redefine o inimigo para reiniciar a varredura
		}
	}
}
