package org.magnum.dataup;

public class VideoNotFoundException extends Exception{

    public VideoNotFoundException() {
    }

    public VideoNotFoundException(String message) {
        super(message);
    }
}