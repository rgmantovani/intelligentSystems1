package fazuele;

import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import robocode.util.Utils;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Yoshi extends AdvancedRobot {
    // Variáveis para controle do comportamento do robô
    public double distancia = 0;           // Armazena a distância até o alvo
    public int contDirection = 0;          // Conta as mudanças de direção
    public long moveTime = 1;              // Tempo restante antes da próxima mudança de direção
    public static int direcaoMove = 1;     // Direção do movimento (1 para frente, -1 para trás)
    public double randomDistParede = 120;  // Distância mínima do robô até as paredes

    @Override
    public void run() {
        // Configuração inicial das cores do robô
        setBodyColor(Color.green);
        setGunColor(Color.white);
        setRadarColor(Color.white);

        // Ajustes para que o radar, a arma e o corpo possam girar independentemente
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Loop principal do robô
        while (true) {
            // Gira o radar continuamente se não houver detecção de alvo
            if (getRadarTurnRemaining() == 0)
                setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            execute();  // Executa ações pendentes
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Processa a detecção de um robô inimigo
        double anguloAlvo = e.getBearingRadians() + getHeadingRadians();  // Calcula o ângulo do alvo
        double distanciaAlvo = e.getDistance() + (Math.random() - 0.5) * 5; // Distância ao alvo com pequena variação aleatória
        double orientacaoRadar = anguloAlvo / 2;  // Calcula uma orientação do radar em relação ao alvo

        // Ajusta o radar e o robô para focar no inimigo
        setTurnRadarRightRadians(Utils.normalRelativeAngle(anguloAlvo - getRadarHeadingRadians()));
        setTurnRightRadians(-orientacaoRadar - getRadarHeadingRadians());

        // Controle de movimentação para evitar tiros previsíveis
        if (--moveTime <= 0) {  
            // Atualiza a distância e o tempo de movimento restante
            distanciaAlvo = Math.max(distanciaAlvo, 100 + Math.random() * 50) * 1.25;
            moveTime = 50 + (long)(distanciaAlvo / 15); // 15 como velocidade média para cálculo
            ++contDirection;

            // Alterna a direção de movimento em intervalos aleatórios ou após várias iterações
            if (Math.random() < 0.5 || contDirection > 16) {
                direcaoMove = -direcaoMove;
                contDirection = 0;
            }
        }

        // Direção desejada do movimento em relação ao inimigo
        double direcaoDesejada = anguloAlvo - Math.PI / 2 * direcaoMove;
        direcaoDesejada += (Math.random() - 0.5) * (Math.random() * 2 + 1);  // Pequena variação aleatória para evitar previsibilidade

        // Coordenadas atuais do robô
        double x = getX();
        double y = getY();

        // Define uma área de segurança, distante das paredes
        double desvioDirecao = 0;  // Contador de ajustes de direção
        Rectangle2D areaCampo = new Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
        
        // Ajusta a direção enquanto estiver fora dos limites seguros
        while (!areaCampo.contains(x + Math.sin(direcaoDesejada) * randomDistParede, y + Math.cos(direcaoDesejada) * randomDistParede)) {
            direcaoDesejada += direcaoMove * 0.1;
            desvioDirecao += 0.1;
        }

        // Se o ajuste de direção for significativo, inverte o sentido do movimento
        if (desvioDirecao > 0.5 + Math.random() * 0.125) {
            direcaoMove = -direcaoMove;
            contDirection = 0;
        }

        // Calcula o ângulo de rotação necessário para alcançar a direção desejada
        double girarYoshi = Utils.normalRelativeAngle(direcaoDesejada - getHeadingRadians());

        // Decide se o robô deve ir para frente ou para trás, dependendo do ângulo
        if (Math.abs(girarYoshi) > Math.PI / 2) {
            girarYoshi = Utils.normalRelativeAngle(girarYoshi + Math.PI);
            setBack(100);  // Movimenta para trás
        } else {
            setAhead(100);  // Movimenta para frente
        }

        setTurnRightRadians(girarYoshi);  // Gira o robô na direção calculada

        // Lógica para disparo da bala
        double balaSpeed = 20 - 3 * 1 + Math.random() * 2;  // Calcula a velocidade da bala considerando potência variável
        double velLatIni = e.getVelocity() * Math.sin(e.getHeadingRadians() - anguloAlvo);  // Calcula a velocidade lateral do inimigo
        double anguloFuga = Math.asin(8 / balaSpeed);  // Ângulo de fuga do alvo em função da velocidade da bala
		double miraAleatorio = (Math.random() * 0.2) - 0.1;  // Variação aleatória para o ângulo de tiro

        // Ajusta a mira e dispara de acordo com a distância
        setTurnGunRightRadians(Utils.normalRelativeAngle(anguloAlvo + miraAleatorio - getGunHeadingRadians()));
        distancia = e.getDistance();

        if (getEnergy() > 2) {  // Verifica se há energia suficiente para disparar
            if (distancia < 100) {
                setBack(50);    // Recuo estratégico para distância curta
                fire(3);        // Disparo de alta potência
            } else if (distancia >= 100 && distancia < 300) {
                setFire(2);     // Disparo médio
            } else if (distancia >= 300 && distancia < 600){
                setFire(1);     // Disparo de baixa potência
            } else {
				setFire(0.3);  // Disparo mínimo para alvos distantes
			}
        }
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Reação ao ser atingido por um tiro
        setBack(100);  // Movimento de recuo
        double anguloAlvo = e.getBearingRadians() + getHeadingRadians();  // Calcula o ângulo do atirador
        setTurnRightRadians(anguloAlvo);  // Gira para evitar mais tiros
        setTurnGunRightRadians(anguloAlvo);  // Mira no atirador
        setFire(2);  // Responde com disparo de potência média
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        // Reage ao colidir com uma parede
        direcaoMove *= -1;  // Inverte a direção do movimento
        setTurnRight(180);    // Gira 180 graus
        ahead(100);           // Avança para sair da parede
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        // Reage ao colidir com outro robô
        double orientacaoInimigo = event.getBearing() + getHeading();  // Calcula a orientação do inimigo
        double girar = normalRelativeAngleDegrees(orientacaoInimigo - getGunHeading());  // Ajusta a mira
        turnGunRight(girar);  // Gira a arma para o inimigo
        fire(3);  // Disparo de alta potência
    }
}
