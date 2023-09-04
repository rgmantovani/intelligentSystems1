package pacotinho;

import robocode.*;
import robocode.util.*;
import java.awt.geom.*;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * TheBeast - a robot by (João Vitor Garcia Carvalho, Gabriel Finger Conte and
 * Maria Eduarda Pedroso).
 */
public class TheBeast extends AdvancedRobot {

    /* Variáveis auxiliares */
    private double last_scan_dist = 0;
    private double angle_absolute = 0;
    private double pos_enemy_x = 0;
    private double pos_enemy_y = 0;
    private int state = 0;
    private int atack = 0;
    private int scan_direction = 0;
    private double moveAmount;
    private double scan_vel = 10;
    private double[] angle_oscil = { 0, 30, 0, -30, 0 };
    private int sentido = 1;
    private int[] oscil = { 0, 1, 0, -1, 0 };
    private double theta = 0;
    private String nomePresa = null;
    private int count = 0;

    /**
     * Bota a besta pra funcionar
     */
    public void run() {

        // Colore a besta
        setColors(new Color(78, 47, 47), Color.black, Color.black, Color.white, Color.black);

        // Da uma geral, reconhecendo o tamanho da área
        moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());

        // Deixa a presa correr solta bebê
        setAdjustGunForRobotTurn(true);

        // Como a besta se comporta
        while (true) {

            if (getOthers() == 1) { // Se só resta mais uma presa
                // mano a mano
                this.state = 1;
                // da umas olhadinha pro lado pra ficar ligado
                turnGunRight(10);
            } else { // Se tem várias presas

                if (getEnergy() > 25) {
                    if (getEnergy() < 70) {
                        // combate afastado
                        this.state = 2;
                        turnGunRight(10);
                    } else { // Se to cheio de energia
                        this.state = 3;
                        // da uma olhada, e partiu pra caça
                        turnGunRight(10);
                    }
                } else { // Se to machucado
                    this.state = 4;
                    // Mete o pé pra se recuperar
                    applyMeteoLocoEFoge();
                } // else

            } // else
            count++;
            // Admite que perdeu a presa
            if (count > 11) {
                nomePresa = null;
            } // if
        } //  while
    }//  run

    /*
     * Quando a energia tá baixa, prioriza a sobrevivência. Após se adaptar,
     * passou-se de geração em geração que a melhor estratégia era ficar nas paredes
     * para não ser encurralada.
     */
    public void applyMeteoLocoEFoge() {
        // Ignora tudo e olha pra uma parede
        turnLeft(getHeading() % 90);
        // Aguça os sentidos da besta pra devora cada possibilidade de fonte de energia
        setTurnRadarRight(Double.POSITIVE_INFINITY);
        // Corre pra parede
        ahead(moveAmount);

        // Vira no canto pra próxima parede
        turnRight(90);
        // Da uma olhada com as presas pra fora
        turnGunRight(45);
    }

    /**
     * Mira na presa.
     */
    public void miraNaPresa(ScannedRobotEvent e) {
        // Verifica a velocidade da presa
        this.scan_vel = 0;
        out.println("Scan vel: " + this.scan_vel);

        // conseguir posição do inimigo
        this.angle_absolute = getHeadingRadians() + e.getBearingRadians();
        this.pos_enemy_x = getX() + Math.sin(this.angle_absolute) * e.getDistance();
        this.pos_enemy_y = getX() + Math.cos(this.angle_absolute) * e.getDistance();

        // Acha o ânguo da presa
        theta = (Math.atan2(pos_enemy_x - getX(), pos_enemy_y - getY())) * 180 / 3.14159;
        if (theta < 0) { // Se der negativo, corrige
            theta = 360 + theta;
        } // if

        // Vira o canhão para ele
        setTurnGunRight(normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading())));
    }

    /**
     * onScannedRobot: What to do when you see another robot
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        // Replace the next line with any behavior you would like

        switch (this.state) {
            case 1: // applyVemTranquilo

                // Mira na presa
                miraNaPresa(e);
                // Vira o corpo para ele
                setTurnRight(e.getBearing());

                // Segue a presa
                setAhead(10);
                // Se está muito perto, recua um pouco
                if (e.getDistance() < 100)
                    setBack(50);
                // Mete bala
                meteBala(e);

                break;

            case 2: // applySaindodeFininho

                // Se não for a presa, ignora
                if (nomePresa != null && !e.getName().equals(nomePresa)) {
                    return;
                }

                // Se não tinha presa, agora tem
                if (nomePresa == null) {
                    nomePresa = e.getName();
                }

                // Marca que achou a presa
                count = 0;

                // Mira na presa
                miraNaPresa(e);

                // Vira o corpo para ele
                setTurnRight(e.getBearing());

                // Segue a presa
                setAhead(10);
                // Se está muito perto, recua um pouco
                if (e.getDistance() < 250)
                    setBack(40);
                // Mete bala
                meteBala(e);

                break;

            case 3: // applyCassadaCelwagem

                // Se não for a presa, ignora
                if (nomePresa != null && !e.getName().equals(nomePresa)) {
                    return;
                }

                // Se não tinha presa, agora tem
                if (nomePresa == null) {
                    nomePresa = e.getName();
                }

                // Marca que achou a presa
                count = 0;

                // Mira na presa
                miraNaPresa(e);

                // Vira o corpo para ele
                setTurnRight(e.getBearing());

                // Segue a presa
                setAhead(10);
                // Se está muito perto, recua um pouco
                if (e.getDistance() < 100)
                    setBack(40);
                // Mete bala
                meteBala(e);

                break;

            case 4: // applyMeteoLocoEFoge

                // Mira na presa
                miraNaPresa(e);
                // Mete bala
                meteBala(e);

                break;
        }

    }

    /**
     * Decide como a besta vai atacar, baseado na distancia da presa.
     */
    public void meteBala(ScannedRobotEvent e) {
        if (getGunHeat() == 0) { // Ve se ta pronto pro bote
            if (e.getDistance() < 150) { // Se tiver do lado, rasga tudo
                fire(3);
            } else if (e.getDistance() < 300) {// Se tiver perto, segura um pouco na forca
                fire(2);
            } else { // Se tiver longe, só um tapa
                fire(1);
            }
        }
    }

    /**
     * Quando alguem me acerta uma bala, se só tem eu e ele agora eu sei onde minha
     * presa está.
     */
    public void onHitByBullet(HitByBulletEvent e) {
        // Dá uma rezinha
        setBack(10);

        // Se só tem mais uma presa
        if (getOthers() < 3) {
            // Aponta pra onde ela está para dar o bote
            double absoluteBearing = getHeading() + e.getBearing();
            double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
            setTurnGunRight(bearingFromGun);
        }
    }

    /**
     * Caso o robo tenha colidido com outro, vira a arma pro salafrário e mete bala.
     * Além de sair de perto
     * do mesmo, afinal de contas bestas não gostam de contato físico.
     */
    public void onHitRobot(HitRobotEvent e) {
        // Marca o salafrário
        nomePresa = e.getName();
        // Calcula o angulo do salafrário
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        // Mira a arma e mete bala
        setTurnGunRight(bearingFromGun);
        fire(3);

        // Sai de perto
        if (e.isMyFault()) {
            setTurnLeft(70);
            setBack(70);
        } else {
            setTurnRight(70);
            setAhead(70);
        }

    }

    // Caso a besta se perca
    public void onSkippedTurn(SkippedTurnEvent e) {
        // Procura uma presa
        turnRadarLeft(180);
        // Roda, roda
        turnLeft(5);
        ahead(5);
    }

}