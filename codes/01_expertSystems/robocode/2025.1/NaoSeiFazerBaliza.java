package stone;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.util.Utils;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

public class NaoSeiFazerBaliza extends AdvancedRobot {
    private final Map<String, InfoInimigo> dadosInimigos = new HashMap<>();
    private double direcaoMovimento = 1;

    public void run() {
        setBodyColor(Color.BLACK);
        setGunColor(Color.WHITE);
        setRadarColor(Color.GREEN);
        setBulletColor(Color.GREEN);
        setScanColor(Color.GREEN);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            setTurnRadarRight(360); 
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double anguloAbsoluto = getHeadingRadians() + e.getBearingRadians();
        double distancia = e.getDistance();
        double inimigoX = getX() + Math.sin(anguloAbsoluto) * distancia;
        double inimigoY = getY() + Math.cos(anguloAbsoluto) * distancia;

        //atualizar dados do inimigo
        dadosInimigos.put(e.getName(), new InfoInimigo(e.getName(), inimigoX, inimigoY, distancia, e.getEnergy(), anguloAbsoluto));

        //ajustar radar
        double ajusteRadar = Utils.normalRelativeAngle(anguloAbsoluto - getRadarHeadingRadians());
        setTurnRadarRightRadians(2 * ajusteRadar);

        //verificar inimigos mais proximos
        List<InfoInimigo> proximos = obterInimigosProximos(300);
        InfoInimigo maisProximo = obterInimigoMaisProximo();

		//se tiver muitos inimigos proximos, vai focar em fugir e atirar no mais perto
        if (proximos.size() > 1) {
       			
            double mediaX = 0, mediaY = 0;
            for (InfoInimigo info : proximos) {
                mediaX += info.x;
                mediaY += info.y;
            }
            mediaX /= proximos.size();
            mediaY /= proximos.size();

            double anguloFuga = Math.toDegrees(Math.atan2(getX() - mediaX, getY() - mediaY));
            setTurnRight(anguloRelativoGraus(anguloFuga - getHeading()));
            setAhead(200);

            if (maisProximo != null) {
                mirarEPredizer(maisProximo, e);
            }
        } else {
            if (maisProximo != null) {
                mirarEPredizer(maisProximo, e);

                setTurnRight(e.getBearing() + 90 - 15);
                if (Math.random() < 0.05) {
                    direcaoMovimento *= -1;
                }
                setAhead(150 * direcaoMovimento);
            }
        }
    }

    private void mirarEPredizer(InfoInimigo inimigo, ScannedRobotEvent e) {
        double potenciaTiro = Math.min(400 / inimigo.distancia, 3);
        double velocidadeTiro = 20 - 3 * potenciaTiro;
        double tempo = inimigo.distancia / velocidadeTiro;

        double predX = inimigo.x + Math.sin(e.getHeadingRadians()) * e.getVelocity() * tempo;
        double predY = inimigo.y + Math.cos(e.getHeadingRadians()) * e.getVelocity() * tempo;

        predX = Math.max(Math.min(predX, getBattleFieldWidth() - 18), 18);
        predY = Math.max(Math.min(predY, getBattleFieldHeight() - 18), 18);

        double dx = predX - getX();
        double dy = predY - getY();
        double angulo = Math.atan2(dx, dy);

        setTurnGunRightRadians(Utils.normalRelativeAngle(angulo - getGunHeadingRadians()));

        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 5) {
            setFire(potenciaTiro);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        direcaoMovimento *= -1;
        setAhead(150 * direcaoMovimento);
    }

    public void onHitWall(HitWallEvent e) {
        direcaoMovimento *= -1;
        setBack(100);
    }

    public void onRobotDeath(RobotDeathEvent e) {
        dadosInimigos.remove(e.getName());
    }

    //------------------------------------------------------------

    private List<InfoInimigo> obterInimigosProximos(double distanciaMaxima) {
        List<InfoInimigo> proximos = new ArrayList<>();
        for (InfoInimigo info : dadosInimigos.values()) {
            if (info.distancia <= distanciaMaxima) {
                proximos.add(info);
            }
        }
        return proximos;
    }

    private InfoInimigo obterInimigoMaisProximo() {
        return dadosInimigos.values().stream()
            .min(Comparator.comparingDouble(e -> e.distancia))
            .orElse(null);
    }

    private double anguloRelativoGraus(double angulo) {
        double corrigido = angulo % 360;
        if (corrigido <= -180) return corrigido + 360;
        if (corrigido > 180) return corrigido - 360;
        return corrigido;
    }

    private static class InfoInimigo {
        String nome;
        double x, y;
        double distancia;
        double energia;
        double anguloAbsoluto;

        public InfoInimigo(String nome, double x, double y, double distancia, double energia, double anguloAbsoluto) {
            this.nome = nome;
            this.x = x;
            this.y = y;
            this.distancia = distancia;
            this.energia = energia;
            this.anguloAbsoluto = anguloAbsoluto;
        }
    }
}