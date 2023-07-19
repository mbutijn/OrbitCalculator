public class Orbiter {
    public static int warpIndex = 0;
    public static final int[] WARP_SPEEDS = {1, 5, 10, 50, 100}; //, 500 1000, 10000, 100000};
    protected final Vector velocity = new Vector(0, 0);
    protected Vector position;
    protected Vector oldPosition = new Vector(0, 0);
    protected int x_int, y_int; // pixel locations

    public static void warpUp(){
        warpIndex = Math.min(WARP_SPEEDS.length - 1, warpIndex + 1);
        System.out.println("warpSpeed: " + WARP_SPEEDS[warpIndex]);
    }

    public static void warpDown(){
        warpIndex = Math.max(0, warpIndex - 1);
        System.out.println("warpSpeed: " + WARP_SPEEDS[warpIndex]);
    }

    protected static double getWarpSpeed() {
        return WARP_SPEEDS[warpIndex];
    }
}
