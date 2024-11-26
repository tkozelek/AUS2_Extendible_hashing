package kozelek.interfaces;

public interface IByteData {
    int getSize();
    byte[] toByteArray();
    void fromByteArray(byte[] bytes);
}
