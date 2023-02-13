package com.mkkl.mcupdater;

import java.io.File;

public class AddUpdateGoal extends ModUpdateGoal {
    public File oldFile;
    public File newFile;

    public AddUpdateGoal(ListModData modData) {
        super(modData);
    }

    @Override
    public void postProcess() {
        if (oldFile!=null && oldFile.exists())
            if (oldFile.delete())
                System.out.println("File " + oldFile.getName() + " removed");
            else System.out.println("Failed to remove " + oldFile.getName());
    }
}
