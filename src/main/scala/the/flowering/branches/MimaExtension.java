package the.flowering.branches;

import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.SourceSet;

import java.util.stream.Collectors;

/**
 * Copyright (C) 15.04.20 - REstore NV
 */

public class MimaExtension {
    private final Property<String> oldGroup;
    private final Property<String> oldName;
    final Property<String> oldVersion;

    private final Property<Boolean> failOnException;
    private final SetProperty<Exclude> exclude;
    private final Property<Boolean> reportSignatureProblems;
    private final Property<String> direction;
    private final Property<SourceSet> sourceSet;
    private final Provider<GroupName> oldGroupName;

    public Property<SourceSet> getSourceSet() {
        return sourceSet;
    }

    public MimaExtension(Project project) {
        this.failOnException = project.getObjects().property(Boolean.class);
        this.exclude = project.getObjects().setProperty(Exclude.class);
        this.reportSignatureProblems = project.getObjects().property(Boolean.class);
        this.direction = project.getObjects().property(String.class);
        this.sourceSet = project.getObjects().property(SourceSet.class);

        oldGroup = project.getObjects().property(String.class);
        oldName = project.getObjects().property(String.class);
        oldVersion = project.getObjects().property(String.class);
        this.oldName.set(project.getProviders().provider(project::getName));
        this.oldGroup.set(
                project.getProviders().provider(() -> project.getGroup().toString()));
        this.oldVersion.set(project.getProviders()
                .provider(
                        () -> GitVersionUtils.previousGitTags(project).findFirst().orElseGet(() -> "0.0.0")));

        this.oldGroupName = project.provider(() ->
                new GroupName(oldGroup.get(),oldName.get()));
    }

    public Property<Boolean> getFailOnException() {
        return failOnException;
    }

    public SetProperty<Exclude> getExclude() {
        return exclude;
    }

    public Property<Boolean> getReportSignatureProblems() {
        return reportSignatureProblems;
    }

    public Property<String> getDirection() {
        return direction;
    }

    public GroupNameVersion groupNameVersion() {
        return new GroupNameVersion(oldGroup.get(), oldName.get(), oldVersion.get());
    }

    public Provider<GroupName> groupName() {
       return oldGroupName;
    }
}
