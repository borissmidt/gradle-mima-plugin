package the.flowering.branches;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

/**
 * Copyright (C) 15.04.20 - REstore NV
 */

public class Exclude {
    final String problemType;
    final SetProperty<String> packages;

    public String getProblemType() {
        return problemType;
    }

    public Exclude(String problemType, ObjectFactory objectFactory) {
        this.problemType = problemType;
        this.packages = objectFactory.setProperty(String.class);
    }
}
