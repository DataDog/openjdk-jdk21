package jdk.jfr;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import java.util.stream.LongStream;

import jdk.jfr.internal.JVM;

public final class ContextSnapshot {
    private static final ContextSnapshot none = new ContextSnapshot(null);

    public static ContextSnapshot none() {
        return none;
    }

    public static ContextSnapshot capture() {
        LongBuffer buff = JVM.getThreadContextBuffer();
        if (buff == null || buff.capacity() == 0) {
            return none();
        }
        long[] data = new long[buff.capacity()];
        buff.get(data, 0, data.length);
        return new ContextSnapshot(data);
    }

    private final long[] data;

    private ContextSnapshot(long[] data) {
        this.data = data;
    }

    public LongStream data() {
        return data == null ? LongStream.empty() : LongStream.of(data);
    }
}
