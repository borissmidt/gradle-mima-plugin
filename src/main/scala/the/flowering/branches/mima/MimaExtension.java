/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;

import java.util.stream.Collectors;

public class MimaExtension {
    private final Property<String> oldGroup;
    private final Property<String> oldName;
    private final SetProperty<String> compareToVersions;
    private final Property<Boolean> failOnException;
    //    private final NamedDomainObjectContainer<Exclude> exclude;
    private final Property<Boolean> reportSignatureProblems;
    private final Property<String> direction;
    private final Provider<GroupName> oldGroupName;

    public SetProperty<String> getCompareToVersions() {
        return compareToVersions;
    }

    public MimaExtension(Project project) {
        this.failOnException = project.getObjects().property(Boolean.class);
//        this.exclude = project.getObjects().domainObjectContainer(Exclude.class, name -> new Exclude(name));
        this.reportSignatureProblems = project.getObjects().property(Boolean.class);
        this.direction = project.getObjects().property(String.class);

        oldGroup = project.getObjects().property(String.class);
        oldName = project.getObjects().property(String.class);
        compareToVersions = project.getObjects().setProperty(String.class);
        this.oldName.set(project.getProviders().provider(project::getName));
        this.oldGroup.set(
                project.getProviders().provider(() -> project.getGroup().toString()));
        this.compareToVersions.set(project.getProviders()
                .provider(
                        () -> GitVersionUtils.previousGitTags(project).limit(1).collect(Collectors.toList())));

        this.oldGroupName = project.provider(() ->
                new GroupName(oldGroup.get(), oldName.get()));
    }

    public Property<Boolean> getFailOnException() {
        return failOnException;
    }
//
//    public NamedDomainObjectContainer<Exclude> getExclude() {
//        return exclude;
//    }

    public Property<Boolean> getReportSignatureProblems() {
        return reportSignatureProblems;
    }

    public Property<String> getDirection() {
        return direction;
    }

    public Provider<GroupName> groupName() {
        return oldGroupName;
    }

//    public void exclude(Closure config) {
//        this.exclude.configure(config);
//    }

}
