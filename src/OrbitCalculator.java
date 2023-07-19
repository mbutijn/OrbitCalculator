import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OrbitCalculator extends JFrame implements KeyListener {
    private final static int xBound = 1024, yBound = 768;
    public static final int x_sun = xBound / 2;
    public static final int y_sun = yBound / 2;
    public static final int scaleFactor = 100;
    private final Space space = new Space();
    public final static Planet planet = new Planet(0.1,0.8, 0.1);
    public final static Star sun = new Star(x_sun, y_sun, 0.2, 10, 1.0);
    private final Spacecraft spacecraft = new Spacecraft(planet);
    private Timer timer;

    public OrbitCalculator(String title) {
        this.setTitle(title);
    }

    public static void main (String[] args) throws Exception {
        new OrbitCalculator("Orbit Calculator").start();
    }

    public void start() throws Exception {
        setVisible(true);
        setSize(xBound, yBound);
        setContentPane(space);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setKeyBoardListeners();

        spacecraft.orbit.recalculate(new Vector(0.5, 0.3), new Vector(0.12, -0.33)); // ellipse
        spacecraft.orbit.updatePixelPosition();

        // start the simulation
        timer = new Timer(50, update);
        timer.start(); // dt = 50 ms -> f_s = 20 Hz
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
        planet.update(0.05);
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
            System.out.println("staticOrbit reset");
            spacecraft.reset();
            planet.staticOrbit.nu = 0;
            Orbiter.warpIndex = 0;
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

            // draw engine thrust direction
            spacecraft.drawThrustVector(g2d);

        }
    }

}
