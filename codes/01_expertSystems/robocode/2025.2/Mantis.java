/*Allan Felipe
 * João Pedro Cavani Meireles
 * Manuela Torres
 */

package mantis;
import robocode.*;
import java.awt.Color;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;



public class Mantis extends AdvancedRobot {


	  private int moveDirection = 1;//Controla a direção frente 1 trás -1
    private boolean moveRight = true;//Utilizando para alterar os movimentos
    private int avoidingWallTicks = 0;//Contador para desviar da parede
    private String inimigoAlvo = null; // Para focar em um inimigo 



public void run() {

    setColors(Color.red, Color.blue, Color.green);

    //Canhao radar e corpo se movem independente do corpo do robo
    setAdjustGunForRobotTurn(true);
    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);



 while (true) {
  //Busca o inimigo alvo,evita que o radar trave e evita as paredes
   if (inimigoAlvo == null) {
    setTurnRadarRight(Double.POSITIVE_INFINITY); // Gira o radar para encontrar um inimigo

   }

   if (getRadarTurnRemaining() == 0) {
    setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

   }

   if (avoidingWallTicks > 0) {
    avoidingWallTicks--;
  }

   execute();

 }

}


public void onScannedRobot(ScannedRobotEvent e) {

 // Travar no inimigo
 if (inimigoAlvo == null || inimigoAlvo.equals(e.getName())) {

   inimigoAlvo = e.getName();

   //Trava o radar no inimigo e para de girar

   double absBearingRad = getHeadingRadians() + e.getBearingRadians();
   double radarTurn = normalRelativeAngle(absBearingRad - getRadarHeadingRadians());
   setTurnRadarRightRadians(radarTurn * 1.5); //Faz um adiantamento da posição para evitar erros

    double dist = e.getDistance();
    //Calcula o poder de fogo com base na distancia,mais perto mais forte ,longe fraco
    double calculatedPower = 3.0 - (dist / 250); 

    // Garantia de que o tiro terá pelo menos 0.5 de força e maximo 3
    double powerByDistance = Math.max(0.5, Math.min(3.0, calculatedPower));

    // Verificação para economizar energia
    double firePower = Math.min(powerByDistance, getEnergy() / 4);

    //Calcula a velocidade da bala com base na distancia
    double bulletSpeed = 20 - 3 * firePower;
    //Calcula o tempo da bala para atingir o inimigo
    long timeToHit = (long) (dist / bulletSpeed);

    //Calcula a posição futura ,assumindo que ele continue em linha reta
    double futureX = getX() + dist * Math.sin(absBearingRad) + e.getVelocity() * timeToHit * Math.sin(e.getHeadingRadians());
    double futureY = getY() + dist * Math.cos(absBearingRad) + e.getVelocity() * timeToHit * Math.cos(e.getHeadingRadians());

    //Mira para esta posição futura
    double futureAngle = Math.atan2(futureX - getX(), futureY - getY());
    setTurnGunRightRadians(normalRelativeAngle(futureAngle - getGunHeadingRadians()));

    // Disparo,com uma tolerancia de 5 graus e a arma não está quente
    if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
    fire(firePower);

    }
    // Se estivermos na manobra de evitar paredes, ignora o movimento normal.
    if (avoidingWallTicks > 0) {
        return;
    }

    // Tenta sempre manter uma distancia de 200px
    double distanciaIdeal = 200;
    double erroDistancia = e.getDistance() - distanciaIdeal;

    //Inclui aleatoriedade no movimento com uma chance de 5% de inverter a direção
    if (Math.random() < 0.05) {
        moveDirection *= -1; // Inverte entre 1 e -1
    }

    //Calcula a orbita do robo para correção
    double anguloOrbita = e.getBearing() + 90 - (erroDistancia / 4);
    setTurnRight(anguloOrbita);

    //Fz com que a velocidade e a distancia do movimento seja aleatoria 
    double distanciaMovimento = (Math.random() * 80 + 50) * moveDirection; 
    setAhead(distanciaMovimento);
  }

}

public void onHitByBullet(HitByBulletEvent e) {
//Quando é atingido por uma bala faz um movimento de saida rapida
  moveRight = !moveRight;
  setTurnRight(30);
  setAhead(60);

}

public void onHitWall(HitWallEvent e) {
  //Quando bate na parede inicia o contador para entrar no modo de desvio e executa uma manobra para se afastar da parede
  avoidingWallTicks = 20;
  moveRight = !moveRight;
  setBack(100);
  setTurnRight(90);

}

 
public void onRobotDeath(RobotDeathEvent e) {
//Verifica se o alvo morreu,caso sim libera a trava
  if (e.getName().equals(inimigoAlvo)) {

    inimigoAlvo = null; 
    }

}

}