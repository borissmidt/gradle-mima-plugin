package the.flowering.branches;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.bundling.Jar;

import java.util.Optional;
import java.util.Set;

/**
 * Copyright (C) 15.04.20 - REstore NV
 */

public class MimaPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        MimaExtension extension = project.getExtensions().create("mima", MimaExtension.class, project);

        Provider<Optional<ResolveOldApi.OldApi>> maybeOldApi = ResolveOldApi.oldApiProvider(project, extension);
        // Note: this should propagate the dependency on the necessary tasks to build the other projects
        Spec<Task> oldApiIsPresent = _task -> maybeOldApi.get().isPresent();
        project.getTasks().register("mimaReportBinaryIssues", ReportBinaryIssues.class, task -> {
            //special thanks to https://github.com/palantir/gradle-revapi/blob/develop/src/main/java/com/palantir/gradle/revapi/RevapiPlugin.java
                    FileCollection thisJarFile = project.getTasks()
                            .withType(Jar.class)
                            .getByName(JavaPlugin.JAR_TASK_NAME)
                            .getOutputs()
                            .getFiles();

                    Configuration revapiNewApiElements = project.getConfigurations()
                            .create("revapiNewApiElements", conf -> {
                                conf.extendsFrom(project.getConfigurations()
                                        .getByName(JavaPlugin.API_ELEMENTS_CONFIGURATION_NAME));
                                configureApiUsage(project, conf);
                                conf.setCanBeConsumed(false);
                                conf.setVisible(false);
                            });

                    FileCollection otherProjectsOutputs = revapiNewApiElements
                            .getIncoming()
                            .artifactView(vc -> vc.componentFilter(ci -> ci instanceof ProjectComponentIdentifier))
                            .getFiles();

                    task.getCurrentArtifact().set(thisJarFile.plus(otherProjectsOutputs));
                    task.getFailOnException().set(extension.getFailOnException());
                    task.getExclude().set(extension.getExclude());
                    task.getReportSignatureProblems().set(extension.getReportSignatureProblems());
                    task.getBuildOutputDirectory().set(extension.getBuildOutputDirectory());
                    task.getDirection().set(extension.getDirection());
                    task.getPreviousArtifact().set(maybeOldApi.map(oldApi ->
                            oldApi.map(x -> x.jars).map(project::files).orElseGet(project::files)));;
                    task.getSourceSet().set(extension.getSourceSet());
                }
        );
    }

    private static void configureApiUsage(Project project, Configuration conf) {
        conf.attributes(attrs ->
                attrs.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API)));
    }
}
