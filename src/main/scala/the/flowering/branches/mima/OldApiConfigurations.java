/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.UnresolvedDependencyResult;

final class OldApiConfigurations {
    private OldApiConfigurations() {
    }

    static Set<File> resolveOldConfiguration(Project project, GroupNameVersion groupNameVersion, boolean transitive)
            throws CouldNotResolveOldApiException {

        Dependency oldApiDependency = project.getDependencies().create(groupNameVersion.asString());

        String transitivityString = transitive ? "_transitive" : "";
        String configurationName = "mimaOldApi_" + groupNameVersion.version + transitivityString;

        Configuration oldApiConfiguration = project.getConfigurations().create(configurationName, conf -> {
            conf.getDependencies().add(oldApiDependency);
            conf.setCanBeConsumed(false);
            conf.setVisible(false);
        });
        oldApiConfiguration.setTransitive(transitive);

        return PreviousVersionResolutionHelpers.withRenamedGroupForCurrentThread(
                project, () -> resolveConfigurationUnlessMissingJars(groupNameVersion.version, oldApiConfiguration));
    }

    private static Set<File> resolveConfigurationUnlessMissingJars(String oldVersion, Configuration configuration)
            throws CouldNotResolveOldApiException {

        Set<? extends DependencyResult> allDependencies =
                configuration.getIncoming().getResolutionResult().getAllDependencies();

        List<Throwable> resolutionFailures = allDependencies.stream()
                .filter(dependencyResult -> dependencyResult instanceof UnresolvedDependencyResult)
                .map(dependencyResult -> (UnresolvedDependencyResult) dependencyResult)
                .map(UnresolvedDependencyResult::getFailure)
                .collect(Collectors.toList());

        if (resolutionFailures.isEmpty()) {
            return configuration.resolve();
        }

        throw new CouldNotResolveOldApiException(oldVersion, resolutionFailures);
    }

    static final class CouldNotResolveOldApiException extends Exception {
        private final String version;
        private final List<Throwable> resolutionFailures;

        CouldNotResolveOldApiException(String version, List<Throwable> resolutionFailures) {
            this.version = version;
            this.resolutionFailures = resolutionFailures;
        }

        @Override
        public String getMessage() {
            return "We tried version " + version + " but it failed with errors:\n\n" +
                    resolutionFailures.stream().map(Throwable::getMessage).collect(Collectors.joining("\n"));
        }
    }
}