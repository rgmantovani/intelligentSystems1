package aa;

//Grupo: 
// Andrei Fernandes Zani, RA: 2367831
// Erik Noda, RA: 2367874
// Thiago Berto Minson, RA: 2270412
import robocode.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import robocode.util.*;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Luva_de_pedreiro extends AdvancedRobot {
    boolean isX1 = true; // Variável para controlar se está em X1 ou Battle Royale

	//-----------------------------------------X1-------------------------------------
		final static double POTENCIA_TIRO = 2;
		final static double VELOCIDADE_TIRO = 20 - POTENCIA_TIRO * 3;
		final static double DANO_BALA = 10;
		static double energiaInimigo;	 
		ArrayList<Luva_de_pedreiro.OndaMovimento> ondasMovimento = new ArrayList<Luva_de_pedreiro.OndaMovimento>();//array para acompanhar o movimento
		ArrayList<Luva_de_pedreiro.OndaArma> ondasArma = new ArrayList<Luva_de_pedreiro.OndaArma>();// array para acompanhar os direcionamentos 
		static double angulosArma[] = new double[16];//array com angulo de movimento mais recente
		
	//-----------------------------------------BR-------------------------------------
		private boolean movimentoAtivo = false; 
   		private boolean noCanto = false; 
   		private String alvo;
  		private byte giros = 0; 
  		private byte direcao = 1;
 		private short energiaAnterior; // energia anterior do robô que estamos mirando
	
//--------------------------------------------------------------------------------
    public void run() {
		//Funcao que retorna se possui um ou mais inimigos em campo
		if (getOthers() == 1) {
   			isX1 =true;
		} else {
     		isX1 =false;
		}
		//cor do nosso robozao
        setColors(Color.gray, Color.black, Color.orange,Color.white,Color.darkGray);

		//ajusta a arma e o radar para direcao oposta
		if(isX1){//-----------------------------------------X1-------------------------------------
			energiaInimigo = 100;
			setAdjustGunForRobotTurn(true);
        	setAdjustRadarForGunTurn(true);
		}else{//-----------------------------------------BR-------------------------------------
			setAdjustGunForRobotTurn(true); 
      		setAdjustRadarForGunTurn(true); 
		}
        while (true) {
            if (isX1) {//-----------------------------------------X1-------------------------------------
                executeX1();
            } else {//-----------------------------------------BR-------------------------------------
                executeBattleRoyale();
            }
        }
    }
//--------------------------------------------------------------------------------
    public void onScannedRobot(ScannedRobotEvent e) {
        if (isX1) {//-----------------------------------------X1-------------------------------------
            executeX1Behavior(e);
        } else {//-----------------------------------------BR-------------------------------------
            executeBattleRoyaleBehavior(e);
        }
    }
//-----------------------------------------X1-------------------------------------
    private void executeX1() {
        if(getRadarTurnRemainingRadians()==0){
				setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
			execute();
    }
//--------------------------------------------------------------------------------
    private void executeX1Behavior(ScannedRobotEvent e) {
        double absBearing = e.getBearingRadians() + getHeadingRadians();
		double mudancaEnergia = (energiaInimigo - (energiaInimigo = e.getEnergy()));
		OndaMovimento w;
		if(mudancaEnergia <= 3 && mudancaEnergia >= 0.1){
			registrarOndaMovimento(e, mudancaEnergia);
		}
		//decidir para onde se mover
		escolherDirecao(projetar(new Point2D.Double(getX(),getY()), e.getDistance(), absBearing));
		
		// Registra uma onda de arma ao disparar;
		if(getGunHeat()==0){
			registrarOndaArma(e);
		}
		//verifica a proximidade de um inimigo
		verificarOndasArma(projetar(new Point2D.Double(getX(),getY()), e.getDistance(), absBearing));
		
		//mirar e atirar
		setTurnGunRightRadians(Utils.normalRelativeAngle(absBearing - getGunHeadingRadians())
				+angulosArma[8+(int)(e.getVelocity()*Math.sin(e.getHeadingRadians()-absBearing))]);
		setFire(POTENCIA_TIRO);
		
		setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians())*2);
    }
//-----------------------------------------------------------------------------------------
	public void onBulletHit(BulletHitEvent e){
		energiaInimigo -= DANO_BALA;
	}
//-----------------------------------------------------------------------------------------

	public void registrarOndaMovimento(ScannedRobotEvent e, double mudancaEnergia){
		double absBearing = e.getBearingRadians() + getHeadingRadians();
		OndaMovimento w = new OndaMovimento();
		//faz o tracking do inimigo
		w.origem = projetar(new Point2D.Double(getX(),getY()), e.getDistance(), absBearing);
		w.velocidade = 20 - 3 * mudancaEnergia;
		w.inicioTempo = getTime();
		w.angulo = Utils.normalRelativeAngle(absBearing + Math.PI);
		w.velLateral = (getVelocity() * Math.sin(getHeadingRadians() - w.angulo)) / w.velocidade;
		ondasMovimento.add(w);
	}
//-----------------------------------------------------------------------------------------

	public void escolherDirecao(Point2D.Double localInimigo){
		OndaMovimento w;
		//analisa cada ângulo individualmente
		double melhorClassificacao = Double.POSITIVE_INFINITY;
		for(double anguloMovimento = 0; anguloMovimento < Math.PI * 2; anguloMovimento += Math.PI / 16D){
			double classificacao = 0;
			
			//Movepoint é a posição em que estaríamos se nos movêssemos um comprimento de robô na direção fornecida.
			Point2D.Double pontoMovimento = projetar(new Point2D.Double(getX(),getY()), 36, anguloMovimento);
			
			//calculo do risco de ser atingido			 
			for(int i = 0; i < ondasMovimento.size(); i++){
				w = ondasMovimento.get(i);
				if(new Point2D.Double(getX(),getY()).distance(w.origem) < (getTime() - w.inicioTempo) * w.velocidade + w.velocidade){
					ondasMovimento.remove(w);
				}
				else{
					classificacao += 1D / Math.pow(pontoMovimento.distance(projetar(w.origem, pontoMovimento.distance(w.origem), w.angulo)), 2);
					classificacao += 1D / Math.pow(pontoMovimento.distance(projetar(w.origem, pontoMovimento.distance(w.origem), w.angulo + w.velLateral)), 2);
				}
			}
			if(ondasMovimento.size() == 0){
				classificacao = 1D / Math.pow(pontoMovimento.distance(localInimigo), 2);
			}
			if(classificacao < melhorClassificacao && new Rectangle2D.Double(50, 50, getBattleFieldWidth() - 100, getBattleFieldHeight() - 100).contains(pontoMovimento)){
				melhorClassificacao = classificacao;
				int direcaoPonto;
				setAhead(1000 * (direcaoPonto = (Math.abs(anguloMovimento - getHeadingRadians()) < Math.PI / 2 ? 1 : -1)));
				setTurnRightRadians(Utils.normalRelativeAngle(anguloMovimento + (direcaoPonto == -1 ? Math.PI : 0) - getHeadingRadians()));
			}
		}
	}
	
//-----------------------------------------------------------------------------------------
	//registra as ondas 
	public void registrarOndaArma(ScannedRobotEvent e){
		OndaArma w = new OndaArma();
		w.absBearing = e.getBearingRadians() + getHeadingRadians();
		w.velocidade = VELOCIDADE_TIRO;
		w.origem = new Point2D.Double(getX(),getY());
		w.velSeg = (int)(e.getVelocity() * Math.sin(e.getHeadingRadians() - w.absBearing));
		w.inicioTempo = getTime();
		ondasArma.add(w);
	}
//-----------------------------------------------------------------------------------------

	//verifica as ondas
	public void verificarOndasArma(Point2D.Double posInimigo){
		OndaArma w;
		for(int i = 0; i < ondasArma.size(); i++){
			w = ondasArma.get(i);
			if((getTime() - w.inicioTempo) * w.velocidade >= w.origem.distance(posInimigo)){
				angulosArma[w.velSeg + 8] = Utils.normalRelativeAngle(Utils.normalAbsoluteAngle(Math.atan2(posInimigo.x - w.origem.x, posInimigo.y - w.origem.y)) - w.absBearing);
				ondasArma.remove(w);
			}
		}
	}
//-----------------------------------------------------------------------------------------
	public Point2D.Double project(Point2D.Double origin,double dist,double angle){
		return new Point2D.Double(origin.x+dist*Math.sin(angle),origin.y+dist*Math.cos(angle));
	}
//-----------------------------------------------------------------------------------------
	//projeta um ponto a partir de outro dado um ângulo e uma distância específicos.	
	public Point2D.Double projetar(Point2D.Double origem, double dist, double angulo){
		return new Point2D.Double(origem.x + dist * Math.sin(angulo), origem.y + dist * Math.cos(angulo));
	}
//-----------------------------------------------------------------------------------------
	//aplica as ondas 
	public static class OndaMovimento{
		Point2D.Double origem;
		double inicioTempo;
		double velocidade;
		double angulo;
		double velLateral;
	}
//-----------------------------------------------------------------------------------------
	public class OndaArma{
		double velocidade;
		Point2D.Double origem;
		int velSeg;
		double absBearing;
		double inicioTempo;
	}
//-----------------------------------------BR----------------------------------------
    private void executeBattleRoyale() {
       turnRadarLeftRadians(1);
    }
//-----------------------------------------------------------------------------------------
	public void onHitByBullet(HitByBulletEvent e){ 
      alvo = e.getName(); 
   }
//-----------------------------------------------------------------------------------------
    private void executeBattleRoyaleBehavior(ScannedRobotEvent e) {
       if (alvo == null || giros > 6) { // se não tivermos um alvo
         alvo = e.getName();
      }
		
	 //padrao de movimentacao em formato quadrado
      if (getDistanceRemaining() == 0 && getTurnRemaining() == 0) { 
         if (noCanto) {
            if (movimentoAtivo) {
               setTurnLeft(90);
               movimentoAtivo = false;
            } else { 
               setAhead(160 * direcao);
               movimentoAtivo = true; 
            }
         } else {
            if ((getHeading() % 90) != 0) {
               setTurnLeft((getY() > (getBattleFieldHeight() / 2)) ? getHeading()
                     : getHeading() - 180);
            }
            else if (getY() > 30 && getY() < getBattleFieldHeight() - 30) {
               setAhead(getHeading() > 90 ? getY() - 20 : getBattleFieldHeight() - getY()
                     - 20);
            }
            else if (getHeading() != 90 && getHeading() != 270) {
               if (getX() < 350) {
                  setTurnLeft(getY() > 300 ? 90 : -90);
               } else {
                  setTurnLeft(getY() > 300 ? -90 : 90);
               }
            }
            else if (getX() > 30 && getX() < getBattleFieldWidth() - 30) {
               setAhead(getHeading() < 180 ? getX() - 20 : getBattleFieldWidth() - getX()
                     - 20);
            }
            else if (getHeading() == 270) {
               setTurnLeft(getY() > 200 ? 90 : 180);
               noCanto = true;
            }
            else if (getHeading() == 90) {
               setTurnLeft(getY() > 200 ? 180 : 90);
               noCanto = true;
            }
         }
      }
      if (e.getName().equals(alvo)) { // se o robô detectado for nosso alvo
         giros = 0; 
         
         // 30% de girar
         if ((energiaAnterior < (energiaAnterior = (short) e.getEnergy())) && Math.random() > 0.70) {
            direcao *= -1; 
         }
         
         setTurnGunRightRadians(Utils.normalRelativeAngle((getHeadingRadians() + e
               .getBearingRadians()) - getGunHeadingRadians())); // mover a arma em direção a eles
         
         if (e.getDistance() < 200) { // se o inimigo estiver perto
            setFire(3); 
         } else {
            setFire(2); 
         }
         
         double giroRadar = getHeadingRadians() + e.getBearingRadians()
               - getRadarHeadingRadians();
         setTurnRadarRightRadians(2 * Utils.normalRelativeAngle(giroRadar));
      } else if (alvo != null) {
         giros++;
      }
   }
 //----------------------------------------------------------------------------------------

}