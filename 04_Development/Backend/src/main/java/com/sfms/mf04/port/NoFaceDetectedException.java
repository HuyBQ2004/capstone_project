package com.sfms.mf04.port;

/**
 * FRS trả 422 "không phát hiện khuôn mặt trong ảnh" — KHÁC {@link FaceRecognitionException}
 * (service lỗi/timeout, 502 SUP-014): đây là lỗi ẢNH của người dùng, sửa được bằng cách chụp
 * lại, không phải hệ thống hỏng. Tách riêng để {@code DeliveryPersonServiceImpl} trả đúng 422
 * (không phải 502) — người dùng cần biết "chụp lại ảnh có mặt rõ", không phải "hệ thống lỗi".
 */
public class NoFaceDetectedException extends FaceRecognitionException {
    public NoFaceDetectedException(String message) {
        super(message);
    }
}
