package bot.utilities;

/***
 * Represents a type which wraps a value that's either null or not null
 * @param <T>
 */
public class Option<T> {
    private T val;
    private boolean isSome;

    public Option(T object) {
        if (object == null) {
            isSome = false;
            return;
        }
        isSome = true;
        this.val = object;
    }

    public Option() {
        isSome = false;
    }

    public boolean isSome() {
        return isSome;
    }

    public T unwrap() {
        if (isSome) {
            return val;
        }
        throw new NullPointerException("Panicked at unwrap() on a 'None' value.");
    }

    public T get() {
        return val;
    }

    public void set(T object) {
        if (object == null) {
            isSome = false;
        } else {
            isSome = true;
            this.val = object;
        }
    }

    public T unwrap_or(T something) {
        if (isSome) {
            return val;
        }
        return something;
    }

    public T expect(String message) {
        if (isSome) {
            return val;
        }
        throw new NullPointerException(message);
    }

    public static <T> Option<T> of(T object) {
        return new Option<>(object);
    }

    public static <T> Option<T> none() {
        return new Option<>();
    }

    @Override
    public String toString() {
        if (isSome) {
            return val.toString();
        }
        return "null";
    }
}
