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
    private int nodeIndex = 0;
    private int warpIndex = 0;
    private final int[] warpSpeeds = {1, 5, 10, 50, 100, 500, 1000, 10000, 100000};
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
//        orbit.recalculate(new Vector(-2, 2), new Vector(1.01*Math.pow(2, -0.75), 1.01*Math.pow(2, -0.75))); // escape trajectory
        orbit.recalculate(new Vector(-4, -3.5), new Vector(0.9, 0.9)); // hyperbolic trajectory
//        orbit.recalculate(new Vector(4.5, 0), new Vector(-0.4, 0.4));

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
        nodeIndex += orbit.skipIndex * warpSpeeds[warpIndex];
        if (nodeIndex >= orbit.numberOfNodes) { // reset nodeIndex if rotation completed
            nodeIndex -= orbit.numberOfNodes;
        }
        if (nodeIndex > orbit.positions.size()){ // reset orbit if off the map
            System.out.println("Orbit reset");
            orbit.reset();
            warpIndex = 0;
        }
        orbiter.setPosition(orbit.positions.get(nodeIndex));

        if (firing && warpIndex == 0) { // engine burn
            orbiter.setVelocity(new Vector((orbit.positions.get(nodeIndex).getX() - orbit.positions.get(nodeIndex -1).getX()) / orbit.dT,
                    (orbit.positions.get(nodeIndex).getY() - orbit.positions.get(nodeIndex -1).getY()) / orbit.dT));

            Vector velocity = orbiter.velocity;
            fireDirection = velocity.getAngle() + engineDirection;

            velocity.addFromRadialCoordinates(0.004, fireDirection);
            try {
                orbit.recalculate(orbit.positions.get(nodeIndex), velocity);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            nodeIndex = 0;
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
        if (warpIndex == 0){
            if (code == KeyEvent.VK_UP) { // prograde engine burn
                firing = true;
                engineDirection = 0;
            }
            if (code == KeyEvent.VK_DOWN) { // retrograde engine burn
                firing = true;
                engineDirection = Math.PI;
            }
            if (code == KeyEvent.VK_RIGHT) { // radial in / radial out
                firing = true;
                engineDirection = 1.5 * Math.PI;
            }
            if (code == KeyEvent.VK_LEFT) { // radial in / radial out
                firing = true;
                engineDirection = 0.5 * Math.PI;
            }
        }

        if (code == KeyEvent.VK_COMMA){
            warpIndex = Math.max(0, warpIndex-1);
            System.out.println("warpSpeed: " + warpSpeeds[warpIndex]);
        }
        if (code == KeyEvent.VK_PERIOD){
            warpIndex = Math.min(warpSpeeds.length, warpIndex+1);
            System.out.println("warpSpeed: " + warpSpeeds[warpIndex]);
        }
        if (code == KeyEvent.VK_ESCAPE){ // orbit reset button
            orbit.reset();
            warpIndex = 0;
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

            // draw planet
            g2d.drawOval(midX - 20, midY - 20, 40, 40);

            // draw extremes
            orbit.drawPeriapsis(g2d);
            orbit.drawApoapsis(g2d);

            // draw engine thrust direction
            if (firing){
                orbiter.drawThrustVector(g2d, fireDirection);
            }
        }
    }

}
