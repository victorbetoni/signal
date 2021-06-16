package net.threader.signal;

public interface ICancellable {
    boolean isCancelled();
    void setCancelled(boolean bool);
}
