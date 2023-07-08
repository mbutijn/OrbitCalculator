public class StaticOrbit {
    double l, e, nu, dAdt, periapsis_angle;

    public StaticOrbit(){
        double a = 1.5;
        e = 0.5;
        double e2 = e * e;
        double b_planet = a * Math.sqrt(1 - e2);
        double period = 2 * Math.PI / 0.05;
        l = a * (1 - e2);

        nu = 0;
        dAdt = 2 * Math.PI * a * b_planet / period;
        periapsis_angle = 0;
    }

    public Vector updatePositionPlanet(double timeStep){
        double r = l / (1 + e * Math.cos(nu));
        double omega = dAdt / (r * r);
        nu += omega * timeStep;
        if (nu > 2 * Math.PI){
            nu -= 2 * Math.PI;
        }
        return new Vector(r, periapsis_angle + nu, true);
    }

}
