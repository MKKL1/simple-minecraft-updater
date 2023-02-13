package com.mkkl.mcupdater;

import java.io.File;

public abstract class ModUpdateGoal {
    protected ListModData modData;

    public ModUpdateGoal(ListModData modData) {
        this.modData = modData;
    }

    public abstract void postProcess();
    public ListModData getModData() {
        return modData;
    }

    @Override
    public String toString() {
        return modData.toString();
    }
}
