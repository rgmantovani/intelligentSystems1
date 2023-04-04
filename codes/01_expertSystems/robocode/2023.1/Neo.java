package Neo;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.*;
import java.lang.*;
import java.util.ArrayList;
import java.awt.Color;

public class Neo extends AdvancedRobot{

	public static int PONTOS = 47;
	public static double PERIGO_ONDAS[] = new double[PONTOS];
	public static double PREVER_PAREDE = 160;
	public static Rectangle2D.Double LIMITE_PAREDE;
	static int[] GUESS_FACTORS = new int[31];
	
	public Point2D.Double posicao_robo;
	public Point2D.Double posicao_inimigo;
	public ArrayList ondas_perigosas = new ArrayList();
	public ArrayList direcoes_surf = new ArrayList();
	public ArrayList anguloabs_surf = new ArrayList();
	public ArrayList<OndaDeTiro> ondas = new ArrayList<OndaDeTiro>();
	
	public double distancia_inimigo;
	public double energia_inimigo = 100.0;
	public double velocidade_inimigo;
	public double distancia_maxima = 200;
	public double distancia_maxima_tiro = 400;
	public double distancia_minima = 120;
	public double virar_aproximar = 5*Math.PI/8.0;
	public double virar_afastar = 3*Math.PI/8.0;
	public double virar_aproximar_D = 6*Math.PI/8.0;
	public double virar_afastar_D = 2*Math.PI/8.0;

	int direcao = 1;

	public void run(){
		setColors(Color.black, Color.black, new Color(57, 255, 20));
		setBulletColor(Color.red);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		LIMITE_PAREDE = new java.awt.geom.Rectangle2D.Double(18, 18, getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
		
		do{
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		}while (true);
	}

	public void onScannedRobot(ScannedRobotEvent e){
		posicao_robo = new Point2D.Double(getX(), getY());
		
		double velocidade_lateral = getVelocity() * Math.sin(e.getBearingRadians());
		double angulo_absoluto = e.getBearingRadians() + getHeadingRadians();
		double energia_bala = energia_inimigo - e.getEnergy();
		setTurnRadarRightRadians(Utils.normalRelativeAngle(angulo_absoluto - getRadarHeadingRadians()) * 2);
		direcoes_surf.add(0, (velocidade_lateral >= 0) ? 1 : -1);
		anguloabs_surf.add(0, angulo_absoluto + Math.PI);
		
		if (energia_bala < 3.01 && energia_bala > 0.09 && direcoes_surf.size() > 2) {
			OndaPerigosa onda_inimigo = new OndaPerigosa();
			onda_inimigo.tempo_tiro = getTime() - 1;
			onda_inimigo.velocidade_bala = velocidadeBala(energia_bala);
			onda_inimigo.distancia_viajada = velocidadeBala(energia_bala);
			onda_inimigo.sentido_surf = ((Integer)direcoes_surf.get(2)).intValue();
			onda_inimigo.angulo_surf = ((Double)anguloabs_surf.get(2)).doubleValue();
			onda_inimigo.posicao_tiro = (Point2D.Double) posicao_inimigo.clone();
			ondas_perigosas.add(onda_inimigo);
		}
		
		energia_inimigo = e.getEnergy();
		distancia_inimigo = e.getDistance();
		posicao_inimigo = projetar(posicao_robo, angulo_absoluto, distancia_inimigo);
		atualizarOndas();
		surfarOndaMaisProxima();
		
		// Aqui é onde definimos a rotina de tiro do robô
		if (energia_inimigo >= 3){
			distancia_inimigo = e.getDistance();
			
			// Encontrar posição do inimigo
			double x_inimigo = getX() + Math.sin(angulo_absoluto) * distancia_inimigo;
			double y_inimigo = getY() + Math.cos(angulo_absoluto) * distancia_inimigo;

			// Processar as ondas
			for (int i = 0; i < ondas.size(); i++) {
				OndaDeTiro onda_atual = (OndaDeTiro) ondas.get(i);
				if (onda_atual.checarHit(x_inimigo, y_inimigo, getTime())) {
					ondas.remove(onda_atual);
					i--;
				}
			}
			double poder = Math.min(3, Math.max(.1, Rules.MAX_BULLET_POWER/1.5));
			
			/* Se ele não está movendo não tentamos adivinhar a direção.
			   Apenas usamos a direção que obtemos anteriormente */
			velocidade_inimigo = e.getVelocity();
			if (velocidade_inimigo != 0) {
				if (Math.sin(e.getHeadingRadians()-angulo_absoluto)*velocidade_inimigo < 0){
					direcao = -1;
				}else{
					direcao = 1;
				}
			}
			
			int[] guess_factors_atuais = GUESS_FACTORS;
			
			OndaDeTiro nova_onda = new OndaDeTiro(getX(), getY(), angulo_absoluto, poder,
					direcao, getTime(), guess_factors_atuais);
					
			// Inicializamos no meio (30/2)
			int melhor_indice = 15;
			for (int i = 0; i < 31; i++)
				if (guess_factors_atuais[melhor_indice] < guess_factors_atuais[i])
					melhor_indice = i;

			double guessfactor = (double) (melhor_indice-(GUESS_FACTORS.length-1)/2) /
											 ((GUESS_FACTORS.length-1)/2);
			double angulo_offset = direcao * guessfactor * nova_onda.anguloMaximoDeEscape();
			double ajustar_arma = Utils.normalRelativeAngle(angulo_absoluto - getGunHeadingRadians() + 
																 angulo_offset);
			setTurnGunRightRadians(ajustar_arma);
			
			ondas.add(nova_onda);
			if (distancia_inimigo < distancia_maxima_tiro){
        		setFire(Rules.MAX_BULLET_POWER/1.5);
			}
			
		}else{
			setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(angulo_absoluto - getGunHeadingRadians()));
			setFire(Rules.MAX_BULLET_POWER/1.5);
		}
	}

	public void atualizarOndas() {
		for (int x = 0; x < ondas_perigosas.size(); x++) {
			OndaPerigosa onda_inimigo = (OndaPerigosa) ondas_perigosas.get(x);
			onda_inimigo.distancia_viajada = (getTime() - onda_inimigo.tempo_tiro) * 
												 onda_inimigo.velocidade_bala;
			if (onda_inimigo.distancia_viajada > posicao_robo.distance(onda_inimigo.posicao_tiro) + 50) {
				ondas_perigosas.remove(x);
				x--;
			}
		}
	}

	public OndaPerigosa getOndaMaisProxima(){
		double distancia_mais_proximo = 50000;
		OndaPerigosa surfar_onda = null;
		for (int x = 0; x < ondas_perigosas.size(); x++){
			OndaPerigosa onda_inimigo = (OndaPerigosa) ondas_perigosas.get(x);
			double distancia = posicao_robo.distance(onda_inimigo.posicao_tiro) - 
														 onda_inimigo.distancia_viajada;
			surfar_onda = onda_inimigo;
			if (distancia > onda_inimigo.velocidade_bala && distancia < distancia_mais_proximo){
				
				distancia_mais_proximo = distancia;
			}
		}
		return surfar_onda;
	}

	public static int getPontoNaOnda(OndaPerigosa onda_inimigo, Point2D.Double posicao_alvo){
		double angulo_offset = (anguloAbsolutoDoTarget(onda_inimigo.posicao_tiro, posicao_alvo) - 
								  onda_inimigo.angulo_surf);
								  
		double fator = Utils.normalRelativeAngle(angulo_offset) / 
						anguloMaximoDeEscape(onda_inimigo.velocidade_bala) * 
						onda_inimigo.sentido_surf;
						
		return (int) limite(0, (fator * ((PONTOS - 1) / 2)) + ((PONTOS - 1) / 2), PONTOS - 1);
	}
 
	public void gravarHit(OndaPerigosa onda_inimigo, Point2D.Double posicao_alvo){
		int indice = getPontoNaOnda(onda_inimigo, posicao_alvo);
		for (int x = 0; x < PONTOS; x++){
			PERIGO_ONDAS[x] += 1.0 / (Math.pow(indice - x, 2) + 1);
		}
	}
	
	public void onHitByBullet(HitByBulletEvent e){
		if (!ondas_perigosas.isEmpty()){
			Point2D.Double local_acertado_por_bala = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			OndaPerigosa onda_hit = null;
			for (int x = 0; x < ondas_perigosas.size(); x++){
				OndaPerigosa onda_inimigo = (OndaPerigosa) ondas_perigosas.get(x);
				if (Math.abs(onda_inimigo.distancia_viajada -
					posicao_robo.distance(onda_inimigo.posicao_tiro)) < 50
					&& Math.abs(velocidadeBala(e.getBullet().getPower()) - 
					onda_inimigo.velocidade_bala) < 0.001){
					onda_hit = onda_inimigo;
					break;
				}
			}
			if (onda_hit != null){
				gravarHit(onda_hit, local_acertado_por_bala);
				ondas_perigosas.remove(ondas_perigosas.lastIndexOf(onda_hit));
			}
		}
	}

	public Point2D.Double predizerPosicao(OndaPerigosa surfar_onda, int sentido_surf){
		Point2D.Double posicao_predita = (Point2D.Double) posicao_robo.clone();
		double velocidade_predita = getVelocity();
		double angulo_predito = getHeadingRadians();
		double virada_maxima, angulo_movimento, direcao_movimento;
		int contador = 0;
		boolean interceptado = false;
		
		do {
			angulo_movimento = suavizarParede(posicao_predita, 
					anguloAbsolutoDoTarget(surfar_onda.posicao_tiro, posicao_predita) + 
					(sentido_surf * (Math.PI/2)), sentido_surf) - 
					angulo_predito;
			direcao_movimento = 1;
			
			if (Math.cos(angulo_movimento) < 0) {
				angulo_movimento += Math.PI;
				direcao_movimento = -1;
			}
			angulo_movimento = Utils.normalRelativeAngle(angulo_movimento);
			virada_maxima = Math.PI / 720d * (40d - 3d * Math.abs(velocidade_predita));
			angulo_predito = Utils.normalRelativeAngle(angulo_predito + limite(-virada_maxima, angulo_movimento, virada_maxima));
			velocidade_predita += (velocidade_predita * direcao_movimento < 0 ? 2 * direcao_movimento : direcao_movimento);
			velocidade_predita = limite(-8, velocidade_predita, 8);
			posicao_predita = projetar(posicao_predita, angulo_predito, velocidade_predita);
			contador++;
			
			if (posicao_predita.distance(surfar_onda.posicao_tiro) < surfar_onda.distancia_viajada + (contador * surfar_onda.velocidade_bala)
					+ surfar_onda.velocidade_bala){
				interceptado = true;
			}
		}while(!interceptado && contador < 500);
		
		return posicao_predita;
	}

	public double checarPerigo(OndaPerigosa surfar_onda, int sentido_surf){
		int indice = getPontoNaOnda(surfar_onda, predizerPosicao(surfar_onda, sentido_surf));
		return PERIGO_ONDAS[indice];
	}

	public void surfarOndaMaisProxima(){
		OndaPerigosa surfar_onda = getOndaMaisProxima();

		if (surfar_onda == null){
			double aproximar_ou_afastar = 0;

			if (distancia_inimigo > distancia_maxima){
				aproximar_ou_afastar = virar_aproximar_D;
				double angulo_para_ir = 0;
				
				if ((posicao_inimigo.x > posicao_robo.x && 
					 posicao_inimigo.y < posicao_robo.y) ||
					(posicao_inimigo.x < posicao_robo.x &&
					 posicao_inimigo.y > posicao_robo.y)){
					angulo_para_ir = suavizarParede(posicao_robo, 
											anguloAbsolutoDoTarget(posicao_inimigo, posicao_robo) + 
											aproximar_ou_afastar, 1);
				}else{
					angulo_para_ir = suavizarParede(posicao_robo, 
											anguloAbsolutoDoTarget(posicao_inimigo, posicao_robo) + 
											aproximar_ou_afastar, -1);
				}
				virarParaTras(this, angulo_para_ir, 10);
			
			}else if (distancia_inimigo < distancia_minima){
				aproximar_ou_afastar = (virar_afastar_D);
				double angulo_para_ir = 0;
				
				if ((posicao_inimigo.x > posicao_robo.x && posicao_inimigo.y < posicao_robo.y) || 
					(posicao_inimigo.x < posicao_robo.x && posicao_inimigo.y > posicao_robo.y)){
					angulo_para_ir = suavizarParede(posicao_robo, anguloAbsolutoDoTarget(posicao_inimigo, posicao_robo) + aproximar_ou_afastar, 1);
				}else{
					angulo_para_ir = suavizarParede(posicao_robo, anguloAbsolutoDoTarget(posicao_inimigo, posicao_robo) + aproximar_ou_afastar, -1);
				}
				virarParaTras(this, angulo_para_ir, 10);
			}
			return;
		}
		
		double perigo_esquerda = checarPerigo(surfar_onda, -1);
		double perigo_direta = checarPerigo(surfar_onda, 1);
		double angulo_para_ir = anguloAbsolutoDoTarget(surfar_onda.posicao_tiro, posicao_robo);

		double aproximar_ou_afastar = 0;
		if (distancia_inimigo > distancia_maxima) {
			aproximar_ou_afastar = (virar_aproximar);
		} else if (distancia_inimigo < distancia_minima) {
			aproximar_ou_afastar = (virar_afastar);
		} else {
			aproximar_ou_afastar = (Math.PI/2);
		}
		if (perigo_esquerda < perigo_direta) {
			angulo_para_ir = suavizarParede(posicao_robo, angulo_para_ir - aproximar_ou_afastar, -1);
		} else {
			angulo_para_ir = suavizarParede(posicao_robo, angulo_para_ir + aproximar_ou_afastar, 1);
		}
		virarParaTras(this, angulo_para_ir, 100);
	}

	class OndaPerigosa {
		Point2D.Double posicao_tiro;
		long tempo_tiro;
		double velocidade_bala, angulo_surf, distancia_viajada;
		int sentido_surf;

		public OndaPerigosa() {
		}
	}

	public double suavizarParede(Point2D.Double local_robo, double angulo, int orientacao) {
		while (!LIMITE_PAREDE.contains(projetar(local_robo, angulo, 160))) {
			angulo += orientacao * 0.05;
		}
		return angulo;
	}

	public static Point2D.Double projetar(Point2D.Double localFonte, double angulo, double comprimento) {
		return new Point2D.Double(localFonte.x + Math.sin(angulo) * comprimento, 
									localFonte.y + Math.cos(angulo) * comprimento);
	}

	public static double anguloAbsolutoDoTarget(Point2D.Double source, Point2D.Double target){
		return Math.atan2(target.x - source.x, target.y - source.y);
	}

	public static double limite(double min, double valor, double max){
		return Math.max(min, Math.min(valor, max));
	}

	public static double velocidadeBala(double poder){
		return (20.0-(3.0*poder));
	}

	public static double anguloMaximoDeEscape(double velocity){
		return Math.asin(8.0/velocity);
	}

	public static void virarParaTras(AdvancedRobot robot, double angulo_para_ir, int vel){
		double angulo = Utils.normalRelativeAngle(angulo_para_ir - robot.getHeadingRadians());
		if (Math.abs(angulo) > (Math.PI/2)){
			if (angulo < 0){
				robot.setTurnRightRadians(Math.PI + angulo);
			}else{
				robot.setTurnLeftRadians(Math.PI - angulo);
			}
			robot.setBack(vel);
		 }else{
			if (angulo < 0) {
				robot.setTurnLeftRadians(-1 * angulo);
			}else{
				robot.setTurnRightRadians(angulo);
			}
			robot.setAhead(100);
		}
	}

	public void onPaint(java.awt.Graphics2D g){
		g.setColor(java.awt.Color.red);
		for (int i = 0; i < ondas_perigosas.size(); i++){
			OndaPerigosa w = (OndaPerigosa) (ondas_perigosas.get(i));
			Point2D.Double center = w.posicao_tiro;
			int radius = (int) w.distancia_viajada;
			if (radius - 40 < center.distance(posicao_robo))
				g.drawOval((int) (center.x - radius), (int) (center.y - radius), radius * 2, radius * 2);
		}
	}

	public class OndaDeTiro{
		private double x_inicial, y_inicial, startBearing, poder;
		private long tempo_atirar;
		private int direcao;
		private int[] segmento_retorno;

		public OndaDeTiro(double x, double y, double bearing, double poder, int direcao, 
							long time, int[] segment){
			x_inicial = x;
			y_inicial = y;
			startBearing = bearing;
			this.poder = poder;
			this.direcao = direcao;
			tempo_atirar = time;
			segmento_retorno = segment;
		}

		public double getVelocidadeBala(){
			return 20 - poder * 3;
		}

		public double anguloMaximoDeEscape(){
			return Math.asin(8 / getVelocidadeBala());
		}

		public boolean checarHit(double x_inimigo, double y_inimigo, long tempo_atual){
		
			// Se a distância da origem da onda ao nosso inimigo passou a distância que a bala teria viajado
			if (Point2D.distance(x_inicial, y_inicial, x_inimigo, y_inimigo) <= 
			   (tempo_atual - tempo_atirar) * getVelocidadeBala()){
				double direcao_desejada = Math.atan2(x_inimigo - x_inicial, y_inimigo - y_inicial);
				double angulo_offset = Utils.normalRelativeAngle(direcao_desejada - startBearing);
				double guessFactor = Math.max(-1, Math.min(1, angulo_offset / anguloMaximoDeEscape()))
									   * direcao;
				int indice = (int) Math.round((segmento_retorno.length - 1) / 2 * (guessFactor + 1));
				segmento_retorno[indice]++;
				
				return true;
			}
			return false;
		}
	}
}