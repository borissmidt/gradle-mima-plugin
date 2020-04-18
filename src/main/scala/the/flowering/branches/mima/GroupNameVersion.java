package the.flowering.branches.mima;

import java.util.Optional;

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

    String asString() {
        return String.join(":", group, name, version);
    }

    static Optional<GroupNameVersion> fromString(String s) {
        return Optional.of(s.split(":"))
                .filter(parts -> parts.length >= 3)
                .map(parts ->
                        new GroupNameVersion(
                                parts[0],
                                parts[1],
                                parts[2]
                        )
                );
    }

    GroupName groupName() {
        return new GroupName(group, name);
    }
}


