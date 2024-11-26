package kozelek.interfaces;

public interface IRecord<T> extends IByteData {
    boolean equal(T other);
    T createInstance();
}
