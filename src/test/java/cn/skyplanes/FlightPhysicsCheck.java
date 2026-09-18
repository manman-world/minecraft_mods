package cn.skyplanes;

public final class FlightPhysicsCheck {
    public static void main(String[] args) {
        double speed = 0;
        for (int tick = 0; tick < 100; tick++) speed = FlightPhysics.speed(speed, true, false);
        check(Math.abs(speed - 0.85) < 1e-9, "Acceleration must reach the speed limit");
        check(FlightPhysics.vertical(speed, -20, 0) > 0, "Looking up at flight speed must climb");
        check(FlightPhysics.vertical(speed, 20, 0) < 0, "Looking down must descend");
        for (int tick = 0; tick < 60; tick++) speed = FlightPhysics.speed(speed, false, true);
        check(speed == 0, "Braking must stop without reversing");
        check(FlightPhysics.vertical(speed, -30, 0) < 0, "A stalled plane must not hover");
        check(FlightPhysics.vertical(0, 0, -10) == -0.6, "Unpowered descent must stay bounded");
        System.out.println("PASS: acceleration, speed limit, climb, descent, braking, stall and descent limit");
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
