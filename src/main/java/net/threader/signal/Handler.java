package net.threader.signal;

public @interface Handler {
    Priority priority() default Priority.NORMAL;
}
