package shaw;
import robocode.*;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Guarana - a robot by (Plinio, Giovanna, Gabriel Takeshi)
 */
public class Guarana extends AdvancedRobot {

    private ScannedRobotEvent alvo;
    private double energiaInimigoAnterior = 100.0; // Para rastrear a energia do inimigo
    private byte direcaoMovimento = 1; // Para alternar a direcao da evasao

    public void run() {
        setBodyColor(Color.green); 
        setGunColor(Color.red);
        setRadarColor(Color.red);
        
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Foca em manter o radar girando para nao perder o alvo
        while (true) {
		 ahead(150 * direcaoMovimento); // Anda para frente ou para trás
            setTurnRight(30); // Vira um pouco para criar o padrao
            direcaoMovimento *= -1; // Inverte a direcao para o proximo passo
            if (alvo == null) {
                setTurnRadarRight(360);
            } else {
                setTurnRadarRight(getRadarTurnRemaining());
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        
        double energiaInimigoAtual = e.getEnergy();
        double quedaDeEnergia = energiaInimigoAnterior - energiaInimigoAtual;

        // Se a energia caiu entre 0.1 e 3.0, o inimigo provavelmente atirou
        if (quedaDeEnergia > 0 && quedaDeEnergia <= 3.0) {
            
            // Calcula o angulo da bala do adversario
            double anguloDaBala = getHeadingRadians() + e.getBearingRadians();
            
            // Calcula o angulo perpendicular para a fuga (90 graus)
            double anguloDeFuga = anguloDaBala + (Math.PI / 2 * direcaoMovimento);

            // Vira o robo para a direcao de fuga
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(anguloDeFuga - getHeadingRadians()));
            
            // Move se rapidamente para frente nessa nova direcao para sair da trajetoria da bala
            setAhead(150);
            
            // Inverte a direcao da proxima evasao para nao ficar preso em um canto
            direcaoMovimento *= -1;
        }
        
        // Atualiza a energia do inimigo 
        energiaInimigoAnterior = energiaInimigoAtual;

        alvo = e; // Atualiza o alvo para ser o ultimo robo escaneado
        
		// Calcula a direcao absoluta do adversario no campo
        double anguloAbsoluto = getHeadingRadians() + e.getBearingRadians();
		//Calcula o menor caminho para a arma virar no adversario
        double anguloArma = robocode.util.Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians());
        setTurnGunRightRadians(anguloArma);
        
        // Trava o radar
        double anguloRadar = robocode.util.Utils.normalRelativeAngle(anguloAbsoluto - getRadarHeadingRadians());
        setTurnRadarRightRadians(anguloRadar * 2);

        // Logica de tiro
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            double distancia = e.getDistance();
            if (distancia < 150) {
                setFire(3);
            } else if (distancia < 200) {
                setFire(2);
            } else if (distancia < 400) {
                setFire(1);
            }else{
				setFire(0.3);
			}
        }
    }
    
    public void onRobotDeath(RobotDeathEvent e) {
        if (alvo != null && e.getName().equals(alvo.getName())) {
            alvo = null;
            energiaInimigoAnterior = 100.0; // Reseta a energia
        }
    }
}