package si1;

import robocode.*;
import java.awt.Color;

public class Murieeel extends AdvancedRobot {

    private int direcaoMovimento = 1;
    private int direcaoRadar = 1;

    public void run() {
        // Cores inspiradas no Coragem, o Cão Covarde
        setBodyColor(new Color(255, 105, 180));
        setRadarColor(new Color(255, 105, 180));
        setGunColor(new Color(139, 69, 19));
        setBulletColor(Color.white);
        
        // Mantém o radar e a arma girando
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        while (true) {
            // Se for o último robô, vai diretamente para o ataque
            if (getOthers() == 1) {
                setAhead(150); // Anda mais rápido quando houver só um robô restante
                setTurnRight(30);
            } else {
                // Anda em zigzag
                setAhead(100 * direcaoMovimento);
                setTurnRight(90);
            }
            
            // Procura por um inimigo
            turnRadarRight(360 * direcaoRadar);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Trava o radar e a arma no inimigo
        double anguloInimigo = getHeading() + e.getBearing();
        
        double anguloRadar = anguloInimigo - getRadarHeading();
        setTurnRadarRight(normalizeBearing(anguloRadar));
        
        double anguloArma = anguloInimigo - getGunHeading();
        setTurnGunRight(normalizeBearing(anguloArma));
        
        // Define a força do tiro com base na velocidade do inimigo
        double forcaTiro;
        if (e.getVelocity() == 0) {
            // Se estiver parado, Murieeel!
            forcaTiro = 3;
        } else {
            // Senão, atira com força baseado na distância
            forcaTiro = Math.min(400 / e.getDistance(), 3);
        }
        
        setFire(forcaTiro);

        // Inverte a direção do movimento e radar para o próximo turno
        direcaoMovimento = -direcaoMovimento;
        direcaoRadar = -direcaoRadar;
        
        execute();
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        // Inverte o movimento ao ser atingido para confundir o inimigo
        direcaoMovimento = -direcaoMovimento;
        setAhead(100 * direcaoMovimento);
        setTurnRight(30);
    }
    
    // Esse método auxilia para não usar ângulos muito grandes na rotação
	// fonte: https://mark.random-article.com/weber/java/robocode/lesson4.html
    public double normalizeBearing(double angulo) {
        while (angulo > 180) angulo -= 360;
        while (angulo < -180) angulo += 360;
        return angulo;
    }
}