/**
 * SPDX-FileCopyrightText: 2015-2023 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.model;

public class MemoryPointer {
    private long target;

    public MemoryPointer(long target) {
        this.target = target;
    }

    public long getTarget() {
        return target;
    }

    public void setTarget(long target) {
        this.target = target;
    }

    public MemoryElement dereference() throws Exception {
        throw new Exception("unimplemented");
    }
}
