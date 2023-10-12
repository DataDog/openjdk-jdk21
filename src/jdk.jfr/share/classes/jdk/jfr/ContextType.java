package jdk.jfr;

import java.io.Closeable;
import java.util.stream.Stream;

import jdk.jfr.internal.context.BaseContextType;
import jdk.jfr.internal.context.ContextRepository;

public abstract class ContextType extends BaseContextType {
    public interface Registration {
        Stream<Class<? extends ContextType>> types();
    }

    public interface Access<T> {
        void set(T target);
        void unset(T target);
    }

    public static final class Captured<T extends ContextType.Capturable<T>> implements Closeable, AutoCloseable {
        private final T parent, current;

        Captured(T parent, T current) {
            assert parent != null;
            assert current != null;
            this.parent = parent;
            this.current = current;
        }

        @Override
        public void close() {
            parent.set();
        }

        public T get() {
            return current;
        }

        public Captured<T> capture() {
            return current.capture();
        }
    }

    public static abstract class Capturable<T extends Capturable<T>> extends ContextType {
        public Capturable() {}

        @SuppressWarnings("unchecked")
        public final Captured<T> capture() {
            return new Captured<T>((T) this, snapshot());
        }

        abstract protected T snapshot();
    }

    protected ContextType() {}

    public static <T> Access<T> accessFor(Class<T> type) {
        return BaseContextType.accessFor(type);
    }
}
