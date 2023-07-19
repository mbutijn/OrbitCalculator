public class StaticOrbit {
    private final double l, e, dAdt, periapsis_angle;
    double nu;

    public StaticOrbit(double mu){
        double a = 3.0;
        e = 0.3;
        double e2 = e * e;
        double b = a * Math.sqrt(1 - e2);
        double period = 2 * Math.PI * Math.sqrt(a*a*a / mu);
        l = a * (1 - e2);

        nu = 0;
        dAdt = Math.PI * a * b / period;
        periapsis_angle = 0;
    }

    public Vector updatePosition(double timeStep){
        double r = l / (1 + e * Math.cos(nu));
        double omega = dAdt / (r * r);
        nu += omega * timeStep;
        if (nu > 2 * Math.PI){
            nu -= 2 * Math.PI;
        }
        return new Vector(r, periapsis_angle + nu, true);
    }

}
