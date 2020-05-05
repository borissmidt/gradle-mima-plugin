/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

public class GroupName {
    final String group;
    final String name;

    public GroupName(String group, String name) {
        this.group = group;
        this.name = name;
    }

    public GroupNameVersion withVersion(String version) {
        return new GroupNameVersion(
                group,
                name,
                version
        );
    }

    String asString(){
        return String.join(":",group,name);
    }
}