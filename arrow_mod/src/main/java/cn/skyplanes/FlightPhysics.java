package cn.skyplanes;

/** Flight tuning in blocks per tick; independent of the game for repeatable checks. */
public final class FlightPhysics {
    private FlightPhysics() {}
    public static double speed(double previous, boolean accelerate, boolean brake) {
        return Math.max(0, Math.min(0.85, previous + (accelerate ? 0.012 : 0) - (brake ? 0.024 : 0) - 0.002));
    }
    public static double vertical(double speed, float pitch, double previous) {
        return speed > 0.25 ? -Math.sin(Math.toRadians(Math.max(-30, Math.min(35, pitch)))) * speed
            : Math.max(previous - 0.04, -0.6);
    }
}
