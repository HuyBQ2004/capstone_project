package com.sfms.mf04.port;

/** Ném bởi {@link FaceRecognitionPort} khi FRS lỗi/timeout/ảnh không đạt chất lượng. */
public class FaceRecognitionException extends RuntimeException {

    public FaceRecognitionException(String message) {
        super(message);
    }

    public FaceRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
