import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OrbitCalculator extends JFrame implements KeyListener {
    private static int xBound;
    private static int yBound;
    public static int scaleFactor = 1000;
    private final static Star sun = new Star(0.1, 10, 0.1, Color.YELLOW);
    private final static Planet earth = new Planet(0.04,0.75, 0.005, Color.CYAN, 2.8, 0.2, "earth");
    private final static Planet mars = new Planet(0.02,0.45, 0.003, Color.RED, 4.9, 0.15, "mars");
    private final Spacecraft spacecraft = new Spacecraft(earth);
    private final Space space = new Space();
    private Timer timer;
    public final static double timeStep = 0.05;
    private static final ArrayList<Planet> planets = new ArrayList<>();
    private Point mousePoint;
    private int cameraIndex = 3;
    private boolean dragged = false;
    private boolean zoomedOut, zoomedIn;
    private Orbiter orbiter;
    private long timeCameraPositionChanged;
    private boolean showUI;
    private Date date;

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

        addMouseWheelListener(e -> {
            if (e.getWheelRotation() == 1){
                zoomedOut = true;
            } else {
                zoomedIn = true;
            }
        });

        this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
                repaint();
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragged = true;
                timeCameraPositionChanged = System.currentTimeMillis();
                Orbiter.xdrag += e.getX() - mousePoint.x;
                Orbiter.ydrag += e.getY() - mousePoint.y;
                mousePoint = e.getPoint();
                repaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
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
        showUI = true;

        planets.add(earth);
        planets.add(mars);
        setCameraPosition();
        timeCameraPositionChanged = System.currentTimeMillis();

        spacecraft.initStartVectors(); // spacecraft starts in circular orbit around earth
        date = new Date();

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
//        long beginTime = System.currentTimeMillis();

        date.update();
        if (zoomedOut) {
            scaleFactor = Math.max(50, (int) (scaleFactor * 0.9));
            updateRadii();
            zoomedOut = false;
        } else if (zoomedIn) {
            scaleFactor = Math.min(5000, (int) (scaleFactor * 1.1));
            updateRadii();
            zoomedIn = false;
        }

        for (Planet planet : getPlanets()){
            planet.update(0.0001);
        }
        spacecraft.update();

        // Update all pixel locations
        for (Planet planet : getPlanets()) {
            planet.updatePixelPosition();
        }
        spacecraft.updatePixelPosition();
        space.repaint();

        if (!dragged){
            setCameraPosition();
        }

//        long endTime = System.currentTimeMillis();
//        System.out.println("elapsed: " + (endTime - beginTime));
    };

    public void updateRadii(){
        sun.updateRadius();
        for (Planet planet : getPlanets()) {
            planet.updateRadius();
        }
    }

    public void setCameraPosition(){
        switch (cameraIndex) {
            case 0 ->
                orbiter = sun;
            case 1, 2 ->
                orbiter = planets.get(cameraIndex - 1);
            case 3 ->
                orbiter = spacecraft;
        }
        Orbiter.xdrag = xBound / 2 - orbiter.x_int;
        Orbiter.ydrag = yBound / 2 - orbiter.y_int;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (Orbiter.warpIndex < 3){
            if (code == KeyEvent.VK_UP) { // prograde engine burn
                spacecraft.firePrograde();
            }
            if (code == KeyEvent.VK_DOWN) { // retrograde engine burn
                spacecraft.fireRetrograde();
            }
            if (code == KeyEvent.VK_RIGHT) { // engine burn radial in for clockwise, radial out for counterclockwise
                spacecraft.fireRight();
            }
            if (code == KeyEvent.VK_LEFT) { // engine burn radial in for counterclockwise, radial out for clockwise
                spacecraft.fireLeft();
            }
        }

        if (code == KeyEvent.VK_COMMA){
            Orbiter.warpDown();
        }
        if (code == KeyEvent.VK_PERIOD){
            Orbiter.warpUp();
        }
        if (code == KeyEvent.VK_ESCAPE){ // pause/resume
            if (timer.isRunning()) {
                timer.stop();
                space.repaint();
            } else {
                timer.start();
            }
        }
        if (code == KeyEvent.VK_SPACE){ // orbit reset button
            System.out.println("Reset simulation");
            spacecraft.reset();
            resetPlanets();
            date.reset();
        }
        if (code == KeyEvent.VK_V){ // make camera focus on next object
            dragged = false;
            cameraIndex++;
            cameraIndex = cameraIndex > 3 ? 0 : cameraIndex;

            timeCameraPositionChanged = System.currentTimeMillis();
            setCameraPosition();
            repaint();
        }
        if (code == KeyEvent.VK_PAGE_UP){
            spacecraft.throttleUp();
        }
        if (code == KeyEvent.VK_PAGE_DOWN){
            spacecraft.throttleDown();
        }
        if (code == KeyEvent.VK_F2){
            showUI = !showUI;
        }
    }

    public static void resetPlanets() {
        for (Planet planet : getPlanets()) {
            planet.reset();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN || code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) {
            spacecraft.engineAcceleration = false;
        }
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
            if (showUI) {
                g2d.setColor(Color.LIGHT_GRAY);
                spacecraft.orbit.draw(g2d);

                // draw extremes
                spacecraft.orbit.drawPeriapsis(g2d);
                spacecraft.orbit.drawApoapsis(g2d);

                // draw SOI
                spacecraft.orbit.drawSOI(g2d);
            }

            // draw celestialBodies
            sun.draw(g2d);
            earth.draw(g2d);
            mars.draw(g2d);

            // draw spacecraft
            g2d.setColor(Color.WHITE);
            spacecraft.draw(g2d);

            if (showUI) {
                // draw engine thrust direction
                spacecraft.drawThrustVector(g2d);

                // draw UI components:
                Orbiter.drawWarpUI(g2d); // warp speeds
            }

            if (!timer.isRunning()) { // pause message
                g2d.drawString("Simulation paused", xBound / 2 - 40, 25);
            }
            if (System.currentTimeMillis() - timeCameraPositionChanged < 2000) { // camera position message
                g2d.drawString("Camera position: " + (dragged ? "free" : orbiter.name), xBound / 2 - 45, yBound - 50);
            }

            if (showUI) {
                // UI backgrounds
                g2d.setColor(Color.GRAY);
                g2d.fillRect(0, yBound - 130, 193, 130); // orbit data
                g2d.fillRect(xBound - 150, yBound - 90, 150, 90); // spacecraft data
                g2d.fillRect(xBound - 100, 0, 100, 20); // date

                // orbit, spacecraft data and date UI
                g2d.setColor(Color.BLACK);
                spacecraft.orbit.drawUI(g2d, yBound);
                spacecraft.drawFlightDataUI(g2d, yBound);
                spacecraft.drawPropellantUI(g2d, xBound, yBound);
                date.drawUI(g2d, xBound);
            }
        }
    }

}
