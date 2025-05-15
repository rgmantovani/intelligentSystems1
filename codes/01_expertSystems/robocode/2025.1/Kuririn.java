package stone;

import robocode.*;
import java.awt.*;

public class Kuririn extends AdvancedRobot {
    // Constantes ajustáveis
    private static final double DISTANCIA_MINIMA = 150;
    private static final double DISTANCIA_MAXIMA = 350;
    private static final double FATOR_POTENCIA_TIRO = 0.5;
    private static final double MARGEM_PAREDE = 60;

    // Variáveis do inimigo
    private double anguloInimigo, distanciaInimigo, direcaoInimigo, velocidadeInimigo;
    private double posicaoInimigoX, posicaoInimigoY;
    private double direcaoInimigoAnterior;
    private double energiaAnteriorInimigo;

    // Direção do movimento (1 = frente, -1 = trás)
    private int direcaoMovimento = 1;

    public void run() {
        // Define as cores do robô
        setColors(Color.orange, Color.black, Color.black);

        // Configurações para que o radar e a arma se movam independentemente do corpo
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        // Inicializa a direção de movimento aleatoriamente
        if (Math.random() > 0.5) {
            direcaoMovimento = 1;
        } else {
            direcaoMovimento = -1;
        }

        setTurnRight(45 * direcaoMovimento);
        moverComSeguranca(100 * direcaoMovimento);

        while (true) {
            // Gira o radar constantemente
            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(360);
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        anguloInimigo = e.getBearing();
        distanciaInimigo = e.getDistance();
        direcaoInimigo = e.getHeading();
        velocidadeInimigo = e.getVelocity();

        // Calcula a posição do inimigo em coordenadas absolutas
        double anguloAbsoluto = Math.toRadians((getHeading() + e.getBearing()) % 360);
        posicaoInimigoX = getX() + Math.sin(anguloAbsoluto) * e.getDistance();
        posicaoInimigoY = getY() + Math.cos(anguloAbsoluto) * e.getDistance();

        manterDistanciaIdeal(e);
        miraAvancada(e);
        atirarInteligente();

        energiaAnteriorInimigo = e.getEnergy();
    }

    private void manterDistanciaIdeal(ScannedRobotEvent e) {
        double proporcaoDistancia = (distanciaInimigo - DISTANCIA_MINIMA) / (DISTANCIA_MAXIMA - DISTANCIA_MINIMA);

        if (distanciaInimigo < DISTANCIA_MINIMA) {
            moverComSeguranca(-100 + (proporcaoDistancia * -100));
            direcaoMovimento = -1;
        } else if (distanciaInimigo > DISTANCIA_MAXIMA) {
            moverComSeguranca(100 * proporcaoDistancia);
            direcaoMovimento = 1;
        } else {
            // Mantém distância girando 90 graus em relação ao inimigo
            setTurnRight(normalizarAngulo(90 - e.getBearing()));
            moverComSeguranca(80 * direcaoMovimento);
        }

        // Muda de direção aleatoriamente de vez em quando
        if (Math.random() < 0.05) {
            direcaoMovimento = direcaoMovimento * -1;
        }
    }

    private void miraAvancada(ScannedRobotEvent e) {
        double potenciaTiro = Math.min(2.5, Math.max(1.5, getEnergy() * FATOR_POTENCIA_TIRO));
        double velocidadeBala = 20 - 3 * potenciaTiro;
        double tempo = distanciaInimigo / velocidadeBala;

        double direcaoAtualRad = Math.toRadians(direcaoInimigo);
        double direcaoAnteriorRad = Math.toRadians(direcaoInimigoAnterior);
        double deltaDirecao = direcaoAtualRad - direcaoAnteriorRad;
        double velocidadeAngular = deltaDirecao / tempo;

        double posicaoFuturaX = posicaoInimigoX + Math.sin(direcaoAtualRad + velocidadeAngular * tempo) * velocidadeInimigo * tempo;
        double posicaoFuturaY = posicaoInimigoY + Math.cos(direcaoAtualRad + velocidadeAngular * tempo) * velocidadeInimigo * tempo;

        double anguloAbsoluto = Math.toDegrees(Math.atan2(posicaoFuturaX - getX(), posicaoFuturaY - getY()));
        setTurnGunRight(normalizarAngulo(anguloAbsoluto - getGunHeading()));

        direcaoInimigoAnterior = e.getHeading();
    }

    private void atirarInteligente() {
        boolean armaPronta = getGunHeat() == 0;
        boolean miraAjustada = Math.abs(getGunTurnRemaining()) < 5;
        boolean distanciaIdeal = distanciaInimigo >= DISTANCIA_MINIMA - 50 && distanciaInimigo <= DISTANCIA_MAXIMA + 50;

        if (armaPronta && miraAjustada && distanciaIdeal) {
            double potenciaTiro = Math.min(2.5, Math.max(1.5, getEnergy() * FATOR_POTENCIA_TIRO));
            fire(potenciaTiro);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (e.getPower() > 1.5) {
            moverComSeguranca(-120);
            direcaoMovimento = -1;
        } else {
            moverComSeguranca(120);
            direcaoMovimento = 1;
        }

        // Vira para se reposicionar lateralmente ao tiro
        setTurnRight(normalizarAngulo(90 - e.getBearing()));
    }

    public void onBulletHit(BulletHitEvent e) {
        direcaoMovimento = -1;
        moverComSeguranca(80);
    }

    public void onHitWall(HitWallEvent e) {
        moverComSeguranca(-100);
        setTurnRight(180 + e.getBearing());
        direcaoMovimento = direcaoMovimento * -1;
    }

    // Normaliza o ângulo para o intervalo [-180, 180]
    private double normalizarAngulo(double angulo) {
        while (angulo > 180) {
            angulo -= 360;
        }
        while (angulo < -180) {
            angulo += 360;
        }
        return angulo;
    }

    // Movimento com verificação de segurança (evita paredes)
    private void moverComSeguranca(double distancia) {
        double anguloRad = Math.toRadians(getHeading());
        double proximaPosX = getX() + Math.sin(anguloRad) * distancia;
        double proximaPosY = getY() + Math.cos(anguloRad) * distancia;

        boolean muitoPertoParede = (
            proximaPosX < MARGEM_PAREDE ||
            proximaPosX > getBattleFieldWidth() - MARGEM_PAREDE ||
            proximaPosY < MARGEM_PAREDE ||
            proximaPosY > getBattleFieldHeight() - MARGEM_PAREDE
        );

        if (muitoPertoParede) {
            // Se estiver perto da parede, vira para longe
            setTurnRight(90);
            return;
        }

        if (distancia > 0) {
            setAhead(distancia);
        } else {
            setBack(-distancia);
        }
    }
}
