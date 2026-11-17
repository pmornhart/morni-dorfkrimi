package de.moinmorni.dorfkrimi.world;

import java.util.List;

/**
 * Aufbau einer Karte ohne Minecraft: wo man startet, wo die Dorfversammlung ist (mit Glocke), wo die Bank vor der
 * Wirtschaft steht (dort sitzen Betäubte und schauen zu), wie groß die Karte ist und wie sie aussieht (Tageszeit, Nebel).
 * Alles relativ zum Mittelpunkt der Karte; y = 0 ist die oberste Bodenschicht (man steht auf y = 1).
 *
 * @param square    Mitte des Dorfplatzes {x, z}: dort stehen zum Start alle im Kreis
 * @param spawnRadius Radius des Kreises zum Start
 * @param meeting   Mitte der Dorfversammlung {x, z}
 * @param meetingRadius Radius des Kreises bei der Versammlung
 * @param bell      Glocke für die Versammlung {x, y, z}
 * @param seats     Plätze auf der Bank vor der Wirtschaft {x, y, z, Blickrichtung} (Sitzfläche: Treppe bei x/y/z)
 * @param halfX     die Karte reicht bis {@code |x| = halfX} …
 * @param halfZ     … und {@code |z| = halfZ}
 * @param voidY     wer tiefer fällt, kommt zurück auf den Dorfplatz
 * @param time      Tageszeit für alle auf der Karte (Minecraft-Ticks, z. B. 18000 = Mitternacht bei Vollmond)
 * @param fog       Nebel (Schwaden um die Leute herum)
 */
public record MapLayout(int[] square, double spawnRadius, int[] meeting, double meetingRadius, int[] bell,
        List<double[]> seats, int halfX, int halfZ, int voidY, long time, boolean fog) {

    /** Startplatz Nummer {@code i} von {@code n}: im Kreis um den Dorfplatz. {x, z, Blickrichtung} */
    public double[] spawn(int i, int n) {
        return ring(square, spawnRadius, i, n);
    }

    /** Platz bei der Versammlung Nummer {@code i} von {@code n}. */
    public double[] meetingSpot(int i, int n) {
        return ring(meeting, meetingRadius, i, n);
    }

    /** Platz {@code i} im Kreis, Blick zur Mitte. */
    static double[] ring(int[] center, double radius, int i, int n) {
        double a = 2 * Math.PI * i / Math.max(1, n);
        double x = center[0] + 0.5 + Math.cos(a) * radius;
        double z = center[1] + 0.5 + Math.sin(a) * radius;
        float yaw = (float) Math.toDegrees(Math.atan2(-(center[0] + 0.5 - x), center[1] + 0.5 - z));
        return new double[] {x, z, yaw};
    }

    /** Liegt (x|z) innerhalb des Zauns? */
    public boolean inside(double x, double z) {
        return Math.abs(x) < halfX && Math.abs(z) < halfZ;
    }
}
