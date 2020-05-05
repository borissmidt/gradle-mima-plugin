/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import org.gradle.api.Project;

final class PreviousVersionResolutionHelpers {
    private PreviousVersionResolutionHelpers() {}

    private static final class GroupThreadLocal extends ThreadLocal<Object> {
        private final Object defaultGroup;

        GroupThreadLocal(Object defaultGroup) {
            this.defaultGroup = defaultGroup;
        }

        @Override
        protected Object initialValue() {
            return defaultGroup;
        }
    }

    private static final class ThreadLocalGroup {
        private final GroupThreadLocal group;

        private ThreadLocalGroup(Object defaultGroup, String newGroupName) {
            this.group = new GroupThreadLocal(defaultGroup);
            this.group.set(newGroupName);
        }

        @Override
        public boolean equals(Object obj) {
            return group.get().equals(obj);
        }

        @Override
        public int hashCode() {
            return group.get().hashCode();
        }

        @Override
        public String toString() {
            return group.get().toString();
        }
    }

    /**
     * When the version of the local java project is higher than the old published dependency and has the same
     * group and name, gradle silently replaces the published external dependency with the project dependency
     * (see https://discuss.gradle.org/t/fetching-the-previous-version-of-a-projects-jar/8571). This happens on
     * tag builds, and would cause the publish to fail. Instead, we change the group for just this thread
     * while resolving these dependencies so the switching out doesnt happen.
     */
    public static <T, E extends Exception> T withRenamedGroupForCurrentThread(
            Project project, CheckedSupplier<T, E> action) throws E {
        Object group = project.getGroup();
        project.setGroup(new ThreadLocalGroup(group, "revapi.changed.group." + group));
        try {
            return action.get();
        } finally {
            project.setGroup(group);
        }
    }

    interface CheckedSupplier<T, E extends Exception> {
        T get() throws E;
    }
}