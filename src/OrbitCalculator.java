import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OrbitCalculator extends JFrame implements KeyListener {
    private static int xBound;
    private static int yBound;
    public static int scaleFactor = 100;
    private final Space space = new Space();
    public final static Star sun = new Star(0, 0, 0.18, 10, 1.0, Color.YELLOW);
    public final static Planet homePlanet = new Planet(0.08,0.75, 0.05, Color.CYAN, 2.4, 0.2);
    public final static Planet mars = new Planet(0.05,0.45, 0.03, Color.RED, 3.8, 0.15);
    private final Spacecraft spacecraft = new Spacecraft(homePlanet);
    private Timer timer;
    public final static double timeStep = 0.05;
    private static final ArrayList<Planet> planets = new ArrayList<>();

    public OrbitCalculator(String title) {
        this.setTitle(title);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                xBound = (int) getSize().getWidth();
                yBound = (int) getSize().getHeight();
                System.out.println("resized to: " + xBound + " by " + yBound);
                sun.updateMiddle(xBound / 2, yBound / 2);
            }
        });
    }

    public static void main (String[] args) {
        new OrbitCalculator("Orbit Simulator").start();
    }

    public void start() {
        setVisible(true);
        xBound = 1024;
        yBound = 768;

        setSize(xBound, yBound);
        setContentPane(space);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setKeyBoardListeners();

        planets.add(homePlanet);
        planets.add(mars);

        spacecraft.initStartVectors(); // starts in elliptical orbit around homePlanet

        // start the simulation
        timer = new Timer((int) (1000 * timeStep), update);
        timer.start(); // timeStep = 50 ms -> f_s = 20 Hz
    }

    private void setKeyBoardListeners() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
    }

    public static Star getSun(){
        return sun;
    }

    public static ArrayList<Planet> getPlanets(){
        return planets;
    }

    private final ActionListener update = e -> {
        for (Planet planet : getPlanets()){
            planet.update(timeStep);
            planet.updatePixelPosition();
        }

        spacecraft.update();
        spacecraft.updatePixelPosition();

        space.repaint();
    };

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (Orbiter.warpIndex == 0){
            if (code == KeyEvent.VK_UP) { // prograde engine burn
                spacecraft.firePrograde();
            }
            if (code == KeyEvent.VK_DOWN) { // retrograde engine burn
                spacecraft.fireRetrograde();
            }
            if (code == KeyEvent.VK_RIGHT) { // radial in for clockwise, radial out for counterclockwise
                spacecraft.fireRight();
            }
            if (code == KeyEvent.VK_LEFT) { // radial in for counterclockwise, radial out for clockwise
                spacecraft.fireLeft();
            }
        }

        if (code == KeyEvent.VK_COMMA){
            Orbiter.warpDown();
        }
        if (code == KeyEvent.VK_PERIOD){
            Orbiter.warpUp();
        }
        if (code == KeyEvent.VK_ESCAPE){ // pause
            if (timer.isRunning()) {
                System.out.println("paused");
                timer.stop();
            } else {
                System.out.println("resumed");
                timer.start();
            }
        }
        if (code == KeyEvent.VK_SPACE){ // orbits reset button
            System.out.println("Reset simulation");
            spacecraft.reset();
            resetPlanets();
        }
        if (code == KeyEvent.VK_HOME){
            scaleFactor = 50; // zoom min
            updateRadii();
        }
        if (code == KeyEvent.VK_PAGE_UP) { // zoom out
            scaleFactor = Math.max(50, scaleFactor - 5);
            updateRadii();
        }
        if (code == KeyEvent.VK_PAGE_DOWN) { // zoom in
            scaleFactor = Math.min(200, scaleFactor + 5);
            updateRadii();
        }
        if (code == KeyEvent.VK_END){
            scaleFactor = 200; // zoom max
            updateRadii();
        }
    }

    private void updateRadii(){
        sun.updateRadius();
        for (Planet planet : getPlanets()) {
            planet.updateRadius();
        }
        System.out.println("zoom level: " + scaleFactor);
    }

    public static void resetPlanets() {
        for (Planet planet : getPlanets()) {
            planet.reset();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        spacecraft.engineAcceleration = false;
    }

    private class Space extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2d = (Graphics2D) graphics;

            // draw space
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, xBound, yBound);

            // draw orbit
            g2d.setColor(Color.WHITE);
            spacecraft.orbit.draw(g2d);

            // draw spacecraft
            spacecraft.draw(g2d);

            // draw celestialBodies
            homePlanet.draw(g2d);
            mars.draw(g2d);
            sun.draw(g2d);

            // draw extremes
            g2d.setColor(Color.WHITE);
            spacecraft.orbit.drawPeriapsis(g2d);
            spacecraft.orbit.drawApoapsis(g2d);

            // draw SOI
            spacecraft.orbit.drawSOI(g2d);

            // draw engine thrust direction
            spacecraft.drawThrustVector(g2d);

        }
    }

}
