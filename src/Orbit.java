import java.awt.*;
import java.util.ArrayList;

public class Orbit {
    public ArrayList<Vector> positionsWrtCb = new ArrayList<>();
    private final ArrayList<Integer> x_ints = new ArrayList<>();
    private final ArrayList<Integer> y_ints = new ArrayList<>();
    private final Vector periapsis = new Vector(0, 0);
    private final Vector apoapsis = new Vector(0, 0);
    public final double dT = 0.001;
    public int numberOfNodes, skipIndex = (int) (1000 * OrbitCalculator.timeStep);
    CelestialBody celestialBody;
    public boolean isOnEscapePath;

    public Orbit(CelestialBody celestialBody){
        this.celestialBody = celestialBody;
    }

    public void recalculate(Vector currentPosition, Vector start_velocity) {
        double distance_start = currentPosition.getAbs();
        double angle_start = currentPosition.getAngle();
        double v_start_abs = start_velocity.getAbs();
        double v_start_abs2 = v_start_abs * v_start_abs;

        Vector positionChange = new Vector(dT * start_velocity.getX(), dT * start_velocity.getY());
        Vector secondPosition = currentPosition.add(positionChange);
        double distance_2 = secondPosition.getAbs();
        double distance_far = positionChange.getAbs();

        boolean isCCW = secondPosition.getAngle() > currentPosition.getAngle();
        if (currentPosition.getX() < 0 && currentPosition.getAngle() * secondPosition.getAngle() < 0) {
            isCCW = !isCCW; // correct for bug at values of angle around pi and -pi
        }
        // System.out.println("orbit is counter clockwise: " + isCCW);

        double semiPeri = (distance_start + distance_far + distance_2) / 2; // semi-perimeter triangle
        double tw_area = 2 * Math.sqrt(semiPeri * (semiPeri - distance_start) * (semiPeri - distance_far) * (semiPeri - distance_2));

        // vis viva equation
        double semiMajorAxis = distance_start / (2 - (distance_start * v_start_abs2 / celestialBody.mu));
         System.out.println("semiMajorAxis: " + semiMajorAxis);

        double dAdt = tw_area / dT; // twice area swept over time
        // System.out.println("dAdt: " + dAdt);

        double eccentricitySquared, eccentricity, semiLatusRectum, distance_apoapsis = 0;
        if (semiMajorAxis > 0) { // ellipse
            double v_start_t = tw_area / (distance_start * dT); // tangential component of velocity vector
            double v_start_t2 = v_start_t * v_start_t;

            eccentricitySquared = (Math.pow((distance_start * v_start_abs2 / celestialBody.mu) - 1, 2) * v_start_t2 + (v_start_abs2 - v_start_t2)) / v_start_abs2;  // eccentricity squared
            eccentricity = Math.sqrt(eccentricitySquared); // eccentricity
            // System.out.println("eccentricity: " + eccentricity);

            semiLatusRectum = semiMajorAxis * (1 - eccentricitySquared);
            // System.out.println("semiLatusRectum: " + semiLatusRectum);

            distance_apoapsis = semiMajorAxis * (1 + eccentricity);
            // System.out.println("distance_apoapsis: " + distance_apoapsis);

        } else { // hyperbola
            semiLatusRectum = dAdt * dAdt / celestialBody.mu; // semi-latus rectum
            // System.out.println("semiLatusRectum: " + semiLatusRectum);

            eccentricitySquared = -semiLatusRectum/semiMajorAxis + 1;
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

        // double Vp = Math.sqrt(2 * distance_apoapsis / (distance_periapsis * (distance_apoapsis + distance_periapsis))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(2 * distance_periapsis / (distance_apoapsis * (distance_apoapsis + distance_periapsis))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // Make the orbital trajectory
        double trueAnomaly = trueAnomalyStart;
        double angularVelocity = 0;
        double distance = distance_start;
        boolean switched = false;
        positionsWrtCb.clear();

        if (semiMajorAxis > 0) { // ellipse
            apoapsis.setVectorFromRadiusAndAngle(distance_apoapsis, periapsis_angle + Math.PI);
            double trueAnomalyEnd;
            if (isCCW) {
                trueAnomalyEnd = trueAnomalyStart + 2 * Math.PI;
                while (trueAnomaly < trueAnomalyEnd + dT * angularVelocity && distance < celestialBody.SOI) {
                    if (distance < celestialBody.radius && !switched){
                        trueAnomaly = -trueAnomaly;
                        switched = true;
                    } else {
                        distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
                        angularVelocity = dAdt / (distance * distance);
                        trueAnomaly += angularVelocity * dT;
                        positionsWrtCb.add(new Vector(distance, periapsis_angle + trueAnomaly, true));
                    }
                }
            } else {
                trueAnomalyEnd = trueAnomalyStart - 2 * Math.PI;
                while (trueAnomaly > trueAnomalyEnd - dT * angularVelocity && distance < celestialBody.SOI) {
                    if (distance < celestialBody.radius && !switched){
                        trueAnomaly = -trueAnomaly;
                        switched = true;
                    } else {
                        distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
                        angularVelocity = dAdt / (distance * distance);
                        trueAnomaly -= angularVelocity * dT;
                        positionsWrtCb.add(new Vector(distance, periapsis_angle + trueAnomaly, true));
                    }
                }
            }
        } else { // hyperbolic trajectory
            while (distance < celestialBody.SOI) {
                if (distance < celestialBody.radius && !switched){
                    trueAnomaly = -trueAnomaly;
                    switched = true;
                } else {
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
        }

        isOnEscapePath = distance > celestialBody.SOI;
        /*
        if (isOnEscapePath){
            if (celestialBody == OrbitCalculator.getHomePlanet()) {
                System.out.println("Orbit is on escape path from planet");
            } else {
                System.out.println("Orbit is on escape path from sun");
            }
        }
        */

        numberOfNodes = positionsWrtCb.size();
        System.out.println("numberOfNodes: " + numberOfNodes);
    }

    public void draw(Graphics g2d, int x_drag, int y_drag) {
        for (int i = 0; i < numberOfNodes - 20; i += 20) {
            g2d.drawLine(x_ints.get(i) + x_drag, y_ints.get(i) + y_drag, x_ints.get(i + 20) + x_drag, y_ints.get(i + 20) + y_drag);
        }
    }

    public void drawPeriapsis(Graphics2D g2d, int x_drag, int y_drag) {
        int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * periapsis.getX()) + x_drag;
        int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * periapsis.getY()) + y_drag;
        g2d.fillOval(x - 3, y - 3, 6 ,6);
        g2d.drawString(String.format("Pe: %.3f", periapsis.abs - celestialBody.radius), x, y - 5);
    }

    public void drawApoapsis(Graphics2D g2d, int x_drag, int y_drag) {
        if (!isOnEscapePath) {
            int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * apoapsis.getX()) + x_drag;
            int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * apoapsis.getY()) + y_drag;
            g2d.fillOval(x - 3, y - 3, 6, 6);
            g2d.drawString(String.format("Ap: %.3f", apoapsis.abs - celestialBody.radius), x, y - 5);
        }
    }

    public void drawSOI(Graphics2D g2d, int x_drag, int y_drag){
        if (isOnEscapePath) {
            int radius = (int) (celestialBody.SOI * OrbitCalculator.scaleFactor);
            g2d.drawOval(celestialBody.x_int - radius + x_drag, celestialBody.y_int - radius + y_drag, 2 * radius, 2 * radius);
        }
    }

    public void reset(){
        System.out.println("Orbit reset");
        skipIndex = (int) (1000 * OrbitCalculator.timeStep);
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
}
