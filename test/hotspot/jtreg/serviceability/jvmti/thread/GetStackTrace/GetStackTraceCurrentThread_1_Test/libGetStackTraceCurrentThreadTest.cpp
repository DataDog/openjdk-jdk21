/*
 * Copyright (c) 2003, 2022, Oracle and/or its affiliates. All rights reserved.
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

#include <stdio.h>
#include <string.h>
#include "jvmti.h"
#include "jvmti_common.h"
#include "../get_stack_trace.h"


extern "C" {

static jvmtiEnv *jvmti = NULL;

JNIEXPORT jint JNICALL
Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {
  jint res = jvm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_1);
  if (res != JNI_OK || jvmti == NULL) {
    printf("Wrong result of a valid call to GetEnv!\n");
    return JNI_ERR;
  }
  return JNI_OK;
}

const int MAX_NUMBER_OF_FRAMES = 32;
jvmtiFrameInfo frames[MAX_NUMBER_OF_FRAMES];
jint count;

JNIEXPORT void JNICALL
Java_GetStackTraceCurrentThreadTest_capture(JNIEnv *env, jclass cls) {
    jvmtiError err = jvmti->GetStackTrace(thread, 0, MAX_NUMBER_OF_FRAMES, frames, &count);
    check_jvmti_status(jni, err, "GetStackTrace failed.");
}

JNIEXPORT void JNICALL
Java_GetStackTraceCurrentThreadTest_check(JNIEnv *jni, jclass cls) {
    jclass method_class;
    char* class_name = nullptr;
    for (int i = 0; i < count; i++) {
        jMethodId method = frames[i].method;
        bool resolved = false;
        if (jvmti->GetMethodDeclaringClass(method, &method_class) == 0) {
            if (jvmti->GetClassSignature(method_class, &class_name, NULL) == 0) {
                resolved = true;
                printf("Frame[%d] resolved to %s\n", i, class_name);
                jvmti->Deallocate((unsigned char*) class_name);
            }
        }
        if (!resolved) {
            printf("Frame [%d] failed to resolved jMethodId: %p\n", i, method);
        }
    }
}
}