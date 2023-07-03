import java.awt.*;
import java.util.ArrayList;

public class Orbit {
    public ArrayList<Vector> positions = new ArrayList<>();
    public ArrayList<Vector> velocities = new ArrayList<>();
    public ArrayList<Integer> x_int = new ArrayList<>();
    public ArrayList<Integer> y_int = new ArrayList<>();
    public Vector periapsis = new Vector(0, 0);
    public Vector apoapsis = new Vector(0, 0);
    public double dT = 0.001;
    public int numberOfNodes, skipIndex = 10;

    public void recalculate(Vector currentPosition, Vector start_velocity) throws Exception {
        positions.clear();
        velocities.clear();
        x_int.clear();
        y_int.clear();

        double r_start = currentPosition.getAbs(); // ~ 2.828 m (case 1)
        double angle_start = currentPosition.getAngle();
        double v_start_abs = start_velocity.getAbs(); // ~ 0.2828 m/s (case 1)
        double v_start_abs2 = v_start_abs * v_start_abs;

        Vector positionChange = new Vector(dT * start_velocity.getX(), dT * start_velocity.getY());
        Vector secondPosition = currentPosition.add(positionChange);
        double r_2 = secondPosition.getAbs();
        double distance = positionChange.getAbs();

        boolean CCW = secondPosition.getAngle() > currentPosition.getAngle();
        if (currentPosition.getX() < 0 && currentPosition.getAngle() * secondPosition.getAngle() < 0) {
            CCW = !CCW; // correct for bug at values of angle around pi and -pi
        }
        // System.out.println("orbit is counter clockwise: " + CCW);

        double s = (r_start + distance + r_2) / 2; // semi-perimeter triangle
        double tw_area = 2 * Math.sqrt(s * (s - r_start) * (s - distance) * (s - r_2));

        // vis viva equation
        double a = r_start / (2 - r_start * v_start_abs2); // semi-major axis (V_esc = 1/(4th_root(2)) ~ 0.8409 m/s)
         System.out.println("a: " + a);

        double dAdt = tw_area / dT; // twice area swept over time
        // System.out.println("dAdt: " + dAdt);

        double e2, e, l, Rp, Ra = 0;
        if (a > 0) { // ellipse
            double v_start_t = tw_area / (r_start * dT); // tangential component of velocity vector
            double v_start_t2 = v_start_t * v_start_t;

            e2 = (Math.pow(r_start * v_start_abs2 - 1, 2) * v_start_t2 + (v_start_abs2 - v_start_t2)) / v_start_abs2;  // eccentricity squared
            e = Math.sqrt(e2); // eccentricity
            // System.out.println("e: " + e);

            l = a * (1 - e2); // semi-latus rectum
            // System.out.println("l: " + l);

            Rp = a * (1 - e); // periapsis distance
            // System.out.println("Rp: " + Rp);

            Ra = a * (1 + e); // apoapsis distance
            // System.out.println("Ra: " + Ra);

        } else { // hyperbola
            l = dAdt * dAdt; // semi-latus rectum
            // System.out.println("l: " + l);

            e2 = -l/a + 1;
            // System.out.println("e2: " + e2);

            e = Math.sqrt(e2);
            // System.out.println("e: " + e);

            Rp = -a * (e - 1);
            // System.out.println("Rp: " + Rp);
        }
        boolean descending = secondPosition.getAbs() < r_start;
        // System.out.println("orbiter descending: " + descending);

        double argument = (-r_start + a - e2 * a) / (r_start * e);
        double nu_start = argument < -1 ? Math.PI : argument > 1 ? 0 : Math.acos(argument);

        if (descending == CCW) {
            nu_start = 2 * Math.PI - nu_start; // true anomaly
        }
        nu_start = nu_start > Math.PI ? nu_start - 2 * Math.PI : nu_start;

        // System.out.println("nu_start: " + Math.toDegrees(nu_start));

        double periapsis_angle = angle_start - nu_start;
        // System.out.println("periapsis_angle: " + Math.toDegrees(periapsis_angle));

        periapsis.setVectorFromRadiusAndAngle(Rp, periapsis_angle);

        // double Vp = Math.sqrt(2 * Ra / (Rp * (Ra + Rp))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(2 * Rp / (Ra * (Ra + Rp))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // Update dT for resolutions issues
        if (a < 0) { // hyperbola
            dT = 0.01;
            skipIndex = 1;
        } else if (a > 0 && a < 3) { // small ellipse
            dT = 0.001;
            skipIndex = 10;
        } else if (a >= 3 && a < 75) { // large ellipse
            dT = 0.01;
            skipIndex = 1;
        } else { // a >= 75
//            throw new Exception("semi major axis too large");
        }

        // Make the orbital trajectory
        double nu = nu_start;
        double omega = 0;
        double r = r_start;
        double SOI = 6;
        boolean switched = false;

        if (a > 0) { // ellipse
            apoapsis.setVectorFromRadiusAndAngle(Ra, periapsis_angle + Math.PI);
            double nu_end;
            if (CCW) {
                nu_end = nu_start + 2 * Math.PI + dT * omega;
                while (nu < nu_end && r < SOI) {
                    if (r < 0.2 && !switched){
                        nu = -nu;
                        switched = true;
                    } else {
                        r = l / (1 + e * Math.cos(nu));
                        omega = dAdt / (r * r);
                        nu += omega * dT;
                        positions.add(new Vector(r, periapsis_angle + nu, true));
                    }

                }
            } else {
                nu_end = nu_start - 2 * Math.PI - dT * omega;
                while (nu > nu_end && r < SOI) {
                    if (r < 0.2 && !switched){
                        nu = -nu;
                        switched = true;
                    } else {
                        r = l / (1 + e * Math.cos(nu));
                        omega = dAdt / (r * r);
                        nu -= omega * dT;
                    }
                    positions.add(new Vector(r, periapsis_angle + nu, true));

                }
            }
        } else { // hyperbolic trajectory
            while (r < SOI) {
                if (r < 0.2 && !switched){
                    nu = -nu;
                    switched = true;
                } else {
                    r = l / (1 + e * Math.cos(nu));
                    omega = dAdt / (r * r);
                    if (CCW) {
                        nu += omega * dT;
                    } else {
                        nu -= omega * dT;
                    }
                }
                positions.add(new Vector(r, periapsis_angle + nu, true));
            }
        }

        numberOfNodes = positions.size();
        System.out.println("numberOfNodes: " + numberOfNodes);
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

    public void reset(){
        dT = 0.001;
        skipIndex = 10;
        try {
            recalculate(new Vector(-2, 2), new Vector(0.2, 0.2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePixelPosition() {
        for (int i = 0; i < numberOfNodes; i++) {
            x_int.add(OrbitCalculator.midX + (int) (OrbitCalculator.scaleFactor * positions.get(i).getX()));
            y_int.add(OrbitCalculator.midY - (int) (OrbitCalculator.scaleFactor * positions.get(i).getY()));
        }
    }
}
