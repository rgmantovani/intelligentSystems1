package SocialAnxiety;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.*;

public class SocialAnxiousBot extends AdvancedRobot {

    // (Todas as suas variáveis de classe permanecem as mesmas)
    private double nivelAnsiedade = 0.0;
    private boolean emAtaquePanico = false;
    private long tempoUltimoAtaquePanico = 0;
    private int inimigosPróximos = 0;
    private HashMap<String, AtiradorInfo> atiradoresAtivos = new HashMap<>();
    private String ultimoAtirador = null;
    private long tempoUltimoTiro = 0;
    private HashMap<String, InimigoInfo> inimigos = new HashMap<>();
    private String alvoAtual = null;
    private long tempoUltimoScan = 0;
    private Random randomizador = new Random();
    private int contadorMovimento = 0;
    private double ultimaDirecao = 1;
    private long ultimaMudancaDirecao = 0;
    private boolean inimigoUsaWaveSurfing = false;
    private ArrayList<Double> historicoTiros = new ArrayList<>();
    private int indiceTiroPadrao = 0;
    private Ponto pontoSeguro = null;
    private long tempoUltimoCalculoPosicao = 0;

    class Ponto {
        double x, y;
        Ponto(double x, double y) { this.x = x; this.y = y; }
    }
    
    // (As classes internas AtiradorInfo e InimigoInfo permanecem as mesmas)
    class AtiradorInfo {
        String nome;
        int tirosRecebidos;
        double danoTotal;
        long ultimoTiro;
        double ultimaPotenciaTiro;
        double x, y;
        double precisao;
        int tirosDispardos;
        boolean atiadorPerigosoDetectado;

        AtiradorInfo(String nome) {
            this.nome = nome;
            this.tirosRecebidos = 0;
            this.danoTotal = 0.0;
            this.ultimoTiro = 0;
            this.precisao = 0.0;
            this.tirosDispardos = 0;
            this.atiadorPerigosoDetectado = false;
        }

        void registrarTiro(double potencia, double x, double y, long tempo) {
            this.tirosRecebidos++;
            this.danoTotal += calcularDano(potencia);
            this.ultimoTiro = tempo;
            this.ultimaPotenciaTiro = potencia;
            this.x = x;
            this.y = y;
            if (tirosDispardos > 0) this.precisao = (double) tirosRecebidos / tirosDispardos;
            if ((precisao > 0.6 && tirosRecebidos >= 3) || danoTotal > 40) atiadorPerigosoDetectado = true;
        }

        void registrarTiroInimigo() {
            this.tirosDispardos++;
            if (tirosDispardos > 0) this.precisao = (double) tirosRecebidos / tirosDispardos;
        }

        double calcularDano(double potencia) {
            return potencia > 1 ? 4 * potencia + 2 * (potencia - 1) : 4 * potencia;
        }

        double getNivelAmeaca() {
            double ameaca = danoTotal * 0.5 + tirosRecebidos * 3 + precisao * 20;
            if (atiadorPerigosoDetectado) ameaca += 15;
            if (getTime() - ultimoTiro > 50) ameaca *= 0.7;
            return ameaca;
        }
    }

    class InimigoInfo {
        double x, y, velocidade, rumo, energia, distancia, ultimaEnergia;
        long ultimaVez;
        boolean possivelWaveSurfer = false;
        int esquivasDetectadas = 0;

        InimigoInfo(double x, double y, double vel, double rumo, double energia, double dist) {
            this.x = x; this.y = y; this.velocidade = vel; this.rumo = rumo;
            this.energia = energia; this.distancia = dist; this.ultimaVez = getTime();
            this.ultimaEnergia = energia;
        }

        boolean detectarTiro() {
            double perdaEnergia = ultimaEnergia - energia;
            if (perdaEnergia >= 0.1 && perdaEnergia <= 3.0) {
                String nome = getNomeInimigoPorPosicao(x, y);
                if (nome != null && atiradoresAtivos.containsKey(nome)) atiradoresAtivos.get(nome).registrarTiroInimigo();
                ultimaEnergia = energia;
                return true;
            }
            ultimaEnergia = energia;
            return false;
        }
    }

    public void run() {
        atualizarCores();
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        inicializarPadroesTiro();

        while (true) {
            calcularNivelAnsiedade();
            if (emAtaquePanico) {
                comportamentoAtaquePanicoTatico();
            } else {
                comportamentoAnsiosoMelhorado();
            }
            execute();
        }
    }

    // ### INÍCIO DAS MODIFICAÇÕES ###

    /**
     * MELHORADO: Usa um sistema de "força de repulsão" para evitar paredes com alta prioridade.
     * @return 'true' se uma manobra de evasão foi acionada, 'false' caso contrário.
     */
    private boolean evitarParedesInteligente() {
        double margemDeSeguranca = 70.0; // A que distância da parede começamos a reagir
        double x = getX();
        double y = getY();
        double larguraCampo = getBattleFieldWidth();
        double alturaCampo = getBattleFieldHeight();

        // Verifica se estamos na "zona de perigo"
        if (x < margemDeSeguranca || x > larguraCampo - margemDeSeguranca ||
            y < margemDeSeguranca || y > alturaCampo - margemDeSeguranca) {

            // Calcula um vetor de força que nos "empurra" para longe das paredes
            double forcaX = 0;
            double forcaY = 0;

            // A força é inversamente proporcional à distância. Quanto mais perto, mais forte.
            forcaX += 1.0 / (x);
            forcaX -= 1.0 / (larguraCampo - x);
            forcaY += 1.0 / (y);
            forcaY -= 1.0 / (alturaCampo - y);

            // Calcula o ângulo absoluto para onde o vetor de força está apontando
            double anguloDeFuga = Math.toDegrees(Math.atan2(forcaX, forcaY));

            // Calcula o quanto precisamos virar para apontar para a rota de fuga
            double anguloVirada = Utils.normalRelativeAngleDegrees(anguloDeFuga - getHeading());

            // Executa a manobra de evasão com prioridade
            setTurnRight(anguloVirada);
            setAhead(150); // Move-se para longe da parede com urgência
            
            // Sinaliza que uma ação evasiva foi tomada
            return true;
        }

        // Não estamos em perigo, nenhuma ação foi tomada
        return false;
    }

    private void movimentoAntiPadrao() {
        // PRIORIDADE 1: EVITAR PAREDES.
        // Se o método de evasão retornar 'true', ele já definiu os movimentos.
        // Então, simplesmente paramos a execução deste método para não sobrepor os comandos.
        if (evitarParedesInteligente()) {
            return;
        }

        // Se não precisamos evitar paredes, continuamos com a lógica normal...
        contadorMovimento++;
        
        double velocidadeBase = 5 + nivelAnsiedade * 0.3;
        if (inimigoUsaWaveSurfing) {
            velocidadeBase += 1.5;
        }
        setMaxVelocity(velocidadeBase);
        
        boolean deveMudarDirecao = false;
        if (contadorMovimento % (15 - (int)(nivelAnsiedade * 0.8)) == 0) {
            deveMudarDirecao = true;
        }
        if (inimigoUsaWaveSurfing && randomizador.nextDouble() < 0.15) {
            deveMudarDirecao = true;
        }
        
        if (deveMudarDirecao) {
            double fatorRandomico = randomizador.nextGaussian() * 20;
            double mudancaBase = (45 + nivelAnsiedade * 8) * ultimaDirecao;
            double mudancaFinal = mudancaBase + fatorRandomico;
            setTurnRight(mudancaFinal);
            ultimaDirecao *= -1;
            ultimaMudancaDirecao = getTime();
        }
        
        if (inimigoUsaWaveSurfing && alvoAtual != null) {
            InimigoInfo alvo = inimigos.get(alvoAtual);
            if (alvo != null && alvo.distancia < 300) {
                if (randomizador.nextDouble() < 0.3) {
                    setAhead((randomizador.nextDouble() - 0.5) * 150);
                }
            }
        }

        if (pontoSeguro != null) {
            double anguloParaPontoSeguro = Math.toDegrees(Math.atan2(pontoSeguro.x - getX(), pontoSeguro.y - getY()));
            double anguloVirada = Utils.normalRelativeAngleDegrees(anguloParaPontoSeguro - getHeading());
            double fatorDeCorrecao = 0.2; 
            setTurnRight(anguloVirada * fatorDeCorrecao);
            setAhead(100);
        }
    }
    
    // ### FIM DAS MODIFICAÇÕES ###


    // (O resto do código permanece o mesmo. Incluí por completeza)

    private void inicializarPadroesTiro() {
        double[] potenciasAntiWave = {0.5, 1.2, 0.8, 2.1, 0.6, 1.8, 0.9, 1.5, 0.7, 2.5};
        for (double pot : potenciasAntiWave) {
            historicoTiros.add(pot);
        }
    }
    
    private void comportamentoAnsiosoMelhorado() {
        gerenciarPosicionamentoEstrategico();
        movimentoAntiPadrao();
        scannerInteligente();
        sistemaDisparoAntiWaveSurf();
    }

    private void calcularNivelAnsiedade() {
        limparInimigosSumidos();
        atualizarTrackingAtiradores();
        inimigosPróximos = 0;
        for (InimigoInfo inimigo : inimigos.values()) {
            if (inimigo.distancia < 200) inimigosPróximos++;
            inimigo.detectarTiro();
            detectarWaveSurfing(inimigo);
        }
        double ansiedadeBase = inimigos.size() * 0.6 + inimigosPróximos * 1.2 + (100 - getEnergy()) * 0.015;
        ansiedadeBase += calcularAnsiedadeAtiradores();
        if (inimigoUsaWaveSurfing) ansiedadeBase += 1.0;
        nivelAnsiedade = nivelAnsiedade * 0.8 + ansiedadeBase * 0.2;
        nivelAnsiedade = Math.min(10.0, Math.max(0.0, nivelAnsiedade));
        if (nivelAnsiedade > 8.0 && !emAtaquePanico && getTime() - tempoUltimoAtaquePanico > 80) iniciarAtaquePanico();
        else if (emAtaquePanico && nivelAnsiedade < 3.0) finalizarAtaquePanico();
        if (getTime() % 8 == 0) atualizarCores();
    }

    private void gerenciarPosicionamentoEstrategico() {
        if (getOthers() <= 1 || getTime() - tempoUltimoCalculoPosicao < 20) return;
        tempoUltimoCalculoPosicao = getTime();
        double somaX = 0, somaY = 0;
        for (InimigoInfo inimigo : inimigos.values()) {
            somaX += inimigo.x;
            somaY += inimigo.y;
        }
        Ponto pontoDePerigo = new Ponto(somaX / inimigos.size(), somaY / inimigos.size());
        Ponto[] cantos = {
            new Ponto(30, 30), new Ponto(getBattleFieldWidth() - 30, 30),
            new Ponto(30, getBattleFieldHeight() - 30), new Ponto(getBattleFieldWidth() - 30, getBattleFieldHeight() - 30)
        };
        double maiorDistancia = -1;
        Ponto melhorCanto = null;
        for (Ponto canto : cantos) {
            double dist = Math.hypot(pontoDePerigo.x - canto.x, pontoDePerigo.y - canto.y);
            if (dist > maiorDistancia) {
                maiorDistancia = dist;
                melhorCanto = canto;
            }
        }
        pontoSeguro = melhorCanto;
    }

    // ... todos os outros métodos ...
    // onScannedRobot, onHitByBullet, etc.
	// O código deles não muda, então omiti para brevidade, mas eles devem continuar no seu arquivo.
	// Apenas os dois métodos no bloco "MODIFICAÇÕES" foram alterados.
	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		String nomeAtirador = e.getName();
		if (nomeAtirador == null) return;
		
		double potenciaTiro = e.getPower();
		
		if (!atiradoresAtivos.containsKey(nomeAtirador)) {
			atiradoresAtivos.put(nomeAtirador, new AtiradorInfo(nomeAtirador));
		}
		
		AtiradorInfo atirador = atiradoresAtivos.get(nomeAtirador);
		atirador.registrarTiro(potenciaTiro, e.getBullet().getX(), e.getBullet().getY(), getTime());
		
		ultimoAtirador = nomeAtirador;
		tempoUltimoTiro = getTime();
		nivelAnsiedade += 2.0;
		
		if (atirador.atiadorPerigosoDetectado) {
			nivelAnsiedade += 1.0;
		}
		
		if (inimigos.containsKey(nomeAtirador)) {
			InimigoInfo atirador_info = inimigos.get(nomeAtirador);
			double anguloAtirador = Math.toDegrees(Math.atan2(atirador_info.x - getX(), atirador_info.y - getY()));
			double anguloFuga = anguloAtirador + 180 + (randomizador.nextDouble() - 0.5) * 60;
			setTurnRight(Utils.normalRelativeAngleDegrees(anguloFuga - getHeading()));
			setAhead((randomizador.nextDouble() + 0.5) * 150);
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		double anguloInimigo = Math.toRadians(getHeading() + e.getBearing());
		double inimigoX = getX() + e.getDistance() * Math.sin(anguloInimigo);
		double inimigoY = getY() + e.getDistance() * Math.cos(anguloInimigo);

		InimigoInfo infoExistente = inimigos.get(e.getName());
		InimigoInfo novaInfo = new InimigoInfo(inimigoX, inimigoY, e.getVelocity(), e.getHeading(), e.getEnergy(), e.getDistance());
		
		if (infoExistente != null) {
			novaInfo.possivelWaveSurfer = infoExistente.possivelWaveSurfer;
			novaInfo.esquivasDetectadas = infoExistente.esquivasDetectadas;
			novaInfo.ultimaEnergia = infoExistente.energia;
		}
		inimigos.put(e.getName(), novaInfo);
		
		tempoUltimoScan = getTime();
		if (e.getDistance() < 120) {
			nivelAnsiedade += 0.3;
		}
	}

    private void atualizarTrackingAtiradores() {
        atiradoresAtivos.entrySet().removeIf(entry -> {
            AtiradorInfo atirador = entry.getValue();
            return (getTime() - atirador.ultimoTiro > 100) || !inimigos.containsKey(entry.getKey());
        });
    }

    private double calcularAnsiedadeAtiradores() {
        double ansiedadeAtiradores = 0;
        for (AtiradorInfo atirador : atiradoresAtivos.values()) {
            double ameaca = atirador.getNivelAmeaca();
            ansiedadeAtiradores += ameaca * 0.1;
            if (getTime() - atirador.ultimoTiro < 20) {
                ansiedadeAtiradores += 0.8;
            }
        }
        return Math.min(3.0, ansiedadeAtiradores);
    }

    private String getNomeInimigoPorPosicao(double x, double y) {
        for (Map.Entry<String, InimigoInfo> entry : inimigos.entrySet()) {
            InimigoInfo inimigo = entry.getValue();
            double dist = Math.hypot(inimigo.x - x, inimigo.y - y);
            if (dist < 50) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String getAtiradorMaisPerigoso() {
        String maisPerigoso = null;
        double maiorAmeaca = -1;
        for (Map.Entry<String, AtiradorInfo> entry : atiradoresAtivos.entrySet()) {
            AtiradorInfo atirador = entry.getValue();
            double ameaca = atirador.getNivelAmeaca();
            if (ameaca > maiorAmeaca) {
                maiorAmeaca = ameaca;
                maisPerigoso = entry.getKey();
            }
        }
        return maisPerigoso;
    }
    
    private void detectarWaveSurfing(InimigoInfo inimigo) {
        if (Math.abs(inimigo.ultimaEnergia - inimigo.energia) > 0.1) {
            double mudancaVelocidade = Math.abs(inimigo.velocidade);
            if (mudancaVelocidade > 6 && inimigo.distancia > 150) {
                inimigo.esquivasDetectadas++;
                if (inimigo.esquivasDetectadas > 3) {
                    inimigo.possivelWaveSurfer = true;
                    inimigoUsaWaveSurfing = true;
                    out.println("WAVE SURFER DETECTADO: " + alvoAtual);
                }
            }
        }
    }

    private void atualizarCores() {
        int ansiedadeInt = (int)(nivelAnsiedade * 25.5);
        if (emAtaquePanico) {
            setBodyColor(Color.RED);
            setGunColor(Color.ORANGE);
            setRadarColor(Color.YELLOW);
        } else if (inimigoUsaWaveSurfing) {
            setBodyColor(Color.MAGENTA);
            setGunColor(Color.CYAN);
            setRadarColor(Color.WHITE);
        } else {
            setBodyColor(new Color(ansiedadeInt, 0, 255 - ansiedadeInt));
            setGunColor(new Color(ansiedadeInt + 50, ansiedadeInt, 200));
            setRadarColor(new Color(100, ansiedadeInt, 255));
        }
    }
    private void comportamentoAtaquePanicoTatico() {
        out.println("ATAQUE DE PÂNICO TÁTICO! Ansiedade: " + String.format("%.1f", nivelAnsiedade));
        if (getTime() % 3 == 0) {
            double anguloAleatorio = randomizador.nextDouble() * 360 - 180;
            setTurnRight(anguloAleatorio);
            setAhead((randomizador.nextDouble() - 0.5) * 300);
        }
        setMaxVelocity(8);
        setTurnRadarRight(360);
        if (getGunHeat() == 0 && alvoAtual != null) {
            double potenciaAleatoria = 0.3 + randomizador.nextDouble() * 2.0;
            fire(potenciaAleatoria);
        }
    }
    private void scannerInteligente() {
        if (alvoAtual != null && inimigos.containsKey(alvoAtual)) {
            InimigoInfo alvo = inimigos.get(alvoAtual);
            double anguloParaAlvo = Math.toDegrees(Math.atan2(alvo.x - getX(), alvo.y - getY()));
            double anguloRadar = Utils.normalRelativeAngleDegrees(anguloParaAlvo - getRadarHeading());
            double tremorReduzido = (Math.random() - 0.5) * (nivelAnsiedade * 0.8);
            anguloRadar += tremorReduzido;
            setTurnRadarRight(anguloRadar);
        } else {
            setTurnRadarRight(60 + (Math.random() - 0.5) * 40);
        }
    }

    private void sistemaDisparoAntiWaveSurf() {
        String inimigoMaisAmeacador = encontrarInimigoMaisAmeacador();
        if (inimigoMaisAmeacador != null) {
            alvoAtual = inimigoMaisAmeacador;
            InimigoInfo alvo = inimigos.get(inimigoMaisAmeacador);
            double anguloParaAlvo = Math.toDegrees(Math.atan2(alvo.x - getX(), alvo.y - getY()));
            double anguloFinal;
            if (alvo.possivelWaveSurfer) {
                anguloFinal = calcularTiroAntiWaveSurf(alvo, anguloParaAlvo);
            } else {
                double tremorReduzido = (Math.random() - 0.5) * (nivelAnsiedade * 0.5);
                anguloFinal = anguloParaAlvo + tremorReduzido;
            }
            double anguloGun = Utils.normalRelativeAngleDegrees(anguloFinal - getGunHeading());
            setTurnGunRight(anguloGun);
            if (Math.abs(anguloGun) < (alvo.possivelWaveSurfer ? 25 : 15) && getGunHeat() == 0) {
                double potencia = calcularPotenciaInteligente(alvo);
                fire(potencia);
            }
        }
    }

    private double calcularTiroAntiWaveSurf(InimigoInfo alvo, double anguloBase) {
        double fatorRandom = nivelAnsiedade * 3.0;
        double offsetAntiWave = Math.sin(getTime() * 0.1) * fatorRandom;
        double ruidoGaussiano = randomizador.nextGaussian() * 5.0;
        return anguloBase + offsetAntiWave + ruidoGaussiano;
    }

    private double calcularPotenciaInteligente(InimigoInfo alvo) {
        double potenciaBase;
        if (alvo.possivelWaveSurfer) {
            indiceTiroPadrao = (indiceTiroPadrao + 1) % historicoTiros.size();
            potenciaBase = historicoTiros.get(indiceTiroPadrao);
        } else {
            potenciaBase = Math.min(3.0, getEnergy() / 10.0);
            if (alvo.distancia < 150) {
                potenciaBase = Math.max(potenciaBase, 2.0);
            }
        }
        double fatorAnsiedade = 1.0 - (nivelAnsiedade * 0.05);
        return Math.max(0.1, potenciaBase * fatorAnsiedade);
    }
    
    private String encontrarInimigoMaisAmeacador() {
        if (inimigos.isEmpty()) return null;

        String atiradorPerigoso = getAtiradorMaisPerigoso();
        if (atiradorPerigoso != null && inimigos.containsKey(atiradorPerigoso)) {
            AtiradorInfo atirador = atiradoresAtivos.get(atiradorPerigoso);
            InimigoInfo inimigo = inimigos.get(atiradorPerigoso);
            if (atirador.atiadorPerigosoDetectado && inimigo.distancia < 400) {
                return atiradorPerigoso;
            }
        }

        String maisAmeacador = null;
        double maiorAmeaca = -1;
        for (Map.Entry<String, InimigoInfo> entry : inimigos.entrySet()) {
            InimigoInfo inimigo = entry.getValue();
            double ameaca = (500 - inimigo.distancia) + inimigo.energia * 1.5;
            if (atiradoresAtivos.containsKey(entry.getKey())) {
                AtiradorInfo atirador = atiradoresAtivos.get(entry.getKey());
                ameaca += atirador.getNivelAmeaca() * 0.8;
                if (getTime() - atirador.ultimoTiro < 30) {
                    ameaca += 100;
                }
            }
            if (inimigo.possivelWaveSurfer) {
                ameaca += 200;
            }
            if (ameaca > maiorAmeaca) {
                maiorAmeaca = ameaca;
                maisAmeacador = entry.getKey();
            }
        }
        return maisAmeacador;
    }

    private void limparInimigosSumidos() {
        inimigos.entrySet().removeIf(entry -> getTime() - entry.getValue().ultimaVez > 25);
    }
    
    private void iniciarAtaquePanico() {
        emAtaquePanico = true;
        tempoUltimoAtaquePanico = getTime();
    }
    
    private void finalizarAtaquePanico() {
        emAtaquePanico = false;
    }
	
	@Override
    public void onHitWall(HitWallEvent e) {
        nivelAnsiedade += 0.8;
        // A lógica de evasão agora é tratada proativamente pelo evitarParedesInteligente(),
        // mas mantemos um pequeno aumento de ansiedade caso aconteça.
    }
    
    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        inimigos.remove(e.getName());
        atiradoresAtivos.remove(e.getName());
        nivelAnsiedade = Math.max(0, nivelAnsiedade - 1.5);
        if (e.getName().equals(alvoAtual)) {
            inimigoUsaWaveSurfing = false;
        }
    }
    
    @Override
    public void onBulletHit(BulletHitEvent e) {
        nivelAnsiedade = Math.max(0, nivelAnsiedade - 0.4);
    }
    
    @Override
    public void onBulletMissed(BulletMissedEvent e) {
        if (alvoAtual != null && inimigos.containsKey(alvoAtual)) {
            InimigoInfo alvo = inimigos.get(alvoAtual);
            alvo.esquivasDetectadas++;
            nivelAnsiedade += 0.1;
        }
    }
}