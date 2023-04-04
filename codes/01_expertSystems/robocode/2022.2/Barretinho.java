package cs;
import robocode.*;
import java.awt.*;
import robocode.util.Utils;


public class Barretinho extends AdvancedRobot{

	int X = Integer.MIN_VALUE; //Variavel para armazenar as coordenadas do inimigo
	int Y = Integer.MIN_VALUE;
	
	public void run() {
	
		
		
		setBodyColor(new Color(255, 0, 0)); //Mudar a cor do bicho
		setGunColor(new Color(0, 0, 0));
		setRadarColor(new Color(255, 255, 255));
		setBulletColor(new Color(240, 240, 65));
		setScanColor(new Color(212, 212, 212));
		
		setAdjustRadarForGunTurn(false); //O radar vai ser dependente da arma, ira rodar junto com a arma
		
		turnGunRightRadians(Double.POSITIVE_INFINITY); //Arma vai rodar o infinito do double
		
	}
	


	public void onSkippedTurn(SkippedTurnEvent e){ //Funcao help caso o robo perca a referencia ele ira re-scannear
		turnGunRightRadians(Double.POSITIVE_INFINITY);
	}
	


	public void onScannedRobot(ScannedRobotEvent e) { //Evento caso o radar localiza um robo
	
	
		setTurnGunRight(Double.POSITIVE_INFINITY * Utils.normalRelativeAngleDegrees(getHeading() + e.getBearing() - getRadarHeading()));//Radar ira oscilar entre a posicao do inimigo a fim de manter ele sempre lockado!


		double angle = Math.toRadians((getHeading() + e.getBearing()) % 360); //Pega do angulo inimigo, em referencia ao nosso robo.
		X = (int)(getX() + Math.sin(angle) * e.getDistance()); //Cacula as cordenadas para o inimigo
    	Y = (int)(getY() + Math.cos(angle) * e.getDistance());
		

		goTo(X,Y); //Funcao para deslocar o robo ate o inimigo
			
		if (e.getDistance() < 100){ //Caso a distancia do robo inimigo seja menor que 100 pixels o robo ira dar um tiro forte, caso seja entre 100 e 300 um tiro medio
			//setBack(50); //Volta atras quando esta muito perto do inimigo, no battle-royale acaba sendo melhor pois desvia das balas perdidas
			fire(Rules.MAX_BULLET_POWER);
		}else if (e.getDistance() >= 100 && e.getDistance() < 300){
			fire(Rules.MAX_BULLET_POWER/4);
		}
	}
		
	private void goTo(int x, int y) { //Funcao deslocamento robo
    	double a; //Variavel para pegar o angulo de destino
	    setTurnRightRadians(Math.tan(a = Math.atan2(x -= (int) getX(), y -= (int) getY()) - getHeadingRadians())); //Ira girar o robo de acordo com o angulo de sua posicao de destino
	    setAhead(Math.hypot(x, y) * Math.cos(a)); //Ira se deslocar ate o destino
	}	
}
