package com.al3x.flags;

public class SpeedFlag extends Flag {

    private final int maxFlags;
    private final double speed;

    public SpeedFlag(int maxFlags, double speed) {
        this.maxFlags = maxFlags;
        this.speed = speed;
    }

    @Override
    public int getMaxFlags() { return maxFlags; }

    @Override
    public String getSource() {
        return "CLIENT_MOVEMENT_PACKET";
    }

    @Override
    public String getReason() {
        return "is moving faster than normal";
    }

    @Override
    public String getDetails() {
        return "SPEED: " + speed;
    }

    @Override
    public String toString() {
        return "SpeedFlag{speed=" + speed + '}';
    }
}
