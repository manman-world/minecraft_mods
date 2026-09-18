package cn.skyplanes;

import static cn.skyplanes.RocketFlight.*;

/** Exercises the production lifecycle, including interruption and save/reload boundaries. */
public final class RocketFlightCheck {
    public static void main(String[] args) {
        RocketFlight idle = new RocketFlight();
        require(idle.canRecover() && idle.tick(64, false, false) == Action.NONE, "Unlit rocket remains recoverable");
        require(!idle.ignite(Double.NaN) && idle.canRecover(), "Invalid launch coordinates must not arm rocket");
        require(idle.ignite(64) && !idle.ignite(64) && !idle.canRecover(), "Ignition must be atomic and single-use");
        for (int tick = 1; tick < COUNTDOWN_TICKS; tick++) {
            require(idle.tick(64, false, false) == Action.NONE, "Launch before three-second countdown");
            require(!idle.canRecover(), "Countdown allowed recovery");
        }
        require(idle.tick(64, false, false) == Action.LAUNCH, "Countdown did not launch on tick 60");
        require(!idle.ignite(64) && !idle.canRecover(), "Flying rocket can be rearmed or recovered");

        double y = 64;
        int explosions = 0;
        for (int tick = 0; tick < MAX_FLIGHT_TICKS + 10; tick++) {
            Action action = idle.tick(y, false, false);
            if (action == Action.ASCEND) {
                require(idle.speed() > 0 && idle.speed() <= MAX_SPEED, "Vertical speed outside limits");
                y += idle.speed();
            }
            if (action == Action.EXPLODE) {
                explosions++;
                require(y >= 64 + BURST_HEIGHT && y < 64 + BURST_HEIGHT + MAX_SPEED, "Height detonation mismatch");
            }
        }
        require(explosions == 1 && idle.phase() == Phase.FINISHED, "Detonation must occur exactly once");
        require(!idle.canRecover() && !idle.ignite(y), "Spent rocket allowed kit duplication");

        RocketFlight obstructed = new RocketFlight();
        obstructed.ignite(64);
        require(obstructed.tick(64, true, false) == Action.FIZZLE, "Countdown obstruction must not explode");
        require(obstructed.tick(64, false, false) == Action.NONE && !obstructed.canRecover(), "Aborted launch returned a kit");
        require(flyingAt(0).tick(75, true, false) == Action.FIZZLE, "Flying obstruction must take priority over blast");
        require(flyingAt(0).tick(64, false, true) == Action.FIZZLE, "Ceiling near pad must not explode");
        require(flyingAt(0).tick(70, false, true) == Action.EXPLODE, "Ceiling aloft should terminate safely");
        require(flyingAt(MAX_FLIGHT_TICKS).tick(69.99, false, false) == Action.FIZZLE, "Low-altitude timeout must not explode");
        require(flyingAt(MAX_FLIGHT_TICKS).tick(70, false, false) == Action.EXPLODE, "Airborne timeout must explode");
        RocketFlight stalled = flyingAt(0);
        for (int i = 0; i < MAX_FLIGHT_TICKS; i++) require(stalled.tick(64, false, false) == Action.ASCEND, "Timeout too early");
        require(stalled.tick(64, false, false) == Action.FIZZLE, "Stationary flight must not run forever");

        // Reload on every tick of a complete launch and compare with uninterrupted execution.
        RocketFlight uninterrupted = new RocketFlight();
        RocketFlight reloaded = new RocketFlight();
        uninterrupted.ignite(64);
        reloaded.ignite(64);
        y = 64;
        for (int tick = 0; tick < COUNTDOWN_TICKS + MAX_FLIGHT_TICKS + 10; tick++) {
            reloaded = RocketFlight.restore(reloaded.save());
            var expected = uninterrupted.tick(y, false, false);
            var actual = reloaded.tick(y, false, false);
            require(actual == expected && uninterrupted.save().equals(reloaded.save()), "Save/reload changed countdown or flight");
            if (actual == Action.ASCEND) y += reloaded.speed();
        }
        require(reloaded.phase() == Phase.FINISHED && !reloaded.canRecover(), "Reload resurrected spent rocket");
        require(RocketFlight.restore(new Saved(0, 0, 0, 64)).canRecover(), "Legacy static rocket must remain usable");
        Saved[] corrupt = {
            new Saved(-1, 0, 0, 64), new Saved(99, 0, 0, 64), new Saved(1, -1, 0, 64),
            new Saved(1, 61, 0, 64), new Saved(1, 0, 0, 64), new Saved(2, 0, 101, 64),
            new Saved(2, 0, -1, 64), new Saved(2, 1, 0, 64), new Saved(1, 30, 0, Double.NaN),
            new Saved(2, 0, 1, Double.POSITIVE_INFINITY), new Saved(0, 30, 0, 64)
        };
        for (Saved saved : corrupt) {
            RocketFlight invalid = RocketFlight.restore(saved);
            require(invalid.phase() == Phase.FINISHED && !invalid.canRecover() && !invalid.ignite(64), "Corrupt save rearmed rocket");
        }
        System.out.println("PASS: countdown, single ignition, acceleration, height/timeout burst, obstacles, minimum blast height, no recovery after ignition, reload continuity, corrupt-save handling");
    }
    private static RocketFlight flyingAt(int ticks) { return RocketFlight.restore(new Saved(2, 0, ticks, 64)); }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
