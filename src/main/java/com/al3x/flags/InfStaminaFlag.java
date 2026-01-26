package com.al3x.flags;

public class InfStaminaFlag extends Flag {

    private final int maxFlags;
    private final float stamina;

    public InfStaminaFlag(int maxFlags, float stamina) {
        this.maxFlags = maxFlags;
        this.stamina = stamina;
    }

    @Override
    public int getMaxFlags() { return maxFlags; }

    @Override
    public String getSource() {
        return "CLIENT_MOVEMENT_PACKET";
    }

    @Override
    public String getReason() {
        return "didn't lose stamina while sprinting";
    }

    @Override
    public String getDetails() {
        return "STAMINA: " + stamina;
    }

    @Override
    public String toString() {
        return "InfStaminaFlag{stamina=" + stamina + '}';
    }
}
