/**
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim;

import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ReuseCheckTest {

    @Test
    @DisplayName("REUSE check")
    @Disabled
    public void reuseCheck() throws IOException, InterruptedException {
        @NonNls Process p = Runtime.getRuntime().exec("reuse lint");
        try (InputStream inputStream = p.getInputStream();
             @NonNls Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            System.out.println(s.hasNext() ? s.next() : null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(p.waitFor() == 0);
    }
}
