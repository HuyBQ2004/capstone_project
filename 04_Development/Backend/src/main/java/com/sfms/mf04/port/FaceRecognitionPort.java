package com.sfms.mf04.port;

/**
 * ADR-M04-5 — chỉ lưu tham chiếu {@code faceId}, ảnh gốc KHÔNG đi vào PostgreSQL/đĩa của
 * MF04. Dev/test dùng stub theo ADR-000 ({@code {match:true, faceId:"mock-uuid"}}); prod thay
 * bằng adapter gọi Face Recognition Service thật.
 */
public interface FaceRecognitionPort {

    /**
     * @return {@code faceId} do FRS cấp
     * @throws FaceRecognitionException FRS lỗi hoặc ảnh không đạt chất lượng (UC-068 AF1)
     */
    String enroll(byte[] image);

    /**
     * Xác thực khuôn mặt tài xế tại cổng trường với {@code expectedFaceId} đã đăng ký.
     */
    boolean verify(byte[] capturedImage, String expectedFaceId);

    /**
     * UC-028 — xóa dữ liệu sinh trắc học ở FRS. Gọi TRƯỚC khi đổi trạng thái DB (chỉ khi FRS
     * xác nhận OK mới coi là xóa xong — hành động không hoàn tác, BR-051).
     *
     * @throws FaceRecognitionException FRS không phản hồi/lỗi (UC-028 AF2)
     */
    void delete(String faceId);
}
