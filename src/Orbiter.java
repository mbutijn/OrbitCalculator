import java.awt.*;

public class Orbiter extends OrbitObject {
    private Vector position;
    public Orbit orbit;
    private int nodeIndex = 0;
    public boolean firing = false;
    private double engineModeDirection = 0;
    private double accelerationDirection;

    public Orbiter(Planet planet){
        orbit = new Orbit(planet);
    }

    public void update(){
        nodeIndex += orbit.skipIndex * warpSpeeds[warpIndex];
        if (nodeIndex >= orbit.numberOfNodes) { // reset nodeIndex after every rotation completed
            nodeIndex -= orbit.numberOfNodes;
        }
        if (nodeIndex > orbit.positions.size()){ // prevent out of bounds exception
            System.out.println("Orbit out of indices");
            reset();
            warpIndex = 0;
        }

        updatePosition();

        if (firing && OrbitObject.warpIndex == 0) { // engine burn

            setOldPosition(orbit.positions.get(nodeIndex - 1));
            updateVelocity(orbit.dT);

            accelerationDirection = velocity.getAngle() + engineModeDirection;

            velocity.addFromRadialCoordinates(0.004, accelerationDirection);
            try {
                orbit.recalculate(position, velocity);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            nodeIndex = 0;
        }
    }

    public void updatePosition() {
        setPosition(orbit.positions.get(nodeIndex));
    }

    public void setPosition(Vector position){
        this.position = position;
    }

    public void updateVelocity(double dT){
        velocity.setX((position.getX() - oldPosition.getX()) / dT);
        velocity.setY((position.getY() - oldPosition.getY()) / dT);
    }

    public void updatePixelPosition(int x, int y) {
        orbit.updatePixelPosition();

        x_int = x + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = y - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics2D g2d) {
        g2d.drawRect(x_int - 2, y_int - 2, 4,4);
    }

    public void drawThrustVector(Graphics2D g2d){
        if (firing) {
            int pointX = x_int + (int) (Math.round(15 * Math.cos(accelerationDirection)));
            int pointY = y_int - (int) (Math.round(15 * Math.sin(accelerationDirection)));
            int pointX2 = x_int + (int) (Math.round(10 * Math.cos(accelerationDirection + 0.5)));
            int pointY2 = y_int - (int) (Math.round(10 * Math.sin(accelerationDirection + 0.5)));
            int pointX3 = x_int + (int) (Math.round(10 * Math.cos(accelerationDirection - 0.5)));
            int pointY3 = y_int - (int) (Math.round(10 * Math.sin(accelerationDirection - 0.5)));

            // draw arrow
            g2d.drawLine(x_int, y_int, pointX, pointY);
            g2d.drawLine(pointX, pointY, pointX2, pointY2);
            g2d.drawLine(pointX, pointY, pointX3, pointY3);
        }
    }

    public void setOldPosition(Vector oldPosition) {
        this.oldPosition = oldPosition;
    }

    public void reset(){
        orbit.reset();
        nodeIndex = 0;
    }

    public void firePrograde() {
        firing = true;
        engineModeDirection = 0;
    }

    public void fireRetrograde() {
        firing = true;
        engineModeDirection = Math.PI;
    }

    public void fireRight() {
        firing = true;
        engineModeDirection = 1.5 * Math.PI;
    }

    public void fireLeft() {
        firing = true;
        engineModeDirection = 0.5 * Math.PI;
    }
}
