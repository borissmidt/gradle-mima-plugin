/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.util.function.Supplier;

final class GradleUtils {
    private GradleUtils() {}

    public static <T> Provider<T> memoisedProvider(Project project, Supplier<T> supplier) {
        Supplier<T> memoised = new MemoizingSupplier<>(supplier);
        return project.provider(memoised::get);
    }

    // Taken from guava
    private static class MemoizingSupplier<T> implements Supplier<T> {
        private final Supplier<T> delegate;
        private transient volatile boolean initialized;
        // "value" does not need to be volatile; visibility piggy-backs
        // on volatile read of "initialized".
        private transient T savedValue;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            // A 2-field variant of Double Checked Locking.
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        T value = delegate.get();
                        this.savedValue = value;
                        initialized = true;
                        return value;
                    }
                }
            }
            return savedValue;
        }

        @Override
        public String toString() {
            return "Suppliers.memoize(" + (initialized ? "<supplier that returned " + savedValue + ">" : delegate)
                    + ")";
        }
    }
}
