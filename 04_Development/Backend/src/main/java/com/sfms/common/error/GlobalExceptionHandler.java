package com.sfms.common.error;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sfms.common.dto.ErrorResponse;
import com.sfms.iam.error.IamError;
import com.sfms.iam.error.IamMessages;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import com.sfms.mf12.domain.ErrorLog;
import com.sfms.mf12.domain.LogSeverity;
import com.sfms.mf12.repository.ErrorLogRepository;
import com.sfms.common.code.CodeGenerator;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * D2 — {@code @RestControllerAdvice} dùng chung: mọi lỗi ra ngoài đều mang
 * envelope
 * {@code { "error": { code, message, details } }}.
 *
 * <p>
 * Ba nguyên tắc bất biến (TestSpec MF01-TC-INT-015/016, SEC-010, SEC-012):
 * <ol>
 * <li>Không bao giờ để Spring trả body mặc định
 * ({@code timestamp}/{@code path}/{@code trace}).</li>
 * <li>Không bao giờ đưa stack trace, tên class Java hay câu SQL ra response
 * body.</li>
 * <li>{@code details} luôn là mảng, không bao giờ {@code null}.</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ModuleCodeResolver codeResolver;
    private final ErrorLogRepository errorLogRepository;
    private final CodeGenerator codeGenerator;

    public GlobalExceptionHandler(ModuleCodeResolver codeResolver,
            ErrorLogRepository errorLogRepository,
            CodeGenerator codeGenerator) {
        this.codeResolver = codeResolver;
        this.errorLogRepository = errorLogRepository;
        this.codeGenerator = codeGenerator;
    }

    // ───────────────────────── Lỗi nghiệp vụ tường minh ─────────────────────────

    /**
     * Mọi {@link ApiException} đã mang sẵn code/status/message nguyên văn của TDS
     * §10.
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.toBody());
    }

    // ───────────────────────── 404 — MSG-137 (IAM-404) ─────────────────────────

    @ExceptionHandler({ EntityNotFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(codeResolver.notFoundCode(request), SystemMessages.MSG_137));
    }

    // ───────────────────────── 422 — MSG-138 (IAM-422) ─────────────────────────

    /** DTO thiếu field / sai {@code @Pattern} / sai {@code @Email} (BR-012). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<ErrorResponse.Detail> details = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> details.add(new ErrorResponse.Detail(fe.getField(), fe.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> details.add(new ErrorResponse.Detail(ge.getObjectName(), ge.getDefaultMessage())));
        return unprocessable(request, details);
    }

    /**
     * Body không parse được: enum sai giá trị (D6 — {@code "Teacher"} thay vì
     * {@code "teacher"}),
     * ngày sai định dạng (D4 — {@code "02/07/2026 07:45"} thay vì ISO-8601), JSON
     * hỏng.
     *
     * <p>
     * Đây là chỗ D4/D6 được <b>enforce</b>: parse "khoan dung" ở đây sẽ để dữ liệu
     * bẩn
     * lọt vào DB đúng như cảnh báo của D4.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        /*
         * Log thêm 10/08/2026 (Nam — giữ lại, không xoá) — handler này trước đây không log
         * gì, nên mọi lỗi "malformed request body" vô hình ở container logs, phải đoán mò
         * (case thật: INSP-422 lặp lại ở POST /inspections/{code}/steps/1, hoá ra do Jackson
         * từ chối field `int` vắng mặt trong JSON — xem `JacksonConfig.objectMapper()`). Có
         * dòng này thì lần sau lỗi tương tự lộ nguyên nhân ngay trong log, không cần vá thêm
         * log tạm rồi build lại như lần này.
         */
        log.error("HttpMessageNotReadableException at {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage(), ex);
        List<ErrorResponse.Detail> details = new ArrayList<>();
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().stream()
                    .map(ref -> ref.getFieldName() == null ? "[]" : ref.getFieldName())
                    .reduce((a, b) -> a + "." + b)
                    .orElse("body");
            details.add(new ErrorResponse.Detail(field, "invalid format"));
        } else {
            details.add(new ErrorResponse.Detail("body", "malformed request body"));
        }
        return unprocessable(request, details);
    }

    /**
     * <p>
     * <b>08/08/2026 — thêm {@code MissingServletRequestPartException}.</b> Thiếu
     * một
     * {@code @RequestPart} bắt buộc (gửi part tên {@code "file"} trong khi endpoint
     * đòi
     * {@code "files"}) trước đây rơi xuống fallback ⇒ <b>500</b>. Đây là lỗi client
     * và tên
     * part là thông tin cần thiết để sửa, nên trả 422 kèm đúng tên part — cùng
     * khuôn với
     * thiếu request param.
     */
    @ExceptionHandler({ MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class })
    public ResponseEntity<ErrorResponse> handleBadRequestInput(Exception ex, HttpServletRequest request) {
        String field = switch (ex) {
            case MissingServletRequestParameterException e -> e.getParameterName();
            case MissingServletRequestPartException e -> e.getRequestPartName();
            case MethodArgumentTypeMismatchException e -> e.getName();
            default -> "request";
        };
        return unprocessable(request, List.of(new ErrorResponse.Detail(field, "invalid value")));
    }

    /**
     * 415 / 405 thay vì 500.
     *
     * <p>
     * <b>Đo được 08/08/2026:</b> gọi {@code POST /partnership-applications} và
     * {@code POST /samples} — hai endpoint {@code consumes = multipart/form-data} —
     * bằng
     * {@code Content-Type: application/json} trả <b>500</b> kèm mã {@code *-500}
     * "unexpected
     * error", vì {@code HttpMediaTypeNotSupportedException} rơi thẳng xuống
     * fallback. Gọi sai
     * METHOD cũng vậy (đã ghi ở Handoff: {@code /incidents/{code}/assess} trả 500
     * chứ không
     * phải 405). Cả hai đều là <b>lỗi của client</b>, báo 5xx làm người tích hợp đi
     * tìm sự cố
     * ở server và làm bẩn cảnh báo giám sát.
     *
     * <p>
     * Thân lỗi vẫn theo đúng khuôn {@code ErrorResponse} +
     * {@code ModuleCodeResolver} như
     * mọi handler khác, không lộ chi tiết nội bộ (SEC-010).
     */
    @ExceptionHandler({ HttpMediaTypeNotSupportedException.class,
            HttpRequestMethodNotSupportedException.class })
    public ResponseEntity<ErrorResponse> handleUnsupportedRequest(Exception ex, HttpServletRequest request) {
        String prefix = codeResolver.prefixFor(request);
        if (ex instanceof HttpMediaTypeNotSupportedException media) {
            String supported = media.getSupportedMediaTypes().stream()
                    .map(Object::toString).reduce((a, b) -> a + ", " + b).orElse("");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .body(ErrorResponse.of(prefix + "-415", "Unsupported media type.",
                            List.of(new ErrorResponse.Detail("Content-Type", supported))));
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of(prefix + "-405", "Method not allowed.",
                        List.of(new ErrorResponse.Detail("method", request.getMethod()))));
    }

    /**
     * File vượt trần multipart ⇒ <b>413</b>, không phải 500.
     *
     * <p>
     * Tomcat chặn NGAY Ở TẦNG PARSE, trước khi request tới controller — nên mọi
     * kiểm tra
     * kích thước viết trong service (MF04/MF06 10MB, MF12 10MB) không bao giờ chạy
     * tới, và
     * người dùng nhận "500 unexpected error" cho một việc hoàn toàn bình thường là
     * chọn nhầm
     * file quá to. Đo được 09/08/2026 khi nạp .docx 1.9MB vào kho tri thức AI.
     *
     * <p>
     * Trần thật khai ở {@code application.yml}
     * (`spring.servlet.multipart.max-file-size`).
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(HttpServletRequest request) {
        String prefix = codeResolver.prefixFor(request);
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(ErrorResponse.of(prefix + "-413", "File too large.",
                        List.of(new ErrorResponse.Detail("file", "vượt quá dung lượng cho phép"))));
    }

    /**
     * Giữ nguyên status của {@link ResponseStatusException} thay vì nuốt thành 500.
     *
     * <p>
     * <b>Đo được 08/08/2026:</b> {@code POST /ingredients} với mã đã tồn tại trả
     * <b>500 IAM-500</b> "unexpected error", trong khi log cho thấy service ném
     * đúng
     * {@code 409 CONFLICT "Mã nguyên liệu đã tồn tại: ING-THIT-HEO"}. Nguyên nhân:
     * {@code @ExceptionHandler(Exception.class)} của lớp này thắng cơ chế mặc định
     * của Spring,
     * nên MỌI {@code ResponseStatusException} đều thành 500 — cả 7 chỗ ném trong
     * {@code IngredientServiceImpl} (409 trùng mã · 404 không tìm thấy · 422
     * validate), tức
     * toàn bộ nhánh lỗi của màn {@code system-admin/ingredients} — màn ĐẦU TIÊN
     * trong thứ tự
     * dựng dữ liệu (runbook mục 11).
     *
     * <p>
     * Mã lỗi vẫn theo {@code ModuleCodeResolver} như mọi handler khác;
     * {@code reason} của
     * service là câu tiếng Việt viết cho người dùng nên hiển thị được, không phải
     * chi tiết
     * nội bộ (SEC-010).
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String code = codeResolver.prefixFor(request) + "-" + status.value();
        String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return ResponseEntity.status(status).body(ErrorResponse.of(code, message));
    }

    private ResponseEntity<ErrorResponse> unprocessable(HttpServletRequest request,
            List<ErrorResponse.Detail> details) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.of(codeResolver.validationCode(request),
                        SystemMessages.MSG_138, details));
    }

    // ───────────────────────── 401 / 403 — auth (MF01 là module nền)
    // ─────────────────────────

    /** MSG-005 — sai RBAC. TDS-MF01 §10 {@code IAM-004} (403). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String prefix = codeResolver.prefixFor(request);
        String code = "IAM".equals(prefix) ? IamError.ACCESS_DENIED : prefix + "-005";
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(code, IamMessages.MSG_005));
    }

    /**
     * MSG-004 — thiếu/hỏng/hết hạn access token. TDS-MF01 §10 {@code IAM-005}
     * (401).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(IamError.SESSION_EXPIRED, IamMessages.MSG_004));
    }

    // ───────────────────────── Fallback 500 ─────────────────────────

    /**
     * Không để lộ bất cứ chi tiết nội bộ nào (SEC-010: <i>"error body không chứa
     * stack trace /
     * tên class Java / câu SQL"</i>). Chi tiết chỉ đi vào log server.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);

        saveErrorLog(ex, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(codeResolver.prefixFor(request) + "-500",
                        "An unexpected error occurred. Please try again later."));
    }

    private void saveErrorLog(Exception ex, HttpServletRequest request) {
        try {
            ErrorLog errorLog = new ErrorLog();
            errorLog.setCode(codeGenerator.next("error_logs", "ERR", 4));
            errorLog.setTimestamp(java.time.Instant.now());

            String msg = ex.getMessage();
            if (msg == null) {
                msg = ex.getClass().getName();
            }
            if (msg.length() > 1000) {
                msg = msg.substring(0, 997) + "...";
            }
            errorLog.setMessage(msg);
            errorLog.setSeverity(LogSeverity.CRITICAL);

            String module = "Unknown";
            StackTraceElement[] stack = ex.getStackTrace();
            if (stack != null && stack.length > 0) {
                module = stack[0].getClassName() + "." + stack[0].getMethodName() + ":" + stack[0].getLineNumber();
            }
            if (module.length() > 120) {
                module = module.substring(0, 117) + "...";
            }
            errorLog.setModule(module);

            String path = request.getRequestURI();
            if (path.length() > 300) {
                path = path.substring(0, 297) + "...";
            }
            errorLog.setPath(path);

            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            ex.printStackTrace(pw);
            errorLog.setStackTrace(sw.toString());

            errorLog.setSessionInfo(getSessionInfo());

            errorLogRepository.save(errorLog);
        } catch (Exception e) {
            log.error("Failed to save error log to DB", e);
        }
    }

    private String getSessionInfo() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                return auth.getName() + " (" + auth.getAuthorities() + ")";
            }
        } catch (Exception e) {
            // Ignored
        }
        return "Anonymous";
    }
}
