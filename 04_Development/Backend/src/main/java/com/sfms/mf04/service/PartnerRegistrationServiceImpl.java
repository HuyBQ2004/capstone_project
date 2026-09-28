package com.sfms.mf04.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.enums.PartnerType;
import com.sfms.common.storage.StorageService;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.domain.PartnerRegistrationDocument;
import com.sfms.mf04.domain.PartnerStatus;
import com.sfms.mf04.dto.PartnerDtos.PartnerRegistrationDocumentResponse;
import com.sfms.mf04.dto.PartnerDtos.PartnerResponse;
import com.sfms.mf04.dto.PartnerDtos.RegisterPartnerRequest;
import com.sfms.mf04.error.SupExceptions;
import com.sfms.mf04.repository.PartnerRegistrationDocumentRepository;
import com.sfms.mf04.repository.PartnerRepository;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sfms.common.enums.RoleKey;
import com.sfms.iam.domain.RoleRecord;
import com.sfms.iam.domain.UserAccount;
import com.sfms.iam.domain.UserStatus;
import com.sfms.iam.repository.RoleRecordRepository;
import com.sfms.iam.repository.UserAccountRepository;
import com.sfms.iam.service.AccountActivationService;

import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;

/** UC-071/067 — BR-040/046, Guest (public, rate-limit 5/min/IP ở tầng controller). */
@Service
public class PartnerRegistrationServiceImpl implements PartnerRegistrationService {

    /** Khớp {@code SubmitApplicationRequest.taxId} (TDS §10 SUP-007). */
    private static final Pattern TAX_ID_PATTERN = Pattern.compile("^\\d{10}(-\\d{3})?$");

    private final PartnerRepository partnerRepository;
    private final PartnerAssembler assembler;
    private final CodeGenerator codeGenerator;
    private final Clock clock;
    private final UserAccountRepository userAccountRepository;
    private final RoleRecordRepository roleRecordRepository;
    private final AccountActivationService accountActivationService;
    private final PartnerRegistrationDocumentRepository registrationDocumentRepository;
    private final StorageService storageService;

    public PartnerRegistrationServiceImpl(PartnerRepository partnerRepository, PartnerAssembler assembler,
                                           CodeGenerator codeGenerator, Clock clock,
                                           UserAccountRepository userAccountRepository,
                                           RoleRecordRepository roleRecordRepository,
                                           AccountActivationService accountActivationService,
                                           PartnerRegistrationDocumentRepository registrationDocumentRepository,
                                           StorageService storageService) {
        this.partnerRepository = partnerRepository;
        this.assembler = assembler;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
        this.userAccountRepository = userAccountRepository;
        this.roleRecordRepository = roleRecordRepository;
        this.accountActivationService = accountActivationService;
        this.registrationDocumentRepository = registrationDocumentRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public PartnerResponse registerSupplier(RegisterPartnerRequest req, List<MultipartFile> documents) {
        return register(req, PartnerType.RAW_FOOD_SUPPLIER, "SUP", documents);
    }

    @Override
    @Transactional
    public PartnerResponse registerCaterer(RegisterPartnerRequest req, List<MultipartFile> documents) {
        return register(req, PartnerType.SERVICE_CATERER, "CAT", documents);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerRegistrationDocumentResponse> listRegistrationDocuments(String partnerCode) {
        Partner partner = partnerRepository.findByCode(partnerCode).orElseThrow(SupExceptions::notFound);
        return registrationDocumentRepository.findByPartnerId(partner.getId()).stream()
                .map(doc -> toDocumentResponse(partner.getCode(), doc))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadRegistrationDocument(String partnerCode, String docCode) {
        PartnerRegistrationDocument doc = registrationDocumentRepository.findByCode(docCode)
                .orElseThrow(SupExceptions::notFound);
        return storageService.load(doc.getFileKey());
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerRegistrationDocument getRegistrationDocumentEntity(String docCode) {
        return registrationDocumentRepository.findByCode(docCode).orElseThrow(SupExceptions::notFound);
    }

    private PartnerRegistrationDocumentResponse toDocumentResponse(String partnerCode, PartnerRegistrationDocument doc) {
        // 13/08/2026 (Nam) — bỏ tiền tố "/api/v1" cùng lý do với SchoolService.toDocumentResponse:
        // frontend gọi qua client `api` đã có baseURL "/api/v1", URL tuyệt đối bị ghép đôi.
        String downloadUrl = "/partners/" + partnerCode + "/registration-documents/" + doc.getCode()
                + "/download";
        return new PartnerRegistrationDocumentResponse(
                doc.getCode(),
                doc.getName(),
                doc.getDocumentType(),
                doc.getFileName(),
                doc.getFileSize(),
                doc.getContentType(),
                doc.getStatus(),
                doc.getUploadedAt(),
                downloadUrl
        );
    }

    /** Mirror {@code SchoolService.storeDocument()} — dùng lúc đăng ký Guest (11/08/2026). */
    private PartnerRegistrationDocument storeDocument(Partner partner, String name, String documentType,
                                                        MultipartFile file) {
        String code = codeGenerator.next("partner_registration_documents", "DOC-PTN", 4);
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.pdf";
        String fileKey = "partner-registration-documents/" + partner.getCode() + "/" + code + "_" + originalFilename;

        storageService.store(fileKey, file);

        PartnerRegistrationDocument doc = new PartnerRegistrationDocument();
        doc.setCode(code);
        doc.setPartnerId(partner.getId());
        doc.setName(name != null && !name.isBlank() ? name : originalFilename);
        doc.setDocumentType(documentType != null && !documentType.isBlank() ? documentType : "REGISTRATION_DOC");
        doc.setFileKey(fileKey);
        doc.setFileName(originalFilename);
        doc.setFileSize(file.getSize());
        doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/pdf");
        doc.setStatus("PENDING");
        doc.setUploadedAt(clock.instant());

        registrationDocumentRepository.saveAndFlush(doc);
        return doc;
    }

    private PartnerResponse register(RegisterPartnerRequest req, PartnerType type, String prefix,
                                      List<MultipartFile> documents) {
        if (!TAX_ID_PATTERN.matcher(req.licenseNo()).matches()) {
            throw SupExceptions.invalidTaxId();                                 // ⚠️ ngoài §1.4, CF-M04-2
        }
        if (partnerRepository.existsByEmailIgnoreCase(req.email())) {
            throw SupExceptions.emailAlreadyUsed();                             // BR-012
        }

        String code;
        if (req.id() != null && !req.id().isBlank()) {
            if (partnerRepository.existsByCode(req.id())) {
                throw SupExceptions.duplicateValue("id", req.id());             // D5
            }
            code = req.id();
        } else {
            code = codeGenerator.next("partners", prefix, 3);
        }

        Partner partner = new Partner();
        partner.setCode(code);
        partner.setName(req.companyName());
        partner.setType(type);
        partner.setStatus(PartnerStatus.PENDING);                               // UC-071/067
        partner.setTaxId(req.licenseNo());
        partner.setAddress(req.address());
        partner.setContact(req.contactPerson() + " — " + req.phone());
        partner.setEmail(req.email());
        partner.setPhone(req.phone());
        partner.setAppliedAt(clock.instant());

        try {
            partnerRepository.saveAndFlush(partner);
        } catch (DataIntegrityViolationException e) {
            throw SupExceptions.duplicateValue("taxId", req.licenseNo());       // chốt chặn cuối — UNIQUE(tax_id)
        }

        if (documents != null) {
            for (MultipartFile file : documents) {
                if (file != null && !file.isEmpty()) {
                    storeDocument(partner, null, "REGISTRATION_DOC", file);
                }
            }
        }

        // Tạo tài khoản PENDING_VERIFICATION & phát hành token gửi email kích hoạt (UC-154, BR-010)
        String userRoleKey = type == PartnerType.SERVICE_CATERER ? RoleKey.SERVICE_CATERERS.key() : RoleKey.SUPPLIER.key();
        RoleRecord role = roleRecordRepository.findByRoleKey(userRoleKey).orElse(null);
        if (role != null && !userAccountRepository.existsByEmail(req.email())) {
            UserAccount user = new UserAccount();
            user.setCode(codeGenerator.next("users", "U", 4));
            /*
             * Sửa 11/08/2026 (theo Nam) — trước đây gán NHẦM `companyName` (tên DOANH NGHIỆP)
             * cho `UserAccount.name`, trong khi trường này là tên NGƯỜI đăng nhập/đại diện.
             * Hệ quả đo thật: mọi nơi hiển thị "người kiểm tra"/"người thực hiện" bằng cách đọc
             * `UserAccount.name` (VD `KitchenInspectionServiceImpl.toResponse()` qua
             * `ReferenceResolver.userById()`) đều ra tên CÔNG TY thay vì tên người cụ thể.
             * DTO đã tách sẵn 2 field — `companyName` cho `Partner.name`, `contactPerson` (VD
             * "Vàng A Chứ") mới đúng là tên người, chỉ là chưa từng được dùng để set `user.name`.
             */
            user.setName(req.contactPerson());
            user.setEmail(req.email());
            user.setPhone(req.phone());
            user.setPasswordHash(null); // PENDING_VERIFICATION requires passwordHash IS NULL (ck_pending_has_no_password)
            user.setRole(role);
            user.setPartnerId(partner.getId());
            user.setStatus(UserStatus.PENDING_VERIFICATION);
            user.setEmailVerified(false);
            userAccountRepository.saveAndFlush(user);

            accountActivationService.issueAndSend(user);
        }

        return assembler.toResponse(partner);
    }
}

