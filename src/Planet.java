import java.awt.*;

public class Planet extends CelestialBody {
    public StaticOrbit staticOrbit;

    public Planet(double radius, double SOI, double mu, Color color, double a, double e){
        super(radius, SOI, mu, color);
        staticOrbit = new StaticOrbit(a, e);
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
        staticOrbit.nu = 0;
    }

}
