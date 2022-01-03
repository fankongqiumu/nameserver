package com.github.nameserver.exceptioin;

public class NotSupportException extends RuntimeException{
    private final String operation;
    private static final String MSG_TEMPLATE = "%s not support";

    public NotSupportException(String operation){
        super();
        this.operation = operation;
    }

    public static NotSupportException createInstanceNotSupportException(){
        return new NotSupportException("instance");
    }


    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        return String.format(MSG_TEMPLATE, operation);
    }
}
