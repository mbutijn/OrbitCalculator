import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OrbitCalculator extends JFrame implements KeyListener {
    private final static int xBound = 1024, yBound = 768;
    public static final int scaleFactor = 100;
    private final Space space = new Space();
    public final static Star sun = new Star(xBound / 2, yBound / 2, 0.2, 10, 0.5);
    public final static Planet planet = new Planet(0.1,0.8, 0.05);
    private final Spacecraft spacecraft = new Spacecraft(planet);
    private Timer timer;
    public final static double timeStep = 0.05;

    public OrbitCalculator(String title) {
        this.setTitle(title);
    }

    public static void main (String[] args) {
        new OrbitCalculator("Orbit Simulator").start();
    }

    public void start() {
        setVisible(true);
        setSize(xBound, yBound);
        setContentPane(space);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setKeyBoardListeners();

        spacecraft.orbit.recalculate(new Vector(0.25, 0.25), new Vector(0.27, -0.27)); // ellipse
        spacecraft.orbit.updatePixelPosition();

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

    public static Planet getPlanet(){
        return planet;
    }

    private final ActionListener update = e -> {
        planet.update(timeStep);
        spacecraft.update();

        planet.updatePixelPosition();
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
            if (code == KeyEvent.VK_RIGHT) { // radial in or radial out
                spacecraft.fireRight();
            }
            if (code == KeyEvent.VK_LEFT) { // radial in or radial out
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

            // draw orbit
            spacecraft.orbit.draw(g2d);

            // draw orbiter
            spacecraft.draw(g2d);

            // draw celestialBody
            planet.draw(g2d);

            // draw sun
            sun.draw(g2d);

            // draw extremes
            spacecraft.orbit.drawPeriapsis(g2d);
            spacecraft.orbit.drawApoapsis(g2d);

            // draw SOI
            spacecraft.orbit.drawSOI(g2d);

            // draw engine thrust direction
            spacecraft.drawThrustVector(g2d);

        }
    }

}
