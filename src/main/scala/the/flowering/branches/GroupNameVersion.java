package the.flowering.branches;

/**
 * Copyright (C) 17.04.20 - REstore NV
 */

public class GroupNameVersion {
    final String group;
    final String name;
    final String version;

    public GroupNameVersion(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    String asString(){
        return String.join(":",group,name,version);
    }

    GroupName groupName() {
        return new GroupName(group,name);
    }
}


