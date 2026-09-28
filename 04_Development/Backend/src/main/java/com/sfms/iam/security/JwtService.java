package com.sfms.iam.security;

import com.sfms.common.enums.RoleKey;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.domain.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Phát hành và xác minh access token (BR-004).
 *
 * <p>Claim (TDS §9.2): {@code sub}=email · {@code uid} · {@code code} · {@code role}
 * (kebab-case, D6) · {@code schoolId} · {@code partnerId}.
 *
 * <p><b>TTL đọc từ {@code SecurityPolicy.sessionTimeoutMinutes}</b>, KHÔNG hardcode 30
 * (TDS §17 C3). MF01-TC-SEC-003 bước 5 đổi policy sang 10 phút và assert token mới hết hạn
 * theo đúng giá trị đó — hardcode ở đây làm màn {@code security-policies} thành trang trí.
 *
 * <p>Ba tấn công mà lớp này phải chặn (MF01-TC-SEC-004):
 * <ul>
 *   <li><b>alg:none</b> — {@code verifyWith(key)} của jjwt 0.12 chỉ chấp nhận token có chữ ký
 *       hợp lệ; header {@code alg: none} bị từ chối, không cần cấu hình thêm.</li>
 *   <li><b>sửa payload giữ chữ ký cũ</b> — chữ ký không khớp ⇒ {@code JwtException}.</li>
 *   <li><b>dùng refresh token làm access token</b> — refresh token là chuỗi ngẫu nhiên opaque,
 *       không phải JWT ⇒ parse thất bại.</li>
 * </ul>
 */
@Service
public class JwtService {

    public static final String CLAIM_UID = "uid";
    public static final String CLAIM_CODE = "code";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_SCHOOL_ID = "schoolId";
    public static final String CLAIM_PARTNER_ID = "partnerId";

    private final SecretKey key;
    private final String issuer;
    private final Clock clock;
    private final ReferenceResolver refs;

    public JwtService(@Value("${sfms.jwt.secret}") String secret,
                      @Value("${sfms.jwt.issuer:sfms}") String issuer,
                      Clock clock,
                      ReferenceResolver refs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.clock = clock;
        this.refs = refs;
    }

    /**
     * @param sessionTimeoutMinutes đọc từ {@code SecurityPolicy} — người gọi có trách nhiệm
     *                              lấy từ DB, không truyền hằng số
     */
    public String issueAccessToken(UserAccount user, int sessionTimeoutMinutes) {
        Instant now = clock.instant();
        Instant exp = now.plus(Duration.ofMinutes(sessionTimeoutMinutes));
        RoleKey role = user.roleKey();

        return Jwts.builder()
                .issuer(issuer)
                .subject(user.getEmail())
                .claim(CLAIM_UID, user.getId().toString())
                .claim(CLAIM_CODE, user.getCode())
                .claim(CLAIM_ROLE, role == null ? null : role.key())
                // D1 — khóa ngoại trong claim là MÃ NGHIỆP VỤ, để downstream so sánh được
                // với "SCH-HN-0042"/"SUP-001" mà FE và các module khác đang dùng.
                .claim(CLAIM_SCHOOL_ID, refs.schoolCode(user.getSchoolId()))
                .claim(CLAIM_PARTNER_ID, refs.partnerCode(user.getPartnerId()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * @return {@link AuthUser} nếu token hợp lệ và chưa hết hạn; {@link Optional#empty()}
     *         với mọi trường hợp còn lại (sai chữ ký, alg:none, hết hạn, claim thiếu).
     *         KHÔNG ném ra ngoài để filter trả 401 gọn theo envelope D2.
     */
    public Optional<AuthUser> parse(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clock(() -> Date.from(clock.instant()))   // biên hết hạn theo Clock bean
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String roleRaw = claims.get(CLAIM_ROLE, String.class);
            String uidRaw = claims.get(CLAIM_UID, String.class);
            if (roleRaw == null || uidRaw == null) {
                return Optional.empty();
            }

            return Optional.of(new AuthUser(
                    UUID.fromString(uidRaw),
                    claims.get(CLAIM_CODE, String.class),
                    claims.getSubject(),
                    RoleKey.from(roleRaw),
                    refs.schoolIdByCode(claims.get(CLAIM_SCHOOL_ID, String.class)).orElse(null),
                    refs.partnerIdByCode(claims.get(CLAIM_PARTNER_ID, String.class)).orElse(null)
            ));
        } catch (JwtException | IllegalArgumentException e) {
            // SEC-012: KHÔNG log nội dung token. Chỉ ghi nhận thất bại ở mức DEBUG nếu cần.
            return Optional.empty();
        }
    }
}
