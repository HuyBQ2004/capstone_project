package com.sfms.mf04.fixture;

import com.sfms.common.enums.BiometricStatus;
import com.sfms.common.enums.PartnerType;
import com.sfms.mf04.domain.DeliveryPerson;
import com.sfms.mf04.domain.DocumentType;
import com.sfms.mf04.domain.Partner;
import com.sfms.mf04.domain.PartnerDocument;
import com.sfms.mf04.domain.PartnerStatus;
import com.sfms.mf04.domain.PartnershipApplication;
import com.sfms.mf04.domain.ApplicationStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * CASE 2.0 — Props Isolation Pattern. Mỗi lời gọi trả instance MỚI (chống AP-AI-002).
 */
public final class Mf04Fixtures {

    public static final UUID PARTNER_A_ID = UUID.fromString("00000000-0000-0000-0000-0000000d0001");
    public static final UUID PARTNER_B_ID = UUID.fromString("00000000-0000-0000-0000-0000000d0002");
    public static final UUID SCHOOL_A_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0042");
    public static final UUID SCHOOL_B_ID = UUID.fromString("00000000-0000-0000-0000-0000000a0051");

    /** FX-014 — đồng hồ cố định, khớp {@code DocumentStatusCalculatorTest}. */
    public static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-07-28T00:00:00Z"), ZoneOffset.UTC);
    public static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);

    private Mf04Fixtures() {
    }

    /** Baseline hợp lệ — đồng bộ FX-001 (GreenFarm). */
    public static Partner aPartner(Consumer<Partner> overrides) {
        Partner p = new Partner();
        p.setId(PARTNER_A_ID);
        p.setCode("SUP-001");
        p.setName("Cty CP Thực phẩm sạch GreenFarm");
        p.setType(PartnerType.RAW_FOOD_SUPPLIER);
        p.setStatus(PartnerStatus.APPROVED);
        p.setTaxId("0107712345");
        p.setAddress("KCN Phú Nghĩa, Chương Mỹ, Hà Nội");
        p.setContact("Lý Thái Sơn — 0913 222 333");
        p.setEmail("sonlt@greenfarm.vn");
        overrides.accept(p);
        return p;
    }

    public static PartnerDocument aDocument(Consumer<PartnerDocument> overrides) {
        PartnerDocument d = new PartnerDocument();
        d.setCode("DOC-GF-01");
        d.setPartnerId(PARTNER_A_ID);
        d.setName("Giấy chứng nhận cơ sở đủ điều kiện ATTP");
        d.setDocumentType(DocumentType.FOOD_SAFETY_CERT);
        d.setNumber("ATTP-HN-2024-1182");
        d.setIssuedBy("Chi cục ATVSTP Hà Nội");
        d.setIssuedDate(LocalDate.parse("2024-03-15"));
        d.setExpiryDate(LocalDate.parse("2027-03-15"));
        overrides.accept(d);
        return d;
    }

    public static DeliveryPerson aDeliveryPerson(Consumer<DeliveryPerson> overrides) {
        DeliveryPerson dp = new DeliveryPerson();
        dp.setCode("DP-01");
        dp.setPartnerId(PARTNER_A_ID);
        dp.setFullName("Đỗ Văn Tám");
        dp.setNationalIdEnc(new byte[]{1, 2, 3});
        dp.setNationalIdHash("hash-dp-01");
        dp.setPhone("0912345678");
        dp.setVehiclePlate("29C-123.45");
        dp.setBiometricStatus(BiometricStatus.NOT_REGISTERED);
        overrides.accept(dp);
        return dp;
    }

    public static PartnershipApplication anApplication(Consumer<PartnershipApplication> overrides) {
        PartnershipApplication app = new PartnershipApplication();
        app.setCode("APP-2026-05");
        app.setPartnerId(PARTNER_A_ID);
        app.setSchoolId(SCHOOL_A_ID);
        app.setCompanyName("Cty CP Thực phẩm sạch GreenFarm");
        app.setTaxId("0107712345");
        app.setAddress("KCN Phú Nghĩa, Chương Mỹ, Hà Nội");
        app.setStatus(ApplicationStatus.PENDING);
        overrides.accept(app);
        return app;
    }
}
