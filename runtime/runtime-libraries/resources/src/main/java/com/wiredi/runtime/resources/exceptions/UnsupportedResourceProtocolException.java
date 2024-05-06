package com.wiredi.runtime.resources.exceptions;

public class UnsupportedResourceProtocolException extends ResourceException {

    public UnsupportedResourceProtocolException(String protocol) {
        super("The protocol '" + protocol + "' is not supported");
    }

}
