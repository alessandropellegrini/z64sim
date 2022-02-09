/**
 *
 * SPDX-FileCopyrightText: 2015-2022 Alessandro Pellegrini <a.pellegrini@ing.uniroma2.it>
 * SPDX-License-Identifier: GPL-3.0-only
 */
package it.uniroma2.pellegrini.z64sim.util.log;

import it.uniroma2.pellegrini.z64sim.controller.SettingsController;
import it.uniroma2.pellegrini.z64sim.isa.instructions.InstructionClass5;

import java.util.Objects;

public class LoggerFactory {
    private static ClassContextSecurityManager SECURITY_MANAGER;
    private static boolean SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = false;

    public static Logger getLogger() {
        Class<?> callingClass = getCallingClass();
        String className = "[Unknown]";
        if(callingClass != null)
            className = callingClass.getCanonicalName();

        return new Logger(className);
    }

    private static Class<?> getCallingClass() {
        ClassContextSecurityManager securityManager = getSecurityManager();
        if (securityManager == null) {
            return null;
        } else {
            Class<?>[] trace = securityManager.getClassContext();
            String thisClassName = LoggerFactory.class.getName();

            int i;
            for(i = 0; i < trace.length && !thisClassName.equals(trace[i].getName()); ++i)
                ;
            if (i < trace.length && i + 2 < trace.length) {
                return trace[i + 2];
            } else {
                throw new IllegalStateException("Failed to identify caller class on the stack; this should not happen.");
            }
        }
    }

    private static ClassContextSecurityManager getSecurityManager() {
        if (SECURITY_MANAGER != null) {
            return SECURITY_MANAGER;
        } else if (SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED) {
            return null;
        } else {
            SECURITY_MANAGER = safeCreateSecurityManager();
            SECURITY_MANAGER_CREATION_ALREADY_ATTEMPTED = true;
            return SECURITY_MANAGER;
        }
    }

    private static ClassContextSecurityManager safeCreateSecurityManager() {
        try {
            return new ClassContextSecurityManager();
        } catch (SecurityException var1) {
            return null;
        }
    }

    private static final class ClassContextSecurityManager extends SecurityManager {
        private ClassContextSecurityManager() {
        }

        protected Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }
}
