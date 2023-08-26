public class StaticOrbit {
    private final double semiLatusRectum, eccentricity, dAdt, periapsis_angle;
    private double trueAnomaly;
    private final Vector position;

    public StaticOrbit(double semiMajorAxis, double eccentricity){
        this.eccentricity = eccentricity;
        double eccentricitySquared = eccentricity * eccentricity;
        double semiMinorAxis = semiMajorAxis * Math.sqrt(1 - eccentricitySquared);
        double period = 2 * Math.PI * Math.sqrt(semiMajorAxis * semiMajorAxis * semiMajorAxis / OrbitCalculator.getSun().mu);
        semiLatusRectum = semiMajorAxis * (1 - eccentricitySquared);

        reset();
        dAdt = 2 * Math.PI * semiMajorAxis * semiMinorAxis / period;
        periapsis_angle = 0;

        position = new Vector(0, 0);
    }

    public Vector updatePosition(double timeStep){
        double distance = semiLatusRectum / (1 + eccentricity * Math.cos(trueAnomaly));
        double angularVelocity = dAdt / (distance * distance);
        trueAnomaly += angularVelocity * timeStep;
        if (trueAnomaly > 2 * Math.PI){
            trueAnomaly -= 2 * Math.PI;
        }

        position.setVectorFromRadiusAndAngle(distance, periapsis_angle + trueAnomaly);

        return position;
    }

    public void reset() {
        trueAnomaly = Math.random() * 2 * Math.PI;
    }
}
