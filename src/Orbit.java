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
        double v_start_abs = startVelocity.getAbs();
        double v_start_abs2 = v_start_abs * v_start_abs;

        double difference = startPosition.getAngle() - startVelocity.getAngle();
        boolean descending = (difference > 0.5 * Math.PI && difference < 1.5 * Math.PI) || (difference < -0.5*Math.PI && difference > -1.5 * Math.PI);
        // System.out.println("orbiter descending: " + descending);

        // calculate specific angular momentum
        double angularMomentum = startPosition.crossMultiplication(startVelocity);
        // System.out.println("angularMomentum: " + angularMomentum);

        boolean isCCW = angularMomentum > 0;
        // System.out.println("orbit is counter clockwise: " + isCCW);

        // vis viva equation
        semiMajorAxis = distance_start / (2 - (distance_start * v_start_abs2 / celestialBody.mu));

        // calculate the eccentricity vector
        Vector eVector = new Vector((v_start_abs2 - celestialBody.mu / distance_start) * startPosition.getX() - (startPosition.dotMultiplication(startVelocity)) * startVelocity.getX(),
                (v_start_abs2 - celestialBody.mu / distance_start) * startPosition.getY() - (startPosition.dotMultiplication(startVelocity)) * startVelocity.getY());
        eccentricity = eVector.getAbs() / celestialBody.mu;
        // System.out.println("eccentricity: " + eccentricity);

        double argumentOfPeriapsis = eVector.getAngle();
        // System.out.println("eVector.getAngle(): " + Math.toDegrees(argumentOfPeriapsis));

        double semiLatusRectum = angularMomentum * angularMomentum / celestialBody.mu; // semi-latus rectum
        // System.out.println("semiLatusRectum: " + semiLatusRectum);

        double distance_periapsis = semiMajorAxis * (1 - eccentricity); // periapsis distance
        // System.out.println("distance_periapsis: " + distance_periapsis);

        double argument = (semiLatusRectum / distance_start - 1) / eccentricity;
        double trueAnomalyStart = argument < -1 ? Math.PI : argument > 1 ? 0 : Math.acos(argument);

        if (descending == isCCW) {
            trueAnomalyStart = 2 * Math.PI - trueAnomalyStart;
        }
        trueAnomalyStart = trueAnomalyStart > Math.PI ? trueAnomalyStart - 2 * Math.PI : trueAnomalyStart < -Math.PI ? trueAnomalyStart + 2 * Math.PI : trueAnomalyStart;
        // System.out.println("trueAnomalyStart: " + Math.toDegrees(trueAnomalyStart));

        periapsis.setVectorFromRadiusAndAngle(distance_periapsis, argumentOfPeriapsis);

        // double Vp = Math.sqrt(celestialBody.mu * 2 * distance_apoapsis / (distance_periapsis * (distance_apoapsis + distance_periapsis))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(celestialBody.mu * 2 * distance_periapsis / (distance_apoapsis * (distance_apoapsis + distance_periapsis))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // make the orbital trajectory around the celestial body
        double trueAnomaly = trueAnomalyStart;
        double angularVelocity = 0;
        double distance = distance_start;
        positionsWrtCb.clear();
        apoapsis.setVectorFromRadiusAndAngle(semiMajorAxis * (1 + eccentricity), argumentOfPeriapsis + Math.PI);

        while (((isCCW && trueAnomaly < trueAnomalyStart + 2 * Math.PI + dT * angularVelocity) ||
                (!isCCW && trueAnomaly > trueAnomalyStart - 2 * Math.PI - dT * angularVelocity) ||
                semiMajorAxis < 0) &&
                distance < celestialBody.SOI &&
                distance > celestialBody.radius) {
            distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
            angularVelocity = angularMomentum / (distance * distance);
            trueAnomaly += angularVelocity * dT;
            positionsWrtCb.add(new Vector(distance, argumentOfPeriapsis + trueAnomaly, true));
        }

        isOnCrashPath = distance < celestialBody.radius;
        isOnEscapePath = distance > celestialBody.SOI;

        numberOfNodes = positionsWrtCb.size();
        // System.out.println("numberOfNodes: " + numberOfNodes);
    }

    public void draw(Graphics g2d) {
        int last_i = 0;
        for (int i = 0; i < numberOfNodes - 20; i += 20) {
            g2d.drawLine(x_ints.get(i) + Orbiter.xdrag, y_ints.get(i) + Orbiter.ydrag, x_ints.get(i + 20) + Orbiter.xdrag, y_ints.get(i + 20) + Orbiter.ydrag);
            last_i = i;
        }
        if (!isOnEscapePath && !isOnCrashPath) {
            g2d.drawLine(x_ints.get(last_i) + Orbiter.xdrag, y_ints.get(last_i) + Orbiter.ydrag, x_ints.get(0) + Orbiter.xdrag, y_ints.get(0) + Orbiter.ydrag);
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
        g2d.drawString("Orbit is around " + celestialBody.name + (isOnEscapePath ? " (leaving SOI)" : isOnCrashPath ? " (crashing)" : ""), 10, y - 110);
        g2d.drawString(String.format("Eccentricity = %.3f", eccentricity) + (eccentricity < 1 ? " (ellipse)" : " (hyperbola)"), 10, y - 95);
        g2d.drawString(String.format("Semi major axis = %.3f km", semiMajorAxis), 10, y - 80);
    }
}
