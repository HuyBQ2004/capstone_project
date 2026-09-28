package com.sfms.mf03.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.HealthHistoryEventType;
import com.sfms.common.error.ApiException;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf03.domain.Student;
import com.sfms.mf03.domain.StudentImportBatch;
import com.sfms.mf03.dto.StudentDtos.ParentInfoRequest;
import com.sfms.mf03.dto.StudentImportDtos.ImportPreviewResponse;
import com.sfms.mf03.dto.StudentImportDtos.ImportResultResponse;
import com.sfms.mf03.dto.StudentImportDtos.ImportRowResult;
import com.sfms.mf03.dto.StudentImportDtos.ParsedRow;
import com.sfms.mf03.error.StuError;
import com.sfms.mf03.error.StuExceptions;
import com.sfms.mf03.error.StuMessages;
import com.sfms.mf03.event.Mf03Events;
import com.sfms.mf03.repository.StudentImportBatchRepository;
import com.sfms.mf03.repository.StudentRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ADR-M03-6 — bulk import 2 pha. {@code validate()} parse Excel + kiểm từng hàng,
 * KHÔNG ghi
 * {@code students}; kết quả (kể cả hàng lỗi) lưu vào
 * {@code student_imports.rows_json} để
 * {@code commit()} — có thể là request KHÁC, vài phút sau (người dùng xem lại
 * bảng lỗi trước
 * khi bấm "Nhập") — đọc lại đúng tập đã kiểm thay vì parse lại file hay tin
 * state bộ nhớ.
 *
 * <p>
 * Format file: {@code .xlsx}, header dòng đầu (nhãn tiếng Việt, chỉ để người
 * điền dễ hiểu — parser đọc THEO VỊ TRÍ cột, không theo tên header), 7 cột
 * đúng thứ tự {@code name,dob,classroomId,parentFullName,parentEmail,
 * parentPhone,relationship}; {@code dob} nhận cả kiểu ngày Excel lẫn text
 * {@code yyyy-MM-dd} (D4). Cột {@code classroomId} có dropdown chọn từ mã lớp
 * thật của trường trong template tải về (13/08/2026, theo Nam — trước đó là
 * CSV, tự split dấu phẩy, không kiểm được mã lớp lúc điền).
 */
@Service
public class StudentImportServiceImpl implements StudentImportService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final List<String> EXPECTED_HEADER = List.of(
            "name", "dob", "classroomId", "parentFullName", "parentEmail", "parentPhone", "relationship");

    private final StudentImportBatchRepository importRepository;
    private final StudentRepository studentRepository;
    private final ParentLinkService parentLinkService;
    private final HealthHistoryService healthHistoryService;
    private final ReferenceResolver refs;
    private final CodeGenerator codeGenerator;
    private final ApplicationEventPublisher events;
    private final ObjectMapper objectMapper;
    private final ClassroomGuard classroomGuard;
    private final StudentCodeAllocator studentCodeAllocator;

    public StudentImportServiceImpl(StudentImportBatchRepository importRepository,
            StudentRepository studentRepository,
            ParentLinkService parentLinkService,
            HealthHistoryService healthHistoryService,
            ReferenceResolver refs,
            CodeGenerator codeGenerator,
            ApplicationEventPublisher events,
            ObjectMapper objectMapper,
            ClassroomGuard classroomGuard,
            StudentCodeAllocator studentCodeAllocator) {
        this.importRepository = importRepository;
        this.studentRepository = studentRepository;
        this.parentLinkService = parentLinkService;
        this.healthHistoryService = healthHistoryService;
        this.refs = refs;
        this.codeGenerator = codeGenerator;
        this.events = events;
        this.objectMapper = objectMapper;
        this.classroomGuard = classroomGuard;
        this.studentCodeAllocator = studentCodeAllocator;
    }

    @Override
    @Transactional
    public ImportPreviewResponse validate(MultipartFile file, AuthUser actor) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(StuError.VALIDATION_FAILED,
                    org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, StuMessages.MSG_138);
        }

        List<String[]> lines = readExcel(file);
        List<ParsedRow> rows = new ArrayList<>();
        Set<String> seenWithinFile = new HashSet<>();
        // Số chỗ trống còn lại của từng lớp, trừ dần theo các dòng hợp lệ đã nhận ở trên — nếu
        // không trừ dần thì cả 60 dòng cùng đổ vào một lớp 45 chỗ đều "còn chỗ" khi xét riêng lẻ.
        Map<UUID, Long> seatsLeft = new HashMap<>();
        int valid = 0;
        int invalid = 0;
        int duplicate = 0;

        for (int i = 0; i < lines.size(); i++) {
            int rowNo = i + 2; // dòng 1 = header (khớp mẫu TDS §9.3)
            String[] cols = lines.get(i);
            String name = col(cols, 0);
            String dob = col(cols, 1);
            String classroomId = col(cols, 2);
            String parentFullName = col(cols, 3);
            String parentEmail = col(cols, 4);
            String parentPhone = col(cols, 5);
            String relationship = col(cols, 6);

            String dedupeKey = (name + "|" + dob + "|" + classroomId).toLowerCase();
            boolean isDuplicate = !seenWithinFile.add(dedupeKey);

            UUID resolvedClassroomId = (classroomId == null || classroomId.isBlank())
                    ? null
                    : refs.classroomIdByCode(classroomId, actor.schoolId()).orElse(null);

            String code = null;
            String message = null;
            boolean rowValid = true;

            if (name == null || name.isBlank() || dob == null || dob.isBlank()
                    || classroomId == null || classroomId.isBlank()
                    || parentFullName == null || parentFullName.isBlank()
                    || parentEmail == null || parentEmail.isBlank()) {
                rowValid = false;
                code = StuError.IMPORT_ROW_INVALID;
                message = StuMessages.MSG_ROW_MISSING_FIELD;
            } else if (!EMAIL_PATTERN.matcher(parentEmail).matches()) {
                rowValid = false;
                code = StuError.INVALID_PARENT_EMAIL;
                message = StuMessages.MSG_PARENT_EMAIL_INVALID;
            } else if (parseDate(dob) == null) {
                rowValid = false;
                code = StuError.IMPORT_ROW_INVALID;
                message = "Date of birth is not a valid ISO date (yyyy-MM-dd).";
            } else if (resolvedClassroomId == null) {
                rowValid = false;
                code = StuError.NOT_FOUND;
                message = StuMessages.MSG_137;
            }

            if (isDuplicate) {
                rowValid = false;
                duplicate++;
                if (code == null) {
                    code = StuError.DUPLICATE_CODE;
                    message = "Duplicate row within the same import file "
                            + "(same name + dob + classroomId).";
                }
            }

            // 13/08/2026 (Nam) — BUG production: trước đây chỉ có `seenWithinFile` ở trên, tức
            // chỉ dò trùng TRONG file. Upload lại đúng file cũ ⇒ không dòng nào bị đánh trùng ⇒
            // tạo mới toàn bộ (Hồ Quỳnh Vy có cả HS-1A-016 lẫn HS-1A-045). Đối chiếu thêm với
            // students ĐANG CÓ, cùng khoá tên+ngày sinh+lớp.
            if (rowValid && studentRepository.existsByNameIgnoreCaseAndDobAndClassroomId(
                    name, parseDate(dob), resolvedClassroomId)) {
                rowValid = false;
                duplicate++;
                code = StuError.DUPLICATE_CODE;
                message = "Student already exists in this classroom "
                        + "(same name + dob + classroomId).";
            }

            // BR-025/BR-193 — bug production thứ hai: cả validate() lẫn commit() đều không đọc
            // classrooms.capacity, nên import đẩy sĩ số vượt sức chứa. Chặn ngay ở bước xem
            // trước để người dùng thấy đúng dòng nào thừa, thay vì để commit() ném lỗi cả lô.
            if (rowValid) {
                try {
                    long left = seatsLeft.computeIfAbsent(resolvedClassroomId,
                            classroomGuard::remainingSeats);
                    if (left <= 0) {
                        rowValid = false;
                        code = StuError.CLASSROOM_CAPACITY_EXCEEDED;
                        message = StuMessages.MSG_142;
                    } else {
                        seatsLeft.put(resolvedClassroomId, left - 1);
                    }
                } catch (ApiException e) {
                    rowValid = false;                       // VD lớp read-only (STU-193)
                    code = e.getCode();
                    message = e.getMessage();
                }
            }

            if (rowValid) {
                valid++;
            } else {
                invalid++;
            }

            rows.add(new ParsedRow(rowNo, name, dob, classroomId, parentFullName, parentEmail,
                    parentPhone, relationship, rowValid, isDuplicate, code, message));
        }

        StudentImportBatch batch = new StudentImportBatch();
        batch.setCode(nextImportCode());
        batch.setSchoolId(actor.schoolId());
        batch.setStatus(StudentImportBatch.Status.VALIDATED);
        batch.setTotalCount(rows.size());
        batch.setValidCount(valid);
        batch.setInvalidCount(invalid);
        batch.setDuplicateCount(duplicate);
        batch.setRowsJson(writeRows(rows));
        importRepository.saveAndFlush(batch);

        List<ImportRowResult> rowResults = rows.stream()
                .map(r -> new ImportRowResult(r.rowNo(), r.name(), r.valid(), r.code(), r.message()))
                .toList();
        return new ImportPreviewResponse(batch.getCode(), rows.size(), valid, invalid, duplicate, rowResults);
    }

    @Override
    @Transactional
    public ImportResultResponse commit(String importCode, AuthUser actor) {
        StudentImportBatch batch = importRepository.findByCode(importCode).orElseThrow(StuExceptions::notFound);
        if (!actor.isSystemAdmin() && !actor.belongsToSchool(batch.getSchoolId())) {
            throw StuExceptions.accessDenied(); // C2
        }
        if (batch.getStatus() == StudentImportBatch.Status.COMMITTED) {
            throw StuExceptions.importAlreadyCommitted(); // ADR-M03-6 — không commit 2 lần
        }

        List<ParsedRow> rows = readRows(batch.getRowsJson());
        int imported = 0;
        List<UUID> newParentUserIds = new ArrayList<>();

        // ADR-M03-6 — all-or-nothing: MỘT transaction bao toàn bộ vòng lặp; bất kỳ lỗi
        // nào (VD
        // lớp vừa hết chỗ giữa lúc validate() và commit()) ném ApiException ⇒ rollback
        // cả batch,
        // KHÔNG import một phần.
        for (ParsedRow row : rows) {
            if (!row.valid()) {
                continue; // bỏ qua hàng lỗi (skipped)
            }
            UUID classroomId = refs.classroomIdByCode(row.classroomId(), batch.getSchoolId())
                    .orElseThrow(StuExceptions::notFound);

            // validate() và commit() là HAI request khác nhau, cách nhau vài phút (người dùng
            // xem bảng lỗi rồi mới bấm "Nhập") — trong khoảng đó học sinh có thể đã được thêm
            // tay, hoặc chính người dùng bấm "Nhập" hai lần trên hai đợt validate khác nhau.
            // Kiểm lại ngay trước khi ghi và BỎ QUA (không ném) — nhập lại đúng lô cũ trở thành
            // vô hại thay vì nhân đôi cả lớp.
            if (studentRepository.existsByNameIgnoreCaseAndDobAndClassroomId(
                    row.name(), parseDate(row.dob()), classroomId)) {
                continue;
            }
            // BR-025/BR-193 — chốt sức chứa NGAY TRƯỚC KHI GHI. Lớp có thể vừa đầy sau bước
            // validate(); ném ⇒ rollback cả batch, đúng all-or-nothing của ADR-M03-6 (trước đây
            // comment dưới đây mô tả hành vi này nhưng KHÔNG có dòng code nào thực hiện).
            classroomGuard.assertHasRoom(classroomId);

            Student student = new Student();
            student.setCode(nextStudentCode(classroomId));
            student.setRegistrationNo(nextRegistrationNo());
            student.setName(row.name());
            student.setDob(parseDate(row.dob()));
            student.setSchoolId(batch.getSchoolId());
            student.setClassroomId(classroomId);
            studentRepository.saveAndFlush(student);

            ParentInfoRequest parentInfo = new ParentInfoRequest(row.parentFullName(), row.parentEmail(),
                    row.parentPhone(), row.relationship());
            var link = parentLinkService.linkOrCreateParent(student.getId(), parentInfo, actor, null);
            if (link.accountCreated()) {
                newParentUserIds.add(link.parentUserId());
            }

            healthHistoryService.append(student.getId(), HealthHistoryEventType.KHAI_BAO,
                    "Tạo hồ sơ học sinh (nhập hàng loạt " + batch.getCode() + ")", actor.userId());
            imported++;
        }

        batch.setStatus(StudentImportBatch.Status.COMMITTED);
        importRepository.saveAndFlush(batch);

        int skipped = batch.getTotalCount() - imported;
        events.publishEvent(Mf03Events.StudentsBulkImported.of(batch.getId(), imported, skipped,
                newParentUserIds, actor.userId()));

        return new ImportResultResponse(imported, skipped, newParentUserIds.size(), StuMessages.MSG_035);
    }

    private static final List<String> TEMPLATE_HEADER_LABELS = List.of(
            "Họ tên học sinh *", "Ngày sinh * (yyyy-mm-dd)", "Mã lớp * (chọn từ danh sách)",
            "Họ tên phụ huynh *", "Email phụ huynh *", "SĐT phụ huynh", "Quan hệ với học sinh");

    @Override
    public Resource downloadTemplate(AuthUser actor) {
        List<String> classroomCodes = refs.classroomCodesOfSchool(actor.schoolId());
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("Học sinh");
            Row header = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADER_LABELS.size(); i++) {
                header.createCell(i).setCellValue(TEMPLATE_HEADER_LABELS.get(i));
                sheet.setColumnWidth(i, 28 * 256);
            }

            Row example = sheet.createRow(1);
            String[] exampleValues = {
                    "Nguyễn Văn A", "2019-03-12", classroomCodes.isEmpty() ? "" : classroomCodes.get(0),
                    "Nguyễn Văn B", "parentb@example.com", "0912345678", "Bố",
            };
            for (int i = 0; i < exampleValues.length; i++) {
                example.createCell(i).setCellValue(exampleValues[i]);
            }

            // Dropdown cột "Mã lớp" — tránh gõ sai mã lớp bị validate() từ chối ở bước sau.
            if (!classroomCodes.isEmpty()) {
                XSSFSheet ref = wb.createSheet("DataRef");
                for (int i = 0; i < classroomCodes.size(); i++) {
                    ref.createRow(i).createCell(0).setCellValue(classroomCodes.get(i));
                }
                wb.setSheetHidden(wb.getSheetIndex(ref), true);

                Name namedRange = wb.createName();
                namedRange.setNameName("ClassroomCodes");
                namedRange.setRefersToFormula("DataRef!$A$1:$A$" + classroomCodes.size());

                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint constraint = dvHelper.createFormulaListConstraint("ClassroomCodes");
                CellRangeAddressList addressList = new CellRangeAddressList(1, 500, 2, 2); // cột C, dòng 2..501
                DataValidation validation = dvHelper.createValidation(constraint, addressList);
                validation.setShowErrorBox(true);
                validation.createErrorBox("Lỗi mã lớp", "Vui lòng chọn mã lớp từ danh sách.");
                sheet.addValidationData(validation);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return new ByteArrayResource(bos.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ───────────────────────── Tiện ích ─────────────────────────

    private List<String[]> readExcel(MultipartFile file) {
        List<String[]> out = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int r = 1; r <= sheet.getLastRowNum(); r++) { // dòng 0 = header — bỏ qua
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                String[] cols = new String[EXPECTED_HEADER.size()];
                boolean blank = true;
                for (int c = 0; c < cols.length; c++) {
                    String v = cellToString(row.getCell(c));
                    cols[c] = v;
                    if (!v.isBlank()) {
                        blank = false;
                    }
                }
                if (blank) {
                    continue;
                }
                out.add(cols);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    /** Excel trả kiểu ô khác nhau (chuỗi/số/ngày) — chuẩn hoá về text như `col()` bên dưới cần. */
    private static String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }
        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString(); // yyyy-MM-dd (D4)
                }
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static String col(String[] cols, int idx) {
        if (idx >= cols.length) {
            return null;
        }
        String v = cols[idx].trim();
        return v.isEmpty() ? null : v;
    }

    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim()); // D4 — yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }

    private String writeRows(List<ParsedRow> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (Exception e) {
            throw new IllegalStateException("Không serialize được rows_json", e);
        }
    }

    private List<ParsedRow> readRows(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<ParsedRow>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Không đọc được rows_json", e);
        }
    }

    private String nextImportCode() {
        return codeGenerator.next("mf03_import:" + Year.now().getValue(),
                "IMP-" + Year.now().getValue(), 4);
    }

    private String nextStudentCode(UUID classroomId) {
        return studentCodeAllocator.next(classroomId);
    }

    private String nextRegistrationNo() {
        return codeGenerator.next("mf03_registration_no:" + Year.now().getValue(),
                "DTD-" + Year.now().getValue(), 4);
    }
}
