import java.awt.*;

public class Orbiter {
    public static int warpIndex = 0;
    public static final int[] WARP_SPEEDS = {1, 5, 10, 50, 100, 500, 1000, 5000, 10000};
    protected final Vector velocity = new Vector(0, 0);
    protected Vector position;
    protected Vector oldPosition = new Vector(0, 0);
    protected int x_int, y_int; // pixel locations
    protected String name;
    protected static int xdrag = 0, ydrag = 0;
    public static int subIndexMax = 0;

    Orbiter(String name){
        this.name = name;
        updateSubIndexMax();
    }

    public static void warpUp(){
        warpIndex = Math.min(WARP_SPEEDS.length - 1, warpIndex + 1);
        updateSubIndexMax();
    }

    public static void warpDown(){
        warpIndex = Math.max(0, warpIndex - 1);
        updateSubIndexMax();
    }

    protected static double getWarpSpeed() {
        return WARP_SPEEDS[warpIndex];
    }

    protected static void updateSubIndexMax(){
        if (warpIndex == 0){
            subIndexMax = 9;
        } else if (warpIndex == 1){
            subIndexMax = 1;
        } else {
            subIndexMax = 0;
        }
    }

    public static void drawWarpUI(Graphics2D g2d){
        int [] x_triangle = new int[]{10, 10, 25};
        int [] y_triangle = {5, 25, 15};

        g2d.setColor(Color.GREEN);
        g2d.fillPolygon(x_triangle, y_triangle, 3);

        for (int i = 0; i < warpIndex; i++){
            for (int j = 0; j < x_triangle.length; j++){
                x_triangle[j] = x_triangle[j] + 10;
            }
            g2d.fillPolygon(x_triangle, y_triangle, 3);
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString("Warp speed = " + WARP_SPEEDS[warpIndex] + "x", 10, 40);
    }
}
