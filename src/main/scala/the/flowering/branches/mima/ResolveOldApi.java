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

package the.flowering.branches.mima;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

final class ResolveOldApi {
    private static final Logger log = LoggerFactory.getLogger(ResolveOldApi.class);

    private ResolveOldApi() {
    }

    public static Provider<Map<GroupNameVersion, OldApi>> oldApiProvider(
            Project project, Set<String> oldVersions) {

        return GradleUtils.memoisedProvider(
                project,
                () -> resolveOldApiAcrossAllOldVersions(
                        project, oldVersions));
    }

    private static Map<GroupNameVersion, OldApi> resolveOldApiAcrossAllOldVersions(
            Project project, Set<String> oldVersions) {

        return oldVersions.stream().map(GroupNameVersion::fromString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(v -> resolveOldApiWithVersion(project, v).map(art -> new Pair<>(v, art)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Pair::getO1, Pair::getO2));
    }

    private static Optional<OldApi> resolveOldApiWithVersion(Project project, GroupNameVersion groupNameVersion) {
        try {
            Set<File> oldOnlyJar = OldApiConfigurations.resolveOldConfiguration(project, groupNameVersion, false);
            Set<File> oldWithDeps = OldApiConfigurations.resolveOldConfiguration(project, groupNameVersion, true);

            Set<File> oldJustDeps = new HashSet<>(oldWithDeps);
            oldJustDeps.removeAll(oldOnlyJar);

            return Optional.of(new OldApi(oldOnlyJar, oldJustDeps));
        } catch (OldApiConfigurations.CouldNotResolveOldApiException e) {
            System.out.println(e.getMessage());
            return Optional.empty();
        }
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