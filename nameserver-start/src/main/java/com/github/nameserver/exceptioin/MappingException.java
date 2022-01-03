package com.github.nameserver.exceptioin;

public class MappingException extends RuntimeException {
    private final String uri;
    private final String reasonPhrase;
    private static final String MSG_TEMPLATE = "the mapping [%s] is illegal, %s";

    public MappingException(String uri, String reasonPhrase) {
        super();
        this.uri = uri;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return the detail message string of this {@code Throwable} instance
     * (which may be {@code null}).
     */
    @Override
    public String getMessage() {
        return String.format(MSG_TEMPLATE, uri, reasonPhrase);
    }
}
