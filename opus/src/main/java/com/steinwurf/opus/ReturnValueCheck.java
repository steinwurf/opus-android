package com.steinwurf.opus;

public class ReturnValueCheck {

    private static class InvalidPacket extends RuntimeException {
        InvalidPacket() {
            super("The compressed data passed is corrupted");
        }
    }
    static int check(int result) {
        switch (result)
        {
            case -1:
                throw new IllegalArgumentException("One or more invalid/out of range arguments");
            case -2:
                throw new IllegalStateException("Invalid state");
            case -3:
                throw new InternalError("An internal error was detected");
            case -4:
                throw new InvalidPacket();
            case -5:
                throw new IllegalStateException("Invalid/unsupported request number");
            case -6:
                throw new IllegalStateException("An encoder or decoder structure is invalid");
            case -7:
                throw new OutOfMemoryError("Memory allocation has failed");
        }
        return result;
    }

}
