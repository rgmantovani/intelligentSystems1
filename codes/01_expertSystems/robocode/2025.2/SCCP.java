package sccp;

import robocode.*;
import robocode.util.*;
import java.awt.Color;
import java.awt.geom.*;
import java.util.*;

/**
 * SCCP - Um robô avançado para Robocode com múltiplas estratégias
 * Implementa movimento inteligente, mira preditiva e radar otimizado
 */
public class SCCP extends AdvancedRobot {
    
    // Sistemas do robô
    private MovementSystem movement;
    private GunSystem gun;
    private RadarSystem radar;
    private EnemyTracker tracker;
    
    // Dados do inimigo
    private String currentTarget = "";
    
    // Configurações
    private long lastScanTime = 0;
    
    public void run() {
        // Inicializa sistemas
        movement = new MovementSystem(this);
        gun = new GunSystem(this);
        radar = new RadarSystem(this);
        tracker = new EnemyTracker(this);
        
        // Configurações iniciais
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);
        
        // Cores personalizadas
        setColors(Color.BLACK, Color.RED, Color.WHITE, Color.YELLOW, Color.ORANGE);
        
        // Loop principal
        while(true) {
            // Verifica se perdeu o alvo
            if (getTime() - lastScanTime > 10) {
                radar.searchMode();
            }
            
            movement.update();
            radar.update();
            execute();
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        lastScanTime = getTime();
        currentTarget = e.getName();
        
        // Atualiza dados do inimigo
        tracker.update(e);
        
        // Sistemas respondem ao scan
        radar.onScannedRobot(e);
        gun.onScannedRobot(e);
        movement.onScannedRobot(e);
    }
    
    public void onHitWall(HitWallEvent e) {
        movement.onHitWall(e);
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        movement.onHitByBullet(e);
    }
    
    public void onHitRobot(HitRobotEvent e) {
        movement.onHitRobot(e);
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
        gun.onBulletMissed(e);
    }
    
    public void onBulletHit(BulletHitEvent e) {
        gun.onBulletHit(e);
    }
}

/**
 * Sistema de Rastreamento de Inimigos
 */
class EnemyTracker {
    private AdvancedRobot robot;
    private HashMap<String, ArrayList<EnemySnapshot>> enemyHistory;
    private HashMap<String, String> enemyPatterns;
    
    public EnemyTracker(AdvancedRobot robot) {
        this.robot = robot;
        this.enemyHistory = new HashMap<String, ArrayList<EnemySnapshot>>();
        this.enemyPatterns = new HashMap<String, String>();
    }
    
    public void update(ScannedRobotEvent e) {
        String enemyName = e.getName();
        
        // Calcula posição absoluta do inimigo
        double enemyX = robot.getX() + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
        double enemyY = robot.getY() + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
        
        // Cria snapshot
        EnemySnapshot snapshot = new EnemySnapshot(
            enemyX, enemyY, e.getHeading(), e.getVelocity(), 
            robot.getTime(), e.getDistance(), e.getBearing()
        );
        
        // Adiciona ao histórico
        if (!enemyHistory.containsKey(enemyName)) {
            enemyHistory.put(enemyName, new ArrayList<EnemySnapshot>());
        }
        enemyHistory.get(enemyName).add(snapshot);
        
        // Mantém apenas dados recentes (últimos 50 scans)
        ArrayList<EnemySnapshot> history = enemyHistory.get(enemyName);
        if (history.size() > 50) {
            history.remove(0);
        }
        
        // Atualiza padrões
        updatePattern(enemyName, e);
    }
    
    private void updatePattern(String enemyName, ScannedRobotEvent e) {
        String pattern = String.format("%.0f,%.0f", e.getHeading(), e.getVelocity());
        enemyPatterns.put(enemyName, pattern);
    }
    
    public Point2D.Double getPredictedPosition(String enemyName, double bulletSpeed) {
        ArrayList<EnemySnapshot> history = enemyHistory.get(enemyName);
        if (history == null || history.isEmpty()) {
            return null;
        }
        
        EnemySnapshot latest = history.get(history.size() - 1);
        double time = latest.distance / bulletSpeed;
        
        // Predição linear simples
        double futureX = latest.x + latest.velocity * Math.sin(Math.toRadians(latest.heading)) * time;
        double futureY = latest.y + latest.velocity * Math.cos(Math.toRadians(latest.heading)) * time;
        
        return new Point2D.Double(futureX, futureY);
    }
    
    public EnemySnapshot getLatestSnapshot(String enemyName) {
        ArrayList<EnemySnapshot> history = enemyHistory.get(enemyName);
        if (history == null || history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }
}

/**
 * Dados de um snapshot do inimigo
 */
class EnemySnapshot {
    public double x, y, heading, velocity, distance, bearing;
    public long time;
    
    public EnemySnapshot(double x, double y, double heading, double velocity, 
                        long time, double distance, double bearing) {
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.velocity = velocity;
        this.time = time;
        this.distance = distance;
        this.bearing = bearing;
    }
}

/**
 * Sistema de Movimento Ultra-Robusto (ZERO TRAVAMENTOS)
 */
class MovementSystem {
    private AdvancedRobot robot;
    private double direction = 1;
    private Point2D.Double destination;

    // Variáveis para movimento imprevisível
    private Random random = new Random();
    private long nextStrategyChangeTime = 0;
    private int moveDistance = 120;
    
    // Sistema anti-travamento APRIMORADO
    private long lastWallHitTime = 0;
    private int consecutiveWallHits = 0;
    private boolean emergencyMode = false;
    private double lastX = 0, lastY = 0;
    private int stuckCounter = 0;
    private long emergencyStartTime = 0;
    
    // Histórico de posições para detecção mais precisa
    private ArrayList<Point2D.Double> positionHistory = new ArrayList<Point2D.Double>();
    private static final int HISTORY_SIZE = 8;
    
    // Sistema de "memória" de paredes problemáticas
    private ArrayList<Rectangle2D.Double> problematicAreas = new ArrayList<Rectangle2D.Double>();

    public MovementSystem(AdvancedRobot robot) {
        this.robot = robot;
        this.lastX = robot.getX();
        this.lastY = robot.getY();
    }

    public void update() {
        // Atualiza histórico de posições
        updatePositionHistory();
        
        // DETECÇÃO MÚLTIPLA DE TRAVAMENTO
        boolean isStuck = detectStuck();
        
        if (isStuck || emergencyMode) {
            executeAdvancedEmergencyEscape();
            return;
        }
        
        // Evita áreas problemáticas conhecidas
        if (isInProblematicArea()) {
            executeAreaEscape();
            return;
        }
        
        if (destination != null) {
            moveToPoint(destination);
        } else {
            // PREVENÇÃO PROATIVA
            if (willHitWall()) {
                executePreventiveManeuver();
            } else if (isNearWall()) {
                executeAdvancedWallSmoothing();
            } else {
                // Movimento padrão
                robot.setAhead(direction * moveDistance);
            }
        }
    }
    
    // ATUALIZA HISTÓRICO DE POSIÇÕES
    private void updatePositionHistory() {
        positionHistory.add(new Point2D.Double(robot.getX(), robot.getY()));
        if (positionHistory.size() > HISTORY_SIZE) {
            positionHistory.remove(0);
        }
    }
    
    // DETECÇÃO AVANÇADA DE TRAVAMENTO
    private boolean detectStuck() {
        if (positionHistory.size() < HISTORY_SIZE) {
            return false;
        }
        
        // Método 1: Movimento muito pequeno
        double distanceMoved = Point2D.distance(lastX, lastY, robot.getX(), robot.getY());
        if (distanceMoved < 1.5) {
            stuckCounter++;
        } else {
            stuckCounter = Math.max(0, stuckCounter - 1);
        }
        
        // Método 2: Oscilação no mesmo local
        boolean oscillating = isOscillating();
        
        // Método 3: Velocidade muito baixa por muito tempo
        boolean lowVelocity = Math.abs(robot.getVelocity()) < 0.5;
        
        // Se qualquer condição for verdadeira por tempo suficiente
        if (stuckCounter > 6 || oscillating || (lowVelocity && stuckCounter > 3)) {
            if (!emergencyMode) {
                emergencyMode = true;
                emergencyStartTime = robot.getTime();
                addProblematicArea();
            }
            return true;
        }
        
        return false;
    }
    
    // DETECTA SE ESTÁ OSCILANDO NO MESMO LUGAR
    private boolean isOscillating() {
        if (positionHistory.size() < HISTORY_SIZE) return false;
        
        Point2D.Double first = positionHistory.get(0);
        Point2D.Double last = positionHistory.get(positionHistory.size() - 1);
        
        // Se começou e terminou muito próximo
        double totalDistance = Point2D.distance(first.x, first.y, last.x, last.y);
        if (totalDistance < 15) {
            // Calcula distância total percorrida
            double pathLength = 0;
            for (int i = 1; i < positionHistory.size(); i++) {
                Point2D.Double prev = positionHistory.get(i-1);
                Point2D.Double curr = positionHistory.get(i);
                pathLength += Point2D.distance(prev.x, prev.y, curr.x, curr.y);
            }
            
            // Se percorreu distância mas acabou no mesmo lugar = oscilação
            return pathLength > 30;
        }
        return false;
    }
    
    // ADICIONA ÁREA PROBLEMÁTICA À MEMÓRIA
    private void addProblematicArea() {
        double x = robot.getX();
        double y = robot.getY();
        Rectangle2D.Double area = new Rectangle2D.Double(x - 40, y - 40, 80, 80);
        problematicAreas.add(area);
        
        // Limita número de áreas na memória
        if (problematicAreas.size() > 5) {
            problematicAreas.remove(0);
        }
    }
    
    // VERIFICA SE ESTÁ EM ÁREA PROBLEMÁTICA
    private boolean isInProblematicArea() {
        Point2D.Double current = new Point2D.Double(robot.getX(), robot.getY());
        for (Rectangle2D.Double area : problematicAreas) {
            if (area.contains(current)) {
                return true;
            }
        }
        return false;
    }
    
    // ESCAPE DE ÁREA PROBLEMÁTICA
    private void executeAreaEscape() {
        double centerX = robot.getBattleFieldWidth() / 2;
        double centerY = robot.getBattleFieldHeight() / 2;
        
        // Vai para o centro, mas com desvio aleatório
        double offsetX = (random.nextDouble() - 0.5) * 200;
        double offsetY = (random.nextDouble() - 0.5) * 200;
        
        double targetX = centerX + offsetX;
        double targetY = centerY + offsetY;
        
        // Garante que o alvo está dentro do campo
        targetX = Math.max(50, Math.min(robot.getBattleFieldWidth() - 50, targetX));
        targetY = Math.max(50, Math.min(robot.getBattleFieldHeight() - 50, targetY));
        
        double angle = Math.atan2(targetX - robot.getX(), targetY - robot.getY());
        double turnAngle = Utils.normalRelativeAngle(angle - Math.toRadians(robot.getHeading()));
        
        robot.setTurnRightRadians(turnAngle);
        robot.setAhead(150);
    }
    
    // ESCAPE DE EMERGÊNCIA AVANÇADO
    private void executeAdvancedEmergencyEscape() {
        // Se está em emergência há muito tempo, usa tática mais drástica
        long emergencyDuration = robot.getTime() - emergencyStartTime;
        
        if (emergencyDuration > 15) {
            // Tática drástica: para tudo e vai em linha reta para longe das paredes
            robot.setAhead(0);
            robot.setTurnRight(0);
            
            // Encontra a direção mais livre
            double bestAngle = findBestEscapeAngle();
            double currentHeading = robot.getHeading();
            double turn = Utils.normalRelativeAngleDegrees(bestAngle - currentHeading);
            
            robot.setTurnRight(turn);
            robot.setAhead(200);
            
            // Reseta após movimento drástico
            if (Math.abs(turn) < 5) { // Se já virou o suficiente
                emergencyMode = false;
                stuckCounter = 0;
                consecutiveWallHits = 0;
            }
        } else {
            // Emergência normal
            executeNormalEmergencyEscape();
        }
    }
    
    // ENCONTRA O MELHOR ÂNGULO DE ESCAPE
    private double findBestEscapeAngle() {
        double x = robot.getX();
        double y = robot.getY();
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        
        // Calcula distâncias para as paredes
        double leftDist = x;
        double rightDist = width - x;
        double bottomDist = y;
        double topDist = height - y;
        
        // Encontra a direção com mais espaço
        if (leftDist >= rightDist && leftDist >= bottomDist && leftDist >= topDist) {
            return 270; // Oeste
        } else if (rightDist >= bottomDist && rightDist >= topDist) {
            return 90;  // Leste
        } else if (bottomDist >= topDist) {
            return 180; // Sul
        } else {
            return 0;   // Norte
        }
    }
    
    // ESCAPE DE EMERGÊNCIA NORMAL
    private void executeNormalEmergencyEscape() {
        double centerX = robot.getBattleFieldWidth() / 2;
        double centerY = robot.getBattleFieldHeight() / 2;
        
        double angleToCenter = Math.atan2(centerX - robot.getX(), centerY - robot.getY());
        double turnAngle = Utils.normalRelativeAngle(angleToCenter - Math.toRadians(robot.getHeading()));
        
        robot.setTurnRightRadians(turnAngle);
        robot.setAhead(100);
        
        // Sai do modo emergência se chegou perto do centro
        double distanceToCenter = Point2D.distance(robot.getX(), robot.getY(), centerX, centerY);
        if (distanceToCenter < 100) {
            emergencyMode = false;
            stuckCounter = 0;
        }
    }
    
    // PREDIZ SE VAI BATER NA PAREDE
    private boolean willHitWall() {
        double futureX = robot.getX() + robot.getVelocity() * Math.sin(Math.toRadians(robot.getHeading())) * 8;
        double futureY = robot.getY() + robot.getVelocity() * Math.cos(Math.toRadians(robot.getHeading())) * 8;
        
        return (futureX < 80 || futureX > robot.getBattleFieldWidth() - 80 ||
                futureY < 80 || futureY > robot.getBattleFieldHeight() - 80);
    }
    
    // MANOBRA PREVENTIVA
    private void executePreventiveManeuver() {
        // Para antes de bater
        robot.setAhead(0);
        
        // Vira para direção mais segura
        double safeAngle = findBestEscapeAngle();
        double turn = Utils.normalRelativeAngleDegrees(safeAngle - robot.getHeading());
        robot.setTurnRight(turn);
        
        // Movimento curto e seguro
        robot.setAhead(50);
    }
    
    // WALL SMOOTHING ULTRA AVANÇADO
    private void executeAdvancedWallSmoothing() {
        double x = robot.getX();
        double y = robot.getY();
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        double margin = 80; // Margem ainda maior
        
        // Calcula "força" de repulsão de cada parede
        double leftForce = margin / Math.max(1, x);
        double rightForce = margin / Math.max(1, width - x);
        double bottomForce = margin / Math.max(1, y);
        double topForce = margin / Math.max(1, height - y);
        
        // Calcula vetor resultante de repulsão
        double repulsionX = rightForce - leftForce;
        double repulsionY = topForce - bottomForce;
        
        // Adiciona componente de movimento tangencial para suavidade
        double tangentX = -repulsionY;
        double tangentY = repulsionX;
        
        // Combina repulsão com movimento tangencial
        double finalX = repulsionX + tangentX * 0.5;
        double finalY = repulsionY + tangentY * 0.5;
        
        // Converte para ângulo
        double targetAngle = Math.toDegrees(Math.atan2(finalX, finalY));
        double turn = Utils.normalRelativeAngleDegrees(targetAngle - robot.getHeading());
        
        robot.setTurnRight(turn);
        robot.setAhead(Math.min(moveDistance, 60)); // Movimento mais conservador perto da parede
    }

    private boolean isNearWall() {
        double margin = 80; // Margem aumentada
        double x = robot.getX();
        double y = robot.getY();
        double width = robot.getBattleFieldWidth();
        double height = robot.getBattleFieldHeight();
        
        return (x < margin || x > width - margin || y < margin || y > height - margin);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        if (emergencyMode) return;
        
        if (robot.getTime() > nextStrategyChangeTime) {
            direction *= -1;
            moveDistance = 90 + random.nextInt(70);
            nextStrategyChangeTime = robot.getTime() + 20 + random.nextInt(30);
        }

        double absoluteBearing = robot.getHeading() + e.getBearing();

        if (e.getDistance() < 120) {
            double escapeAngle = absoluteBearing + 90 + (direction * 45);
            robot.setTurnRight(Utils.normalRelativeAngleDegrees(escapeAngle - robot.getHeading()));
            robot.setAhead(-moveDistance);
        } else {
            double moveAngle = absoluteBearing - robot.getHeading() + (90 * direction);
            robot.setTurnRight(Utils.normalRelativeAngleDegrees(moveAngle));
            robot.setAhead(direction * moveDistance);
        }
        
        // Atualiza posição anterior
        lastX = robot.getX();
        lastY = robot.getY();
    }

    public void onHitWall(HitWallEvent e) {
        // RESPOSTA IMEDIATA E EFETIVA
        long currentTime = robot.getTime();
        
        // Conta hits consecutivos
        if (currentTime - lastWallHitTime < 5) {
            consecutiveWallHits++;
        } else {
            consecutiveWallHits = 1;
        }
        lastWallHitTime = currentTime;
        
        // Para TUDO imediatamente
        robot.setAhead(0);
        robot.setTurnRight(0);
        
        // Resposta escalonada baseada em hits consecutivos
        if (consecutiveWallHits >= 3) {
            // Situação crítica - ativação imediata do modo emergência
            emergencyMode = true;
            emergencyStartTime = currentTime;
            addProblematicArea();
            return;
        }
        
        // Calcula escape inteligente baseado no ângulo da colisão
        double escapeAngle = 180 + (90 * direction) + random.nextInt(60) - 30;
        robot.setTurnRight(escapeAngle);
        robot.setAhead(100 + (consecutiveWallHits * 20));
        
        direction *= -1;
        nextStrategyChangeTime = robot.getTime();
        
        // Marca como área problemática se hit múltiplo
        if (consecutiveWallHits >= 2) {
            addProblematicArea();
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (emergencyMode) return;
        
        direction *= -1;
        double dodgeAngle = e.getBearing() + 70 + random.nextInt(40);
        robot.setTurnRight(Utils.normalRelativeAngleDegrees(dodgeAngle));
        robot.setAhead(direction * 150);
        nextStrategyChangeTime = robot.getTime();
    }

    public void onHitRobot(HitRobotEvent e) {
        if (e.isMyFault()) {
            robot.setTurnRight(10);
        } else {
            robot.setBack(50);
            robot.setTurnRight(90);
        }
        nextStrategyChangeTime = robot.getTime();
    }

    private void moveToPoint(Point2D.Double point) {
        double angle = Math.atan2(point.x - robot.getX(), point.y - robot.getY());
        double turn = Utils.normalRelativeAngle(angle - Math.toRadians(robot.getHeading()));
        robot.setTurnRightRadians(turn);
        robot.setAhead(Point2D.distance(robot.getX(), robot.getY(), point.x, point.y));
    }
}

/**
 * Sistema de Mira e Disparo
 */
class GunSystem {
    private AdvancedRobot robot;
    private int hits = 0;
    private int shots = 0;
    
    public GunSystem(AdvancedRobot robot) {
        this.robot = robot;
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        double bulletPower = calculateOptimalFirePower(e.getDistance(), robot.getEnergy());
        double bulletSpeed = 20 - (3 * bulletPower);
        
        // Mira preditiva linear
        Point2D.Double predictedPos = predictLinearPosition(e, bulletSpeed);
        
        if (predictedPos != null) {
            double angle = Math.atan2(predictedPos.x - robot.getX(), predictedPos.y - robot.getY());
            double gunTurn = Utils.normalRelativeAngle(angle - Math.toRadians(robot.getGunHeading()));
            
            robot.setTurnGunRightRadians(gunTurn);
            
            // Dispara apenas se o canhão estiver bem alinhado
            if (Math.abs(gunTurn) < 0.05) { // ~3 graus
                robot.setFire(bulletPower);
                shots++;
            }
        }
    }
    
    private Point2D.Double predictLinearPosition(ScannedRobotEvent e, double bulletSpeed) {
        double enemyX = robot.getX() + e.getDistance() * Math.sin(Math.toRadians(robot.getHeading() + e.getBearing()));
        double enemyY = robot.getY() + e.getDistance() * Math.cos(Math.toRadians(robot.getHeading() + e.getBearing()));
        
        double time = e.getDistance() / bulletSpeed;
        
        double futureX = enemyX + e.getVelocity() * Math.sin(Math.toRadians(e.getHeading())) * time;
        double futureY = enemyY + e.getVelocity() * Math.cos(Math.toRadians(e.getHeading())) * time;
        
        // Verifica se a posição prevista está dentro do campo
        if (futureX < 0 || futureX > robot.getBattleFieldWidth() || 
            futureY < 0 || futureY > robot.getBattleFieldHeight()) {
            // Predição simples se sair do campo
            time = time * 0.5;
            futureX = enemyX + e.getVelocity() * Math.sin(Math.toRadians(e.getHeading())) * time;
            futureY = enemyY + e.getVelocity() * Math.cos(Math.toRadians(e.getHeading())) * time;
        }
        
        return new Point2D.Double(futureX, futureY);
    }
    
    private double calculateOptimalFirePower(double distance, double energy) {
        // Conserva energia quando baixa
        if (energy < 16) return 1.0;
        
        // Ajusta potência baseada na distância
        if (distance < 100) return 3.0;      // Máxima potência de perto
        if (distance < 300) return 2.0;      // Potência média
        if (distance < 500) return 1.5;      // Potência baixa-média
        return 1.0;                          // Mínima para longas distâncias
    }
    
    public void onBulletHit(BulletHitEvent e) {
        hits++;
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
        // Ajusta estratégia se errando muito
    }
    
    public double getAccuracy() {
        return shots > 0 ? (double) hits / shots : 0;
    }
}

/**
 * Sistema de Radar Inteligente
 */
class RadarSystem {
    private AdvancedRobot robot;
    private double radarDirection = 1;
    private boolean lockMode = false;
    private String lockedTarget = "";
    
    public RadarSystem(AdvancedRobot robot) {
        this.robot = robot;
    }
    
    public void update() {
        if (!lockMode) {
            // Modo busca - varredura completa
            robot.setTurnRadarRight(360);
        }
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        // Ativa modo de travamento no alvo
        lockMode = true;
        lockedTarget = e.getName();
        
        // Infinity lock - mantém radar no inimigo
        double radarTurn = robot.getHeading() + e.getBearing() - robot.getRadarHeading();
        radarTurn = Utils.normalRelativeAngleDegrees(radarTurn);
        
        // Determina direção do radar
        if (radarTurn < 0) {
            radarDirection = -1;
        } else {
            radarDirection = 1;
        }
        
        // Aplica correção para manter lock
        robot.setTurnRadarRight(radarTurn + (radarDirection * 15));
    }
    
    public void searchMode() {
        lockMode = false;
        lockedTarget = "";
        robot.setTurnRadarRight(radarDirection * 360);
    }
}