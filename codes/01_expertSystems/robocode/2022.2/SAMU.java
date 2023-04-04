/**
* @file SAMU.java
* @authors Anderson Silva, Adalberto Guedes, Mariana Rodrigues, Bruno Keller
* @date 02 Set 2022
* @brief  Um advanced robot inspirado no comportamento dos sampleRobots Walls e Tracker
*/
package upa;
import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import java.awt.*;

public class SAMU extends AdvancedRobot {
	int motionDirection = 1;//direcao do movimento (frente tras)
	double edgeMovement; // movimento necessario para alcançar a borda]
	int aux = 0;
	boolean atirei = false;

	public void run() {
		setColors(Color.white,Color.red,Color.red); // Corpo X Arma Y Radar Z
		setBulletColor(Color.red);//cor do projetil
		edgeMovement = Math.max(getBattleFieldWidth(), getBattleFieldHeight()); //get no tamanho do campo de batalha
	if(getOthers() > 2){
		turnLeft(getHeading() % 90); // vira para a esquerda para ir para a parede
		ahead(edgeMovement); //de fato, corre até a parede
		turnGunRight(90);// vira a arma para deixar ela sempre apontada para dentro do campo de batalha
		turnRight(90);//vira o robo junto com a arma (nesse ponto o robo esta a uma diferença de 90 graus da direcao arma)
	}

	while (true) {
		if(getOthers() < 3)//caso so existam dois inimigos no campo, o modo walls será desabilitado 
			break;
		ahead(edgeMovement);//depois de espiar, esta autorizado correr para a proxima borda do campo
		turnRight(90);//ao chegar na proxima borda, preciso virar 90 graus para continuar no sentido horário percorrendo as paredes
	}
	//modo tracker ativado
	setAdjustRadarForRobotTurn(true);//mantem o radar estavel quando o robo virar para qualquer direcao		
	setAdjustGunForRobotTurn(true);  //mantem a arma estavel quando o robo virar para qualquer direcao	
	turnRadarRightRadians(Double.POSITIVE_INFINITY);//matem o radar virando para a direita
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double angleObject = e.getBearingRadians() + getHeadingRadians();//angulo absoluto entre meu robo e o inimigo
		double enemyVel = e.getVelocity() * Math.sin(e.getHeadingRadians() - angleObject);//determina velocidade do inimigo
		if(getOthers() < 3){//quando o modo tracker esta ativado
			inCorner();//caso eu esteja em alguma quina          
			double turnCannon = 0.0;//o quanto eu devo virar minha arma
			setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//trava o radar em algum inimigo scaneado
			aimBot(turnCannon, angleObject, enemyVel, motionDirection, e);//funcao de mira inteligente
			atirei = true;
			shoot(e);//atiro
			atirei = false;
		}else{//quando o modo walls esta ativado
			double enPosX = getX() + e.getDistance() * Math.sin(angleObject);//posicao do inimigo no eixo x
			double enPosY = getY() + e.getDistance() * Math.cos(angleObject);//posicao do inimigo no eixo y
			double enemyHeading = e.getHeadingRadians();
			intelAim(enPosX, enPosY, enemyHeading, enemyVel, angleObject);//funcao de mira inteligente
			atirei = true;
			shoot(e);
			atirei = false;
			setAhead((e.getDistance() - 140) * motionDirection);//faz um "pendulo" para atirar e desviar
		}
	}

	public void onHitWall(HitWallEvent e){//quando o modo tracker esta ativado
		if(getOthers() < 3){
			aux++;
			motionDirection=-motionDirection;//direcao oposta caso eu colida com a parede
			if(aux == 10){
				clearAllEvents();//limpa eventos pendentes
				aux = 0;
			}
		}
	}
	public void onHitByBullet(HitByBulletEvent e){
		if(getOthers() > 2 && !atirei){
			if (e.getBearing() > -90 && e.getBearing() < 90) //se o inimigo esta na nossa frente, va para tras um pouco
				back(100);          
			else // se o inimigo esta atras de nos, va para frente um pouco
				ahead(100);
			System.out.println("tomei tiro e nao dei");
		}
	}
		public void onHitRobot(HitRobotEvent e) {//quando o modo walls esta ativado
		if(getOthers() > 2){
			if (e.getBearing() > -90 && e.getBearing() < 90) //se o inimigo esta na nossa frente, va para tras um pouco
				back(100);          
			else // se o inimigo esta atras de nos, va para frente um pouco
				ahead(100);
		}else{
			if (e.getBearing() > -90 && e.getBearing() < 90) //se o inimigo esta na nossa frente, va para tras um pouco
				back(100);          
			else // se o inimigo esta atras de nos, va para frente um pouco
				ahead(100);
		}
	}

	public void shoot(ScannedRobotEvent e) {//funcao que determina a forma correta de atirar
		if(e.getDistance() < 800){//caso a distancia do inimigo scaneado seja menor que 800 eu posso atirar
			double firePower = decideFirePower(e);//define a potencia do tiro na funcao decideFirePower	
			if(e.getVelocity() == 0){
				fireBullet(firePower);
			}else{
				fire(firePower);//atira com a potencia dinamica
			}
		}
	}

	public double decideFirePower(ScannedRobotEvent e){//funcao que determina a potencia do tiro de acordo com a distancia e energia
		double firePower = getOthers() == 1 ? 3.0 : 2.0;//se houver um inimigo a potencia e 3, senao e 2
		if(e.getDistance() > 400){//caso a distancia seja maior que 400
			firePower = 1.0;//potencia passa a ser 1
		}else if(e.getDistance() < 200) {//caso a distancia seja menor que 200
			firePower = 3.0;//a potencia passa a ser 3
		}
		if(getEnergy() < 1){//caso a minha energia seja menor que 1
			firePower = 0.1;//potencia passa a ser 0.1 para economizar energia
		}else if(getEnergy() < 10){//caso minha energia seja menor que 10
			firePower = 1.0;//potencia passa a ser 1
		}
		return Math.max(e.getEnergy() / 4, firePower);//a potencia do tiro será definida pelo maior valor entre minha energia dividida por 4 e a potencia calculada acima
	}

	public void inCorner(){//funcao que determina oq fazer caso eu fique nos cornes no modo tracker
		if((getX() == 0 && getY() == getBattleFieldHeight()) || 
		(getX() == getBattleFieldWidth() && getY() == 0) ||
		(getX() == getBattleFieldWidth() && getY() == getBattleFieldHeight() ||
		(getX() == 0 && getY() == 0))){//na condicao desse if está definido os cantos 
			turnLeft(getHeading() % 45);//vou virar 45 graus em relacao a direcao do meu robo
			setAhead(getBattleFieldWidth() / 2);//vou para frente 
		}
	}

	public void aimBot(double turnCannon, double angleObject, double enemyVel, int motionDirection, ScannedRobotEvent e){//funcao de mira inteligente do traker
		if(Math.random()>.9)
			setMaxVelocity((12 * Math.random()) + 12);//mudança de velocidade randomica
		if (e.getDistance() > 150) {//caso a distancia seja maior que 150
			turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 22);//o quanto eu devo virar minha arma
			setTurnGunRightRadians(turnCannon);//vira a minha arma
			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(angleObject - getHeadingRadians() + enemyVel / getVelocity()));//anda para a posicao futura prevista do inimigo
			setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
		}else{//caso eu nao esteja perto o suficiente
			turnCannon = robocode.util.Utils.normalRelativeAngle(angleObject - getGunHeadingRadians() + enemyVel / 15);//o quanto eu devo virar minha arma
			setTurnGunRightRadians(turnCannon);//vira a minha arma
			setTurnLeft(-90 - e.getBearing()); //viro perpendicular ao meu inimigo scaneado
			setAhead((e.getDistance() - 140) * motionDirection);//corro para frente
		}
		
	}

	public void intelAim(double enPosX, double enPosY, double enemyHeading, double enemyVel, double angleObject){//funcao de mira inteligente do walls
		double bulletPower = Math.min(3.0,getEnergy());
		double myPosX = getX();//minha posicao no eixo x
		double myPosY = getY();//minha posicao no eixo y
		double delta = 0;
		double alturaCampo = getBattleFieldHeight(), larguraCampo = getBattleFieldWidth();//get largura e altura
		double prevX = enPosX, prevY = enPosY;//inicio as previsoes no eixo x e y com a posicao x e y do inimigo
		while((++delta) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(myPosX, myPosY, prevX, prevY)){	//point2d, distancia entre pontos 	
			prevX += Math.sin(enemyHeading) * enemyVel; //previsao de x do inimigo de acordo com sua velocidade 
			prevY += Math.cos(enemyHeading) * enemyVel; //previsao de y do inimigo de acordo com sua velocidade 
			if(prevX < 18.0 || prevY < 18.0 || prevX > larguraCampo - 18.0 || prevY > alturaCampo - 18.0){
				prevX = Math.min(Math.max(18.0, prevX), larguraCampo - 18.0);	//entre 18 e 982
				prevY = Math.min(Math.max(18.0, prevY), alturaCampo - 18.0);
				break;
			}
		}
		double alpha = Utils.normalAbsoluteAngle(Math.atan2(prevX - getX(), prevY - getY())); //retorna o valor do angulo em radianos entre x e y
		setTurnRadarRightRadians(Utils.normalRelativeAngle(angleObject - getRadarHeadingRadians())); // mirar o radar no inimigo
		setTurnGunRightRadians(Utils.normalRelativeAngle(alpha - getGunHeadingRadians())); //mirar a arma no inimigo
	}
	
	public void onWin(WinEvent e) { //span emotes
		for (int i = 0; i < 50; i++) {
			turnRight(30);
			turnLeft(30);
		}
	}
}
