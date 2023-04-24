import java.awt.*;

public class Orbiter {
    private int x_int, y_int; // pixel locations
    public Vector position;
    public Vector velocity;

    public void setPosition(Vector position){
        this.position = position;
    }

    public void setVelocity(Vector velocity){
        this.velocity = velocity;
    }

    public void updatePixelPosition() {
        x_int = OrbitCalculator.midX + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = OrbitCalculator.midY - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics2D g2d) {
        g2d.drawRect(x_int - 2, y_int - 2, 4 ,4);
    }

    public void drawThrustVector(Graphics2D g2d, double direction){
        int pointX = x_int + (int) (Math.round(15 * Math.cos(direction)));
        int pointY = y_int - (int) (Math.round(15 * Math.sin(direction)));
        int pointX2 = x_int + (int) (Math.round(10 * Math.cos(direction + 0.5)));
        int pointY2 = y_int - (int) (Math.round(10 * Math.sin(direction + 0.5)));
        int pointX3 = x_int + (int) (Math.round(10 * Math.cos(direction - 0.5)));
        int pointY3 = y_int - (int) (Math.round(10 * Math.sin(direction - 0.5)));

        // draw arrow
        g2d.drawLine(x_int, y_int, pointX, pointY);
        g2d.drawLine(pointX, pointY, pointX2, pointY2);
        g2d.drawLine(pointX, pointY, pointX3, pointY3);
    }

}
