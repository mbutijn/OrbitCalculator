import java.awt.*;

public class Planet extends CelestialBody {
    public StaticOrbit staticOrbit;

    public Planet(double radius, double SOI, double mu, Color color, double semiMajorAxis, double eccentricity){
        super(radius, SOI, mu, color);
        staticOrbit = new StaticOrbit(semiMajorAxis, eccentricity);
    }

    public void update(double dt){
        double timeStep = dt * Orbiter.getWarpSpeed();
        position = staticOrbit.updatePosition(timeStep);

        velocity.setX((position.getX() - oldPosition.getX()) / timeStep);
        velocity.setY((position.getY() - oldPosition.getY()) / timeStep);

        oldPosition.setX(position.getX());
        oldPosition.setY(position.getY());

    }

    public void reset(){
        staticOrbit.trueAnomaly = 0;
    }

}
