import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class OrbitCalculator extends JFrame implements KeyListener {
    private final static int xBound = 1024, yBound = 768;
    public static final int midX = xBound / 2;
    public static final int midY = yBound / 2;
    public static final int scaleFactor = 100;
    private final Space space = new Space();
    private final Orbiter orbiter = new Orbiter();
    private final Orbit orbit = new Orbit();
    private int index = 0;
    private int warp = 1;
    private boolean firing = false;
    private double engineDirection = 0;
    private double fireDirection;

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

//        orbit.recalculate(new Vector(-2, 2), new Vector(0.2, 0.2)); // ellipse
        orbit.dT = 0.01;
        orbit.skipIndex = 1;
        //orbit.recalculate(new Vector(-2, 2), new Vector(1.01*Math.pow(2, -0.75), 1.01*Math.pow(2, -0.75))); // escape trajectory
        orbit.recalculate(new Vector(4.5, 0), new Vector(-0.4, 0.4));

        orbit.updatePixelPosition();

        // start the simulation
        new Timer(50, update).start(); // dt = 50 ms -> f_s = 20 Hz
    }

    private void setKeyBoardListeners() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
    }

    private final ActionListener update = e -> {
        index += orbit.skipIndex * warp;
        if (index >= orbit.numberOfNodes) {
            index -= orbit.numberOfNodes;
        }

        orbiter.setPosition(orbit.positions.get(index));

        if (firing && warp == 1) { // engine burn
            orbiter.setVelocity(new Vector((orbit.positions.get(index).getX() - orbit.positions.get(index-1).getX()) / orbit.dT,
                    (orbit.positions.get(index).getY() - orbit.positions.get(index-1).getY()) / orbit.dT));

            Vector velocity = orbiter.velocity;
            fireDirection = velocity.getAngle() + engineDirection;

            velocity.addFromRadialCoordinates(0.002, fireDirection);
            try {
                orbit.recalculate(orbit.positions.get(index), velocity);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            index = 0;
        }

        orbit.updatePixelPosition();
        orbiter.updatePixelPosition();

        space.repaint();
    };

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP && warp == 1) { // prograde engine burn
            firing = true;
            engineDirection = 0;
        }
        if (code == KeyEvent.VK_DOWN && warp == 1) { // retrograde engine burn
            firing = true;
            engineDirection = Math.PI;
        }
        if (code == KeyEvent.VK_RIGHT && warp == 1){ // radial in / radial out
            firing = true;
            engineDirection = 1.5*Math.PI;
        }
        if (code == KeyEvent.VK_LEFT && warp == 1){ // radial in / radial out
            firing = true;
            engineDirection = 0.5*Math.PI;
        }
        if (code == KeyEvent.VK_COMMA){
            warp = Math.max(1, warp / 10);
            System.out.println("warp: " + warp);
        }
        if (code == KeyEvent.VK_PERIOD){
            warp = warp * 10;
            System.out.println("warp: " + warp);
        }
        if (code == KeyEvent.VK_ESCAPE){ // orbit reset button
            try {
                orbit.dT = 0.001;
                orbit.skipIndex = 10;
                orbit.recalculate(new Vector(-2, 2), new Vector(0.2, 0.2));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            warp = 1;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        firing = false;
    }

    private class Space extends JPanel {
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2d = (Graphics2D) graphics;

            // draw orbit
            orbit.draw(g2d);

            // draw orbiter
            orbiter.draw(g2d);

            // draw celestial body
            g2d.drawOval(midX - 20, midY - 20, 40, 40);

            // draw extremes
            orbit.drawPeriapsis(g2d);
            orbit.drawApoapsis(g2d);

            // draw engine thrust direction
            if(firing){
                orbiter.drawThrustVector(g2d, fireDirection);
            }
        }
    }

}
