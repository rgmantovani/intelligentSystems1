package MkIII;
//importando a biblioteca robocode e as classes necessarias
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import robocode.util.*;

public class MkIII extends RateControlRobot {
    /**
     * run: executado quando o round for iniciado
     */
    public void run() {
        //Definindo cor amarelo para o corpo do robo
        setBodyColor(Color.yellow);
        //Definindo cor amarelo para a arma do robo
        setGunColor(Color.yellow);
        //Definindo cor amarelo para o radar do robo
        setRadarColor(Color.yellow);
        //Definindo cor amarelo para as balas disparadas pelo robo
        setBulletColor(Color.yellow);
        //Definindo cor amarelo para a varredura do radar
        setScanColor(Color.yellow);
        // Definindo posição inicial e direção do robo
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        // Dando vida ao robo em um loop infinito
        while (true) {
            setVelocityRate(5); // definindo a velocidade do robo
            setTurnRateRadians(0); //definindo a velocidade de rotacao do robo
            execute(); //executando a acao definida pelos comandos acima
            turnRadarRight(360); //girando o radar para a direita em 360 graus
        }
    }
    /**
     * onScannedRobot: Executado quando o radar encontra um robo.
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Define a potência do tiro como sendo o mínimo entre 3 e a energia atual do robô.        
        double potenciaDoTiro = Math.min(3, getEnergy());
        // Obtém a distância entre o robô e o inimigo, em radianos.
        double distancia = getHeadingRadians() + e.getBearingRadians();
        // Calcula as coordenadas x e y do inimigo.
        double inimigoX = getX() + e.getDistance() * Math.sin(distancia);
        double inimigoY = getY() + e.getDistance() * Math.cos(distancia);
        // Obtém a posição e a velocidade atual do inimigo.
        double posicaoDoInimigo = e.getHeadingRadians();
        double velocidadeDoInimigo = e.getVelocity();
        // Obtém a altura e a largura do campo de batalha.
        double altDoCampDeBatalha = getBattleFieldHeight(),larDoCampDeBatalha = getBattleFieldWidth();
        double previsaoX = inimigoX, previsaoY = inimigoY;
        //Previsao das coordendas do inimigo
        previsaoX += Math.sin(posicaoDoInimigo) * velocidadeDoInimigo;
        previsaoY += Math.cos(posicaoDoInimigo) * velocidadeDoInimigo;
        //se prever o inimigo indo para fora da arena recalcula novas coordendas
        if (previsaoX < 18.0 || previsaoY < 18.0 || previsaoX > larDoCampDeBatalha - 18.0 || previsaoY > altDoCampDeBatalha - 18.0) {
            previsaoX = Math.min(Math.max(18.0, previsaoX), larDoCampDeBatalha - 18.0);
            previsaoY = Math.min(Math.max(18.0, previsaoY), altDoCampDeBatalha - 18.0);
        }
        //calculamos o angulo absoluto entre a posição atual e a prosição prevista,
        //abs normaliza o angulo para ficar entre 0 e 2Pi
        double anguloAbsoluto = Utils.normalAbsoluteAngle(
            Math.atan2(
                previsaoX - getX(), previsaoY - getY()
            )
        );
        /*
         * Nesta seção do código, o robô define os movimentos do radar e da arma,
         *  com base na distância e posição prevista do inimigo. Primeiro,
         *  o robô gira metade da distância para a esquerda ou para a direita em relação à direção do radar atual. Em seguida,
         *  o robô gira o radar para a posição em que o inimigo é esperado. 
         * Por fim,o robô gira a arma em direção à posição absoluta do inimigo.
        */
        setTurnRightRadians(distancia / 2 * - 1 - getRadarHeadingRadians());
        setTurnRadarRightRadians(Utils.normalRelativeAngle(distancia - getRadarHeadingRadians()));
        setTurnGunRightRadians(Utils.normalRelativeAngle(anguloAbsoluto - getGunHeadingRadians()));
        fire(potenciaDoTiro);
        
        // uma estrutura de controle para controlar a velocidade e a taxa de rotação do robo
        if (getVelocityRate() > 0){//se a velocidade atual for maior que zero aumentamos a vlocidade
            setVelocityRate(getVelocityRate() + 1);
        } 
        else {//se não decrementamos a velocidade
            setVelocityRate(- 1);
        }
        //verifica se esta girando na direção correta em relação o inimigo se não inverte.
        if (getVelocityRate() > 0 && ((getTurnRate() < 0 && distancia > 0) || (getTurnRate() > 0 && distancia < 0))) {
            setTurnRate(getTurnRate() * -1);
        }
    }
    /**
     * onHitByBullet: É executado quando o robô leva um tiro.
     */
    public void onHitByBullet(HitByBulletEvent e) {
        double giroDoRadar = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getRadarHeading());
        setTurnRadarRight(giroDoRadar);
        setTurnLeft(-3);
        setTurnRate(3);
        setVelocityRate(-1 * getVelocityRate());
    }
    /**
     * onHitWall: É executado quando o robô colide com a parede.
     */
	public void onHitWall(HitWallEvent e) {
	    // Obtenha o rumo atual e direção do robô
	    double currentBearing = getHeadingRadians() + e.getBearingRadians();
	    double currentHeading = getHeadingRadians();
	
	    // Calcule a distância do robô até a parede mais próxima
	    double distanceToWall = Math.min(Math.min(getX(), getBattleFieldWidth() - getX()), Math.min(getY(), getBattleFieldHeight() - getY()));
	
	
        //Calcule a nova direção longe da parede
	    double awayFromWall = currentBearing + Math.PI;
	
	    // Defina a nova velocidade e taxa de giro com base nos valores calculados
	    setVelocityRate(-2 * getVelocityRate());
	    setTurnRate(getTurnRate() + (int) Math.toDegrees(Utils.normalRelativeAngle(awayFromWall - currentHeading)));
	    execute();
	}

    /**
     * onHitRobot: É executado quando o robô colide em outro robô.
     */
    public void onHitRobot(HitRobotEvent e) {
        //calculamos o ângulo que o canhão deve girar para apontar para o robô que foi atingido
        double giroDoCanhao = normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
        //gira o canhao para o robo atingido
        turnGunRight(giroDoCanhao);
        //atira com potencia fixa de 3.5
        setFire(3.5);
        //aumentamos a velocidade para afastar do robo atingido
        setVelocityRate(getVelocity() + 3);
        //garante que as ações do robo sejam executadas imediatamente
        execute();
    }

}