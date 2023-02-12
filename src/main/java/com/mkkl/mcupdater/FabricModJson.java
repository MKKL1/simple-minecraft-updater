package com.mkkl.mcupdater;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FabricModJson {
    public String id;
    public String name;
    public String version;
}
