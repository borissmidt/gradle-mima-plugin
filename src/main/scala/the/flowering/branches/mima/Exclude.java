package the.flowering.branches.mima;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

/**
 * Copyright (C) 15.04.20 - REstore NV
 */

public class Exclude {
    private final String name;
    private final SetProperty<String> packages;

    public SetProperty<String> getPackages() {
        return packages;
    }

    public String getName() {
        return name;
    }

    public Exclude(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.packages = objectFactory.setProperty(String.class);
    }
}
