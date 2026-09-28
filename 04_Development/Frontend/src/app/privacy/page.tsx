import type { Metadata } from "next";
import Link from "next/link";
import {
  DPO_EMAIL,
  PDPL_DECREE,
  PDPL_LAW,
  PRIVACY_POLICY_EFFECTIVE_DATE,
  PRIVACY_POLICY_VERSION,
} from "@/lib/privacy";

export const metadata: Metadata = {
  title: "Chính sách bảo vệ dữ liệu cá nhân — SFMS",
  description:
    "Chính sách bảo vệ dữ liệu cá nhân của Hệ thống hỗ trợ quản lý an toàn thực phẩm học đường (SFMS), ban hành theo Luật 91/2025/QH15 và Nghị định 356/2025/NĐ-CP.",
};

/**
 * Chính sách bảo vệ dữ liệu cá nhân — trang CÔNG KHAI.
 *
 * <p><b>Vì sao hệ thống này cần một trang riêng thay vì một đoạn "điều khoản" chung chung.</b>
 * SFMS xử lý ba nhóm dữ liệu mà {@link PDPL_LAW} xếp vào loại NHẠY CẢM: tình trạng sức khỏe của
 * trẻ em (dị ứng thực phẩm, bệnh mạn tính), dữ liệu sinh trắc học khuôn mặt (xác thực người giao
 * hàng), và dữ liệu vị trí (GPS gắn kèm ảnh hiện trường lúc giao nhận). Với dữ liệu nhạy cảm của
 * người chưa thành niên, nghĩa vụ thông báo và lấy sự đồng ý là bắt buộc chứ không phải tùy chọn.
 *
 * <p><b>Cấu trúc</b> bám bố cục quy định mẫu của Khối Phổ thông FPT (3 chương: quy định chung →
 * quy định cụ thể → tổ chức thực hiện) để trường học đọc thấy quen, nhưng NỘI DUNG được viết lại
 * theo đúng thứ SFMS thực sự thu thập và làm với dữ liệu — mỗi điều đều đối chiếu được với một
 * bảng/luồng có thật trong hệ thống. Không chép nguyên văn quy định của tổ chức khác: một chính
 * sách mô tả sai hoạt động xử lý dữ liệu thì bản thân nó là một vi phạm.
 *
 * <p>Phiên bản và ngày hiệu lực nằm ở `lib/privacy.ts` — hộp thoại đồng ý (BR-009) và mẫu văn bản
 * đồng ý dùng chung, để mốc `terms_accepted_at` lưu trong DB luôn truy được về đúng bản này.
 */
export default function PrivacyPolicyPage() {
  return (
    <article className="rounded-xl border bg-background p-6 shadow-sm print:border-none print:shadow-none sm:p-10">
      <header className="border-b pb-6">
        <p className="text-xs font-semibold uppercase tracking-wide text-primary">
          Hệ thống hỗ trợ quản lý an toàn thực phẩm học đường (SFMS)
        </p>
        <h1 className="mt-2 text-2xl font-bold sm:text-3xl">
          Chính sách bảo vệ dữ liệu cá nhân
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Ban hành theo {PDPL_LAW} (sau đây gọi tắt là <b>Luật 91</b>) và {PDPL_DECREE} (sau đây
          gọi tắt là <b>Nghị định 356</b>).
        </p>
        <dl className="mt-4 grid gap-x-8 gap-y-1 text-sm sm:grid-cols-2">
          <div className="flex gap-2">
            <dt className="text-muted-foreground">Phiên bản:</dt>
            <dd className="font-medium">{PRIVACY_POLICY_VERSION}</dd>
          </div>
          <div className="flex gap-2">
            <dt className="text-muted-foreground">Ngày hiệu lực:</dt>
            <dd className="font-medium">{PRIVACY_POLICY_EFFECTIVE_DATE}</dd>
          </div>
          <div className="flex gap-2">
            <dt className="text-muted-foreground">Đầu mối tiếp nhận yêu cầu:</dt>
            <dd className="font-medium">{DPO_EMAIL}</dd>
          </div>
        </dl>
      </header>

      <Chapter no="I" title="Quy định chung" />

      <Article no={1} title="Mục đích và phạm vi điều chỉnh">
        <Ol>
          <li>
            Chính sách này cụ thể hóa các nguyên tắc, quy trình xử lý và bảo vệ dữ liệu cá nhân,
            cùng trách nhiệm của các bên liên quan trong toàn bộ hoạt động của Hệ thống hỗ trợ quản
            lý an toàn thực phẩm học đường (sau đây gọi là <b>Hệ thống</b>) theo quy định tại Luật
            91 và Nghị định 356.
          </li>
          <li>
            Phạm vi áp dụng là mọi dữ liệu cá nhân được tạo lập, tiếp nhận, lưu trữ hoặc chia sẻ
            thông qua Hệ thống, bao gồm cả bản web và ứng dụng di động.
          </li>
          <li>
            Nhà trường sử dụng Hệ thống là bên kiểm soát dữ liệu đối với dữ liệu của người học và
            cán bộ, nhà giáo thuộc trường mình. Đơn vị vận hành Hệ thống là bên xử lý dữ liệu, chỉ
            được xử lý trong phạm vi nhà trường ủy quyền và theo Chính sách này.
          </li>
        </Ol>
      </Article>

      <Article no={2} title="Đối tượng áp dụng">
        <Ol>
          <li>
            Các trường học, đơn vị suất ăn, nhà cung cấp nguyên liệu và cá nhân được cấp tài khoản
            trên Hệ thống, gồm bảy nhóm vai trò: Quản trị hệ thống, Quản trị nhà trường, Giáo viên,
            Nhân viên y tế, Đơn vị suất ăn, Nhà cung cấp và Phụ huynh.
          </li>
          <li>
            <b>Chủ thể dữ liệu</b> gồm:
            <Ul>
              <li>Người học tại trường sử dụng Hệ thống.</li>
              <li>Cha mẹ người học, người giám hộ hợp pháp, người bảo lãnh.</li>
              <li>Cán bộ quản lý, nhà giáo, nhân viên y tế học đường.</li>
              <li>
                Nhân sự của đơn vị suất ăn và nhà cung cấp, bao gồm người trực tiếp giao hàng vào
                trường.
              </li>
            </Ul>
          </li>
        </Ol>
      </Article>

      <Article no={3} title="Các dữ liệu cá nhân được thu thập">
        <P>
          Hệ thống chỉ thu thập dữ liệu phát sinh từ hoạt động bảo đảm an toàn thực phẩm bữa ăn học
          đường. Danh mục dưới đây là danh mục đầy đủ; Hệ thống không thu thập dữ liệu ngoài danh
          mục này.
        </P>
        <H4>1. Dữ liệu cá nhân cơ bản</H4>
        <Ul>
          <li>
            <b>Người học:</b> họ và tên, ngày sinh, mã người học, lớp và khối, trường đang theo học.
          </li>
          <li>
            <b>Tài khoản người dùng:</b> họ và tên, thư điện tử, số điện thoại, ảnh đại diện, vai
            trò, đơn vị công tác, thời điểm đăng nhập gần nhất và lịch sử thao tác trên Hệ thống.
          </li>
          <li>
            <b>Quan hệ gia đình:</b> liên kết giữa tài khoản phụ huynh và người học được bảo trợ.
          </li>
          <li>
            <b>Thiết bị nhận thông báo:</b> mã định danh thiết bị dùng để gửi thông báo đẩy, do
            người dùng chủ động cấp quyền.
          </li>
          <li>
            <b>Người giao hàng:</b> họ và tên, số điện thoại, biển kiểm soát phương tiện.
          </li>
        </Ul>
        <H4>2. Dữ liệu cá nhân nhạy cảm</H4>
        <P>
          Các nhóm dưới đây thuộc dữ liệu cá nhân nhạy cảm theo Luật 91 và chỉ được xử lý khi có sự
          đồng ý theo Điều 8 Chính sách này, hoặc trong tình huống khẩn cấp nhằm bảo vệ tính mạng,
          sức khỏe của chủ thể dữ liệu.
        </P>
        <Ul>
          <li>
            <b>Dữ liệu sức khỏe của người học:</b> khai báo dị ứng thực phẩm, bệnh lý mạn tính, chỉ
            số chiều cao, cân nặng, nhóm máu và các hồ sơ y tế do gia đình cung cấp để chứng minh.
          </li>
          <li>
            <b>Diễn biến sự cố an toàn thực phẩm:</b> danh sách người học bị ảnh hưởng, triệu chứng
            ghi nhận và diễn biến xử trí, phục vụ điều tra nguyên nhân và báo cáo cơ quan y tế.
          </li>
          <li>
            <b>Dữ liệu sinh trắc học:</b> đặc trưng khuôn mặt của người giao hàng, dùng để xác thực
            danh tính tại cổng trường trước khi bàn giao nguyên liệu.
          </li>
          <li>
            <b>Dữ liệu vị trí:</b> tọa độ định vị gắn kèm ảnh hiện trường tại bước giao nhận và
            kiểm thực, dùng làm bằng chứng việc kiểm tra được thực hiện đúng nơi, đúng lúc.
          </li>
        </Ul>
        <H4>3. Dữ liệu Hệ thống chủ động không lưu</H4>
        <Ul>
          <li>
            Số căn cước công dân của người giao hàng chỉ được lưu ở dạng đã băm một chiều để đối
            chiếu trùng lặp; Hệ thống không lưu và không hiển thị lại số gốc.
          </li>
          <li>
            Mật khẩu được lưu ở dạng băm, không thể khôi phục về dạng gốc kể cả bởi quản trị viên.
          </li>
          <li>
            Hệ thống không thu thập dữ liệu về quan điểm chính trị, tôn giáo, nguồn gốc chủng tộc,
            đời sống tình dục hay tình trạng tài chính của bất kỳ chủ thể dữ liệu nào.
          </li>
        </Ul>
      </Article>

      <Article no={4} title="Giải thích từ ngữ">
        <P>
          Ngoài các từ ngữ đã được giải thích tại Luật 91 và Nghị định 356, trong Chính sách này
          các từ ngữ dưới đây được hiểu như sau:
        </P>
        <Ol>
          <li>
            <b>Khử nhận dạng</b> là quá trình kỹ thuật loại bỏ hoặc mã hóa các thông tin định danh,
            khiến dữ liệu không thể dùng để nhận biết một cá nhân cụ thể nếu không có thông tin bổ
            sung.
          </li>
          <li>
            <b>Xử lý tự động</b> là việc sử dụng công nghệ (như trí tuệ nhân tạo, thuật toán) để tự
            động đánh giá, phân tích hoặc dự đoán các khía cạnh liên quan đến chủ thể dữ liệu mà
            không có sự can thiệp của con người.
          </li>
          <li>
            <b>Dữ liệu sinh trắc học</b> là dữ liệu về đặc điểm thể chất cho phép nhận dạng duy
            nhất một cá nhân; trong Hệ thống là đặc trưng khuôn mặt đã được trích xuất thành vector
            số, không phải ảnh chân dung.
          </li>
          <li>
            <b>Cán bộ bảo vệ dữ liệu (DPO)</b> là cá nhân hoặc bộ phận chuyên trách bảo vệ dữ liệu
            được đơn vị bổ nhiệm, là đầu mối tiếp nhận yêu cầu của chủ thể dữ liệu.
          </li>
        </Ol>
      </Article>

      <Chapter no="II" title="Quy định cụ thể về hoạt động xử lý dữ liệu" />

      <Article no={5} title="Mục đích xử lý dữ liệu cá nhân">
        <P>
          Hệ thống chỉ xử lý dữ liệu cá nhân cho các mục đích sau, và không sử dụng cho bất kỳ mục
          đích nào khác nếu chưa được chủ thể dữ liệu đồng ý:
        </P>
        <Ol>
          <li>
            <b>Bảo đảm an toàn cho bữa ăn của người học</b> — đối chiếu khai báo dị ứng và bệnh lý
            với thành phần món ăn để cảnh báo trước khi thực đơn được duyệt, chuẩn bị suất ăn riêng
            cho người học có nhu cầu đặc biệt, và chặn phân phối món ăn không phù hợp tới lớp.
          </li>
          <li>
            <b>Thực hiện nghĩa vụ pháp lý về an toàn thực phẩm</b> — ghi nhận kiểm thực ba bước,
            lưu và hủy mẫu thức ăn, lập hồ sơ theo quy định của pháp luật về an toàn thực phẩm và
            cung cấp cho cơ quan quản lý nhà nước có thẩm quyền khi được yêu cầu.
          </li>
          <li>
            <b>Truy vết và xử lý sự cố an toàn thực phẩm</b> — xác định người học bị ảnh hưởng, món
            ăn nghi ngờ, lô nguyên liệu và nhà cung cấp liên quan nhằm khoanh vùng, thu hồi và ngăn
            sự cố lan rộng. Đây là hoạt động nhằm bảo vệ tính mạng, sức khỏe của chủ thể dữ liệu
            trong tình huống khẩn cấp.
          </li>
          <li>
            <b>Kiểm soát người ra vào khu vực bếp ăn</b> — xác thực danh tính người giao hàng bằng
            đối sánh khuôn mặt tại thời điểm bàn giao, bảo đảm an ninh và truy được trách nhiệm
            trong chuỗi cung ứng.
          </li>
          <li>
            <b>Liên lạc và thông báo</b> — gửi thông báo về thực đơn, sự cố, kết quả xử lý yêu cầu
            và các thông tin vận hành khác qua Hệ thống, thư điện tử hoặc thông báo đẩy.
          </li>
          <li>
            <b>Bảo đảm an toàn thông tin</b> — ghi nhật ký truy cập và thao tác để phát hiện truy
            cập trái phép và phục vụ điều tra khi có sự cố.
          </li>
        </Ol>
      </Article>

      <Article no={6} title="Chia sẻ dữ liệu cá nhân">
        <Ol>
          <li>
            Hệ thống <b>không bán</b> dữ liệu cá nhân của chủ thể dữ liệu cho bất kỳ bên nào, và
            không sử dụng dữ liệu cá nhân cho mục đích quảng cáo hay tiếp thị.
          </li>
          <li>
            Dữ liệu chỉ được chia sẻ ở mức tối thiểu cần thiết cho từng mục đích tại Điều 5, cụ thể:
            <Ul>
              <li>
                <b>Đơn vị suất ăn</b> nhận thông tin về nhu cầu ăn đặc biệt và cảnh báo dị ứng theo
                từng suất ăn để chế biến đúng; chỉ nhận thông tin định danh người học ở mức cần
                thiết cho việc bàn giao suất ăn tới lớp.
              </li>
              <li>
                <b>Nhà cung cấp</b> chỉ nhận thông tin về đơn hàng, lô nguyên liệu và người tiếp
                nhận tại trường; không nhận bất kỳ dữ liệu nào của người học.
              </li>
              <li>
                <b>Cơ quan quản lý nhà nước có thẩm quyền</b> nhận dữ liệu theo yêu cầu hợp pháp
                bằng văn bản, trong phạm vi yêu cầu.
              </li>
              <li>
                <b>Cơ sở y tế</b> nhận thông tin sức khỏe cần thiết của người học trong tình huống
                cấp cứu.
              </li>
            </Ul>
          </li>
          <li>
            Dữ liệu của mỗi trường được cách ly với các trường khác trong Hệ thống. Tài khoản của
            một trường không truy cập được dữ liệu người học của trường khác.
          </li>
          <li>
            Mọi bên nhận dữ liệu phải tuân thủ Chính sách này và pháp luật hiện hành về bảo vệ dữ
            liệu cá nhân; việc chia sẻ được thực hiện trên cơ sở hợp đồng hoặc thỏa thuận xử lý dữ
            liệu đã ký kết.
          </li>
        </Ol>
      </Article>

      <Article no={7} title="Quyền và nghĩa vụ của chủ thể dữ liệu">
        <H4>1. Quyền của chủ thể dữ liệu</H4>
        <Ol>
          <li>
            <b>Quyền được biết</b> — được biết về hoạt động xử lý dữ liệu cá nhân của mình, trừ
            trường hợp pháp luật có quy định khác.
          </li>
          <li>
            <b>Quyền đồng ý</b> — được đồng ý hoặc không đồng ý cho phép xử lý dữ liệu cá nhân của
            mình, trừ trường hợp luật có quy định khác.
          </li>
          <li>
            <b>Quyền xem, chỉnh sửa</b> — được truy cập, xem, yêu cầu chỉnh sửa và trích xuất dữ
            liệu cá nhân của mình trên Hệ thống. Nhà trường có trách nhiệm xác minh và cập nhật
            theo thời hạn tại Điều 9.
          </li>
          <li>
            <b>Quyền yêu cầu xóa dữ liệu</b> — được yêu cầu xóa dữ liệu cá nhân của mình. Yêu cầu
            xóa có thể bị từ chối một phần đối với dữ liệu phải lưu trữ theo quy định của pháp luật
            về an toàn thực phẩm, giáo dục, kế toán, lưu trữ và lao động, hoặc dữ liệu cần thiết để
            thiết lập, thực hiện và bảo vệ quyền, lợi ích hợp pháp trước pháp luật. Cụ thể trong Hệ
            thống, các hồ sơ kiểm thực ba bước, sổ lưu và hủy mẫu thức ăn, hồ sơ sự cố an toàn thực
            phẩm là chứng cứ pháp lý bắt buộc lưu và không thể xóa theo yêu cầu; trường hợp bị từ
            chối, Hệ thống nêu rõ lý do và căn cứ.
          </li>
          <li>
            <b>Quyền rút lại sự đồng ý, hạn chế và phản đối xử lý</b> — được thực hiện bất cứ lúc
            nào qua kênh chính thức tại Điều 19. Việc rút lại sự đồng ý không ảnh hưởng đến tính
            hợp pháp của việc xử lý dữ liệu trước đó. Chủ thể dữ liệu cần lưu ý: nếu rút lại sự
            đồng ý đối với dữ liệu dị ứng và bệnh lý, Hệ thống sẽ không còn cảnh báo được nguy cơ
            cho suất ăn của người học.
          </li>
          <li>
            <b>Quyền khiếu nại, tố cáo, khởi kiện và yêu cầu bồi thường thiệt hại</b> theo quy định
            của pháp luật.
          </li>
        </Ol>
        <H4>2. Nghĩa vụ của chủ thể dữ liệu</H4>
        <Ol>
          <li>
            Tự bảo vệ dữ liệu cá nhân của mình: giữ bí mật mật khẩu và mã xác thực, đăng xuất khỏi
            thiết bị dùng chung, không chia sẻ tài khoản cho người khác.
          </li>
          <li>
            Cung cấp thông tin chính xác và cập nhật kịp thời khi có thay đổi, đặc biệt là khai báo
            dị ứng và bệnh lý của người học — thông tin sai lệch trực tiếp làm mất hiệu lực cảnh
            báo an toàn của Hệ thống.
          </li>
          <li>
            Tôn trọng dữ liệu cá nhân của người khác khi sử dụng Hệ thống; không sao chụp, phát tán
            dữ liệu người học ra ngoài phạm vi công việc được giao.
          </li>
        </Ol>
      </Article>

      <Article no={8} title="Sự đồng ý của trẻ em và người chưa thành niên">
        <Ol>
          <li>
            Người học <b>dưới 07 tuổi</b>: sự đồng ý do cha, mẹ hoặc người giám hộ hợp pháp quyết
            định.
          </li>
          <li>
            Người học <b>từ đủ 07 tuổi đến dưới 16 tuổi</b>: thực hiện theo Điều 24 Luật 91 — hoạt
            động xử lý dữ liệu phải có sự đồng ý của cả người học và cha, mẹ hoặc người giám hộ hợp
            pháp.
          </li>
          <li>
            Người học <b>từ đủ 16 tuổi trở lên</b> có quyền tự quyết định sự đồng ý.
          </li>
          <li>
            Do phần lớn chủ thể dữ liệu của Hệ thống là trẻ em, sự đồng ý được thu thập thông qua
            tài khoản của cha mẹ hoặc người giám hộ hợp pháp, theo{" "}
            <Link href="/privacy/consent" className="font-medium text-primary hover:underline">
              Mẫu văn bản đồng ý
            </Link>{" "}
            kèm theo Chính sách này.
          </li>
          <li>
            Hình thức đồng ý phải rõ ràng, thể hiện bằng văn bản, thao tác tích chọn trên phần mềm,
            tin nhắn hoặc thư điện tử xác nhận. <b>Sự im lặng không được coi là đồng ý.</b>
          </li>
          <li>
            Mọi lần đồng ý đều được Hệ thống ghi nhận kèm thời điểm và phiên bản Chính sách tương
            ứng, để chứng minh khi cần.
          </li>
        </Ol>
      </Article>

      <Article no={9} title="Thời hạn xử lý yêu cầu của chủ thể dữ liệu">
        <Ol>
          <li>
            Hệ thống và nhà trường cam kết xử lý yêu cầu của chủ thể dữ liệu theo thời hạn quy định
            tại Điều 5 Nghị định 356:
            <div className="mt-3 overflow-x-auto">
              <table className="w-full border-collapse text-sm">
                <thead>
                  <tr className="bg-muted/60">
                    <th className="border px-3 py-2 text-left font-semibold">Loại yêu cầu</th>
                    <th className="border px-3 py-2 text-left font-semibold">Thời hạn</th>
                    <th className="border px-3 py-2 text-left font-semibold">
                      Nếu cần bên thứ ba phối hợp
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td className="border px-3 py-2">Phản hồi xác nhận và gửi thủ tục</td>
                    <td className="border px-3 py-2">02 ngày làm việc</td>
                    <td className="border px-3 py-2">—</td>
                  </tr>
                  <tr>
                    <td className="border px-3 py-2">
                      Rút lại sự đồng ý / hạn chế xử lý / phản đối xử lý
                    </td>
                    <td className="border px-3 py-2">12 ngày</td>
                    <td className="border px-3 py-2">18 ngày</td>
                  </tr>
                  <tr>
                    <td className="border px-3 py-2">Xem, chỉnh sửa, yêu cầu chỉnh sửa dữ liệu</td>
                    <td className="border px-3 py-2">08 ngày</td>
                    <td className="border px-3 py-2">12 ngày</td>
                  </tr>
                  <tr>
                    <td className="border px-3 py-2">Xóa dữ liệu</td>
                    <td className="border px-3 py-2">18 ngày</td>
                    <td className="border px-3 py-2">25 ngày</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </li>
          <li>
            Trường hợp phức tạp, thời hạn được gia hạn thêm 01 lần, không quá thời hạn quy định
            gốc, và phải thông báo lý do cho chủ thể dữ liệu.
          </li>
        </Ol>
      </Article>

      <Article no={10} title="Xử lý dữ liệu thông qua công nghệ trí tuệ nhân tạo">
        <Ol>
          <li>
            Hệ thống có sử dụng trí tuệ nhân tạo ở hai chức năng, và chủ thể dữ liệu được thông báo
            rõ về nguyên tắc hoạt động của từng chức năng:
            <Ul>
              <li>
                <b>Trợ lý hỏi đáp về an toàn thực phẩm</b> — trả lời câu hỏi nghiệp vụ dựa trên kho
                tri thức về an toàn thực phẩm và dinh dưỡng học đường. Nội dung câu hỏi của người
                dùng được gửi tới mô hình ngôn ngữ của nhà cung cấp dịch vụ; <b>không</b> đưa dữ
                liệu định danh người học vào nội dung gửi đi. Người dùng không nên nhập thông tin
                cá nhân của người học vào ô câu hỏi.
              </li>
              <li>
                <b>Đối sánh khuôn mặt người giao hàng</b> — so khớp khuôn mặt tại thời điểm giao
                nhận với đặc trưng đã đăng ký, chỉ nhằm xác thực danh tính. Chức năng này{" "}
                <b>không</b> được dùng để chấm điểm, đánh giá hay dự đoán hành vi của bất kỳ ai.
              </li>
            </Ul>
          </li>
          <li>
            Hệ thống <b>không ra quyết định hoàn toàn tự động</b> gây ảnh hưởng pháp lý hoặc ảnh
            hưởng đáng kể tới chủ thể dữ liệu. Các cảnh báo do thuật toán sinh ra — như cảnh báo dị
            ứng, cảnh báo lệch dinh dưỡng — là thông tin hỗ trợ; quyết định cuối cùng về thực đơn,
            suất ăn và xử lý sự cố luôn do con người có thẩm quyền thực hiện và chịu trách nhiệm.
          </li>
          <li>
            Chủ thể dữ liệu có quyền không tham gia các hoạt động xử lý tự động nêu trên. Riêng với
            đối sánh khuôn mặt, người giao hàng không đồng ý đăng ký sinh trắc học sẽ được xác thực
            bằng phương thức thay thế do nhà trường quy định.
          </li>
        </Ol>
      </Article>

      <Article no={11} title="Lưu trữ dữ liệu cá nhân">
        <Ol>
          <li>
            Dữ liệu cá nhân chỉ được lưu trữ trong thời gian cần thiết để đạt được các mục đích nêu
            tại Điều 5, hoặc theo thời hạn pháp luật quy định đối với từng loại hồ sơ.
          </li>
          <li>
            Hồ sơ kiểm thực ba bước, sổ lưu và hủy mẫu thức ăn, hồ sơ sự cố an toàn thực phẩm được
            lưu theo quy định của pháp luật về an toàn thực phẩm và không bị xóa theo yêu cầu của
            chủ thể dữ liệu trong thời hạn lưu trữ bắt buộc.
          </li>
          <li>
            Mẫu thức ăn lưu và các thông tin gắn với mẫu được xử lý theo thời hạn lưu mẫu tối thiểu
            do pháp luật quy định; sau thời hạn, mẫu được hủy và việc hủy được ghi nhận vào sổ.
          </li>
          <li>
            Nhật ký truy cập và thao tác được lưu để phục vụ điều tra sự cố an toàn thông tin và
            không được sử dụng cho mục đích đánh giá cá nhân.
          </li>
          <li>
            Dữ liệu của tài khoản đã bị vô hiệu hóa được ngừng xử lý cho các mục đích vận hành,
            nhưng vẫn lưu phần gắn với hồ sơ pháp lý đã lập, ở mức tối thiểu cần thiết để hồ sơ đó
            còn nguyên giá trị chứng cứ.
          </li>
          <li>
            Bản sao lưu dữ liệu được tạo định kỳ, được kiểm soát truy cập tương đương dữ liệu gốc
            và bị hủy theo cùng chính sách thời hạn.
          </li>
        </Ol>
      </Article>

      <Article no={12} title="Chuyển dữ liệu cá nhân ra nước ngoài">
        <Ol>
          <li>
            Hệ thống có sử dụng dịch vụ hạ tầng điện toán đám mây, dịch vụ lưu trữ tệp và mô hình
            ngôn ngữ do nhà cung cấp nước ngoài vận hành. Việc này dẫn tới dữ liệu cá nhân có thể
            được lưu trữ hoặc xử lý trên máy chủ đặt ngoài lãnh thổ Việt Nam.
          </li>
          <li>
            Trước khi chuyển dữ liệu ra nước ngoài, đơn vị chủ trì phải:
            <Ul>
              <li>Thực hiện đánh giá tác động chuyển dữ liệu cá nhân ra nước ngoài.</li>
              <li>
                Ký kết hợp đồng hoặc thỏa thuận xử lý dữ liệu với bên nhận, bảo đảm tiêu chuẩn bảo
                vệ tương đương quy định của pháp luật Việt Nam.
              </li>
              <li>
                Lập hồ sơ đánh giá tác động gửi Bộ Công an theo quy định tại Điều 17 và Điều 18
                Nghị định 356, và gửi bản sao đến DPO.
              </li>
            </Ul>
          </li>
          <li>Dữ liệu chuyển đi phải được mã hóa trong quá trình truyền tải.</li>
          <li>
            Dữ liệu cá nhân nhạy cảm của người học không được chuyển ra nước ngoài cho mục đích
            phân tích, huấn luyện mô hình hoặc bất kỳ mục đích nào ngoài các mục đích tại Điều 5.
          </li>
        </Ol>
      </Article>

      <Chapter no="III" title="Tổ chức thực hiện và ứng phó sự cố" />

      <Article no={13} title="Cơ cấu đơn vị bảo vệ dữ liệu cá nhân">
        <Ol>
          <li>
            Người đứng đầu đơn vị vận hành Hệ thống và người đứng đầu mỗi trường sử dụng Hệ thống
            bổ nhiệm cán bộ bảo vệ dữ liệu cá nhân (DPO) theo quy định của pháp luật.
          </li>
          <li>
            Mỗi bộ phận chỉ định 01 đầu mối bảo vệ dữ liệu để phối hợp thực thi chính sách tại bộ
            phận mình.
          </li>
          <li>
            Nhiệm vụ của DPO: tham mưu xây dựng chính sách bảo vệ dữ liệu; giám sát, kiểm tra tính
            tuân thủ; tiếp nhận và xử lý yêu cầu của chủ thể dữ liệu; là đầu mối liên lạc duy nhất
            với cơ quan chức năng.
          </li>
          <li>
            Mọi yêu cầu của chủ thể dữ liệu gửi về địa chỉ <b>{DPO_EMAIL}</b>.
          </li>
        </Ol>
      </Article>

      <Article no={14} title="Đánh giá tác động xử lý dữ liệu">
        <P>
          Hệ thống thuộc diện bắt buộc thực hiện đánh giá tác động xử lý dữ liệu cá nhân do có xử
          lý dữ liệu nhạy cảm của người chưa thành niên trên quy mô lớn, sử dụng công nghệ sinh
          trắc học và trí tuệ nhân tạo, và có chuyển dữ liệu ra nước ngoài. Hồ sơ đánh giá tác động
          phải được DPO thẩm định trước khi triển khai và được rà soát lại khi phát sinh chức năng
          mới có xử lý dữ liệu cá nhân.
        </P>
      </Article>

      <Article no={15} title="Biện pháp bảo vệ dữ liệu cá nhân">
        <P>Hệ thống áp dụng các biện pháp kỹ thuật và tổ chức sau:</P>
        <Ol>
          <li>
            <b>Kiểm soát truy cập theo vai trò</b> — mỗi tài khoản chỉ thấy dữ liệu thuộc phạm vi
            công việc của vai trò đó; dữ liệu giữa các trường và các đối tác được cách ly ở tầng
            máy chủ, không phụ thuộc vào giao diện.
          </li>
          <li>
            <b>Bảo vệ thông tin xác thực</b> — mật khẩu được băm một chiều với thuật toán chuyên
            dụng cho mật khẩu; phiên đăng nhập dùng mã truy cập ngắn hạn, mã làm mới lưu trong
            cookie chỉ máy chủ đọc được; khóa tài khoản tạm thời khi đăng nhập sai nhiều lần.
          </li>
          <li>
            <b>Bảo vệ tài liệu nhạy cảm</b> — hồ sơ y tế và tài liệu chứng minh được lưu ở khu vực
            riêng, chỉ truy cập được qua liên kết có kiểm soát quyền và có thời hạn, không phát tán
            đường dẫn công khai.
          </li>
          <li>
            <b>Giảm thiểu dữ liệu</b> — chỉ thu thập trường dữ liệu thực sự cần cho nghiệp vụ; số
            căn cước công dân được băm thay vì lưu số gốc.
          </li>
          <li>
            <b>Mã hóa đường truyền</b> — toàn bộ kết nối tới Hệ thống được mã hóa.
          </li>
          <li>
            <b>Nhật ký kiểm toán</b> — ghi lại các thao tác trên dữ liệu quan trọng cùng danh tính
            người thực hiện, phục vụ truy vết trách nhiệm.
          </li>
          <li>
            <b>Sao lưu định kỳ</b> và kiểm tra khả năng khôi phục, nhằm bảo đảm tính sẵn sàng và
            toàn vẹn của dữ liệu.
          </li>
        </Ol>
      </Article>

      <Article no={16} title="Ứng phó sự cố lộ, lọt dữ liệu">
        <Ol>
          <li>
            Mọi cán bộ, nhân viên khi phát hiện hoặc nghi ngờ có lộ, lọt dữ liệu phải báo cáo ngay
            cho DPO và cấp trên trực tiếp <b>trong vòng 02 giờ</b>.
          </li>
          <li>
            Đơn vị có trách nhiệm thông báo cho cơ quan chức năng và các chủ thể dữ liệu bị ảnh
            hưởng <b>trong vòng 72 giờ</b> kể từ khi xác nhận sự cố, theo quy định tại Điều 29 Nghị
            định 356. Với dữ liệu nhạy cảm, dữ liệu sinh trắc học và dữ liệu vị trí, việc thông báo
            là bắt buộc.
          </li>
          <li>
            Ngay khi phát hiện, đơn vị thực hiện các biện pháp khẩn cấp: thu hồi phiên đăng nhập
            nghi ngờ, khóa tài khoản liên quan, bảo toàn nhật ký phục vụ điều tra.
          </li>
        </Ol>
      </Article>

      <Article no={17} title="Trách nhiệm của người sử dụng Hệ thống">
        <Ol>
          <li>
            Không chia sẻ tài khoản, mật khẩu; không để phiên đăng nhập mở trên thiết bị dùng chung
            tại khu vực bếp ăn, phòng y tế hay lớp học.
          </li>
          <li>
            Không sao chụp, in ấn, gửi ra ngoài danh sách người học và thông tin sức khỏe của người
            học ngoài phạm vi công việc được giao.
          </li>
          <li>
            Chỉ chụp ảnh hiện trường phục vụ nghiệp vụ kiểm thực và giao nhận; hạn chế tối đa việc
            đưa hình ảnh nhận diện được người học vào ảnh bằng chứng.
          </li>
          <li>
            Cán bộ, nhân viên vi phạm Chính sách này bị xử lý kỷ luật theo nội quy lao động và quy
            định của pháp luật; nếu gây thiệt hại thì phải bồi thường theo quy định của pháp luật.
          </li>
        </Ol>
      </Article>

      <Article no={18} title="Hậu quả, thiệt hại không mong muốn có thể xảy ra">
        <Ol>
          <li>
            <b>Từ phía chủ thể dữ liệu:</b> dữ liệu cá nhân có thể bị lộ, lọt do bất cẩn hoặc do bị
            lừa đảo, do truy cập website và cài ứng dụng chứa phần mềm độc hại, do chia sẻ tài
            khoản. Chủ thể dữ liệu lưu ý chủ động thực hiện các biện pháp bảo vệ dữ liệu của mình.
          </li>
          <li>
            <b>Từ phía Hệ thống:</b> đơn vị vận hành cam kết áp dụng các biện pháp tại Điều 15 để
            bảo vệ dữ liệu cá nhân. Tuy nhiên vẫn có thể xảy ra sự cố nằm ngoài khả năng kiểm soát
            như lỗi phần cứng, phần mềm, hoặc lỗ hổng bảo mật của thành phần bên thứ ba bị khai
            thác. Khi xảy ra, đơn vị thực hiện quy trình ứng phó tại Điều 16 và chịu trách nhiệm
            theo quy định của pháp luật.
          </li>
        </Ol>
      </Article>

      <Article no={19} title="Thông tin liên lạc, thông báo và sửa đổi">
        <Ol>
          <li>
            Chủ thể dữ liệu gửi yêu cầu, câu hỏi hoặc khiếu nại liên quan đến dữ liệu cá nhân qua:
            <Ul>
              <li>
                Thư điện tử tới DPO: <b>{DPO_EMAIL}</b>.
              </li>
              <li>Tài khoản của mình trên Hệ thống, mục Hồ sơ cá nhân.</li>
              <li>Bộ phận tiếp nhận của nhà trường nơi người học đang theo học.</li>
            </Ul>
          </li>
          <li>
            Chính sách này được cập nhật theo từng thời kỳ. Bản cập nhật được thông báo tới chủ thể
            dữ liệu qua Hệ thống và các kênh liên lạc trước khi áp dụng. Khi nội dung sửa đổi làm
            thay đổi phạm vi hoặc mục đích xử lý dữ liệu, Hệ thống sẽ yêu cầu chủ thể dữ liệu xác
            nhận đồng ý lại thay vì mặc định coi việc tiếp tục sử dụng là chấp thuận.
          </li>
          <li>
            Phiên bản hiện hành và các phiên bản trước được đăng tải công khai tại trang này.
          </li>
        </Ol>
      </Article>

      <Article no={20} title="Điều khoản thi hành">
        <Ol>
          <li>
            Chính sách này có hiệu lực từ ngày {PRIVACY_POLICY_EFFECTIVE_DATE} và áp dụng cho toàn
            bộ hoạt động xử lý dữ liệu cá nhân trên Hệ thống.
          </li>
          <li>
            Đối với dữ liệu đã thu thập trước ngày Chính sách có hiệu lực: các đơn vị tiến hành rà
            soát; nếu thiếu sự đồng ý hợp lệ thì phải bổ sung trong vòng 06 tháng.
          </li>
          <li>
            Các hợp đồng với đối tác thứ ba đang có hiệu lực cần được ký bổ sung Phụ lục bảo vệ dữ
            liệu cá nhân trong lần tái ký tiếp theo.
          </li>
          <li>
            Định kỳ 01 năm/lần, DPO phối hợp với bộ phận công nghệ thông tin và bộ phận pháp chế
            đánh giá việc tuân thủ Chính sách này.
          </li>
        </Ol>
      </Article>

      <footer className="mt-10 rounded-lg border bg-muted/40 p-4 text-sm print:hidden">
        <p className="font-medium">Tài liệu kèm theo</p>
        <p className="mt-1 text-muted-foreground">
          <Link href="/privacy/consent" className="font-medium text-primary hover:underline">
            Mẫu văn bản đồng ý cho phép thu thập, xử lý dữ liệu cá nhân của người học
          </Link>{" "}
          — dùng khi nhà trường cần bản có chữ ký của cha mẹ hoặc người giám hộ hợp pháp.
        </p>
      </footer>
    </article>
  );
}

/* ── Khối trình bày ──────────────────────────────────────────────────────────────────────
 * Để cục bộ trong file thay vì đưa ra `components/ui`: đây là kiểu chữ riêng của văn bản
 * pháp lý (chương/điều/khoản), không màn nào khác dùng lại.
 */

function Chapter({ no, title }: { no: string; title: string }) {
  return (
    <h2 className="mt-10 rounded-lg bg-primary px-4 py-2.5 text-sm font-bold uppercase tracking-wide text-primary-foreground print:bg-transparent print:px-0 print:text-foreground">
      Chương {no}: {title}
    </h2>
  );
}

function Article({
  no,
  title,
  children,
}: {
  no: number;
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section id={`dieu-${no}`} className="mt-6 rounded-lg border p-4 sm:p-5 print:border-none print:p-0">
      <h3 className="text-base font-semibold">
        <span className="mr-2 rounded bg-primary-soft px-2 py-0.5 text-sm text-primary print:bg-transparent print:px-0">
          Điều {no}
        </span>
        {title}
      </h3>
      <div className="mt-3 space-y-3 text-sm leading-relaxed">{children}</div>
    </section>
  );
}

function P({ children }: { children: React.ReactNode }) {
  return <p>{children}</p>;
}

function H4({ children }: { children: React.ReactNode }) {
  return <p className="pt-1 font-medium">{children}</p>;
}

function Ol({ children }: { children: React.ReactNode }) {
  return <ol className="list-decimal space-y-2 pl-5 marker:font-medium">{children}</ol>;
}

function Ul({ children }: { children: React.ReactNode }) {
  return <ul className="mt-2 list-disc space-y-1.5 pl-5">{children}</ul>;
}
