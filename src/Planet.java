import java.awt.*;

public class Planet extends OrbitObject {
    public double radius;
    public int radius_int;
    public Vector position = new Vector(0, 0);

    public StaticOrbit staticOrbit;

    public Planet(){
        radius = 0.1;
        radius_int = (int) (OrbitCalculator.scaleFactor * radius);
        staticOrbit = new StaticOrbit();
    }

    public void update(double dt){
        double timeStep = dt * OrbitObject.getWarpSpeed();
        position = staticOrbit.updatePositionPlanet(timeStep);

        velocity.setX((position.getX() - oldPosition.getX()) / timeStep);
        velocity.setY((position.getY() - oldPosition.getY()) / timeStep);

        oldPosition.setX(position.getX());
        oldPosition.setY(position.getY());

    }

    public void updatePixelPosition() {
        x_int = OrbitCalculator.x_sun + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = OrbitCalculator.y_sun - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics g2d){
        g2d.drawOval(x_int - radius_int, y_int - radius_int, 2 * radius_int, 2 * radius_int);
    }
}
