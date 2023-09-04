package pacotinho;

// Membros:
// Ana Carolina Ribeiro Miranda
// Eiti Parruca Adama
// Lucas Eduardo Pires Parra

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
import robocode.Robot;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Miauzinho extends AdvancedRobot {
	
	public double distancia = 0;
	//Quantas vezes decidimos não mudar de direção
	public int contDirection = 0;
    // Por quanto tempo devemos continuar a avançar na direção atual 
    public long moveTime = 1;
    // A direção em que estamos nos movendo
    public static int moveDirection = 1;
	// A velocidade da última bala que nos atingiu, usada para determinar a distância a percorrer antes de decidir mudar de direção novamente.    
	public static double velocidadeBalinha = 15;
   	
	public double randomDistParede = 120;
	
	 public void run() {
	 
        while (true) {

       		// Ajustando configurações para o movimento e radar do robô
        	setAdjustGunForRobotTurn(true);
        	setAdjustRadarForGunTurn(true);
        	setAdjustRadarForRobotTurn(true);

           if (getRadarTurnRemaining() == 0)
       			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);  
				// Executando as ações
				execute();
		}  // while
    }  // run
	
    // Função Detecta robô inimigo
    public void onScannedRobot(ScannedRobotEvent e) {        

        if(e.getTime() % 13 == 0) {
			// corzinhas random
     		setBodyColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       	setGunColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       	setRadarColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       	setBulletColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       	setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
			// Mudar a distancia da parede pra deixar mais aleatorio ainda
           	randomDistParede = 120 + Math.random()*40;
		}//if
		
		// Calcula o ângulo absoluto do alvo em relação à orientação atual do robô
	    double anguloAlvo = e.getBearingRadians() + getHeadingRadians();
		// Calcula a distancia com a aleatoriedade de um número entre -2,5 e 2,5
        double distance = e.getDistance() + (Math.random()-0.5)*5; 
	
		// Calculo da distância angular do inimigo
       	double orientacaoInimigo = getHeadingRadians() + e.getBearingRadians();
		double orientacaoRadar = orientacaoInimigo/2;	
        
		// Calculo das coordenadas X e Y do inimigo
		double inimigoX = getX() + e.getDistance() * Math.sin(orientacaoInimigo);
       	double inimigoY = getY() + e.getDistance() * Math.cos(orientacaoInimigo);
		double inimigoX_real = inimigoX - getX();
		double inimigoY_real = inimigoY - getY();
		
		// Calculo do arco tangente do inimigo
		double arcTgIni = Math.atan2(inimigoX_real, inimigoY_real);
        
		// Controlar o radar do robo
		setTurnRadarRightRadians(Utils.normalRelativeAngle(orientacaoInimigo - getRadarHeadingRadians()));
        
		// Controlar a rotação do robo
		setTurnRightRadians(-orientacaoRadar - getRadarHeadingRadians());
 
		// Movimentar aleatoriamente
        if(--moveTime <= 0) {
			// Escolher uma nova distância de movimento aleatoriamente entre 100 e 150% da distância anterior
            distance = Math.max(distance, 100 + Math.random()*50) * 1.25; 
			// Atualiza o valor calculado com base na nova distância de movimento
            moveTime = 50 + (long)(distance / velocidadeBalinha); 
            ++contDirection;
			// Verifica se um número aleatório gerado é menor que 0.5 ou é maior que 16.
            if(Math.random() < 0.5 || contDirection > 16) {
                moveDirection = -moveDirection;
                contDirection = 0;
            }//if
        }//if
	
		// Calcula a direção do objetivo em relação à direção de movimento do robô e adiciona uma pequena variação aleatória a essa direção
		double desejadaDirection = anguloAlvo-Math.PI/2*moveDirection;
    	desejadaDirection += (Math.random()-0.5) * (Math.random()*2 + 1); //gera um valor aleatório entre -0,5 e 0,5 multiplica com um valor aleatório entre 1.0 e 3.0, ampliando a quantidade de variação.

		// Para contornar a parede sem bater
	 	double x = getX();
      	double y = getY();
       	double smooth = 0;
		
		// Cria um retângulo e verifica se o ponto calculado com base na direção desejada do robô está dentro desse retângulo.
        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
        while (!fieldRect.contains(x+Math.sin(desejadaDirection)*randomDistParede, y+ Math.cos(desejadaDirection)*randomDistParede)) {
        	// Vira pro inimigo e tenta dnovo
			desejadaDirection += moveDirection*0.1;
        	smooth += 0.1;
        }//while

        // Se for mt, inverte a direção
        if(smooth > 0.5 + Math.random()*0.125) {
            moveDirection = -moveDirection;
            contDirection = 0;
        }//if
		
		// Calcular a quantidade de rotação necessária para alinhar a direção atual do robô com a direção desejada 
        double viraVirou = Utils.normalRelativeAngle(desejadaDirection - getHeadingRadians());

        // Ajusta para ir para trás
        if (Math.abs(viraVirou) > Math.PI/2) {
            viraVirou = Utils.normalRelativeAngle(viraVirou + Math.PI);
            setBack(100);
        } else {
            setAhead(100);
        }//else

        setTurnRightRadians(viraVirou);

        // BANG BANG
       	double balaPower = 1 + Math.random()*2;
	   	double balaSpeed = 20 - 3 * balaPower; // Quanto maior a potência do tiro, menor será a velocidade do projétil.

		// Mira em um deslocamento aleatório na direção geral para a qual o inimigo está indo
        double velLatIni = e.getVelocity()*Math.sin(e.getHeadingRadians() - anguloAlvo);
        double anguloFuga = Math.asin(8 / balaSpeed);

        // Signum produz 0 se não estiver se movendo, o que significa que atiraremos diretamente de frente para um alvo imóvel
		double iniDirection = Math.signum(velLatIni);
        double angleOffset = anguloFuga * iniDirection * Math.random();
        setTurnGunRightRadians(Utils.normalRelativeAngle(anguloAlvo + angleOffset - getGunHeadingRadians()));

		distancia = e.getDistance();

		// Não atirar se isso for fazer ficarmos com 0 de energia
        if(getEnergy() > balaPower) {
  
		   	if (distancia < 100){ // De perto, força máxima
				setBack(50); //Volta atras quando esta muito perto do inimigo
				fire(3);
			}else if (distancia >= 100 && distancia < 300){  //Caso a distancia do robo inimigo seja menor que 100 pixels o robo ira dar um tiro forte, caso seja entre 100 e 300 um tiro médio
				setFire(2);
			}//else
			else{
				setFire(balaPower);
			}//else
        }//if
    } // onScannedRobot

    // Função para quando o robô é atingido
    public void onHitByBullet(HitByBulletEvent e) {
		
		// Calcula a distância estimada com base na potência do tiro
        double estimatedDistance = (400 / (20 - 3 * e.getBullet().getPower())) * e.getBullet().getPower();
		
		// Verifica se o robo agressor está mais perto que o robo que estamos atacando, se sim, mudamos de alvo
		if (distancia > estimatedDistance){
			scan();
		} //if

		execute();
    } // onHitByBullet
	
    // Função quando bate em outro robô, VEM X1 
    public void onHitRobot(HitRobotEvent e) {
        double orientacaoInimigo = e.getBearing() + getHeading();
        double girar = normalRelativeAngleDegrees(orientacaoInimigo - getGunHeading());
		
        turnGunRight(girar);
        fire(3);
		
        execute();
    } // onHitRobot

	// Dança da vitória
	public void onWin(WinEvent e) {
		System.out.println("GATOS VÃO DOMINAR O MUNDO!");
		for (int i = 0; i < 150; i++) {
			turnRight(30);
            turnLeft(30);
        }//for
	}//onWin

  } // Miauuu