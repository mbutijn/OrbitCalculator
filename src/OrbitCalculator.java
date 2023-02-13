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

    public OrbitCalculator(String title) {
        this.setTitle(title);
    }

    public static void main (String[] args) {
        new OrbitCalculator("Orbit Calculator").start();
    }

    public void start(){
        setVisible(true);
        setSize(xBound, yBound);
        setContentPane(space);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setKeyBoardListeners();

        orbit.recalculate(new Vector(-2, 2), new Vector(0.2, 0.2));

        // start the simulation
        new Timer(40, update).start(); // dt = 40 ms -> f_s = 25 Hz
    }

    private void setKeyBoardListeners() {
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
    }

    private final ActionListener update = e -> {
        index += warp;
        if (index >= orbit.numberOfNodes) {
            index -= orbit.numberOfNodes;
        }
        if (firing && warp == 1) { // engine burn
            Vector velocity = orbit.velocities.get(index);
            double direction = velocity.getAngle() + engineDirection;

            velocity.add(0.002 * Math.cos(direction), 0.002 * Math.sin(direction));
            orbit.recalculate(orbit.positions.get(index), velocity);
            index = 0;
        }
        orbiter.setX(orbit.x_int.get(index));
        orbiter.setY(orbit.y_int.get(index));
        space.repaint();
    };

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP) { // prograde engine burn
            firing = true;
            engineDirection = 0;
        }
        if (code == KeyEvent.VK_DOWN) { // retrograde engine burn
            firing = true;
            engineDirection = Math.PI;
        }
        if (code == KeyEvent.VK_RIGHT){ // radial in / radial out
            firing = true;
            engineDirection = 1.5*Math.PI;
        }
        if (code == KeyEvent.VK_LEFT){ // radial in / radial out
            firing = true;
            engineDirection = 0.5*Math.PI;
        }
        if (code == KeyEvent.VK_COMMA){
            warp = Math.max(1, warp / 10);
            System.out.println("warp: "+ warp);
        }
        if (code == KeyEvent.VK_PERIOD){
            warp = warp * 10;
            System.out.println("warp: "+ warp);
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
            g2d.fillOval(midX - 5, midY - 5, 10, 10);

            // draw extremes
            orbit.drawPeriapsis(g2d);
            orbit.drawApoapsis(g2d);
        }
    }

}
