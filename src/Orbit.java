import java.awt.*;
import java.util.ArrayList;

public class Orbit {
    public ArrayList<Vector> positions = new ArrayList<>();
    public ArrayList<Vector> velocities = new ArrayList<>();
    public ArrayList<Integer> x_int = new ArrayList<>();
    public ArrayList<Integer> y_int = new ArrayList<>();
    public int numberOfNodes;
    public Vector periapsis = new Vector(0, 0);
    public Vector apoapsis = new Vector(0, 0);

    public void recalculate(Vector currentPosition, Vector start_velocity) {
        positions.clear();
        velocities.clear();
        x_int.clear();
        y_int.clear();

        double dT = 0.0001f;
        double r_start = currentPosition.getAbs();
        double angle_start = currentPosition.getAngle();
        double v_start_abs = start_velocity.getAbs();

        Vector positionChange = new Vector(dT * start_velocity.getX(), dT * start_velocity.getY());
        Vector secondPosition = currentPosition.add(positionChange);
        double r_2 = secondPosition.getAbs();
        double distance = positionChange.getAbs();

        boolean CCW = secondPosition.getAngle() > currentPosition.getAngle();
        // System.out.println("orbit is counter clockwise: " + CCW);

        double s = (r_start + distance + r_2) / 2; // semi-perimeter triangle
        double area = Math.sqrt(s * (s - r_start) * (s - distance) * (s - r_2));

        double v_start_t = 2 * area / (r_start * dT); // tangential component of velocity vector
        double v_start_abs2 = v_start_abs * v_start_abs;
        double v_start_t2 = v_start_t * v_start_t;

        // vis viva equation
        double a = r_start / (2 - r_start * v_start_abs2); // semi-major axis
        // System.out.println("a: " + a);

        double p = 2 * Math.PI * Math.pow(a, 1.5); // orbital period
        // System.out.println("P: " + p);

        double e2 = (Math.pow(r_start * v_start_abs2 - 1, 2) * v_start_t2 + (v_start_abs2 - v_start_t2)) / v_start_abs2;  // eccentricity squared
        double e = Math.sqrt(e2); // eccentricity
        // System.out.println("e: " + e);

        double b = a * Math.sqrt(1 - e2); // semi-minor axis
        // System.out.println("b: " + b);

        boolean descending = secondPosition.getAbs() < r_start;
        // System.out.println("orbiter descending: " + descending);

        double argument = (-r_start + a - e2 * a) / (r_start * e);
        double nu_start = argument < -1 ? 1.5 * Math.PI: argument > 1 ? 0.5*Math.PI : Math.acos(argument);

        if (descending == CCW) {
            nu_start = 2 * Math.PI - nu_start; // true anomaly
        }

        // System.out.println("nu_start: " + Math.toDegrees(nu_start));

        double periapsis_angle = angle_start - nu_start;
        // System.out.println("periapsis_angle: " + Math.toDegrees(periapsis_angle));

        double Rp = a * (1 - e); // periapsis distance
        // System.out.println("Rp: " + Rp);
        periapsis.setVectorFromRadiusAndAngle(Rp, periapsis_angle);

        double Ra = a * (1 + e); // apoapsis distance
        // System.out.println("Ra: " + Ra);
        apoapsis.setVectorFromRadiusAndAngle(Ra, periapsis_angle + Math.PI);

        // double Vp = Math.sqrt(2 * Ra / (Rp * (Ra + Rp))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(2 * Rp / (Ra * (Ra + Rp))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // Make the orbital trajectory
        double nu = nu_start;
        double omega = 0;
        double numerator = a * (1 - e2);
        double OrbitArea = 2 * Math.PI * a * b / p;

        if (CCW) {
            while (nu < nu_start + 2 * Math.PI + dT * omega) {
                double r = numerator / (1 + e * Math.cos(nu));
                omega = OrbitArea / (r * r);
                nu += omega * dT;
                double angle = periapsis_angle + nu;
                double x = r * Math.cos(angle);
                double y = r * Math.sin(angle);
                positions.add(new Vector(x, y));
                x_int.add(OrbitCalculator.midX + (int) (OrbitCalculator.scaleFactor * x));
                y_int.add(OrbitCalculator.midY - (int) (OrbitCalculator.scaleFactor * y));
            }
        } else {
            while (nu > nu_start - 2 * Math.PI - dT * omega) {
                double r = numerator / (1 + e * Math.cos(nu));
                omega = OrbitArea / (r * r);
                nu -= omega * dT;
                double angle = periapsis_angle + nu;
                double x = r * Math.cos(angle);
                double y = r * Math.sin(angle);
                positions.add(new Vector(x, y));
                x_int.add(OrbitCalculator.midX + (int) (OrbitCalculator.scaleFactor * x));
                y_int.add(OrbitCalculator.midY - (int) (OrbitCalculator.scaleFactor * y));
            }
        }

        numberOfNodes = positions.size();

        // calculate orbital velocities
        for (int i = 0; i < numberOfNodes - 1; i++) {
            velocities.add(new Vector((positions.get(i + 1).getX() - positions.get(i).getX()) / dT, (positions.get(i + 1).getY() - positions.get(i).getY()) / dT));
        }
    }

    public void draw(Graphics g2d) {
        for (int i = 0; i < numberOfNodes - 1; i++) {
            g2d.drawLine(x_int.get(i), y_int.get(i), x_int.get(i+1), y_int.get(i+1));
        }
    }

    public void drawPeriapsis(Graphics2D g2d) {
        int x = OrbitCalculator.midX + (int) (OrbitCalculator.scaleFactor * periapsis.getX());
        int y = OrbitCalculator.midY - (int) (OrbitCalculator.scaleFactor * periapsis.getY());
        g2d.fillOval(x-3, y-3, 6 ,6);
        g2d.drawString(String.format("Pe: %.3f", periapsis.abs), x, y-5);
    }

    public void drawApoapsis(Graphics2D g2d) {
        int x = OrbitCalculator.midX + (int) (OrbitCalculator.scaleFactor * apoapsis.getX());
        int y = OrbitCalculator.midY - (int) (OrbitCalculator.scaleFactor * apoapsis.getY());
        g2d.fillOval(x-3, y-3, 6 ,6);
        g2d.drawString(String.format("Ap: %.3f", apoapsis.abs), x, y-5);
    }
}
