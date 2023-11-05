import java.awt.*;

public class Spacecraft extends Orbiter {
    public Orbit orbit;
    private int nodeIndex = 0;
    private int subIndex = 0;
    public boolean engineAcceleration = false;
    private double engineModeDirection = 0;
    private double accelerationDirection;
    private double fuelMass = 500;
    private double deltaV;
    private final double dryMass = 500;
    private final double equivalentVelocity = 2.7;
    private final double [] massFlowRates = {0, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1.0};
    private int throttleIndex = massFlowRates.length;
    private double massFlowRate = 1.0;

    public Spacecraft(CelestialBody celestialBody){
        super("spacecraft");
        orbit = new Orbit(celestialBody);
        deltaV = equivalentVelocity * Math.log((dryMass + fuelMass) / dryMass);
        setPosition(new Vector(0, 0));
    }

    public void update(){
        if (subIndex < subIndexMax){
            subIndex ++; // spacecraft does not move
        } else {
            updateNodeIndex();
            subIndex = 0;
        }

        // check rotation completed (only applicable for ellipse)
        if (!orbit.isOnEscapePath && !orbit.isOnCrashPath && nodeIndex >= orbit.numberOfNodes) {
            nodeIndex = nodeIndex % orbit.numberOfNodes;
        }

        // check for escape from SOI of planet
        for (Planet planet : OrbitCalculator.getPlanets()) {
            if (orbit.celestialBody == planet && orbit.isOnEscapePath && nodeIndex >= orbit.numberOfNodes) {
                System.out.println("The spacecraft is now leaving " + planet.name + " SOI"); // Spacecrafts starts to orbit the sun
                orbit.celestialBody = OrbitCalculator.getSun();
                recalculateOrbit(planet.position.add(position), planet.velocity.add(velocity));
                updateNodeIndex();

                setPosition(orbit.positionsWrtCb.get(nodeIndex));
                updatePixelPosition();

                return;
            }
        }

        // check for encounter planet's SOI
        if (orbit.celestialBody == OrbitCalculator.getSun()) {
            for (Planet planet : OrbitCalculator.getPlanets()) {
                Vector positionAtEncounter = position.subtract(planet.position);

                if (positionAtEncounter.getAbs() < planet.SOI) {
                    System.out.println("Spacecraft is now entering " + planet.name + " SOI");

                    orbit.celestialBody = planet;
                    recalculateOrbit(positionAtEncounter, velocity.subtract(planet.velocity));
                    return;
                }
            }
        }

        // check flew out of sun's SOI or crashed
        if (nodeIndex >= 0) {
            if (nodeIndex >= orbit.numberOfNodes || orbit.positionsWrtCb.get(nodeIndex).getAbs() < orbit.celestialBody.radius) {
                reset();
                OrbitCalculator.resetPlanets();
                return;
            }

            // update vectors
            setPosition(orbit.positionsWrtCb.get(nodeIndex));
            if (nodeIndex > 0) {
                oldPosition = orbit.positionsWrtCb.get(nodeIndex - 1);
                velocity.setX((position.getX() - oldPosition.getX()) / orbit.dT);
                velocity.setY((position.getY() - oldPosition.getY()) / orbit.dT);
            }
        }

        // check engine acceleration
        if (fuelMass > massFlowRate * Orbiter.getWarpSpeed() && massFlowRate > 0) {
            if (engineAcceleration && Orbiter.warpIndex < 3) {

                accelerationDirection = velocity.getAngle() + engineModeDirection;

                // Rocket equation over one timestep:
                double currentMass = dryMass + fuelMass;
                double deltaVUpdate = equivalentVelocity * Math.log(currentMass / (currentMass - massFlowRate * Orbiter.getWarpSpeed()));
                fuelMass -= massFlowRate * Orbiter.getWarpSpeed();

                velocity.addFromRadialCoordinates(deltaVUpdate, accelerationDirection);
                recalculateOrbit(position, velocity);
                nodeIndex = -1;

                // Remaining deltaV
                deltaV = equivalentVelocity * Math.log((dryMass + fuelMass) / dryMass);
            }
        } else {
            engineAcceleration = false;
        }
    }

    private void updateNodeIndex(){
        if (warpIndex > 2) { // spacecraft moves
            nodeIndex += WARP_SPEEDS[warpIndex - 2];
        } else {
            nodeIndex ++;
        }
    }

    public void throttleUp() {
        throttleIndex = Math.min(massFlowRates.length - 1, throttleIndex + 1);
        massFlowRate = massFlowRates[throttleIndex];
    }

    public void throttleDown() {
        throttleIndex = Math.max(0, throttleIndex - 1);
        massFlowRate = massFlowRates[throttleIndex];
    }

    public void initStartVectors(){
        double startAngle = 0.25 * Math.PI;
        double height = 0.1;

        Vector position = new Vector(height, startAngle, true);
        Vector velocity = new Vector(0.0001 * Math.round(10000 * Math.sqrt(orbit.celestialBody.mu / height)), startAngle - 0.5 * Math.PI, true);

        recalculateOrbit(position, velocity); // ellipse
        orbit.updatePixelsPositions();

        setPosition(orbit.positionsWrtCb.get(nodeIndex));
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

    private Vector getPosition(){
        return position;
    }

    public void updatePixelPosition() {
        orbit.updatePixelsPositions();

        x_int = orbit.celestialBody.x_int + (int) Math.round(OrbitCalculator.scaleFactor * position.getX());
        y_int = orbit.celestialBody.y_int - (int) Math.round(OrbitCalculator.scaleFactor * position.getY());
    }

    public void draw(Graphics2D g2d) {
        g2d.fillRect(x_int - 2 + xdrag, y_int - 2 + ydrag, 4,4);
    }

    public void reset(){
        System.out.println("Spacecraft reset");
        orbit.reset();
        initStartVectors();
        fuelMass = 500;
        warpIndex = 0;
        updateSubIndexMax();
        deltaV = equivalentVelocity * Math.log((dryMass + fuelMass) / dryMass);
        throttleIndex = massFlowRates.length;
        massFlowRate = 1.0;
    }

    public void firePrograde() {
        if (fuelMass > 0 && massFlowRate > 0) {
            engineAcceleration = true;
            engineModeDirection = 0;
        }
    }

    public void fireRetrograde() {
        if (fuelMass > 0 && massFlowRate > 0) {
            engineAcceleration = true;
            engineModeDirection = Math.PI;
        }
    }

    public void fireRight() {
        if (fuelMass > 0 && massFlowRate > 0) {
            engineAcceleration = true;
            engineModeDirection = 1.5 * Math.PI;
        }
    }

    public void fireLeft() {
        if (fuelMass > 0 && massFlowRate > 0) {
            engineAcceleration = true;
            engineModeDirection = 0.5 * Math.PI;
        }
    }

    public void drawThrustVector(Graphics2D g2d){
        if (engineAcceleration) {
            int pointX = x_int + (int) (Math.round(15 * Math.cos(accelerationDirection))) + xdrag;
            int pointY = y_int - (int) (Math.round(15 * Math.sin(accelerationDirection))) + ydrag;
            int pointX2 = x_int + (int) (Math.round(10 * Math.cos(accelerationDirection + 0.5))) + xdrag;
            int pointY2 = y_int - (int) (Math.round(10 * Math.sin(accelerationDirection + 0.5))) + ydrag;
            int pointX3 = x_int + (int) (Math.round(10 * Math.cos(accelerationDirection - 0.5))) + xdrag;
            int pointY3 = y_int - (int) (Math.round(10 * Math.sin(accelerationDirection - 0.5))) + ydrag;

            // draw arrow
            g2d.drawLine(x_int + xdrag, y_int + ydrag, pointX, pointY);
            g2d.drawLine(pointX, pointY, pointX2, pointY2);
            g2d.drawLine(pointX, pointY, pointX3, pointY3);
        }
    }

    public void drawFlightDataUI(Graphics2D g2d, int y){
        g2d.drawString(String.format("Velocity = %.3f km/s", velocity.getAbs()), 8, y - 65);
        g2d.drawString(String.format("Height = %.3f km", getPosition().getAbs() - orbit.celestialBody.radius), 8, y - 50);
    }

    public void drawPropellantUI(Graphics2D g2d, int x, int y){
        if (deltaV > 0.9){
            g2d.setColor(Color.GREEN);
        } else if (deltaV > 0.45) {
            g2d.setColor(Color.ORANGE);
        } else {
            g2d.setColor(Color.RED);
        }
        int leftEdge = x - 142;
        g2d.fillRect(leftEdge, y - 82, (int) (deltaV * 60), 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString(String.format("DeltaV = %.3f km/s", deltaV), leftEdge, y - 70);
        g2d.drawString(String.format("Throttle = %.0f%%", 100 * massFlowRate), leftEdge, y - 55);
        if (engineAcceleration) {
            g2d.fillRect(leftEdge, y - 50, (int) (120 * massFlowRate), 5);
        }
        for (int i = leftEdge; i <= x - 22; i += 12){
            g2d.drawLine(i, y - 42, i, y - 45);
        }
    }

}
