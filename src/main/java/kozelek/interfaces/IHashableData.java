package kozelek.interfaces;

import java.util.BitSet;

public interface IHashableData<T> extends IRecord<T> {
    BitSet getHash();
}
