package com.al3x.flags;

public class FlyFlag extends Flag {

    private final int maxFlags;

    public FlyFlag(int maxFlags) {
        this.maxFlags = maxFlags;
    }

    @Override
    public int getMaxFlags() { return maxFlags; }

    @Override
    public String getSource() {
        return "CLIENT_MOVEMENT_PACKET";
    }

    @Override
    public String getReason() {
        return "tried to fly in Adventure";
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public String toString() {
        return "FlyFlag{}";
    }
}
