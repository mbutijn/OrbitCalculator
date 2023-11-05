import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Date {
    private Calendar calender;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private final StaticOrbit earthOrbit;
    private final double rotationEarthPerDay;
    private double referenceAngleEarth;

    public Date(){
        calender = Calendar.getInstance();
        earthOrbit = OrbitCalculator.getPlanets().get(0).staticOrbit;
        rotationEarthPerDay = 2 * Math.PI / 365.256; // = 0.01720214125 rad/day
        referenceAngleEarth = earthOrbit.getTrueAnomaly();
    }

    public void update(){
        if (earthOrbit.getTrueAnomaly() - referenceAngleEarth > rotationEarthPerDay) {
            int daysPassed = (int) Math.floor((earthOrbit.getTrueAnomaly() - referenceAngleEarth) / rotationEarthPerDay);
            referenceAngleEarth += daysPassed * rotationEarthPerDay;
            calender.add(Calendar.DAY_OF_MONTH, daysPassed);
        }
    }

    public void drawUI(Graphics2D g2d, int x){
        String date = dateFormat.format(calender.getTime());
        g2d.drawString(date, x - 90, 15);
    }

    public void reset() {
        calender = Calendar.getInstance();
        referenceAngleEarth = earthOrbit.getTrueAnomaly();
    }
}
