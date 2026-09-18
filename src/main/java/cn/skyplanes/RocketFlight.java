package cn.skyplanes;

/** Server-owned launch lifecycle, kept independent of Minecraft for deterministic checks. */
public final class RocketFlight {
    public static final int COUNTDOWN_TICKS = 60;
    public static final int MAX_FLIGHT_TICKS = 100;
    public static final double BURST_HEIGHT = 24.0;
    public static final double MIN_BURST_HEIGHT = 6.0;
    public static final double INITIAL_SPEED = 0.15;
    public static final double ACCELERATION = 0.035;
    public static final double MAX_SPEED = 0.8;
    public static final float EXPLOSION_POWER = 3.0f;

    public enum Phase { IDLE, COUNTDOWN, FLYING, FINISHED }
    public enum Action { NONE, LAUNCH, ASCEND, EXPLODE, FIZZLE }
    public record Saved(int phase, int countdown, int flightTicks, double launchY) {}

    private Phase phase = Phase.IDLE;
    private int countdown;
    private int flightTicks;
    private double launchY;

    public Phase phase() { return phase; }
    public int countdown() { return countdown; }
    public int flightTicks() { return flightTicks; }
    public boolean canRecover() { return phase == Phase.IDLE; }
    public double speed() { return Math.min(MAX_SPEED, INITIAL_SPEED + flightTicks * ACCELERATION); }
    public double nextSpeed() { return Math.min(MAX_SPEED, INITIAL_SPEED + (flightTicks + 1) * ACCELERATION); }

    public boolean ignite(double y) {
        if (phase != Phase.IDLE || !Double.isFinite(y)) return false;
        launchY = y;
        countdown = COUNTDOWN_TICKS;
        flightTicks = 0;
        phase = Phase.COUNTDOWN;
        return true;
    }

    /** Called once per server tick; terminal effects are returned exactly once. */
    public Action tick(double y, boolean blocked, boolean atHeightLimit) {
        if (phase == Phase.IDLE || phase == Phase.FINISHED) return Action.NONE;
        if (!Double.isFinite(y) || blocked) return finish(Action.FIZZLE);
        if (phase == Phase.COUNTDOWN) {
            if (atHeightLimit) return finish(Action.FIZZLE);
            if (--countdown == 0) {
                phase = Phase.FLYING;
                return Action.LAUNCH;
            }
            return Action.NONE;
        }
        double rise = y - launchY;
        if (rise >= BURST_HEIGHT || flightTicks >= MAX_FLIGHT_TICKS || atHeightLimit)
            return finish(rise >= MIN_BURST_HEIGHT ? Action.EXPLODE : Action.FIZZLE);
        flightTicks++;
        return Action.ASCEND;
    }

    private Action finish(Action action) { phase = Phase.FINISHED; return action; }
    public Saved save() { return new Saved(phase.ordinal(), countdown, flightTicks, launchY); }

    public static RocketFlight restore(Saved saved) {
        RocketFlight result = new RocketFlight();
        // Malformed active saves are spent, never converted back into recoverable kits.
        result.phase = Phase.FINISHED;
        if (saved.phase < 0 || saved.phase >= Phase.values().length || !Double.isFinite(saved.launchY)) return result;
        Phase phase = Phase.values()[saved.phase];
        boolean valid = switch (phase) {
            case IDLE -> saved.countdown == 0 && saved.flightTicks == 0;
            case COUNTDOWN -> saved.countdown > 0 && saved.countdown <= COUNTDOWN_TICKS && saved.flightTicks == 0;
            case FLYING -> saved.countdown == 0 && saved.flightTicks >= 0 && saved.flightTicks <= MAX_FLIGHT_TICKS;
            case FINISHED -> true;
        };
        if (!valid) return result;
        result.phase = phase;
        result.countdown = saved.countdown;
        result.flightTicks = saved.flightTicks;
        result.launchY = saved.launchY;
        return result;
    }
}
