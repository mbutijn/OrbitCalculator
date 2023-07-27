import java.awt.*;

public class Spacecraft extends Orbiter {
    public Orbit orbit;
    private int nodeIndex = 0;
    public boolean engineAcceleration = false;
    private double engineModeDirection = 0;
    private double accelerationDirection;

    public Spacecraft(CelestialBody celestialBody){
        orbit = new Orbit(celestialBody);
    }

    public void update(){
        nodeIndex += orbit.skipIndex * WARP_SPEEDS[warpIndex];

        // check for escape from SOI
        for (Planet planet : OrbitCalculator.getPlanets()) {
            if (orbit.celestialBody == planet && orbit.isOnEscapePath && nodeIndex >= orbit.numberOfNodes) {
                System.out.println("The spacecraft is now leaving a planet's SOI"); // Spacecrafts starts to orbit the sun
                Star sun = OrbitCalculator.getSun();
                Vector positionAtEscape = new Vector((double) (this.x_int - sun.x_int) / OrbitCalculator.scaleFactor,
                        (double) (sun.y_int - this.y_int) / OrbitCalculator.scaleFactor);

                Vector velocityAtEscape = velocity.add(orbit.celestialBody.velocity);

                orbit.celestialBody = sun;
                recalculateOrbit(positionAtEscape, velocityAtEscape);
                setPosition(orbit.positionsWrtCb.get(nodeIndex));
                updatePixelPosition();

                return;
            }
        }

        // check for encounter planet's SOI
        if (orbit.celestialBody == OrbitCalculator.getSun()) {
            for (Planet planet : OrbitCalculator.getPlanets()) {
                Vector positionAtEncounter = new Vector((double) (this.x_int - planet.x_int) / OrbitCalculator.scaleFactor,
                        (double) (planet.y_int - this.y_int) / OrbitCalculator.scaleFactor);

                if (positionAtEncounter.getAbs() < 0.9 * planet.SOI) {
                    System.out.println("Spacecraft is now encountering the planet");
                    Vector velocityAtEncounter = velocity.subtract(planet.velocity);

                    orbit.celestialBody = planet;
                    recalculateOrbit(positionAtEncounter, velocityAtEncounter);
                    return;
                }
            }
        }

        if (!orbit.isOnEscapePath && nodeIndex >= orbit.numberOfNodes) { // reset nodeIndex after every rotation completed
            nodeIndex = nodeIndex % orbit.numberOfNodes;
        }

        if (nodeIndex >= orbit.numberOfNodes){
            reset(); // prevents out of bounds exception
            OrbitCalculator.resetPlanets();
            return;
        }

        setPosition(orbit.positionsWrtCb.get(nodeIndex));
        if (nodeIndex == 0){
            setOldPosition(orbit.positionsWrtCb.get(orbit.numberOfNodes - 1));
        } else {
            setOldPosition(orbit.positionsWrtCb.get(nodeIndex - 1));
        }
        updateVelocity(orbit.dT);


        // check engine burn
        if (engineAcceleration && Orbiter.warpIndex == 0) {

            accelerationDirection = velocity.getAngle() + engineModeDirection;
            velocity.addFromRadialCoordinates(0.004, accelerationDirection);
            recalculateOrbit(position, velocity);
        }
    }

    public void initStartVectors(){
        double startAngle = 0.25 * Math.PI;
        double height = 0.35;

        Vector position = new Vector(height, startAngle, true);
        Vector velocity = new Vector(0.001 * Math.round(1000*Math.sqrt(orbit.celestialBody.mu / height)), startAngle - 0.5 * Math.PI, true);

        recalculateOrbit(position, velocity); // ellipse
        orbit.updatePixelPosition();
    }

    public void recalculateOrbit(Vector position, Vector velocity){
        try {
            orbit.recalculate(position, velocity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nodeIndex = 0;
    }

    public void setPosition(Vector position){
        this.position = position;
    }

    public void updateVelocity(double dT){
        velocity.setX((position.getX() - oldPosition.getX()) / dT);
        velocity.setY((position.getY() - oldPosition.getY()) / dT);
    }

    public void updatePixelPosition() {
        orbit.updatePixelPosition();

        x_int = orbit.celestialBody.x_int + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = orbit.celestialBody.y_int - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics2D g2d) {
        g2d.fillRect(x_int - 2, y_int - 2, 4,4);
    }

    public void drawThrustVector(Graphics2D g2d){
        if (engineAcceleration) {
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
        System.out.println("Spacecraft reset");
        orbit.reset();
        initStartVectors();
        nodeIndex = 0;
        warpIndex = 0;
    }

    public void firePrograde() {
        engineAcceleration = true;
        engineModeDirection = 0;
    }

    public void fireRetrograde() {
        engineAcceleration = true;
        engineModeDirection = Math.PI;
    }

    public void fireRight() {
        engineAcceleration = true;
        engineModeDirection = 1.5 * Math.PI;
    }

    public void fireLeft() {
        engineAcceleration = true;
        engineModeDirection = 0.5 * Math.PI;
    }
}
