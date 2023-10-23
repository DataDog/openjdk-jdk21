/*
 * Copyright (c) 2003, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 *
 * @library /test/lib
 * @compile GetStackTraceCurrentThreadTest.java
 * @run main/othervm/native -agentlib:GetStackTraceCurrentThreadTest GetStackTraceCurrentThreadTest
 */

public class GetStackTraceCurrentThreadTest {

    static {
        System.loadLibrary("GetStackTraceCurrentThreadTest");
    }

    native static void capture();
    native static void check();

    public static interface ServiceInterface {
        void doit();
    }

    private static class IsolatedClassLoader extends ClassLoader {
        protected IsolatedClassLoader(ClassLoader parent) {
            super(parent);
        }
    }

    public static void main(String args[]) throws Exception {
        IsolatedClassLoader cl = new IsolatedClassLoader(GetStackTraceCurrentThreadTest.class.getClassLoader());

        ServiceInterface proxy = (ServiceInterface) Proxy.newProxyInstance(cl, new Class<?>[]{ServiceInterface.class}, (proxy1, method, args1) -> {
            if (method.getName().equals("doit")) {
                capture();
            }
            return null;
        });

        proxy.doit();

        proxy = null;

        Thread t = new Thread(() -> {
           for (int = 0; i < 800_000; i++) {
               System.out.println("=== " + i);
               check();
               System.gc();
            }
        });
        t.start();

        cl = null;

        t.join();
    }
}