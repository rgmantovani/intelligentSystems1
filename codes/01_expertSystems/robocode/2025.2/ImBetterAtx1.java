import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;

public class ImBetterAtX1 extends AdvancedRobot {

    private ScannedRobotEvent inimigoMaisProximo = null;
    
    // Variáveis para a nova lógica de mira
    private Point2D.Double ultimaPosicaoInimigo = null;
    private int contadorInimigoParado = 0;

    public void run() {
        setColors(Color.cyan, Color.red, Color.blue);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true); // Radar gira independente do canhão

        while (true) {
            // Radar gira 360 graus continuamente para procurar inimigos
            setTurnRadarRight(360);
            
            setAhead(100);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Obtenha a posição atual do inimigo
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        Point2D.Double posicaoAtualInimigo = new Point2D.Double(getX() + Math.sin(absoluteBearing) * e.getDistance(), getY() + Math.cos(absoluteBearing) * e.getDistance());

        // Atualiza a informação do inimigo
        inimigoMaisProximo = e;

        // --- Lógica para detectar inimigo parado ---
        if (ultimaPosicaoInimigo != null && Math.abs(posicaoAtualInimigo.x - ultimaPosicaoInimigo.x) < 1 && Math.abs(posicaoAtualInimigo.y - ultimaPosicaoInimigo.y) < 1) {
            // Se a posição atual for a mesma da última, incrementa o contador
            contadorInimigoParado++;
        } else {
            // Se a posição mudou, reseta o contador
            contadorInimigoParado = 0;
            ultimaPosicaoInimigo = posicaoAtualInimigo;
        }

        // Lógica de movimentação em ziguezague
        setTurnRight(e.getBearing() + 90);
        setAhead(150);

        // --- Lógica de Disparo ---
        // Se o inimigo está parado por 3 varreduras ou mais
        if (contadorInimigoParado >= 3) {
            setTurnGunRight(normalizeBearing(e.getBearing() + getHeading() - getGunHeading()));
            fire(3); // Disparo de alta potência
        } else {
            // Se o inimigo está se movendo, usa a estratégia de disparo normal
            setTurnGunRight(normalizeBearing(e.getBearing() + getHeading() - getGunHeading()));

            if (e.getDistance() < 150) {
                fire(3);
            } else if (e.getDistance() < 400) {
                fire(2);
            } else {
                fire(1);
            }
        }
    }

    public void onHitWall(HitWallEvent e) {
        setBack(100);
        setTurnRight(90);
    }
    
    public double normalizeBearing(double bearing) {
        while (bearing > 180) bearing -= 360;
        while (bearing < -180) bearing += 360;
        return bearing;
    }
}