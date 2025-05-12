import robocode.*;
import java.awt.Color;

public class JamantaMasterX extends AdvancedRobot {
  int gunDirection = 1;

  public void run() {
    
    setBodyColor(Color.red);
    setRadarColor(Color.black);
    setGunColor(Color.black);
    setBulletColor(Color.orange);

    
    while (true) {
      turnGunRight(360);
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
   
    setTurnRight(e.getBearing());
    setFireBullet(3);
   
    setAhead(100);
   
    gunDirection = -gunDirection;
    
    setTurnGunRight(360 * gunDirection);
   
    execute();
  }
}