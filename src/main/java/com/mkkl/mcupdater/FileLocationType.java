package com.mkkl.mcupdater;

public enum FileLocationType {
    LOCAL("local"),
    NET("net");

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
