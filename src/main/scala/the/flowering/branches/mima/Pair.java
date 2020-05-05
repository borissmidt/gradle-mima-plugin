/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 *  v. 2.0. If a copy of the MPL was not distributed with this file,
 *  You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package the.flowering.branches.mima;

public class Pair<A, B> {
    public final A o1;
    public final B o2;

    public Pair(A o1, B o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public A getO1() {
        return o1;
    }

    public B getO2() {
        return o2;
    }
}
