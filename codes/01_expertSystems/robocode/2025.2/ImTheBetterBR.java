package barraco; // Ou o nome do seu pacote

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ImTheBetterBR extends AdvancedRobot {

    private static final double DISTANCIA_OTIMA = 350;
    private static final double FAIXA_DISTANCIA = 100;
    private static int direcaoCirculo = 1;

    // --- NOVAS VARIÁVEIS PARA CONTROLE DE TIRO ---
    private int contadorDeRajada = 0;
    private long pausarTiroAteTurno = 0;

    private HashMap<String, EnemyState> inimigos = new HashMap<>();
    private String alvoPrimarioNome;

    class EnemyState {
        String nome;
        Point2D.Double posicao = new Point2D.Double();
        double bearing; // Em Radianos
        double distancia;
        double energia;

        public void atualizar(ScannedRobotEvent e, AdvancedRobot roboto) {
            this.nome = e.getName();
            // CORREÇÃO: Usando radianos consistentemente para evitar erros de conversão
            double anguloAbsoluto = roboto.getHeadingRadians() + e.getBearingRadians();
            this.posicao.setLocation(roboto.getX() + e.getDistance() * Math.sin(anguloAbsoluto),
                                     roboto.getY() + e.getDistance() * Math.cos(anguloAbsoluto));
            this.energia = e.getEnergy();
            this.bearing = e.getBearingRadians(); // Armazena em radianos
            this.distancia = e.getDistance();
        }
    }

    public void run() {
        setColors(new Color(60, 0, 90), Color.CYAN, Color.MAGENTA);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            if (getRadarTurnRemaining() == 0.0) {
                 setTurnRadarRight(360);
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (!inimigos.containsKey(e.getName())) {
            inimigos.put(e.getName(), new EnemyState());
        }
        EnemyState estadoInimigo = inimigos.get(e.getName());
        boolean inimigoAtirou = (estadoInimigo.energia > e.getEnergy() && estadoInimigo.energia - e.getEnergy() <= 3);
        estadoInimigo.atualizar(e, this);

        selecionarAlvo(e.getName());

        if (alvoPrimarioNome != null && alvoPrimarioNome.equals(e.getName())) {
            executarLogicaDeScan(estadoInimigo);
            executarLogicaDeMovimento(estadoInimigo, inimigoAtirou);
            executarLogicaDeTiro(estadoInimigo);
        }
    }

    private void selecionarAlvo(String nomeScaneado) {
        String alvoAnterior = alvoPrimarioNome;
        
        ArrayList<EnemyState> listaInimigos = new ArrayList<>(inimigos.values());
        listaInimigos.removeIf(s -> s.nome == null);
        if (listaInimigos.isEmpty()) {
             alvoPrimarioNome = null;
             return;
        }

        listaInimigos.sort(Comparator.comparingDouble(a -> a.distancia));
        alvoPrimarioNome = listaInimigos.get(0).nome;

        // --- CORREÇÃO: Reseta a lógica de tiro se o alvo primário mudar ---
        if (alvoPrimarioNome != null && !alvoPrimarioNome.equals(alvoAnterior)) {
            contadorDeRajada = 0;
            pausarTiroAteTurno = 0;
        }
    }

    private void executarLogicaDeScan(EnemyState alvo) {
        double anguloRadar = getHeadingRadians() + alvo.bearing - getRadarHeadingRadians();
        double giroExtra = Math.min(Math.atan(36.0 / alvo.distancia), Rules.RADAR_TURN_RATE);
        setTurnRadarRightRadians(Utils.normalRelativeAngle(anguloRadar + (giroExtra * direcaoCirculo)));
    }

    private void executarLogicaDeMovimento(EnemyState alvo, boolean inimigoAtirou) {
        double muitoPerto = DISTANCIA_OTIMA - FAIXA_DISTANCIA / 2;
        double muitoLonge = DISTANCIA_OTIMA + FAIXA_DISTANCIA / 2;
        
        double anguloParaVirar = alvo.bearing;
        double anguloMovimento = 0;
        double distanciaMovimento = 100;

        if (alvo.distancia > muitoLonge) {
            anguloMovimento = alvo.bearing;
        } else if (alvo.distancia < muitoPerto) {
            anguloMovimento = alvo.bearing + Math.PI; // Inverte a direção
        } else {
            anguloMovimento = alvo.bearing + (Math.PI / 2 * direcaoCirculo);
            distanciaMovimento = inimigoAtirou ? 100 : 20;
        }
        
        setTurnRightRadians(Utils.normalRelativeAngle(anguloMovimento - getHeadingRadians()));
        setAhead(distanciaMovimento);
    }
    
    // --- CORREÇÃO: LÓGICA DE TIRO EM RAJADA ---
    private void executarLogicaDeTiro(EnemyState alvo) {
        // Se estivermos na pausa, não faz nada
        if (getTime() < pausarTiroAteTurno) {
            // Mantém a mira travada mesmo durante a pausa
            double anguloAbsolutoParaInimigo = getHeadingRadians() + alvo.bearing;
            double giroArma = Utils.normalRelativeAngle(anguloAbsolutoParaInimigo - getGunHeadingRadians());
            setTurnGunRightRadians(giroArma);
            return;
        }

        double poderTiro = calcularPoderTiroGranular(alvo.distancia);
        
        // Mira
        double anguloAbsolutoParaInimigo = getHeadingRadians() + alvo.bearing;
        double giroArma = Utils.normalRelativeAngle(anguloAbsolutoParaInimigo - getGunHeadingRadians());
        setTurnGunRightRadians(giroArma);

        // Atira se a arma estiver fria e a mira estável
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 8) {
            fire(poderTiro);
            contadorDeRajada++;

            if (contadorDeRajada >= 2) {
                // Calcula o tempo de viagem da bala
                double tempoViagem = alvo.distancia / (20 - 3 * poderTiro);
                // Define a pausa
                pausarTiroAteTurno = getTime() + (long)tempoViagem;
                // Reseta o contador para a próxima rajada
                contadorDeRajada = 0;
            }
        }
    }
    
    private double calcularPoderTiroGranular(double distancia) {
        final double DISTANCIA_MAX_POWER = 60;
        final double DISTANCIA_MIN_POWER = 500;
        if (distancia <= DISTANCIA_MAX_POWER) return 3.0;
        if (distancia >= DISTANCIA_MIN_POWER) return 0.1;
        double potencia = 3.0 - (distancia - DISTANCIA_MAX_POWER) * (2.9 / (DISTANCIA_MIN_POWER - DISTANCIA_MAX_POWER));
        return Math.round(potencia * 10.0) / 10.0;
    }
    
    // --- CORREÇÃO: LÓGICA DE FUGA DA PAREDE MAIS ROBUSTA ---
    @Override
    public void onHitWall(HitWallEvent e) {
        double bearingParede = e.getBearing(); // Em graus
        // Vira para a direção oposta da parede
        setTurnRight(-bearingParede);
        setAhead(150);
        // Reseta a pausa de tiro para poder reagir
        pausarTiroAteTurno = 0;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        direcaoCirculo *= -1;
    }

    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        inimigos.remove(e.getName());
        if (e.getName() != null && e.getName().equals(alvoPrimarioNome)) {
            alvoPrimarioNome = null;
        }
    }
}