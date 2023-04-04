import java.awt.Color;
import java.awt.geom.Point2D;

import robocode.*;
import robocode.util.Utils;

public class MkIII_X1 extends AdvancedRobot {

   

    final static double potenciaDoTiro=3;//potencia bala
    final static double velocidadeTiro=20-3*potenciaDoTiro;//formula velocidade da bala 

    //Variaveis
    static double dir=1;
    static double posAntigaInimigo;
    static double energiaInimigo;


    public void run(){

        //Define cores do tanque
        setBodyColor(Color.yellow);
        setGunColor(Color.yellow);
        setRadarColor(Color.yellow);

        setAdjustGunForRobotTurn(true); //permite que a arma do robô gire independentemente do corpo do robô
        setAdjustRadarForGunTurn(true);//permite que o radar do robô gire independentemente da arma do robô
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);// faz com que o radar do robô gire continuamente à direita
    }

    public void onScannedRobot(ScannedRobotEvent e){
    
    double rumoAbsoluto=e.getBearingRadians()+getHeadingRadians(); //calcula o valor absoluto do rumo do inimigo em relação ao robô atual. 

       
    double girar=rumoAbsoluto+Math.PI/2; //Isso faz com que a quantidade que queremos girar seja perpendicular ao inimigo.

    girar-=Math.max(0.5,(1/e.getDistance())*100)*dir; //calcula um valor de ajuste para o ângulo da arma do robô em direção ao inimigo.

    setTurnRightRadians(Utils.normalRelativeAngle(girar-getHeadingRadians()));//valor de ângulo para o robô, que será usado para girá-lo em direção ao inimigo.

    //Este bloco de código detecta quando a energia do oponente cai.
    if(energiaInimigo>(energiaInimigo=e.getEnergy())){

    //Usamos 200/e.getDistance() para decidir se queremos mudar de direção.
    //Isso significa que teremos menos probabilidade de reverter à direita quando estivermos prestes a colidir com o robô inimigo.
        if(Math.random()>200/e.getDistance()){
            dir=-dir;
        }
    }

    //Essa linha nos faz diminuir a velocidade quando precisamos girar bruscamente.
    setMaxVelocity(400/getTurnRemaining());

    setAhead(100*dir);

    //Encontrando a direção e a mudança de direção.
    double dirInimigo = e.getHeadingRadians();
    double MudancaDirInimigo = dirInimigo - posAntigaInimigo;
    posAntigaInimigo = dirInimigo;

    /*Este método de mirar é conhecido como mira circular; você assume que seu inimigo irá
    * continuar se movendo com a mesma velocidade e taxa de rotação que está usando no momento do disparo. 
    * O código base vem da wiki.
    */
    double difTempo = 0;
    double estimaX = getX()+e.getDistance()*Math.sin(rumoAbsoluto);
    double estimaY = getY()+e.getDistance()*Math.cos(rumoAbsoluto);
    while((++difTempo) * velocidadeTiro <  Point2D.Double.distance(getX(), getY(), estimaX, estimaY)){

        //Adicione o movimento que achamos que nosso inimigo fará ao atual X e Y do nosso inimigo
        estimaX += Math.sin(dirInimigo) * e.getVelocity();
        estimaY += Math.cos(dirInimigo) * e.getVelocity();

        //Encontre as mudanças na direção do inimigo.
        dirInimigo += MudancaDirInimigo;

        //Se as coordenadas previstas estiverem fora das paredes, coloque-as a 18 unidades de distância das paredes,
        //para garantir que as coordenadas previstas do inimigo estejam dentro dos limites do campo de batalha.
        estimaX=Math.max(Math.min(estimaX,getBattleFieldWidth()-18),18);
        estimaY=Math.max(Math.min(estimaY,getBattleFieldHeight()-18),18);

    }
    //Encontre o ângulo de mira de nossas coordenadas previstas em relação a nós.
    double mira = Utils.normalAbsoluteAngle(Math.atan2(  estimaX - getX(), estimaY - getY()));

    //Mire e atire.
    setTurnGunRightRadians(Utils.normalRelativeAngle(mira - getGunHeadingRadians()));
    setFire(potenciaDoTiro);
    setTurnRadarRightRadians(Utils.normalRelativeAngle(rumoAbsoluto-getRadarHeadingRadians())*2);
    }
    public void onHitWall(HitWallEvent e){
        dir=-dir;
    }
}
   