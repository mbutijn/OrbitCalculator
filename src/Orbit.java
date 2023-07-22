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
        double r_start = currentPosition.getAbs();
        double angle_start = currentPosition.getAngle();
        double v_start_abs = start_velocity.getAbs();
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
        double a = r_start / (2 - (r_start * v_start_abs2 / celestialBody.mu)); // semi-major axis (V_esc = 1/(4th_root(2)) ~ 0.8409 m/s)
         System.out.println("a: " + a);

        double dAdt = tw_area / dT; // twice area swept over time
        // System.out.println("dAdt: " + dAdt);

        double e2, e, l, Ra = 0;
        if (a > 0) { // ellipse
            double v_start_t = tw_area / (r_start * dT); // tangential component of velocity vector
            double v_start_t2 = v_start_t * v_start_t;

            e2 = (Math.pow((r_start * v_start_abs2 / celestialBody.mu) - 1, 2) * v_start_t2 + (v_start_abs2 - v_start_t2)) / v_start_abs2;  // eccentricity squared
            e = Math.sqrt(e2); // eccentricity
            // System.out.println("e: " + e);

            l = a * (1 - e2); // semi-latus rectum
            // System.out.println("l: " + l);

            Ra = a * (1 + e); // apoapsis distance
            // System.out.println("Ra: " + Ra);

        } else { // hyperbola
            l = dAdt * dAdt / celestialBody.mu; // semi-latus rectum
            // System.out.println("l: " + l);

            e2 = -l/a + 1;
            // System.out.println("e2: " + e2);

            e = Math.sqrt(e2);
            // System.out.println("e: " + e);

        }
        double Rp = a * (1 - e); // periapsis distance
        // System.out.println("Rp: " + Rp);

        boolean descending = secondPosition.getAbs() < r_start;
        // System.out.println("orbiter descending: " + descending);

        double argument = (-r_start + a - e2 * a) / (r_start * e);
        double nu_start = argument < -1 ? Math.PI : argument > 1 ? 0 : Math.acos(argument);

        if (descending == CCW) {
            nu_start = 2 * Math.PI - nu_start; // true anomaly
        }
        nu_start = nu_start > Math.PI ? nu_start - 2 * Math.PI : nu_start < -Math.PI ? nu_start + 2 * Math.PI : nu_start;

        // System.out.println("nu_start: " + Math.toDegrees(nu_start));

        double periapsis_angle = angle_start - nu_start;
        // System.out.println("periapsis_angle: " + Math.toDegrees(periapsis_angle));

        periapsis.setVectorFromRadiusAndAngle(Rp, periapsis_angle);

        // double Vp = Math.sqrt(2 * Ra / (Rp * (Ra + Rp))); // periapsis velocity
        // System.out.println("Vp: " + Vp);

        // double Va = Math.sqrt(2 * Rp / (Ra * (Ra + Rp))); // apoapsis velocity
        // System.out.println("Va: " + Va);

        // Make the orbital trajectory
        double nu = nu_start;
        double omega = 0;
        double r = r_start;
        boolean switched = false;
        positionsWrtCb.clear();

        if (a > 0) { // ellipse
            apoapsis.setVectorFromRadiusAndAngle(Ra, periapsis_angle + Math.PI);
            double nu_end;
            if (CCW) {
                nu_end = nu_start + 2 * Math.PI;
                while (nu < nu_end + dT * omega && r < celestialBody.SOI) {
                    if (r < celestialBody.radius && !switched){
                        nu = -nu;
                        switched = true;
                    } else {
                        r = l / (1 + e * Math.cos(nu));
                        omega = dAdt / (r * r);
                        nu += omega * dT;
                        positionsWrtCb.add(new Vector(r, periapsis_angle + nu, true));
                    }
                }
            } else {
                nu_end = nu_start - 2 * Math.PI;
                while (nu > nu_end - dT * omega && r < celestialBody.SOI) {
                    if (r < celestialBody.radius && !switched){
                        nu = -nu;
                        switched = true;
                    } else {
                        r = l / (1 + e * Math.cos(nu));
                        omega = dAdt / (r * r);
                        nu -= omega * dT;
                        positionsWrtCb.add(new Vector(r, periapsis_angle + nu, true));
                    }
                }
            }
        } else { // hyperbolic trajectory
            while (r < celestialBody.SOI) {
                if (r < celestialBody.radius && !switched){
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

                    positionsWrtCb.add(new Vector(r, periapsis_angle + nu, true));
                }
            }
        }

        isOnEscapePath = r > celestialBody.SOI;
        if (isOnEscapePath){
            if (celestialBody == OrbitCalculator.getPlanet()) {
                System.out.println("Orbit is on escape path from planet");
            } else {
                System.out.println("Orbit is on escape path from sun");
            }
        }

        numberOfNodes = positionsWrtCb.size();
        System.out.println("numberOfNodes: " + numberOfNodes);
    }

    public void draw(Graphics g2d) {
        for (int i = 0; i < numberOfNodes - 20; i += 20) {
            g2d.drawLine(x_ints.get(i), y_ints.get(i), x_ints.get(i+20), y_ints.get(i+20));
        }
    }

    public void drawPeriapsis(Graphics2D g2d) {
        int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * periapsis.getX());
        int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * periapsis.getY());
        g2d.fillOval(x-3, y-3, 6 ,6);
        g2d.drawString(String.format("Pe: %.3f", periapsis.abs), x, y-5);
    }

    public void drawApoapsis(Graphics2D g2d) {
        if (!isOnEscapePath) {
            int x = celestialBody.x_int + (int) (OrbitCalculator.scaleFactor * apoapsis.getX());
            int y = celestialBody.y_int - (int) (OrbitCalculator.scaleFactor * apoapsis.getY());
            g2d.fillOval(x - 3, y - 3, 6, 6);
            g2d.drawString(String.format("Ap: %.3f", apoapsis.abs), x, y - 5);
        }
    }

    public void drawSOI(Graphics2D g2d){
        Planet planet = OrbitCalculator.getPlanet();
        if (isOnEscapePath && celestialBody == planet){
            int radius = (int) (planet.SOI * OrbitCalculator.scaleFactor);
            g2d.drawOval(planet.x_int - radius, planet.y_int - radius, 2 * radius, 2 * radius);
        }
    }

    public void reset(){
        System.out.println("Orbit reset");
        skipIndex = (int) (1000 * OrbitCalculator.timeStep);
        isOnEscapePath = false;

        celestialBody = OrbitCalculator.getPlanet();

        try {
            recalculate(new Vector(0.25, 0.25), new Vector(0.27, -0.27));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
