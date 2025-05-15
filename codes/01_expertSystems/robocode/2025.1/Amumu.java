
package stone;

import robocode.*;
import robocode.util.Utils;
import robocode.Rules;
import java.awt.Color;
import java.awt.geom.*;

public class Amumu extends AdvancedRobot {

    static final double DIST_CIMA = 17.0;
    static final double DIST_BAIXO = 34.0;
    static final Color zhonyas = new Color(255, 215, 0);
    static double dirAnterior;
    static double velAnterior;
    static double campoLarg;
    static double campoAlt;
    static Rectangle2D.Double areaDisp;
    static double esquiva = 0.2;
    static Point2D.Double proximaPos = new Point2D.Double();

    public Amumu(){

    }

    public static enum Cor{
        AMUMU,
        ZHONYAS,
        DAMAGE
    }

    public void setCorzinha(Cor cor){
        switch(cor){
            case AMUMU:
                setColors(new Color(69, 127, 77), new Color(114,188,109), new Color(190, 238,242));
                break;
            case ZHONYAS:
                setColors(zhonyas, zhonyas, zhonyas);
                break;
            case DAMAGE:
                setColors(new Color(233,0,0), new Color(164,20,20), new Color(164,20,20));
                break;
        }
    }

    public void run() {
        setCorzinha(Cor.AMUMU);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        campoLarg = getBattleFieldWidth();
        campoAlt = getBattleFieldHeight();
        areaDisp = new Rectangle2D.Double(DIST_CIMA, DIST_CIMA, campoLarg-DIST_BAIXO, campoAlt-DIST_BAIXO);

        while (true) {
           this.turnRadarRight(Double.NEGATIVE_INFINITY);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double tempBearing = getHeadingRadians() + e.getBearingRadians();
        double armaBearing;
        double anguloVirar;
        Point2D.Double ponto = new Point2D.Double();
        double poder;
        long delta;
        double tempDir, tempVel;
        double dir, vel;
        double tempHor, tempVer;

        poder = Math.min(getEnergy()-0.1, (e.getDistance() > 74.01065284083685D ? 1.8342171685458617D : 3.0));

        setTurnRadarRightRadians(2.2* Utils.normalRelativeAngle(tempBearing - getRadarHeadingRadians()));

        proximaPos.setLocation(getX()+Math.sin(getHeadingRadians())*getVelocity(),getY()+Math.cos(getHeadingRadians())*getVelocity());

        tempHor = getX()+Math.sin(tempBearing)*e.getDistance();
        tempVer = getY()+Math.cos(tempBearing)*e.getDistance();
        dir = e.getHeadingRadians();
        tempDir = dir - dirAnterior;
        vel = e.getVelocity();
        tempVel = Math.min(1.0, Math.max(vel-velAnterior, -1.0));
        velAnterior = vel;
        delta = -1;
        do{
            tempHor += Math.sin(dir)*vel;
            tempVer += Math.cos(dir)*vel;
            dir += tempDir;
            vel = Math.min(8.0, Math.max(vel+tempVel, -8.0));
            delta++;
            if(!areaDisp.contains(tempHor, tempVer)){
                ponto.setLocation((pontoArena((tempHor), tempVer)));
                if(delta>9){
                    poder = (20.0 - (ponto.distance(proximaPos)-18)/delta)/3.0;
                }
                break;
            }
            ponto.setLocation(tempHor, tempVer);
        }while((int)Math.round((ponto.distance(proximaPos)-18)/Rules.getBulletSpeed(poder))>delta);
        ponto.setLocation(pontoArena(tempHor, tempVer));
        tempBearing = ((Math.PI/2)-Math.atan2(ponto.y-proximaPos.getY(),ponto.x-proximaPos.getX()));
        setTurnGunRightRadians(armaBearing = Utils.normalRelativeAngle(tempBearing-getGunHeadingRadians()));
        if((e.getDistance() <= 350) && (poder>0.0) && (getGunHeat()==0.0) && (Math.abs(armaBearing)<0.35)){
            setFire(poder);
        }
        if((getTime()%6) == 0 && Math.abs(getTurnRemainingRadians())<2*Rules.getTurnRateRadians(getVelocity())){
            esquiva = -esquiva;
        }
        anguloVirar = Utils.normalRelativeAngle(tempBearing-getHeadingRadians());
        setTurnRightRadians(Math.atan(Math.tan(Utils.normalRelativeAngle(anguloVirar+=esquiva))));
        setAhead(Double.POSITIVE_INFINITY * Math.cos(anguloVirar));
    }

    public Point2D.Double pontoArena(double x, double y){
        Point2D.Double posicao = new Point2D.Double();

        posicao.x = Math.min(campoLarg-DIST_BAIXO, Math.max(DIST_BAIXO, x));
        posicao.y = Math.min(campoAlt-DIST_BAIXO, Math.max(DIST_BAIXO, y));
        return posicao;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        setCorzinha(Cor.DAMAGE);
        execute();
        for(int i = 0; i <1; i++){
            doNothing();
        }
        setCorzinha(Cor.AMUMU);
        execute();
    }
    
    public void onWin(WinEvent e){
        setCorzinha(Cor.ZHONYAS);
        setTurnGunRight(Double.POSITIVE_INFINITY);
        setTurnRadarLeft(Double.POSITIVE_INFINITY);
        setTurnRight(10.0);
        ahead(10.0);
        waitFor(new RadarTurnCompleteCondition(this));
    }

}
