import java.awt.*;

public class CelestialBody extends Orbiter{
    protected double radius;
    protected int radius_int;
    protected double SOI;
    protected final double mu; // standard gravitational parameter
    protected Color color;

    public CelestialBody(double radius, double SOI, double mu, Color color, String name){
        super(name);
        this.radius = radius;
        this.SOI = SOI;
        this.mu = mu;
        this.color = color;
        updateRadius();
    }

    public void updateRadius(){
        radius_int = (int) (OrbitCalculator.scaleFactor * radius);
    }

    public void updatePixelPosition() {
        x_int = OrbitCalculator.getSun().x_int + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = OrbitCalculator.getSun().y_int - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics2D g2d){
        g2d.setColor(color);
        g2d.fillOval(x_int - radius_int + xdrag, y_int - radius_int + ydrag, 2 * radius_int, 2 * radius_int);
    }

}
