package me.cullycross.arbuz.events;

/**
 * Created by: cullycross
 * Date: 9/22/15
 * For my shining stars!
 */
public class SaveToLocalStoreEvent extends Event {

    protected final int mCount;

    public SaveToLocalStoreEvent(int count) {
        mCount = count;
    }

    public int getCount() {
        return mCount;
    }
}
