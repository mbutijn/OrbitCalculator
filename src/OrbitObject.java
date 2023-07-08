public class OrbitObject {
    public static int warpIndex = 0;
    public static final int[] warpSpeeds = {1, 5, 10, 50, 100, 500, 1000, 10000, 100000};
    protected final Vector velocity = new Vector(0, 0);
    protected Vector oldPosition = new Vector(0, 0);
    protected int x_int, y_int; // pixel locations

    public static void warpUp(){
        warpIndex = Math.min(warpSpeeds.length, warpIndex + 1);
        System.out.println("warpSpeed: " + warpSpeeds[warpIndex]);
    }

    public static void warpDown(){
        warpIndex = Math.max(0, warpIndex - 1);
        System.out.println("warpSpeed: " + warpSpeeds[warpIndex]);
    }

    protected static double getWarpSpeed() {
        return warpSpeeds[warpIndex];
    }
}
