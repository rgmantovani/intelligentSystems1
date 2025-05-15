package stone;

import robocode.*;
import java.awt.Color;
import java.awt.Graphics2D;
import robocode.util.Utils;
import java.awt.geom.*; 
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Utilities;

/*
“O sangue de Jesus tem poder, faz o inferno estremecer.”
— Irmã Dora.
*/

// API help: https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html
//Tutorial usado de base para o wavesurf: https://robowiki.net/wiki/Wave_Surfing_Tutorial
//Tutorial de base para o guess factor targeting: https://robowiki.net/wiki/GuessFactor_Targeting_Tutorial

//O SANGUE DE JESUS TEM PODER v2.22
//v1.00: WaveSurf e GuessFactor Targetting seguindo os tutoriais
//v2.00: reescrito do zero, apenas WaveSurf foi implementado nesse momento
//v2.01: adicionado gráficos com o onPaint() para ajudar no debbug
//v2.10: adicionado segmentação no WaveSurf
	//v2.12: adicionado número de visitas para os BINS
	//v2.14: adicionado rolling averages no WaveSurf
	//v2.16: atualizado onBulletHitBullet
	//v2.17: adicionado opção de ficar parado no WaveSurf
//v2.20: implementado o GuessFactor Targetting
	//V2.22: adicionado segmentação na arma

//-------------------------------------------------------------------------------------------------------------
public class SangueDeJesus extends AdvancedRobot{
	public static double _inimigoX;
	public static double _inimigoY;
	public static double _inimigoEnergia = 100.0;

	public static int BINS = 47; //qnd de "pontos" na onda
	public static int DIST_SEG = 5;//para segmentacao(ajuda na previsao)
	public static int VELO_SEG = 5;
	public static double _statsSurf[][][] = new double[DIST_SEG][VELO_SEG][BINS];
	public static int _contVisitas[][][] = new int[DIST_SEG][VELO_SEG][BINS];
	public static double _alphaRoll = 0.3; //pro rolling averages, 0 é mais conservados, 1 muda mais rápido
	public Point2D.Double _meuLocal;
	public Point2D.Double _inimigoLocal;

	public ArrayList _ondasInimigas;
	public ArrayList _direcoesSurf;//nossa direcao em relacao ao inimigo(1 hora, -1 anti), a ultima
								//direcao q ele pode ter mirado e de 2 ticks antes da queda de energia
	public ArrayList _angAbsSurf;

	public static Rectangle2D.Double _campoRetang;
	public Point2D.Double _pontoProjetado = null;
	public static double DIST_FRENTE = 160;

	public List<OndaBala> _ondasBala = new ArrayList<OndaBala>();
	public static int[][][] _statsBala = new int[DIST_SEG][VELO_SEG][BINS];
	public int _direcaoBala = 1;
//=============================================================================================================================================
	public void run() {
		setColors(new Color(71, 6, 6), new Color(204, 0, 0), new Color(124, 82, 82)); // body,gun,radar
		setRadarColor(new Color(255, 128, 0));
		setAdjustGunForRobotTurn(true); //separa a arma do robo
		setAdjustRadarForGunTurn(true); //separa o radar da arma

		_campoRetang = new java.awt.geom.Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);//tamanho do campo, 18 é o tamanho do robo (36 no total, 18 pra cada lado)
		_ondasInimigas = new ArrayList();
		_direcoesSurf = new ArrayList();
		_angAbsSurf = new ArrayList();
	
	    do{
	        turnRadarRightRadians(Double.POSITIVE_INFINITY);
	    }while (true);
	}//run
//=============================================================================================================================================
	public void onScannedRobot(ScannedRobotEvent e) {
		double inimigoAngAbs = e.getBearingRadians()+getHeadingRadians();//ang abs do inimigo no campo
																//0 norte, 90 leste(dir), 180 sul, 270 oeste(esq)
		_inimigoX = getX()+Math.sin(inimigoAngAbs)*e.getDistance();
		_inimigoY = getY()+Math.cos(inimigoAngAbs)*e.getDistance();

		_meuLocal = new Point2D.Double(getX(), getY());
		double veloLateral = getVelocity()*Math.sin(e.getBearingRadians());

		setTurnRadarRightRadians(Utils.normalRelativeAngle(inimigoAngAbs-getRadarHeadingRadians())*2);//*2 pra travar melhor*/
		_direcoesSurf.add(0, new Integer((veloLateral>=0)? 1 : -1));//se o inimigo ta indo pra esq ou dir
		_angAbsSurf.add(0, new Double(inimigoAngAbs+Math.PI));//seria da onde a bala ta vindo

		double inimigoForca = _inimigoEnergia-e.getEnergy();
		if(inimigoForca<3.01 && inimigoForca>0.09 && _direcoesSurf.size()>2){
			OndaInimiga onda = new OndaInimiga();
			onda.tempoFogo = getTime()-1;
			onda.veloBala = velocidadeBala(inimigoForca);
			onda.distViajada = velocidadeBala(inimigoForca);
			onda.direcao = ((Integer)_direcoesSurf.get(2)).intValue();
			onda.angDireto = ((Double)_angAbsSurf.get(2)).doubleValue();
			onda.localFogo = (Point2D.Double)_inimigoLocal.clone();
			onda.veloInimigo = e.getVelocity();

			_ondasInimigas.add(onda);
		}//if nova onda

		_inimigoEnergia = e.getEnergy();
		_inimigoLocal = projetaPonto(_meuLocal, inimigoAngAbs, e.getDistance());

		attOndas();
		vaiSurfar();

		//parte da mira
		for(int i=0; i<_ondasBala.size(); i++){
			OndaBala ondaAtuBala = (OndaBala)_ondasBala.get(i);
			if(ondaAtuBala.verificaHit(_inimigoX, _inimigoY, getTime())){
				_ondasBala.remove(ondaAtuBala);//onda acertou, entao pode tirar
				i--;
			}//if onda pasosu
		}//for ondas

		double forcaTiro = Math.min(3, Math.max(.1, 600/e.getDistance()));
		if(e.getVelocity()!=0){
			if(Math.sin(e.getHeadingRadians()-inimigoAngAbs)*e.getVelocity()<0)//direcao lateral do inimigo
				_direcaoBala = -1;
			else
				_direcaoBala = 1;
		}//direita ou esquerda em relacao a nos

		int segDist = segDistancia(e.getDistance());
		int segVelo = segVelocidade(e.getVelocity());
		int[] statsBalaAtual = _statsBala[segDist][segVelo];
		OndaBala novaOnda = new OndaBala(getX(), getY(), inimigoAngAbs, forcaTiro, _direcaoBala, getTime(), statsBalaAtual);
		
		int melhorIndex = (BINS-1)/2; //comeca no meio, guessFac 0
		for(int i=0; i<BINS; i++){
			if(statsBalaAtual[melhorIndex]<statsBalaAtual[i])
				melhorIndex = i;
		}//for BINS_ARMA
			double guessFactor = (double)(melhorIndex-(_statsBala[segDist][segVelo].length-1)/2)/((_statsBala[segDist][segVelo].length-1)/2);
			double angOffset = _direcaoBala*guessFactor*angEscapeMax(velocidadeBala(novaOnda.forca));
			double ajusteArma = Utils.normalRelativeAngle(inimigoAngAbs-getGunHeadingRadians()+angOffset);
			setTurnGunRightRadians(ajusteArma);

			if(getGunHeat()==0 && ajusteArma<Math.atan2(9, e.getDistance()) && setFireBullet(forcaTiro)!=null){
				_ondasBala.add(novaOnda);
			}//atira se a mira tiver meio perto

		//pra manter distancia
		if(e.getDistance() < 100){ 
			double angVai = suavizaParede(_meuLocal, (-(e.getBearingRadians())), 1);//ang negativo pra ir ao contrário
			setFrenteComoTras(this, angVai); //se afasta
		}//dist de 100
	}//scanned robot
//-------------------------------------------------------------------------------------------------------------
	public void attOndas(){
		for(int i=0; i<_ondasInimigas.size(); i++){
			OndaInimiga onda = (OndaInimiga)_ondasInimigas.get(i);
			onda.distViajada = (getTime()-onda.tempoFogo)*onda.veloBala;
			if(onda.distViajada>_meuLocal.distance(onda.localFogo)+50){
				_ondasInimigas.remove(i);
				i--;
			}//if passou
		}//for tds as ondas
	}//atualiza as onda
//-------------------------------------------------------------------------------------------------------------
	public OndaInimiga ondaMaisProxima(){
		double distPerto = 50000;//valor grande só pra pegar a primeira
		OndaInimiga ondaSurf = null;

		for(int i=0; i<_ondasInimigas.size(); i++){
			OndaInimiga onda = (OndaInimiga)_ondasInimigas.get(i);
			double dist = _meuLocal.distance(onda.localFogo)-onda.distViajada;
			if(dist>onda.veloBala && dist<distPerto){
				ondaSurf = onda;
				distPerto = dist;
			}//ve se ta mais perto
		}//passa por tds as ondas
		return ondaSurf;
	}//onda perto
//-------------------------------------------------------------------------------------------------------------
	public static int fatorIndex(OndaInimiga onda, Point2D.Double localAlvo){
		double angOffset = (angAbsoluto(onda.localFogo, localAlvo)-onda.angDireto);//ang relativo q mirou em nos
		double fator = Utils.normalRelativeAngle(angOffset)/angEscapeMax(onda.veloBala)*onda.direcao;
		return (int)limitaValor(0, (fator*((BINS-1)/2))+((BINS-1)/2), BINS-1);
	}//calcula qual BIN q foi atingido
//-------------------------------------------------------------------------------------------------------------
	public void registraHit(OndaInimiga onda, Point2D.Double localAlvo){
		int index = fatorIndex(onda, localAlvo);//esse seria o centro da regiao perigosa
		int segDist = segDistancia(_meuLocal.distance(onda.localFogo));
		int segVelo = segVelocidade(onda.veloInimigo);
		for(int i=0; i<BINS; i++){
			double val = 1.0/(Math.pow(index-i, 2)+1);
			_statsSurf[segDist][segVelo][i] = (1-_alphaRoll)*_statsSurf[segDist][segVelo][i]+_alphaRoll*val;
			_contVisitas[segDist][segVelo][i] += 1;
		}//for
	}//registra hit
//-------------------------------------------------------------------------------------------------------------
	public Point2D.Double prevePos(OndaInimiga ondaSurf, int direcao){
		Point2D.Double posPrevista = (Point2D.Double)_meuLocal.clone();
		double veloPrevista = getVelocity();
		double dirPrevista = getHeadingRadians();
		double maxEsterco, moveAng, moveDir;

		int cont = 0; //num d tick no futuro
		boolean interceptado = false;

		do{
			moveAng = suavizaParede(posPrevista, angAbsoluto(ondaSurf.localFogo, posPrevista)+(direcao*(Math.PI/2)), direcao)-dirPrevista;
			moveDir = 1;

			if(Math.cos(moveAng)<0){//direcao do movimento
				moveAng += Math.PI;
				moveDir = -1;
			}//if
			moveAng = Utils.normalRelativeAngle(moveAng);
			maxEsterco = Math.PI/720d*(40d-3d*Math.abs(veloPrevista));//max q vira em 1 tick
			dirPrevista = Utils.normalRelativeAngle(dirPrevista+limitaValor(-maxEsterco, moveAng, maxEsterco));
			veloPrevista += (veloPrevista*moveDir<0? 2*moveDir : moveDir);//se veloP e moveD tiver sinal diferente, freia, se ñ acelera
			veloPrevista = limitaValor(-8, veloPrevista, 8); //limite do jogo de velocidade

			posPrevista = projetaPonto(posPrevista, dirPrevista, veloPrevista);
			cont++;
			if(posPrevista.distance(ondaSurf.localFogo)<ondaSurf.distViajada+(cont*ondaSurf.veloBala)+ondaSurf.veloBala){
				interceptado = true;
			}//if
		}while(!interceptado && cont<500);
		return posPrevista;
	}//preve posicao
//-------------------------------------------------------------------------------------------------------------
	public double verificaPerigo(OndaInimiga ondaSurf, int direcao){
		Point2D.Double pos;
		if(direcao==0){
			pos = _meuLocal;
		}else{
			pos = prevePos(ondaSurf, direcao);
		}//caso de ficar parado ser mais seguro
		int index = fatorIndex(ondaSurf, pos);
		int segDist = segDistancia(_meuLocal.distance(ondaSurf.localFogo));
		int segVelo = segVelocidade(ondaSurf.veloInimigo);
		
		if(_contVisitas[segDist][segVelo][index]==0){ //faz uma media dos perigos pra por nos bins nao visitados
			double soma = 0;
			int cont = 0;
			for(int d=0; d<DIST_SEG; d++){
				for(int v=0; v<VELO_SEG; v++){
					for(int b=0; b<BINS; b++){
						if(_contVisitas[d][v][b]>0){
							soma += _statsSurf[d][v][b]/_contVisitas[d][v][b];
							cont++;
						}//if ja visitou
					}//for bins
				}//for vel seg
			}//for dist seg
			return cont>0 ? soma/cont : 0.5;
		}//if faz uma media pro bins q ainda nao visitou
		return _statsSurf[segDist][segVelo][index] / _contVisitas[segDist][segVelo][index];
	}//ve perigo
//-------------------------------------------------------------------------------------------------------------
	public void vaiSurfar(){
		OndaInimiga ondaSurf = ondaMaisProxima();
		if(ondaSurf==null){return;}
		
		double perigoEsq = verificaPerigo(ondaSurf, -1);
		double perigoDir = verificaPerigo(ondaSurf, 1);
		double perigoParado = verificaPerigo(ondaSurf, 0);
		double angVai = angAbsoluto(ondaSurf.localFogo, _meuLocal);

		if(perigoParado<perigoEsq && perigoParado<perigoDir){
			setAhead(0);//parado mais seguro
			setTurnRight(0);
		}else if(perigoEsq<perigoDir){
			angVai = suavizaParede(_meuLocal, angVai-(Math.PI/3), -1);
		}else{
			angVai = suavizaParede(_meuLocal, angVai+(Math.PI/3), 1);
		}//if
		setFrenteComoTras(this, angVai);
	}//vai surfar
//=============================================================================================================================================
	public void onHitByBullet(HitByBulletEvent e) {
		if(!_ondasInimigas.isEmpty()){
			Point2D.Double localAcerto = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			OndaInimiga ondaHit = null;

			for(int i=0; i<_ondasInimigas.size(); i++){//algo acertou, entao procura qual onda q foi
				OndaInimiga onda = (OndaInimiga)_ondasInimigas.get(i);
				if(Math.abs(onda.distViajada-_meuLocal.distance(onda.localFogo))<50 && Math.abs(velocidadeBala(e.getBullet().getPower())-onda.veloBala)<0.001){
				//dist viajada deve ser parecida com a dist minha-localFogo e a velocidade deve ser parecida com a guardada
					ondaHit = onda;
					break;
				}//if achou
			}//for
			if(ondaHit != null){
				registraHit(ondaHit, localAcerto);
				_ondasInimigas.remove(_ondasInimigas.lastIndexOf(ondaHit));
			}//if acertaro
		}//if tem onda vindo
	}//hit by bullet
//=============================================================================================================================================
	public void onBulletHitBullet(BulletHitBulletEvent e){
		if(!_ondasInimigas.isEmpty()){
			Point2D.Double localAcerto = new Point2D.Double(e.getBullet().getX(), e.getBullet().getY());
			OndaInimiga ondaHit = null;

			for(int i=0; i<_ondasInimigas.size(); i++){//algo acertou, entao procura qual onda q foi
				OndaInimiga onda = (OndaInimiga)_ondasInimigas.get(i);
				if(Math.abs(onda.distViajada-_meuLocal.distance(onda.localFogo))<50 && Math.abs(velocidadeBala(e.getBullet().getPower())-onda.veloBala)<0.001){
				//dist viajada deve ser parecida com a dist minha-localFogo e a velocidade deve ser parecida com a guardada
					ondaHit = onda;
					break;
				}//if achou
			}//for
			if(ondaHit != null){
				registraHit(ondaHit, localAcerto);
				_ondasInimigas.remove(_ondasInimigas.lastIndexOf(ondaHit));
			}//if acertaro
		}//if tem onda vindo
	}//baleia baleia baleia
//=============================================================================================================================================
	public void onHitWall(HitWallEvent e) {

	}//hit wall
//=============================================================================================================================================
	public void onWin(WinEvent e){
		turnRadarRight(180);
		turnRadarLeft(180);
	}//win
//=============================================================================================================================================
//métodos de ajuda para o wave surf
	class OndaInimiga{
		Point2D.Double localFogo;
		long tempoFogo;
		double veloBala, angDireto, distViajada;
		int direcao;
		double veloInimigo;

		public OndaInimiga(){}
	}//onda inimiga
//-------------------------------------------------------------------------------------------------------------
	public double suavizaParede(Point2D.Double localizacao, double ang, int orientacao){
		while(!_campoRetang.contains(_pontoProjetado = projetaPonto(localizacao, ang, DIST_FRENTE))){//verifica se o ponto ainda ta dentro do limite no campo
			ang += orientacao*0.05; //vai ajustando o angulo pra "arrendodar" a quina da parede
		}//projeta um ponto na frente DIST_PAREDE de distancia do robo na direcao de ang
		return ang;
	}//suavizador de parede
//-------------------------------------------------------------------------------------------------------------
	public static Point2D.Double projetaPonto(Point2D.Double origem, double ang, double comp){
		return new Point2D.Double(origem.x + Math.sin(ang)*comp, origem.y+Math.cos(ang)*comp);
	}//faz o ponto
//-------------------------------------------------------------------------------------------------------------
	public static double angAbsoluto(Point2D.Double origem, Point2D.Double alvo){
		return Math.atan2(alvo.x-origem.x, alvo.y-origem.y);
	}//ang abs
//-------------------------------------------------------------------------------------------------------------
	public static double limitaValor(double min, double valor, double max){
		return Math.max(min, Math.min(valor, max));
	}//pega o menor entre o valor e o maximo, dps o maior entre resultado e minimo
//-------------------------------------------------------------------------------------------------------------
	public static double velocidadeBala(double forca){
		return (20.0-(3.0*forca));
	}//calcula a velocidade da bala
//-------------------------------------------------------------------------------------------------------------
	public static double angEscapeMax(double velocidade){
		return Math.asin(8.0/velocidade);
	}//calcula o angulo maximo q o robo pode desviar de uma bala baseada na velocidade dele
//-------------------------------------------------------------------------------------------------------------
	public static void setFrenteComoTras(AdvancedRobot robo, double angVai){
		double ang = Utils.normalRelativeAngle(angVai-robo.getHeadingRadians()); //dif entre direcao atual e a desejada(angVai), o normalRel...() é pra normalizar entre pi e -pi, deixar no menor ang possível
		if(Math.abs(ang)>(Math.PI/2)){//se maior q 90, entao o alvo ta pra tras(0 graus seria a frente)
			if(ang<0){
				robo.setTurnRightRadians(Math.PI+ang);
			}else{
				robo.setTurnLeftRadians(Math.PI-ang);
			}//if 
			robo.setBack(100);
		}else{
			if(ang<0){
				robo.setTurnLeftRadians(-1*ang);
			}else{
				robo.setTurnRightRadians(ang);
			}//
			robo.setAhead(100);
		}//por exemplo, se o alvo ta a 170, invés de virar quase 180 ele roda 10 pro outro lado e da ré
	}//
//-------------------------------------------------------------------------------------------------------------
	public int segDistancia(double dist){
		return Math.min((int)(dist/150), DIST_SEG-1);
	}//segDist
//-------------------------------------------------------------------------------------------------------------
	public int segVelocidade(double velo){
		return Math.min((int)Math.abs(velo)/2, VELO_SEG-1);
	}//segVelo
//=============================================================================================================================================
//métodos para o guessfact target
	class OndaBala{
		private double inicioX, inicioY, inicioAng, forca;
		private long tempoTiro;
		private int direcao;
		private int[] segRetorno;

		public OndaBala(double x, double y, double ang, double forca, int dir, long tempo, int[] seg){
			inicioX = x;
			inicioY = y;
			inicioAng = ang;
			this.forca = forca;
			direcao = dir;
			tempoTiro = tempo;
			segRetorno = seg;
		}//onda bala
//-------------------------------------------------------------------------------------------------------------
		public boolean verificaHit(double inimigoX, double inimigoY, long tempoAtual){
			if(Point2D.distance(inicioX, inicioY, inimigoX, inimigoY)<=(tempoAtual-tempoTiro)*velocidadeBala(forca)){
				double dirDesejada = Math.atan2(inimigoX-inicioX, inimigoY-inicioY);
				double angOffset = Utils.normalRelativeAngle(dirDesejada-inicioAng);
				double guessFactor = Math.max(-1, Math.min(1, angOffset/angEscapeMax(velocidadeBala(forca))))*direcao;
				int index = (int)Math.round((segRetorno.length-1)/2*(guessFactor+1));//acha o BIN com o guessFac
				segRetorno[index]++;//aumenta o BIN q acerto
				return true;
			}//se a onda acertou, q é diferente do tiro acertar
			return false;
		}//verifica hit
	}//onda da minha bala
//=============================================================================================================================================
	public void onPaint(Graphics2D g) {
	//destaque no inimigo
		g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
		g.drawLine((int)_inimigoX, (int)_inimigoY, (int)getX(), (int)getY());
		g.fillRect((int)(_inimigoX-20), (int)(_inimigoY-20), 40, 40);

	//ponto projetado
		if(_pontoProjetado != null){
			g.setColor(Color.ORANGE);
			int raio=5;
			g.fillOval((int)(_pontoProjetado.x-raio), (int)(_pontoProjetado.y-raio), raio*2, raio*2);
		}//if

	//ondas
		g.setColor(Color.BLACK);
		for(Object obj: _ondasInimigas){
			OndaInimiga onda = (OndaInimiga)obj;
			double raio = (getTime()-onda.tempoFogo)*onda.veloBala;
			int x = (int)(onda.localFogo.x-raio);
			int y = (int)(onda.localFogo.y - raio);
			int dia = (int)(2*raio);
			g.drawOval(x, y, dia, dia);

	//BINS das ondas
			int segDist = segDistancia(_meuLocal.distance(onda.localFogo));
			int segVelo = segVelocidade(onda.veloInimigo);
			double max = 0;
			for(int i=0; i<BINS; i++){
				if(_contVisitas[segDist][segVelo][i]>0){
					double med = _statsSurf[segDist][segVelo][i] / _contVisitas[segDist][segVelo][i];
					max = Math.max(max, med);
				}//if
			}//for max

			for(int i=0; i<BINS; i++){
				if(_contVisitas[segDist][segVelo][i]==0) continue;

				double guessFactor = (i-(BINS-1)/2.0)/((BINS-1)/2.0);
				guessFactor *= onda.direcao;
				double ang = onda.angDireto+guessFactor*angEscapeMax(onda.veloBala);
				Point2D.Double centroBin = projetaPonto(onda.localFogo, ang, onda.distViajada);

				double med = _statsSurf[segDist][segVelo][i] / _contVisitas[segDist][segVelo][i];
				float intensidade = (float)(med/max);
				intensidade = Math.min(1f, Math.max(0f, intensidade));

				float verm = intensidade;
				float verd = 1f-intensidade;
				g.setColor(new Color(verm, verd, 0f));
				g.fillOval((int)centroBin.x-2, (int)centroBin.y-2, 4, 4);
			}//for bins
		}//for ondas

	}//onPaint
}//SANGUE DE JESUS TEM PODER
