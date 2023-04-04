
package gangue;
import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.util.*;



public class Walle extends AdvancedRobot {

    double pi = Math.PI;
    double inimigoDistancia;
    double inimigoDirecao;
    double inimigoEnergia;
	boolean atirei = false;
	int deslocamento = 1;
    double cancelarTiroForca = 0;
	int controle=1;
	 double movimentoLateral;



    public void run() {
        
		setBodyColor(new Color(163,86,26));
		setGunColor(new Color(189,189,189));
		setRadarColor(new Color(51,56,69));
		setScanColor(new Color(255,0,0));
		setBulletColor(new Color(163,86,26));
		
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
		


		
        while (true) {
            turnRadarRightRadians(Double.POSITIVE_INFINITY); 
			execute();
        }
    }
	
    public void onScannedRobot(ScannedRobotEvent e) {
	
        inimigoDistancia = e.getDistance();
        inimigoDirecao = getHeadingRadians() + e.getBearingRadians();
        inimigoEnergia = e.getEnergy();
		double inimigoAngulo = e.getBearingRadians() + getHeadingRadians();
		double inimigoVelocidade = e.getVelocity() * Math.sin(e.getHeadingRadians() - inimigoAngulo);

		
        //Move em um círculo em torno do inimigo
        setTurnRightRadians(e.getBearingRadians() + pi/2 - 0.1);
       // setAhead(inimigoDistancia/0.5);
		setTurnRadarLeftRadians(getRadarTurnRemainingRadians());
       	miraInteligente(inimigoAngulo, inimigoVelocidade, deslocamento, e);	
		atirei = true;
        atirar(e);
		atirei = false;	
    }


	public double otimizaTiro(ScannedRobotEvent e){
	

		double forcaTiro;
		
			if(getEnergy() < 30 || e.getDistance() > 300)
			forcaTiro = 2.6;
			else if(e.getDistance() > 250)
			forcaTiro = 3;
			else if(e.getDistance() > 150){
			forcaTiro = 1.5;//fire 2
			}else{
			forcaTiro = 2.5;//fire 2.5
			}
		//if(e.getDistance() > 400 && getEnergy() > 1){
		//	forcaTiro = 2;
		//}else if(e.getDistance() < 150 && getEnergy() > 10) {
		//	forcaTiro = 3.0;
		//}
		//if(getEnergy() < 1){//caso a minha energia seja menor que 1
		//	forcaTiro = 0.1;//potencia passa a ser 0.1 para economizar energia
		//}else if(getEnergy() < 10){//caso minha energia seja menor que 10
		//	forcaTiro = 1.0;//potencia passa a ser 1
		//}
		return Math.max(e.getEnergy() / 4, forcaTiro);
		}

	
    public void onHitWall(HitWallEvent e) {
 	deslocamento=-deslocamento;
	setTurnRight(movimentoLateral*controle);
	
	execute();
    }
	
	public void onHitRobot(HitRobotEvent e){
		deslocamento=-deslocamento;
		setTurnLeft(10);
		setBack(15);
		execute();
	}
	
	public void	onHitByBullet(HitByBulletEvent e){
		controle *=-1;
	}
     
    public void atirar(ScannedRobotEvent e) {
	
			double forcaTiro = otimizaTiro(e);
			if(e.getVelocity() == 0){
				fireBullet(forcaTiro);
			}else{
				fire(forcaTiro);
			}
		
	}
	
    public void onRobotDeath(RobotDeathEvent e) {

        setTurnGunRightRadians(Double.POSITIVE_INFINITY);
    }
	

	
public void miraInteligente(double inimigoAngulo, double inimigoVelocidade, int deslocamento, ScannedRobotEvent e) {
    double apontarArma = getGunHeadingRadians();
    double direcaoCorpo = getHeadingRadians();
    double velocidade = getVelocity();
    double distancia = e.getDistance();
    double inimigoDirecao = e.getBearing();

    if(Math.random() > 0.9) {
        double velocidadeMaxima = (12 * Math.random() + 12);
        setMaxVelocity(velocidadeMaxima);
    }

    if(distancia > 75) {
        double moveArma = Utils.normalRelativeAngle(inimigoAngulo - apontarArma + inimigoVelocidade / 11);
        setTurnGunRightRadians(moveArma);

        double mudaDirecao = Utils.normalRelativeAngle(inimigoAngulo - direcaoCorpo + inimigoVelocidade / velocidade);
        setTurnRightRadians(mudaDirecao);

        int distanciaFrontal = (int)((distancia - 160) * deslocamento);
        setAhead(distanciaFrontal);
    } else {
        double moveArma = Utils.normalRelativeAngle(inimigoAngulo - apontarArma + inimigoVelocidade / 15);
        setTurnGunRightRadians(moveArma);

        movimentoLateral = -90 - inimigoDirecao;
        setTurnLeft(movimentoLateral);

        int distanciaFrontal = (int)((distancia - 140) * deslocamento);
        setAhead(distanciaFrontal);
    }
}




	public void onWin(WinEvent e){
			System.out.println("WALL-E IS INVENCIBLE!");
	}
}