// Z3R0 - Versão 3.0 
// Estratégias: Movimento Adaptativo + Mira Preditiva

package robo;

import robocode.*;
import java.awt.Color;
import java.awt.Graphics2D;
import robocode.util.Utils;
import java.awt.geom.*;
import java.util.*;

public class Z3R0 extends AdvancedRobot {
    
    // Sistema de estados do robô
    private enum ModoCombate { 
        AGRESSIVO, DEFENSIVO, EVASIVO, PATRAO 
    }
    private ModoCombate modoAtual = ModoCombate.PATRAO;
    
    // Dados do inimigo
    private DadosInimigo inimigo = new DadosInimigo();
    private CampoBatalha campo = new CampoBatalha();
    
    // Sistemas principais
    private SistemaMovimento movimento;
    private SistemaMira mira;
    private SistemaRadar radar;
    
    // Controle de padrões
    private GeradorPadroes padroes;
    private long ultimaMudancaModo = 0;
    
    public void run() {
        inicializarSistemas();
        
        setColors(new Color(0, 102, 204), new Color(255, 204, 0), new Color(255, 128, 0));
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        movimento = new SistemaMovimento(this);
        mira = new SistemaMira(this);
        radar = new SistemaRadar(this);
        padroes = new GeradorPadroes(this);
        
        // Loop principal otimizado
        while (true) {
            radar.atualizar();
            execute();
        }
    }
    
    private void inicializarSistemas() {
        campo.atualizar(getBattleFieldWidth(), getBattleFieldHeight());
    }
    
    public void onScannedRobot(ScannedRobotEvent e) {
        long tempoAtual = getTime();
        
        // Atualiza dados do inimigo
        inimigo.atualizar(e, this);
        
        // Atualiza radar com sistema próprio
        radar.travar(inimigo.anguloAbsoluto);
        
        // Decide estratégia baseada em condições de batalha
        atualizarModoCombate(e);
        
        // Executa sistemas principais
        movimento.executar(inimigo, modoAtual);
        mira.apontarEDisparar(inimigo);
        
        // Atualiza padrões de movimento
        padroes.registrarMovimentoInimigo(inimigo);
    }
    
    private void atualizarModoCombate(ScannedRobotEvent e) {
        long tempoAtual = getTime();
        
        // Muda de modo baseado em condições
        if (tempoAtual - ultimaMudancaModo < 30) return;
        
        if (e.getEnergy() < 20 || getEnergy() > e.getEnergy() + 30) {
            modoAtual = ModoCombate.AGRESSIVO;
        } else if (e.getDistance() < 150) {
            modoAtual = ModoCombate.EVASIVO;
        } else if (padroes.detectarPadraoInimigo()) {
            modoAtual = ModoCombate.PATRAO;
        } else {
            modoAtual = ModoCombate.DEFENSIVO;
        }
        
        ultimaMudancaModo = tempoAtual;
    }
    
    public void onHitByBullet(HitByBulletEvent e) {
        movimento.aoSerAtingido(e.getBearingRadians());
        modoAtual = ModoCombate.EVASIVO;
        ultimaMudancaModo = getTime();
    }
    
    public void onHitWall(HitWallEvent e) {
        movimento.aoBaterParede(e.getBearingRadians());
    }
    
    public void onWin(WinEvent e) {
        // Dança da vitória única
        for (int i = 0; i < 5; i++) {
            turnRight(90);
            turnLeft(90);
        }
    }
    
    // ========== SUBSISTEMAS ============
    
    // Sistema de dados do inimigo
    private class DadosInimigo {
        double x, y, energia, velocidade, direcao;
        double distancia, bearing, anguloAbsoluto;
        long ultimoScanTempo;
        
        void atualizar(ScannedRobotEvent e, Z3R0 robo) {
            this.energia = e.getEnergy();
            this.velocidade = e.getVelocity();
            this.direcao = e.getHeadingRadians();
            this.distancia = e.getDistance();
            this.bearing = e.getBearingRadians();
            this.anguloAbsoluto = robo.getHeadingRadians() + e.getBearingRadians();
            this.ultimoScanTempo = robo.getTime();
            
            // Calcula posição
            this.x = robo.getX() + Math.sin(anguloAbsoluto) * distancia;
            this.y = robo.getY() + Math.cos(anguloAbsoluto) * distancia;
        }
    }
    
    // Sistema de campo de batalha
    private class CampoBatalha {
        double largura, altura;
        Rectangle2D.Double zonaSegura;
        
        void atualizar(double l, double a) {
            largura = l; altura = a;
            zonaSegura = new Rectangle2D.Double(25, 25, l-50, a-50);
        }
        
        boolean estaPertoParede(double x, double y, double margem) {
            return x < margem || x > largura - margem || 
                   y < margem || y > altura - margem;
        }
    }
    
    // Sistema de movimento adaptativo
    private class SistemaMovimento {
        private Z3R0 robo;
        private double ultimaDirecaoMovimento = 1;
        private int padraoMovimento = 0;
        
        SistemaMovimento(Z3R0 robo) { this.robo = robo; }
        
        void executar(DadosInimigo inimigo, ModoCombate modo) {
            double quantidadeMovimento = calcularQuantidadeMovimento(inimigo, modo);
            double anguloMovimento = calcularAnguloMovimento(inimigo, modo);
            
            robo.setTurnRightRadians(Utils.normalRelativeAngle(anguloMovimento - robo.getHeadingRadians()));
            robo.setAhead(quantidadeMovimento);
        }
        
        private double calcularQuantidadeMovimento(DadosInimigo inimigo, ModoCombate modo) {
            switch (modo) {
                case AGRESSIVO: return 120 * Math.signum(ultimaDirecaoMovimento);
                case EVASIVO: return 150 * (Math.random() > 0.5 ? 1 : -1);
                case DEFENSIVO: return 80 * ultimaDirecaoMovimento;
                default: return 100 * ultimaDirecaoMovimento;
            }
        }
        
        private double calcularAnguloMovimento(DadosInimigo inimigo, ModoCombate modo) {
            double anguloBase = inimigo.anguloAbsoluto + Math.PI/2; // Movimento perpendicular
            
            // Adiciona variação baseada no modo
            switch (modo) {
                case EVASIVO:
                    return anguloBase + (Math.PI/4 * (Math.random() - 0.5));
                case PATRAO:
                    return anguloBase + Math.sin(robo.getTime() * 0.1) * Math.PI/3;
                default:
                    return anguloBase;
            }
        }
        
        void aoSerAtingido(double bearingBala) {
            // Movimento evasivo quando atingido
            ultimaDirecaoMovimento = -Math.signum(bearingBala);
        }
        
        void aoBaterParede(double bearingParede) {
            ultimaDirecaoMovimento *= -1;
        }
    }
    
    // Sistema de mira preditiva
    private class SistemaMira {
        private Z3R0 robo;
        private Map<String, double[]> estatisticasInimigo = new HashMap<>();
        private static final int NUM_BINS = 31;
        
        SistemaMira(Z3R0 robo) { this.robo = robo; }
        
        void apontarEDisparar(DadosInimigo inimigo) {
            double forcaTiro = calcularForcaTiro(inimigo.distancia);
            double anguloMira = calcularAnguloMira(inimigo, forcaTiro);
            
            // Mira suave
            double giroArma = Utils.normalRelativeAngle(anguloMira - robo.getGunHeadingRadians());
            robo.setTurnGunRightRadians(giroArma * 1.2);
            
            // Dispara se estiver alinhado
            if (robo.getGunHeat() == 0 && Math.abs(giroArma) < Math.PI/6) {
                if (robo.setFireBullet(forcaTiro) != null) {
                    registrarDisparo(inimigo, anguloMira);
                }
            }
        }
        
        private double calcularForcaTiro(double distancia) {
            // Força adaptativa baseada na distância
            if (distancia < 150) return 3.0;
            if (distancia < 300) return 2.0;
            if (distancia < 500) return 1.5;
            return 1.0;
        }
        
        private double calcularAnguloMira(DadosInimigo inimigo, double forcaTiro) {
            // Mira preditiva com correção de movimento
            double velocidadeBala = 20 - 3 * forcaTiro;
            double direcaoInimigo = inimigo.direcao;
            double velocidadeInimigo = inimigo.velocidade;
            
            // Predição de movimento
            double tempoPrevisao = inimigo.distancia / velocidadeBala;
            double xFuturo = inimigo.x + Math.sin(direcaoInimigo) * velocidadeInimigo * tempoPrevisao;
            double yFuturo = inimigo.y + Math.cos(direcaoInimigo) * velocidadeInimigo * tempoPrevisao;
            
            // Correção baseada em estatísticas
            double correcao = obterCorrecaoMira(inimigo);
            double anguloFinal = Math.atan2(xFuturo - robo.getX(), yFuturo - robo.getY());
            
            return anguloFinal + correcao;
        }
        
        private double obterCorrecaoMira(DadosInimigo inimigo) {
            // Correção baseada em padrões observados
            String chaveInimigo = String.format("%.1f-%.1f", inimigo.velocidade, inimigo.distancia);
            double[] estatisticas = estatisticasInimigo.get(chaveInimigo);
            
            if (estatisticas == null) return 0;
            
            // Encontra o bin mais provável
            int melhorBin = 0;
            for (int i = 1; i < NUM_BINS; i++) {
                if (estatisticas[i] > estatisticas[melhorBin]) melhorBin = i;
            }
            
            double fatorAjuste = (melhorBin - (NUM_BINS-1)/2.0) / ((NUM_BINS-1)/2.0);
            return fatorAjuste * Math.asin(8 / (20 - 3 * 2.0));
        }
        
        private void registrarDisparo(DadosInimigo inimigo, double anguloMira) {
            // Registro para estatísticas futuras (implementar se necessário)
        }
    }
    
    // Sistema de radar otimizado
    private class SistemaRadar {
        private Z3R0 robo;
        private double ultimoGiroRadar = 0;
        
        SistemaRadar(Z3R0 robo) { this.robo = robo; }
        
        void atualizar() {
            // Varredura contínua quando não há alvo
            if (robo.getTime() - robo.inimigo.ultimoScanTempo > 5) {
                robo.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
            }
        }
        
        void travar(double anguloAbsoluto) {
            double giroRadar = Utils.normalRelativeAngle(anguloAbsoluto - robo.getRadarHeadingRadians());
            double varreduraExtra = Math.toRadians(45); // Campo de visão ampliado
            
            if (giroRadar < 0) {
                giroRadar -= varreduraExtra;
            } else {
                giroRadar += varreduraExtra;
            }
            
            robo.setTurnRadarRightRadians(giroRadar);
        }
    }
    
    // Gerador e detector de padrões
    private class GeradorPadroes {
        private Z3R0 robo;
        private LinkedList<Double> movimentosInimigo = new LinkedList<>();
        private static final int MEMORIA_PADROES = 10;
        
        GeradorPadroes(Z3R0 robo) { this.robo = robo; }
        
        void registrarMovimentoInimigo(DadosInimigo inimigo) {
            movimentosInimigo.add(inimigo.direcao);
            if (movimentosInimigo.size() > MEMORIA_PADROES) {
                movimentosInimigo.removeFirst();
            }
        }
        
        boolean detectarPadraoInimigo() {
            if (movimentosInimigo.size() < 5) return false;
            
            // Detecta padrões cíclicos simples
            Double[] movimentos = movimentosInimigo.toArray(new Double[0]);
            double variacao = calcularVariacao(movimentos);
            
            return variacao < 0.5; // Baixa variação indica padrão
        }
        
        private double calcularVariacao(Double[] dados) {
            double media = 0;
            for (double d : dados) media += d;
            media /= dados.length;
            
            double variacao = 0;
            for (double d : dados) variacao += Math.pow(d - media, 2);
            return variacao / dados.length;
        }
    }
    
    // Sistema de visualização para debug
    public void onPaint(Graphics2D g) {
        // Desenha informações de debug
        g.setColor(Color.WHITE);
        g.drawString("Modo: " + modoAtual, 10, 20);
        g.drawString(String.format("Inimigo: %.1fpx", inimigo.distancia), 10, 40);
        
        // Desenha linha para o inimigo
        if (inimigo.ultimoScanTempo == getTime() - 1) {
            g.setColor(Color.GREEN);
            g.drawLine((int)getX(), (int)getY(), (int)inimigo.x, (int)inimigo.y);
        }
        
        // Desenha zona segura
        g.setColor(new Color(255, 255, 0, 50));
        g.fill(campo.zonaSegura);
        
        // Desenha a direção atual do robô
        g.setColor(Color.CYAN);
        double angulo = getHeadingRadians();
        double comprimentoFlecha = 50;
        double xFim = getX() + Math.sin(angulo) * comprimentoFlecha;
        double yFim = getY() + Math.cos(angulo) * comprimentoFlecha;
        g.drawLine((int)getX(), (int)getY(), (int)xFim, (int)yFim);
    }
    
    // Eventos adicionais para melhor controle
    public void onBulletHit(BulletHitEvent e) {
        // Confirmação de acerto
        if (modoAtual == ModoCombate.AGRESSIVO) {
            setAhead(50); // Avança após acerto bem-sucedido
        }
    }
    
    public void onBulletMissed(BulletMissedEvent e) {
        // Reage a erros de disparo
        if (modoAtual == ModoCombate.AGRESSIVO) {
            modoAtual = ModoCombate.DEFENSIVO;
            ultimaMudancaModo = getTime();
        }
    }
    
    public void onRobotDeath(RobotDeathEvent e) {
        // Reinicia sistemas quando um robô é destruído
        movimento = new SistemaMovimento(this);
        padroes = new GeradorPadroes(this);
    }
}