package sistemasInteligentes;

import robocode.*;
import java.awt.Color;


/**
 * Legolas - a class by (Angélica B. G. Luciano)
 */
public class Legolas extends AdvancedRobot {
	private boolean movimento = true;
	private boolean inimigoFracoDetectado = false; 

	public void run() {
		//cores
		setColors(Color.green, Color.yellow, Color.green, Color.black, Color.white);
		//movimentação
		while (true) {
			move();
			scan();
		}
	}
	
	public void move(){
		if(movimento){
			ataque();
		} else {
			fuga();
		}
	}
	public void ataque(){
		// Move-se aleatoriamente em uma direção
        setAhead(100);
        setTurnRight(45);
        execute();
	}
	
    public void fuga() {
        setBack(50);
		setTurnRight(30);
        
        // Se um inimigo fraco foi detectado, ataque-o
        if (inimigoFracoDetectado) {
			movimento=true;
			ataque();
        }
		execute();
    }


	//inimigo detectado
	public void onScannedRobot(ScannedRobotEvent e) {
		double distancia = e.getDistance();
		
		if(e.getEnergy() > getEnergy()){
			if(distancia<100){
				fire(3);
			}else{
				fire(1);
			}
			
			movimento = false;
			fuga();
		}else{
			movimento = true;
			inimigoFracoDetectado = true;
			turnRight(e.getBearing());
			//Verifique se o inimigo está fraco
			if (e.getEnergy() < 20 && distancia < 250){
				fire(3);
			}else{
				if(distancia < 350){
					fire(2);
				}else{
					fire(1);
				}
			}
			
		}
		execute();
	}


	//atingido 
	public void onHitByBullet(HitByBulletEvent e){
		//fugir de futuros tiros
		setTurnLeft(180 - e.getBearing());
		fire(3);
		execute();
		fuga();
	}

	public void onHitRobot(HitRobotEvent e){
		// Reverter o movimento para se afastar do robô adversário
    	setBack(50);
	    // Girar o robô para uma nova direção após a colisão
	    setTurnLeft(45);

	    execute();
	}
	
	//comemoração
	public void onWin(WinEvent e) {
        for (int i = 0; i < 100; i++) {
			
			for(int j = 0; j<2; j++){
				setBodyColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       		setGunColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       		setRadarColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       		setBulletColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
	       		setScanColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
			}
			turnRight(45);
            turnLeft(45);
        }
    }	

}
