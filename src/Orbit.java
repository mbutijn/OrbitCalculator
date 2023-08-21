import java.awt.*;
import java.util.ArrayList;

public class Orbit {
    public ArrayList<Vector> positionsWrtCb = new ArrayList<>();
    private final ArrayList<Integer> x_ints = new ArrayList<>();
    private final ArrayList<Integer> y_ints = new ArrayList<>();
    private final Vector periapsis = new Vector(0, 0);
    private final Vector apoapsis = new Vector(0, 0);
    public final double dT = 0.001;
    public int numberOfNodes;
    public CelestialBody celestialBody;
    public boolean isOnEscapePath, isOnCrashPath;
    private double semiMajorAxis, eccentricity;

    public Orbit(CelestialBody celestialBody){
        this.celestialBody = celestialBody;
    }

    public void recalculate(Vector startPosition, Vector startVelocity) {
        double distance_start = startPosition.getAbs();
        double angle_start = startPosition.getAngle();
        double v_start_abs = startVelocity.getAbs();
        double v_start_abs2 = v_start_abs * v_start_abs;

        Vector positionChange = new Vector(dT * startVelocity.getX(), dT * startVelocity.getY());
        Vector secondPosition = startPosition.add(positionChange);
        double distance_2 = secondPosition.getAbs();
        double distance_far = positionChange.getAbs();

        boolean isCCW = secondPosition.getAngle() > startPosition.getAngle();
        if (startPosition.getX() < 0 && startPosition.getAngle() * secondPosition.getAngle() < 0) {
            isCCW = !isCCW; // correct for bug at values of angle around pi and -pi
        }
        // System.out.println("orbit is counter clockwise: " + isCCW);

        double semiPeri = (distance_start + distance_far + distance_2) / 2; // semi-perimeter triangle
        double tw_area = 2 * Math.sqrt(semiPeri * (semiPeri - distance_start) * (semiPeri - distance_far) * (semiPeri - distance_2));

        // vis viva equation
        semiMajorAxis = distance_start / (2 - (distance_start * v_start_abs2 / celestialBody.mu));

        double dAdt = tw_area / dT; // area swept over time
        // System.out.println("dAdt Spacecraft: " + dAdt);

        double eccentricitySquared, semiLatusRectum, distance_apoapsis = 0;
        if (semiMajorAxis > 0) { // ellipse
            double v_start_t = tw_area / (distance_start * dT); // tangential component of velocity vector
            double v_start_t2 = v_start_t * v_start_t;

            eccentricitySquared = (Math.pow((distance_start * v_start_abs2 / celestialBody.mu) - 1, 2) * v_start_t2 + (v_start_abs2 - v_start_t2)) / v_start_abs2;  // eccentricity squared
            eccentricity = Math.sqrt(eccentricitySquared); // eccentricity

            semiLatusRectum = semiMajorAxis * (1 - eccentricitySquared);
            // System.out.println("semiLatusRectum: " + semiLatusRectum);

            distance_apoapsis = semiMajorAxis * (1 + eccentricity);
            // System.out.println("distance_apoapsis: " + distance_apoapsis);

        } else { // hyperbola
            semiLatusRectum = dAdt * dAdt / celestialBody.mu; // semi-latus rectum
            // System.out.println("semiLatusRectum: " + semiLatusRectum);

            eccentricitySquared = -semiLatusRectum / semiMajorAxis + 1;
            // System.out.println("eccentricitySquared: " + eccentricitySquared);

            eccentricity = Math.sqrt(eccentricitySquared);
            // System.out.println("eccentricity: " + eccentricity);

        }
        double distance_periapsis = semiMajorAxis * (1 - eccentricity); // periapsis distance
        // System.out.println("distance_periapsis: " + distance_periapsis);

        boolean descending = secondPosition.getAbs() < distance_start;
        // System.out.println("orbiter descending: " + descending);

        double argument = (-distance_start + semiMajorAxis - eccentricitySquared * semiMajorAxis) / (distance_start * eccentricity);
        double trueAnomalyStart = argument < -1 ? Math.PI : argument > 1 ? 0 : Math.acos(argument);

        if (descending == isCCW) {
            trueAnomalyStart = 2 * Math.PI - trueAnomalyStart;
        }
        trueAnomalyStart = trueAnomalyStart > Math.PI ? trueAnomalyStart - 2 * Math.PI : trueAnomalyStart < -Math.PI ? trueAnomalyStart + 2 * Math.PI : trueAnomalyStart;
        // System.out.println("trueAnomalyStart: " + Math.toDegrees(trueAnomalyStart));

        double periapsis_angle = angle_start - trueAnomalyStart;
        // System.out.println("periapsis_angle: " + Math.toDegrees(periapsis_angle));

        periapsis.setVectorFromRadiusAndAngle(distance_periapsis, periapsis_angle);

        // double Vp = Math.sqrt(celestialBody.mu * 2 * distance_apoapsis / (distance_periapsis * (distance_apoapsis + distance_periapsis))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(celestialBody.mu * 2 * distance_periapsis / (distance_apoapsis * (distance_apoapsis + distance_periapsis))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // Make the orbital trajectory
        double trueAnomaly = trueAnomalyStart;
        double angularVelocity = 0;
        double distance = distance_start;
        positionsWrtCb.clear();

        if (semiMajorAxis > 0) { // ellipse
            apoapsis.setVectorFromRadiusAndAngle(distance_apoapsis, periapsis_angle + Math.PI);
            double trueAnomalyEnd;
            if (isCCW) {
                trueAnomalyEnd = trueAnomalyStart + 2 * Math.PI + dT * angularVelocity;
                while (trueAnomaly < trueAnomalyEnd && distance < celestialBody.SOI && distance > celestialBody.radius) {
                    distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
                    angularVelocity = dAdt / (distance * distance);
                    trueAnomaly += angularVelocity * dT;
                    positionsWrtCb.add(new Vector(distance, periapsis_angle + trueAnomaly, true));
                }
            } else {
                trueAnomalyEnd = trueAnomalyStart - 2 * Math.PI - dT * angularVelocity;
                while (trueAnomaly > trueAnomalyEnd && distance < celestialBody.SOI && distance > celestialBody.radius) {
                    distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
                    angularVelocity = dAdt / (distance * distance);
                    trueAnomaly -= angularVelocity * dT;
                    positionsWrtCb.add(new Vector(distance, periapsis_angle + trueAnomaly, true));
                }
            }
        } else { // hyperbolic trajectory
            while (distance < celestialBody.SOI && distance > celestialBody.radius) {
                distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
                angularVelocity = dAdt / (distance * distance);
                if (isCCW) {
                    trueAnomaly += angularVelocity * dT;
                } else {
                    trueAnomaly -= angularVelocity * dT;
                }
                positionsWrtCb.add(new Vector(distance, periapsis_angle + trueAnomaly, true));

            }
        }

        isOnCrashPath = distance < celestialBody.radius;
        isOnEscapePath = distance > celestialBody.SOI;

        numberOfNodes = positionsWrtCb.size();
        // System.out.println("numberOfNodes: " + numberOfNodes);
    }

    public void draw(Graphics g2d) {
        for (int i = 0; i < numberOfNodes - 20; i += 20) {
            g2d.drawLine(x_ints.get(i) + Orbiter.xdrag, y_ints.get(i) + Orbiter.ydrag, x_ints.get(i + 20) + Orbiter.xdrag, y_ints.get(i + 20) + Orbiter.ydrag);
        }
    }

    public void drawPeriapsis(Graphics2D g2d) {
        if (!isOnCrashPath) {
            int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * periapsis.getX()) + Orbiter.xdrag;
            int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * periapsis.getY()) + Orbiter.ydrag;
            g2d.fillOval(x - 3, y - 3, 6, 6);

            int x_offset = periapsis.getX() < apoapsis.getX() ? -55 : 5;
            int y_offset = periapsis.getY() < apoapsis.getY() ? 10 : -10;

            g2d.drawString(String.format("Pe: %.3f", periapsis.abs - celestialBody.radius), x + x_offset, y + y_offset);
        }
    }

    public void drawApoapsis(Graphics2D g2d) {
        if (!isOnEscapePath) {
            int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * apoapsis.getX()) + Orbiter.xdrag;
            int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * apoapsis.getY()) + Orbiter.ydrag;
            g2d.fillOval(x - 3, y - 3, 6, 6);

            int x_offset = apoapsis.getX() < periapsis.getX() ? -55 : 5;
            int y_offset = apoapsis.getY() < periapsis.getY() ? 10 : -10;

            g2d.drawString(String.format("Ap: %.3f", apoapsis.abs - celestialBody.radius), x + x_offset, y + y_offset);
        }
    }

    public void drawSOI(Graphics2D g2d){
        if (isOnEscapePath) {
            int radius = (int) (celestialBody.SOI * OrbitCalculator.scaleFactor);
            g2d.drawOval(celestialBody.x_int - radius + Orbiter.xdrag, celestialBody.y_int - radius + Orbiter.ydrag, 2 * radius, 2 * radius);
        }
    }

    public void reset(){
        System.out.println("Orbit reset");
        isOnEscapePath = false;
        celestialBody = OrbitCalculator.getPlanets().get(0);
    }

    public void updatePixelPosition() {
        x_ints.clear();
        y_ints.clear();
        for (int i = 0; i < numberOfNodes; i++) {
            x_ints.add(celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * positionsWrtCb.get(i).getX()));
            y_ints.add(celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * positionsWrtCb.get(i).getY()));
        }
    }

    public void drawUI(Graphics2D g2d, int y) {
        g2d.drawString("Celestial body: " + celestialBody.name + (isOnEscapePath ? " (leaving SOI)" : isOnCrashPath ? " (crashing)" : ""), 10, y - 170);
        g2d.drawString(String.format("Eccentricity = %.3f", eccentricity) + (eccentricity < 1 ? " (ellipse)" : " (hyperbola)"), 10, y - 155);
        g2d.drawString(String.format("Semi major axis = %.3f km", semiMajorAxis), 10, y - 140);
    }
}
