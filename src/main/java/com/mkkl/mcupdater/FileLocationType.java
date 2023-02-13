package com.mkkl.mcupdater;

public enum FileLocationType {
    LOCAL("lokalne"),
    NET("zdalne");

    private final String name;

    FileLocationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
