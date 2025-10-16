import robocode.*;
import robocode.util.Utils;
import java.awt.*;
import java.awt.geom.*;
import java.lang.*;
import java.util.*;
import java.awt.Color;

public class C3_BO extends AdvancedRobot {
    
    // ------------------------ Wave Surfing (Evasao) ------------------------
    //Tecnica de evasao que preve onde a onda de tiro de um inimigo vai atingir e move o robo para o local mais seguro, com base em dados historicos.

    //O número de 'bins' (segmentos) usados para armazenar estatisticas de onde os tiros do inimigo nos atingiram.
    public static int BINS = 51;
    // O array _surfStats armazena as estatisticas de "perigo" para cada bin
    // Quanto maior o valor, mais perigoso é se mover para aquele bin
    public static double _surfStats[] = new double[BINS];
    // Nossas localizacões e a do inimigo sao armazenadas
    public Point2D.Double _myLocation;
    public Point2D.Double _enemyLocation;
    // As ondas de tiro inimigas em andamento. Cada onda é uma bala que o inimigo atirou e esta se aproximando
    public ArrayList<EnemyWave> _enemyWaves;
    // Listas para armazenar a direcao lateral (para tras ou para frente) e o ângulo absoluto do inimigo em relacao ao robo
    public ArrayList<Integer> _surfDirections;
    public ArrayList<Double> _surfAbsBearings;
    // A energia do inimigo é rastreada para detectar quando ele atira
    public static double _oppEnergy = 100.0;

    // ------------------------ GFTargetingBot (Mira) ------------------------
    // Tecnica de mira que preve a futura posiçao de um inimigo com base em seu movimento e estatisticas de tiro.

    // A potencia da bala usada para atirar
    private static final double BULLET_POWER = 1.9;
    // A direçao lateral do inimigo, usada para prever seu movimento
    private static double lateralDirection;
    // A velocidade do inimigo no tick anterior
    private static double lastEnemyVelocity;
    // Uma instância da classe GFTMovement para lidar com a movimentaçao
    private static GFTMovement movement;
    
    // Construtor do robo. Inicializa a classe de movimento
    public C3_BO() {
        movement = new GFTMovement(this);
    }

    //Retangulo que representa um campo de batalha 800x600, usado para um método simples de WallSmoothing, que garamte que o robo nao fique preso na parede
    public static Rectangle2D.Double _fieldRect
        = new java.awt.geom.Rectangle2D.Double(18, 18, 764, 564);
    // A distancia que o robo tenta manter das paredes
    public static double WALL_STICK = 160;

    // Método principal
    public void run() {

		setBodyColor(new Color(204, 195, 105));
	    setGunColor(new Color(204, 195, 105));
	    setRadarColor(new Color(204, 195, 105));
	    setBulletColor(new Color(204, 195, 105));
	    setScanColor(new Color(204, 195, 105));

        // Inicializa as listas de ondas inimigas, direções e ângulos para o Wave Surfing
        _enemyWaves = new ArrayList<>();
        _surfDirections = new ArrayList<>();
        _surfAbsBearings = new ArrayList<>();

        // Inicializa as variáveis para o GFTargeting
        lateralDirection = 1;
        lastEnemyVelocity = 0;
        // Permite que o radar e o canhao girem de forma independente do corpo do robo
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        // Loop infinito para girar o radar continuamente, procurando por inimigos
        do {
            turnRadarRightRadians(Double.POSITIVE_INFINITY);
        } while (true);
    }


    //onScannedRobot: O que fazer quando você vê outro robo
    public void onScannedRobot(ScannedRobotEvent e) {
        // ------------------------Wave Surfing------------------------
        // Atualiza a localizaçao no campo
        _myLocation = new Point2D.Double(getX(), getY());
        // Calcula a velocidade lateral para determinar direçao de movimento
        double lateralVelocity = getVelocity() * Math.sin(e.getBearingRadians());
        // Calcula o ângulo absoluto para o inimigo.
        double absBearing = e.getBearingRadians() + getHeadingRadians();

        // Gira o radar para travar no inimigo, mantendo na mira
        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
            - getRadarHeadingRadians()) * 2);

        // Armazena a direçao lateral e o ângulo absoluto do inimigo para o rastreamento das ondas de tiro
        _surfDirections.add(0,
            Integer.valueOf((lateralVelocity >= 0) ? 1 : -1));
        _surfAbsBearings.add(0, Double.valueOf(absBearing + Math.PI));

        // Detecta quando o inimigo atirou, com base na queda de sua energia
        double bulletPower = _oppEnergy - e.getEnergy();
        if (bulletPower < 3.01 && bulletPower > 0.09
            && _surfDirections.size() > 2) {
            // Se um tiro foi detectado, cria uma nova "EnemyWave"
            EnemyWave ew = new EnemyWave();
            ew.fireTime = getTime() - 1;
            ew.bulletVelocity = bulletVelocity(bulletPower);
            ew.distanceTraveled = bulletVelocity(bulletPower);
            ew.direction = _surfDirections.get(2);
            ew.directAngle = _surfAbsBearings.get(2);
            ew.fireLocation = (Point2D.Double)_enemyLocation.clone();
            // Adiciona a nova onda à lista de ondas ativas
            _enemyWaves.add(ew);
        }

        // Atualiza a energia do inimigo para o próximo tick
        _oppEnergy = e.getEnergy();

        // Atualiza a localizaçao do inimigo para que seja usada para a detecçao da próxima onda
        _enemyLocation = project(_myLocation, absBearing, e.getDistance());

        // Processa as ondas de tiro existentes
        updateWaves();
        // Executa a lógica de evasao do Wave Surfing.
        doSurfing();

        // ------------------------ GFTargeting------------------------
        double enemyAbsoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double enemyDistance = e.getDistance();
        double enemyVelocity = e.getVelocity();
        // Determina a direçao lateral do inimigo (para frente ou para trás)
        if (enemyVelocity != 0) {
            lateralDirection = GFTUtils.sign(enemyVelocity * Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
        }
        // Cria uma nova "onda" de tiro do robo
        GFTWave wave = new GFTWave(this);
        wave.gunLocation = new Point2D.Double(getX(), getY());
        GFTWave.targetLocation = GFTUtils.project(wave.gunLocation, enemyAbsoluteBearing, enemyDistance);
        wave.lateralDirection = lateralDirection;
        wave.bulletPower = BULLET_POWER;
        // Segmenta os dados de acordo com a distancia e velocidade para buscar as estatisticas de mira corretas
        wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
        lastEnemyVelocity = enemyVelocity;
        wave.bearing = enemyAbsoluteBearing;
        // Gira o canhao para o ponto de mira mais provável, com base nas estatisticas
        setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
        // Atira com a potência definida
        setFire(wave.bulletPower);
        // Adiciona a onda de tiro como um evento personalizado para rastrear o tiro
        if (getEnergy() >= BULLET_POWER) {
            addCustomEvent(wave);
        }
        // Executa a lógica de movimento do GFTMovement
        movement.onScannedRobot(e);
        // Mantém o radar travado no inimigo
        setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing - getRadarHeadingRadians()) * 2);
    }

    // Atualiza a distancia percorrida por cada onda inimiga
    public void updateWaves() {
        for (java.util.Iterator<EnemyWave> it = _enemyWaves.iterator(); it.hasNext();) {
            EnemyWave ew = it.next();
            ew.distanceTraveled = (getTime() - ew.fireTime) * ew.bulletVelocity;
            if (ew.distanceTraveled > _myLocation.distance(ew.fireLocation) + 50) {
                it.remove(); // Remove a onda da lista de forma segura
            }
        }
    }

    // Encontra a onda de tiro inimiga mais próxima
    public EnemyWave getClosestSurfableWave() {
        double closestDistance = 50000;
        EnemyWave surfWave = null;

        // Itera sobre todas as ondas inimigas ativas
        for (int x = 0; x < _enemyWaves.size(); x++) {
            EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
            // Calcula a distancia restante para a bala atingir
            double distance = _myLocation.distance(ew.fireLocation)
                - ew.distanceTraveled;

            // Se a bala está se aproximando e é a mais próxima, seleciona
            if (distance > ew.bulletVelocity && distance < closestDistance) {
                surfWave = ew;
                closestDistance = distance;
            }
        }

        return surfWave;
    }

    // Calcula o índice do bin estatístico para um determinado local de impacto
    public static int getFactorIndex(EnemyWave ew, Point2D.Double targetLocation) {
        // Calcula o ângulo de desvio da bala
        double offsetAngle = (absoluteBearing(ew.fireLocation, targetLocation)
            - ew.directAngle);
        double factor = Utils.normalRelativeAngle(offsetAngle)
            / maxEscapeAngle(ew.bulletVelocity) * ew.direction;

        // Retorna o índice do bin correspondente
        return (int)limit(0,
            (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
            BINS - 1);
    }

    // Atualiza o array de estatisticas quando somos atingidos
    public void logHit(EnemyWave ew, Point2D.Double targetLocation) {
        int index = getFactorIndex(ew, targetLocation);

        // Adiciona um valor aos bins de perigo, com maior peso no bin
        // onde fomos atingidos e menor peso nos bins adjacentes
        for (int x = 0; x < BINS; x++) {
            _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
        }
    }

    //onHitByBullet: O que fazer quando você é atingido por uma bala
    public void onHitByBullet(HitByBulletEvent e) {
        // Se a lista de ondas nao estiver vazia, procura a onda que atingiu o robo
        if (!_enemyWaves.isEmpty()) {
            Point2D.Double hitBulletLocation = new Point2D.Double(
                e.getBullet().getX(), e.getBullet().getY());
            EnemyWave hitWave = null;

            for (int x = 0; x < _enemyWaves.size(); x++) {
                EnemyWave ew = (EnemyWave)_enemyWaves.get(x);

                // Encontra a onda que corresponde a bala 
                if (Math.abs(ew.distanceTraveled -
                    _myLocation.distance(ew.fireLocation)) < 50
                    && Math.abs(bulletVelocity(e.getBullet().getPower())
                        - ew.bulletVelocity) < 0.001) {
                    hitWave = ew;
                    break;
                }
            }

            // Se a onda for encontrada, registra o acerto e remove a onda da lista
            if (hitWave != null) {
                logHit(hitWave, hitBulletLocation);
                _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
            }
        }
    }
    

    //onHitWall: Recua um pouco quando bater em uma parede
    public void onHitWall(HitWallEvent e) {
        back(20);
    }

    // Preve a posiçao futura do robo para uma determinada onda e direçao
    public Point2D.Double predictPosition(EnemyWave surfWave, int direction) {
        Point2D.Double predictedPosition = (Point2D.Double)_myLocation.clone();
        double predictedVelocity = getVelocity();
        double predictedHeading = getHeadingRadians();
        double maxTurning, moveAngle, moveDir;

        int counter = 0;
        boolean intercepted = false;

        do {
            // Calcula o ângulo de movimento, incluindo a suavizaçao de parede
            moveAngle =
                wallSmoothing(predictedPosition, absoluteBearing(surfWave.fireLocation,
                predictedPosition) + (direction * (Math.PI/2)), direction)
                - predictedHeading;
            moveDir = 1;

            if(Math.cos(moveAngle) < 0) {
                moveAngle += Math.PI;
                moveDir = -1;
            }

            moveAngle = Utils.normalRelativeAngle(moveAngle);

            // Calcula a taxa máxima de rotaçao para o próximo tick
            maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
            predictedHeading = Utils.normalRelativeAngle(predictedHeading
                + limit(-maxTurning, moveAngle, maxTurning));

            // Simula a aceleraçao e desaceleraçao do robo
            predictedVelocity +=
                (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
            predictedVelocity = limit(-8, predictedVelocity, 8);

            // Calcula a nova posiçao prevista
            predictedPosition = project(predictedPosition, predictedHeading,
                predictedVelocity);

            counter++;

            // Verifica se a bala da onda irá nos atingir na posiçao prevista
            if (predictedPosition.distance(surfWave.fireLocation) <
                surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
                + surfWave.bulletVelocity) {
                intercepted = true;
            }
        } while(!intercepted && counter < 500);

        return predictedPosition;
    }

    // Calcula o perigo de se mover para uma determinada direçao
    public double checkDanger(EnemyWave surfWave, int direction) {
        // Preve nossa posiçao e obtém o índice do bin correspondente
        int index = getFactorIndex(surfWave,
            predictPosition(surfWave, direction));
        // Retorna a pontuaçao de perigo desse bin
        return _surfStats[index];
    }

    // Executa a lógica de evasao principal do Wave Surfing
    public void doSurfing() {
        // Encontra a onda mais próxima para surfar
        EnemyWave surfWave = getClosestSurfableWave();

        if (surfWave == null) { return; }

        // Calcula o perigo de se mover para a esquerda ou para a direita
        double dangerLeft = checkDanger(surfWave, -1);
        double dangerRight = checkDanger(surfWave, 1);

        // Escolhe a direçao com o menor perigo.
        double goAngle = absoluteBearing(surfWave.fireLocation, _myLocation);
        if (dangerLeft < dangerRight) {
            goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
        } else {
            goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
        }

        // Define a direçao de movimento do robo
        setBackAsFront(this, goAngle);
    }

    // Classe para representar uma onda de tiro inimiga
    class EnemyWave {
        Point2D.Double fireLocation; // Onde a bala foi atirada
        long fireTime; // O tempo em que a bala foi atirada
        double bulletVelocity, directAngle, distanceTraveled; // Velocidade, ângulo e distancia percorrida
        int direction; // A direçao lateral do inimigo quando atirou

        public EnemyWave() { }
    }

    // MÉTODOS AUXILIARES
    
    // Suaviza o movimento perto das paredes para evitar ficar preso
    public double wallSmoothing(Point2D.Double botLocation, double angle, int orientation) {
        while (!_fieldRect.contains(project(botLocation, angle, WALL_STICK))) {
            angle += orientation*0.05;
        }
        return angle;
    }

    // Projeta um ponto a partir de uma origem, ângulo e distancia
    public static Point2D.Double project(Point2D.Double sourceLocation,
        double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length,
            sourceLocation.y + Math.cos(angle) * length);
    }

    // Calcula o ângulo absoluto entre dois pontos
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }

    // Limita um valor a um intervalo (min, max)
    public static double limit(double min, double value, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // Calcula a velocidade da bala com base na potência
    public static double bulletVelocity(double power) {
        return (20.0 - (3.0*power));
    }

    // Calcula o ângulo máximo de fuga para uma determinada velocidade da bala
    public static double maxEscapeAngle(double velocity) {
        return Math.asin(8.0/velocity);
    }

    // Configura o robo para andar para trás ou para frente para alcançar um ângulo alvo
    public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
        double angle =
            Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
        if (Math.abs(angle) > (Math.PI/2)) {
            if (angle < 0) {
                robot.setTurnRightRadians(Math.PI + angle);
            } else {
                robot.setTurnLeftRadians(Math.PI - angle);
            }
            robot.setBack(100);
        } else {
            if (angle < 0) {
                robot.setTurnLeftRadians(-1*angle);
           } else {
                robot.setTurnRightRadians(angle);
           }
            robot.setAhead(100);
        }
    }

    // ------------------------ Classes de suporte para GFT ------------------------
    
    // Representa uma onda de tiro do robo
    class GFTWave extends Condition {
        // Localizaçao do alvo no momento do disparo
        static Point2D targetLocation;

        double bulletPower; // Potência da bala
        Point2D gunLocation; // Localizaçao do canhao
        double bearing; // O ângulo absoluto para o alvo
        double lateralDirection; // Direçao lateral do inimigo

        private static final double MAX_DISTANCE = 1000;
        private static final int DISTANCE_INDEXES = 5;
        private static final int VELOCITY_INDEXES = 5;
        private static final int BINS = 25; // Número de bins para as estatisticas
        private static final int MIDDLE_BIN = (BINS - 1) / 2; // O bin do meio
        private static final double MAX_ESCAPE_ANGLE = 0.7;
        private static final double BIN_WIDTH = MAX_ESCAPE_ANGLE / (double)MIDDLE_BIN;
        
        // Armazena as estatisticas de mira, segmentadas por distancia, velocidade e velocidade anterior
        private static int[][][][] statBuffers = new int[DISTANCE_INDEXES][VELOCITY_INDEXES][VELOCITY_INDEXES][BINS];

        private int[] buffer; // O buffer de estatisticas atual
        private AdvancedRobot robot;
        private double distanceTraveled; // distancia percorrida pela bala
        
        GFTWave(AdvancedRobot _robot) {
            this.robot = _robot;
        }
        
        // O método test() é chamado a cada tick e verifica se a bala já atingiu seu alvo
        public boolean test() {
            advance();
            if (hasArrived()) {
                // Se a bala chegou ao alvo, incrementa o bin estatístico e remove o evento
                buffer[currentBin()]++;
                robot.removeCustomEvent(this);
            }
            return false;
        }

        // Retorna o desvio de mira para o bin mais visitado
        double mostVisitedBearingOffset() {
            return (lateralDirection * BIN_WIDTH) * (mostVisitedBin() - MIDDLE_BIN);
        }
        
        // Define o buffer de estatisticas com base na segmentaçao atual
        void setSegmentations(double distance, double velocity, double lastVelocity) {
            int distanceIndex = (int)(distance / (MAX_DISTANCE / DISTANCE_INDEXES));
            int velocityIndex = (int)Math.abs(velocity / 2);
            int lastVelocityIndex = (int)Math.abs(lastVelocity / 2);
            buffer = statBuffers[distanceIndex][velocityIndex][lastVelocityIndex];
        }

        // Avanca a distancia percorrida pela bala
        private void advance() {
            distanceTraveled += GFTUtils.bulletVelocity(bulletPower);
        }

        // Verifica se a bala já chegou ao alvo
        private boolean hasArrived() {
            return distanceTraveled > gunLocation.distance(targetLocation) - 18;
        }
        
        // Retorna o bin atual, com base no ângulo para o alvo
        private int currentBin() {
            int bin = (int)Math.round(((Utils.normalRelativeAngle(GFTUtils.absoluteBearing(gunLocation, targetLocation) - bearing)) /
                (lateralDirection * BIN_WIDTH)) + MIDDLE_BIN);
            return GFTUtils.minMax(bin, 0, BINS - 1);
        }
        
        // Encontra o bin com o maior número de acertos
        private int mostVisitedBin() {
            int mostVisited = MIDDLE_BIN;
            for (int i = 0; i < BINS; i++) {
                if (buffer[i] > buffer[mostVisited]) {
                    mostVisited = i;
                }
            }
            return mostVisited;
        }
    }

    // Classe de utilidade com metodos estaticos para calculos
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

    // Lógica de movimento para o GFT
    class GFTMovement {
        private static final double BATTLE_FIELD_WIDTH = 800;
        private static final double BATTLE_FIELD_HEIGHT = 600;
        private static final double WALL_MARGIN = 18;
        private static final double MAX_TRIES = 125;
        private static final double REVERSE_TUNER = 0.421075;
        private static final double DEFAULT_EVASION = 1.2;
        private static final double WALL_BOUNCE_TUNER = 0.699484;

        private AdvancedRobot robot;
        // O retangulo do campo de batalha para deteccao de parede
        private Rectangle2D fieldRectangle = new Rectangle2D.Double(WALL_MARGIN, WALL_MARGIN,
            BATTLE_FIELD_WIDTH - WALL_MARGIN * 2, BATTLE_FIELD_HEIGHT - WALL_MARGIN * 2);
        private double enemyFirePower = 3;
        private double direction = 0.4;

        GFTMovement(AdvancedRobot _robot) {
            this.robot = _robot;
        }
        
        // Executa a lógica de movimento quando um inimigo é escaneado
        public void onScannedRobot(ScannedRobotEvent e) {
            double enemyAbsoluteBearing = robot.getHeadingRadians() + e.getBearingRadians();
            double enemyDistance = e.getDistance();
            Point2D robotLocation = new Point2D.Double(robot.getX(), robot.getY());
            Point2D enemyLocation = GFTUtils.project(robotLocation, enemyAbsoluteBearing, enemyDistance);
            Point2D robotDestination;
            double tries = 0;
            // Tenta encontrar um destino seguro para se mover, longe da linha de fogo
            while (!fieldRectangle.contains(robotDestination = GFTUtils.project(enemyLocation, enemyAbsoluteBearing + Math.PI + direction,
                enemyDistance * (DEFAULT_EVASION - tries / 100.0))) && tries < MAX_TRIES) {
                tries++;
            }
            // Decide se deve reverter a direcao com base na distancia e nas paredes
            if ((Math.random() < (GFTUtils.bulletVelocity(enemyFirePower) / REVERSE_TUNER) / enemyDistance ||
                tries > (enemyDistance / GFTUtils.bulletVelocity(enemyFirePower) / WALL_BOUNCE_TUNER))) {
                direction = -direction;
            }
            // Define o movimento do robo para o destino calculado
            double angle = GFTUtils.absoluteBearing(robotLocation, robotDestination) - robot.getHeadingRadians();
            robot.setAhead(Math.cos(angle) * 100);
            robot.setTurnRightRadians(Math.tan(angle));
        }
    }
}