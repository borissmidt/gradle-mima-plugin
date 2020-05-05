/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

import java.util.Collections;
import java.util.Set;

public class Exclude {
    private final String name;
    private Set<String> packages;

    public void setPackages(Set<String> packages) {
        this.packages = packages;
    }

    public Set<String> getPackages() {
        return packages;
    }

    public String getName() {
        return name;
    }

    public Exclude(String name) {
        this.name = name;
        this.packages = Collections.emptySet();
    }
}
