/* 
	Felipe Franscico Lorusso
	João Pedro Neigri Heleno - 2270323 

*/

package Atom;

import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
import java.lang.*;
import java.util.*;
import java.util.ArrayList;
import java.awt.Color;
import robocode.Robot;
import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Atom extends AdvancedRobot {
	// Variáveis principais utilizadas no robô
		public static int CAIXAS = 47; // Número de divisões para cálculos de esquiva
		public static double evitar[] = new double[CAIXAS]; // Array para armazenar valores de esquiva
		public Point2D.Double localRobo;  // Coordenadas da posição atual do robô
		public Point2D.Double localInimigo;  // Coordenadas da posição do inimigo detectado
		public ArrayList ondasInimigo = new ArrayList(); // Armazena as ondas de radar do inimigo
		public ArrayList dEsquiva = new ArrayList(); // Armazena distâncias de esquiva
		public ArrayList evitarRolamentoAbs = new ArrayList(); // Valores de esquiva com referência absoluta
		private static double valorBala = 1.7; // Potência padrão da bala
		private static double direcao_Lateral; // Direção lateral para manobras evasivas
		private static double velocidadeUltimoInimigo; // Última velocidade detectada do inimigo
		public static double energia_inimigo = 100.0; // Energia restante do inimigo
		public static Rectangle2D.Double mapa; // Representação retangular do mapa para cálculos de movimento
		static Point2D.Double alvoInimigo; // Ponto de destino previsto para o inimigo
		public double distanciaInimigo; // Distância até o inimigo detectado


    public void run() {
    
		setBodyColor(new Color(0, 0, 0));
	    setGunColor(new Color(0,  0, 0));
	    setRadarColor(new Color(0, 0, 0));
	    setBulletColor(new Color(0, 0, 0));
	    setScanColor(new Color(0, 0, 0));

		// Configurações de movimento e radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Inicializando variáveis
        direcao_Lateral = 1;
		velocidadeUltimoInimigo = 0;

		// Definindo o limite do mapa com retângulo gerado
		mapa = new java.awt.geom.Rectangle2D.Double(17.9, 17.9, getBattleFieldWidth() - 35.8, getBattleFieldHeight() - 35.8);
        alvoInimigo = new Point2D.Double(getBattleFieldWidth() / 2,
				getBattleFieldHeight() / 2);

        // Inicializa os utilitários
        Utility.initRound(this);

		// Ativação do radar
        do {
    		Utility.update();
    
    		// Faz o radar girar continuamente para escanear o campo
		    Tracker.doRadar(this);
		} while (true);

    }

	public void onScannedRobot(ScannedRobotEvent e) {
		
		Features scannedRobot;
		scannedRobot = Utility.getEnemy(e.getName());
		scannedRobot.update(this, e);

		if (Utility.getCurrentTarget() == null || (Utility.getTargetRating(scannedRobot) < Utility.getTargetRating(Utility.getCurrentTarget()) && getGunHeat()/getGunCoolingRate() > 4)){
			Utility.setCurrentTarget(scannedRobot);
		}

		if (scannedRobot == Utility.getCurrentTarget()){ 
			double power = Utility.getPower(scannedRobot);
			valorBala = power;
		} 

		//Esquiva
        localRobo = new Point2D.Double(getX(), getY()); // Definindo a localização do boneco
		
        double velLateral = getVelocity()*Math.sin(e.getBearingRadians());
        double absRolamento = e.getBearingRadians() + getHeadingRadians();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(absRolamento - getRadarHeadingRadians()) * 2);

        dEsquiva.add(0, new Integer((velLateral >= 0) ? 1 : -1));
        evitarRolamentoAbs.add(0, new Double(absRolamento + Math.PI));


        double poderDeFogoIni = energia_inimigo - e.getEnergy(); // Através do gasto de energia do inimigo é possível verificar o poder de fogo da bala
        if (poderDeFogoIni < 3.01 && poderDeFogoIni > 0.09 && dEsquiva.size() > 2) {
            OndaInimiga oi = new OndaInimiga();
            oi.tempoDisparo = getTime() - 1;
            oi.velDisparo = velDisparo(poderDeFogoIni); // Velocidade da bala disparada
            oi.distPercorrida = velDisparo(poderDeFogoIni); // Distância percorrida do disparo inimigo
            oi.direcaoDispInimigo = ((Integer)dEsquiva.get(2)).intValue(); // Direção do disparo inimigo
            oi.angDireto = ((Double)evitarRolamentoAbs.get(2)).doubleValue(); // Angulo do disparo inimigo
            oi.localDisparo = (Point2D.Double)localInimigo.clone(); // Local de onde o inimigo disparou a bala

            ondasInimigo.add(oi);
        }

        energia_inimigo = e.getEnergy(); // Reset do valor da energia do inimigo para realizar o calculo do poder de fogo novamente

        localInimigo = project(localRobo, absRolamento, e.getDistance()); // Atualização da localização do inimigo após a detecção de uma onda inimiga

		//Chamada dos métodos de execução
        atualizaOndas(); 
        evitar();

    
        double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
		double enemyDistance = e.getDistance();
        distanciaInimigo = enemyDistance;
		double enemyVelocity = e.getVelocity();
		if (enemyVelocity != 0) {
			direcao_Lateral = GFTUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
		}

		// Trackeia o inimigo para atirar
		GFTWave wave = new GFTWave(this);
		wave.locArma = new Point2D.Double(getX(), getY());
		GFTWave.targetLocation = GFTUtils.project(wave.locArma, enemyAbsoluteBearing, enemyDistance);
		wave.direcao_Lateral = direcao_Lateral;
		wave.valorBala = valorBala;
		wave.setSegmentations(enemyDistance, enemyVelocity, velocidadeUltimoInimigo);
		velocidadeUltimoInimigo = enemyVelocity;
		wave.bearing = enemyAbsoluteBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
     	
		if(getEnergy() > (valorBala + 0.1)){// Atira
			setFire(wave.valorBala);
        }
		if (getEnergy() >= valorBala) { // Verifica se acertou
			addCustomEvent(wave);
		}
        

		if(e.getDistance() > 200 && e.getEnergy() > 1.9){// Matem distancia do inimigo
			scan();
			if(e.getDistance() < 100){
				setBack(50);
			}
		}
		
    }

	public void atualizaOndas() {
        for (int x = 0; x < ondasInimigo.size(); x++) {
            OndaInimiga oi = (OndaInimiga)ondasInimigo.get(x);

            oi.distPercorrida = (getTime() - oi.tempoDisparo) * oi.velDisparo;
            if (oi.distPercorrida > localRobo.distance(oi.localDisparo) + 50) {
                ondasInimigo.remove(x);
                x--;
            }
        }
    }

	public OndaInimiga getPossEsquiva() {
        double menorDistancia = 50000; // Declarando um número exagerado
        OndaInimiga evitarr = null;

        for (int x = 0; x < ondasInimigo.size(); x++) {
            OndaInimiga oi = (OndaInimiga)ondasInimigo.get(x);
            double distance = localRobo.distance(oi.localDisparo) - oi.distPercorrida;

            if (distance > oi.velDisparo && distance < menorDistancia) {
                evitarr = oi;
                menorDistancia = distance;
            }
        }
        return evitarr;
    }

	public static int getFatorIndex(OndaInimiga oi, Point2D.Double localAlvo) {
        double angOffset = (rumoAbs(oi.localDisparo, localAlvo) - oi.angDireto);
        double fator = Utils.normalRelativeAngle(angOffset) / angDesvioMax(oi.velDisparo) * oi.direcaoDispInimigo;

        return (int)limite(0, (fator * ((CAIXAS - 1) / 2)) + ((CAIXAS - 1) / 2), CAIXAS - 1);
    }

	public void logDanoRecebido(OndaInimiga oi, Point2D.Double localAlvo) {
        int index = getFatorIndex(oi, localAlvo);

        for (int x = 0; x < CAIXAS; x++) {
            evitar[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

	public void onHitByBullet(HitByBulletEvent e) {
        if (!ondasInimigo.isEmpty()) {
            Point2D.Double localDano = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
            OndaInimiga ondaRecebida = null;

            // Procura por OndaInimiga até encontrar alguma que pode atingir
            for (int x = 0; x < ondasInimigo.size(); x++) {
                OndaInimiga oi = (OndaInimiga)ondasInimigo.get(x);

                if (Math.abs(oi.distPercorrida - localRobo.distance(oi.localDisparo)) < 50 && Math.abs(velDisparo(e.getBullet().getPower()) - oi.velDisparo) < 0.001) {
                    ondaRecebida = oi;
                    break;
                }
            }

            if (ondaRecebida != null) {
                logDanoRecebido(ondaRecebida, localDano);
                ondasInimigo.remove(ondasInimigo.lastIndexOf(ondaRecebida)); // Remove a onda já recebida
            }

			double estimatedDistance = (200 / (20 - 3 * e.getBullet().getPower())) * e.getBullet().getPower();
			if (distanciaInimigo > estimatedDistance){
				scan();
				execute();
			}
        }
		
		Features tempEnemy = Utility.getEnemy(e.getName());
		tempEnemy.logHit(e.getTime());
		tempEnemy.energy += e.getPower()*3;
    }

	
	public void onBulletHit(BulletHitEvent e){
		double tempPower = e.getBullet().getPower();
		double tempDamage = Math.max(4*tempPower, 6*tempPower-2);
		Features tempEnemy = Utility.getEnemy(e.getName());
		tempEnemy.energy -= tempDamage;
	}


	public Point2D.Double prevendoFuturo(OndaInimiga evitarr, int direcao) {
    	Point2D.Double posicaoPrevista = (Point2D.Double)localRobo.clone();
    	double velocidadePrevista = getVelocity();
    	double preveRumo = getHeadingRadians();
    	double giroMax, moverAng, moverDirecao;

        int contador = 0; 
        boolean interceptado = false;

    	do {
    		moverAng = paredeSuavizada(posicaoPrevista, rumoAbs(evitarr.localDisparo, posicaoPrevista) + (direcao * (Math.PI/2)), direcao)- preveRumo;
    		moverDirecao = 1;

    		if(Math.cos(moverAng) < 0) {
    			moverAng += Math.PI;
    			moverDirecao = -1;
    		}

    		moverAng = Utils.normalRelativeAngle(moverAng);

    		giroMax = Math.PI/720d*(40d - 3d*Math.abs(velocidadePrevista));
    		preveRumo = Utils.normalRelativeAngle(preveRumo + limite(-giroMax, moverAng, giroMax));

    		
    		velocidadePrevista += (velocidadePrevista * moverDirecao < 0 ? 2*moverDirecao : moverDirecao);
    		velocidadePrevista = limite(-8, velocidadePrevista, 8);

    		
    		posicaoPrevista = project(posicaoPrevista, preveRumo, velocidadePrevista);

            contador++;

            if (posicaoPrevista.distance(evitarr.localDisparo) <
                evitarr.distPercorrida + (contador * evitarr.velDisparo)
                + evitarr.velDisparo) {
                interceptado = true;
            }
    	} while(!interceptado && contador < 500);

    	return posicaoPrevista;
    }

	public double verPerigo(OndaInimiga evitarr, int direcao) {
        int index = getFatorIndex(evitarr, prevendoFuturo(evitarr, direcao));

        return evitar[index];
    }

	public void evitar() {
        OndaInimiga evitarr = getPossEsquiva();

        if (evitarr == null) { return; }

        double perigoEsquerda = verPerigo(evitarr, -1);
        double perigoDireita = verPerigo(evitarr, 1);

        double pAng = rumoAbs(evitarr.localDisparo, localRobo);
        if (perigoEsquerda < perigoDireita) {
            pAng = paredeSuavizada(localRobo, pAng - (Math.PI/2), -1);
        } else {
            pAng = paredeSuavizada(localRobo, pAng + (Math.PI/2), 1);
        }

        virarFrenteTras(this, pAng);
    }

    class OndaInimiga {
        Point2D.Double localDisparo;
        long tempoDisparo;
        double velDisparo, angDireto, distPercorrida;
        int direcaoDispInimigo;

        public OndaInimiga() { }
    }

	public double paredeSuavizada(Point2D.Double rLocal, double ang, int ori) {
        while (!mapa.contains(project(rLocal, ang, 160))) {
            ang += ori * 0.05;
        }
        return ang;
    }

	public static Point2D.Double project(Point2D.Double sourceLocation, double ang, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(ang) * length,
            sourceLocation.y + Math.cos(ang) * length);
    }

    public static double rumoAbs(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    public static double limite(double min, double valor, double max) {
        return Math.max(min, Math.min(valor, max));
    }

    public static double velDisparo(double poder) {
        return (20D - (3D*poder));
    }

    public static double angDesvioMax(double velocidade) {
        return Math.asin(8.0/velocidade);
    }

	//Método para usar as costas do boneco como parte frontal
	public static void virarFrenteTras(AdvancedRobot boneco, double pAng) {
        double ang = Utils.normalRelativeAngle(pAng - boneco.getHeadingRadians());
        if (Math.abs(ang) > (Math.PI/2)) {
            if (ang < 0) {
                boneco.setTurnRightRadians(Math.PI + ang);
            } else {
                boneco.setTurnLeftRadians(Math.PI - ang);
            }
            boneco.setBack(100);
        } else {
            if (ang < 0) {
                boneco.setTurnLeftRadians(-1*ang);
           } else {
                boneco.setTurnRightRadians(ang);
           }
            boneco.setAhead(100);
        }
    } 

    private double angRelativoInimigo(double heading) {
		return Utils.normalRelativeAngle(Math.atan2(alvoInimigo.x - getX(), alvoInimigo.y
				- getY())
				- heading);
	}

    private static int sign(double v) {
		return v > 0 ? 1 : -1;
	}
	
	public void onDeath(DeathEvent e){
		Utility.uninitRound();
	}

	public void onWin(WinEvent e){
		Utility.uninitRound();
	}

	public void onRobotDeath(RobotDeathEvent e){
		Utility.robotDeath(e.getName());
	}
}

class GFTWave extends Condition {
	static Point2D targetLocation;

	double valorBala;
	Point2D locArma;
	double bearing;
	double direcao_Lateral;

	private static final double MAX_DISTANCE = 900;
	private static final int DISTANCE_INDEXES = 5;
	private static final int VELOCITY_INDEXES = 5;
	private static final int CAIXAS = 25;
	private static final int MIDDLE_BIN = (CAIXAS - 1) / 2;
	private static final double MAX_ESCAPE_ANGLE = 0.7;
	private static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;
	
	private static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][CAIXAS];

	private int[] buffer;
	private AdvancedRobot robot;
	private double distanceTraveled;
	
	GFTWave(AdvancedRobot _robot) {
		this.robot = _robot;
	}
	
	public boolean test() {
		advance();
		if (hasArrived()) {
			buffer[currentBin()]++;
			robot.removeCustomEvent(this);
		}
		return false;
	}

	double mostVisitedBearingOffset() {
		return (direcao_Lateral * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
	}
	
	void setSegmentations(double distance, double velocity, double lastVelocity) {
		int distanceIndex = Math.min(DISTANCE_INDEXES-1, (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES)));
		int velocityIndex = (int)Math.abs(velocity / 2);
		int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
		buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
	}

	private void advance() {
		distanceTraveled += GFTUtils.bulletVelocity(valorBala);
	}

	private boolean hasArrived() {
		return distanceTraveled > locArma.distance(targetLocation) - 18;
	}
	
	private int currentBin() {
		int bin = (int)Math.round(((Utils.normalRelativeAngle(GFTUtils.absoluteBearing(locArma, targetLocation) - bearing)) /
				(direcao_Lateral * BIN_WIDTH)) + MIDDLE_BIN);
		return GFTUtils.minMax(bin, 0, CAIXAS - 1);
	}
	
	private int mostVisitedBin() {
		int mostVisited = MIDDLE_BIN;
		for (int i = 0; i < CAIXAS; i++) {
			if (buffer[i] > buffer[mostVisited]) {
				mostVisited = i;
			}
		}
		return mostVisited;
	}	
}

class GFTUtils {
	static double bulletVelocity(double power) {
		return 20 - 3 * power;
	}
	
	static Point2D project(Point2D sourceLocation, double angle, double length) {
		return new Point2D.Double(sourceLocation.getX() + Math.sin(angle) * length,
				sourceLocation.getY() + Math.cos(angle) * length);
	}
	
	static double absoluteBearing(Point2D source, Point2D target) {
		return Math.atan2(target.getX() - source.getX(), target.getY() - source.getY());
	}

	static int sign(double v) {
		return v < 0 ? -1 : 1;
	}
	
	static int minMax(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}
}

class Utility {
    private static Point2D myLocation;
    private static AdvancedRobot robot;
    private static Features currentTarget;
    private static HashMap enemies;

    public static void initRound(AdvancedRobot r){
		robot = r;
        enemies = new HashMap();
		currentTarget = null;
		myLocation = new Point2D.Double();
		update();
	}

    public static void uninitRound(){
		enemies.clear();
	}

    public static Features getCurrentTarget(){
		return currentTarget;
	}

    public static void setCurrentTarget(Features e){
		currentTarget = e;
	}

    public static Point2D projectPoint(Point2D startPoint, double theta, double dist){
		return new Point2D.Double(startPoint.getX() + dist * Math.sin(theta), startPoint.getY() + dist * Math.cos(theta));
	}

    public static Point2D getMyLocation(){
		return myLocation;
	}

    public static Features getEnemy(String name){
		if (!enemies.containsKey(name))
			enemies.put(name, new Features());
		return (Features)enemies.get(name);
	}

    public static double normalize(double angle){
		return robocode.util.Utils.normalRelativeAngle(angle);
	}

	public static double angle(Point2D point2, Point2D point1){
		return Math.atan2(point2.getX()-point1.getX(), point2.getY()-point1.getY());
	}

    public static void update(){
		myLocation.setLocation(robot.getX(), robot.getY());
	}

    public static double getTargetRating(Features e){
		return e.distance(myLocation) - e.energy;
	}

    public static double getPower(Features target){
		double power = Math.min(robot.getEnergy()/5, 3);
		if (target.energy < 16) {
			double powerToFinish = Math.min(target.energy/4, (target.energy+2)/6);
			if (target.distance(myLocation) < 300){
				double targetEnergy = target.energy + target.lastBulletPower*3*Math.min(1, (1.5-target.distance(myLocation)/200));
				powerToFinish = Math.min(targetEnergy/4, (targetEnergy+2)/6);
			}
			power = Math.min(power, powerToFinish);
		} 
        power = Math.min(power, 1200/target.distance(myLocation));
		return power;
	}

    public static void robotDeath(String name){
		if (enemies.containsKey(name)){
			if (enemies.remove(name) == currentTarget){
                currentTarget = null;
            }
        }
	}
}

class Features extends Point2D.Double{

    public double distance, bearing, heading, velocity, velocityChange, energy, lastBulletPower, lastMoveDirection;
	public long lastHitTime, lastScanTime, lastScanDelay, lastNewMovementTime, lastShotTime;
	public boolean firstUpdate;
	public String name;
	public Point2D lastMyLocation;
	public Features currentTarget;

    public Features() {
		lastMoveDirection = 1;
		lastBulletPower = 3;
		firstUpdate = true;
		energy = 100;
	}

    public void update(AdvancedRobot robot, ScannedRobotEvent e){
		name = e.getName();
		double energydiff = energy-e.getEnergy();
		energy = e.getEnergy();
		if (energydiff > .0999 && energydiff <= 3)
		{
			lastShotTime = e.getTime()-1;
			lastBulletPower = energydiff;
		}
		// Atualiza informações do estado atual do inimigo, incluindo posição e energia
		setLocation(Utility.projectPoint(Utility.getMyLocation(), robot.getHeadingRadians() + e.getBearingRadians(), e.getDistance()));
		lastScanDelay = e.getTime() - lastScanTime;
		lastScanTime = e.getTime();
		velocityChange = e.getVelocity()-velocity;
		if (Math.abs(velocityChange) >= .5)
			lastNewMovementTime = e.getTime();
		velocity = e.getVelocity();
		heading = e.getHeadingRadians();
		bearing = e.getBearingRadians();
		distance = e.getDistance();
		if (velocity != 0)
		{
			lastMoveDirection = (velocity < 0) ? -1 : 1;
		}
	}

    public void update(AdvancedRobot robot){
		name = robot.getName();
		double energydiff = energy-robot.getEnergy();
		energy = robot.getEnergy();
		if (energydiff > .0999 && energydiff <= 3)
		{
			lastShotTime = robot.getTime()-1;
			lastBulletPower = energydiff;
		}
		// Atualiza informações do estado atual do inimigo, incluindo posição e energia
		setLocation(robot.getX(), robot.getY());
		lastScanDelay = robot.getTime() - lastScanTime;
		lastScanTime = robot.getTime();
		velocityChange = robot.getVelocity()-velocity;
		if (Math.abs(velocityChange) >= .5)
			lastNewMovementTime = robot.getTime();
		velocity = robot.getVelocity();
		heading = robot.getHeadingRadians();
		if (velocity != 0)
		{
			lastMoveDirection = (velocity < 0) ? -1 : 1;
		}
	}

	public void logHit(long time){
		lastHitTime = time;
	}
}

class Tracker {
    private static double radarDirection = 1;

    public static void doRadar(AdvancedRobot robot) {
        // Mantém o radar girando para monitoramento constante do inimigo
        robot.turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }

    public static void onScannedRobot(AdvancedRobot robot, ScannedRobotEvent e) {
        // Atualiza a posição do robô e calcula a localização do inimigo com base na última detecção
        Point2D.Double myLocation = new Point2D.Double(robot.getX(), robot.getY());

        double absBearing = e.getBearingRadians() + robot.getHeadingRadians();
        double enemyDistance = e.getDistance();

        double enemyX = myLocation.x + Math.sin(absBearing) * enemyDistance;
        double enemyY = myLocation.y + Math.cos(absBearing) * enemyDistance;
        Point2D.Double enemyLocation = new Point2D.Double(enemyX, enemyY);

        // Ajusta a direção do radar com base na posição relativa e movimento do inimigo
        double radarTurn = Utils.normalRelativeAngle(absBearing - robot.getRadarHeadingRadians());
        radarTurn += (radarTurn >= 0 ? Math.PI / 8 : -Math.PI / 8);
        robot.setTurnRadarRightRadians(radarTurn);
    }
}