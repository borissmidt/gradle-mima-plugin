package flowering.branches.mima;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;

import java.util.Collections;
import java.util.Set;

/**
 * Copyright (C) 15.04.20 - REstore NV
 */

public class Exclude {
    private final String name;
    private Set<String> packages;

    public void setPackages(Set<String> packages) {
        this.packages = packages;
    }

    public Set<String> getPackages() {
        return packages;
    }

    public String getName() {
        return name;
    }

    public Exclude(String name) {
        this.name = name;
        this.packages = Collections.emptySet();
    }
}
