/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package the.flowering.branches;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ResolveOldApi {
    private static final Logger log = LoggerFactory.getLogger(ResolveOldApi.class);

    private ResolveOldApi() {
    }

    public static Provider<Optional<OldApi>> oldApiProvider(
            Project project, MimaExtension extension) {

        return GradleUtils.memoisedProvider(
                project,
                () -> resolveOldApiAcrossAllOldVersions(
                        project, extension));
    }

    private static Optional<OldApi> resolveOldApiAcrossAllOldVersions(
            Project project, MimaExtension extension) {

        String oldVersionStrings = extension.oldVersion.get();

        GroupName oldGroupAndName = extension.groupName().get();


        try {
            return Optional.of(resolveOldApiWithVersion(project, extension.groupNameVersion()));
        } catch (OldApiConfigurations.CouldNotResolveOldApiException e) {
            return Optional.empty();
        }
    }

    private static OldApi resolveOldApiWithVersion(Project project, GroupNameVersion groupNameVersion)
            throws OldApiConfigurations.CouldNotResolveOldApiException {

        Set<File> oldOnlyJar = OldApiConfigurations.resolveOldConfiguration(project, groupNameVersion, false);
        Set<File> oldWithDeps = OldApiConfigurations.resolveOldConfiguration(project, groupNameVersion, true);

        Set<File> oldJustDeps = new HashSet<>(oldWithDeps);
        oldJustDeps.removeAll(oldOnlyJar);

        return new OldApi(oldOnlyJar, oldJustDeps);
    }

    static class OldApi {
        final Set<File> jars;

        final Set<File> dependencyJars;

        public OldApi(Set<File> jars, Set<File> dependencyJars) {
            this.jars = jars;
            this.dependencyJars = dependencyJars;
        }
    }
}