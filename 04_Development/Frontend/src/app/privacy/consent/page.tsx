"use client";

import Link from "next/link";
import { ArrowLeft, Printer } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  PDPL_DECREE,
  PDPL_LAW,
  PRIVACY_POLICY_EFFECTIVE_DATE,
  PRIVACY_POLICY_VERSION,
} from "@/lib/privacy";

/**
 * Mẫu văn bản đồng ý cho phép thu thập, xử lý dữ liệu cá nhân của người học.
 *
 * <p><b>Vì sao cần bản in chứ không chỉ ô tích trên phần mềm.</b> Điều 8 Chính sách (theo Điều 24
 * Luật 91) yêu cầu với người học từ đủ 07 đến dưới 16 tuổi phải có sự đồng ý của CẢ người học lẫn
 * cha mẹ. Ô tích trong tài khoản phụ huynh chỉ chứng minh được một nửa vế đó; phần ý kiến của
 * người học cần chữ ký trên giấy. Ngoài ra nhiều trường vẫn lưu hồ sơ nhập học bản cứng, và khi
 * cơ quan quản lý kiểm tra thì bản có chữ ký là thứ xuất trình được ngay.
 *
 * <p><b>Cách dùng.</b> Nhà trường in mẫu này kèm hồ sơ nhập học hoặc phát đầu năm học, thu lại
 * bản có chữ ký và lưu cùng hồ sơ người học. Kết quả tích chọn của cha mẹ được nhập vào Hệ thống
 * ở màn khai báo dị ứng/bệnh lý tương ứng.
 *
 * <p>Mẫu CỐ Ý để trống mọi thông tin định danh — đây là biểu mẫu trắng dùng chung, không sinh ra
 * từ dữ liệu của một người học cụ thể. Điền sẵn tên và tình trạng sức khỏe của trẻ vào một trang
 * công khai không cần đăng nhập thì chính việc in mẫu đã là một sự cố lộ dữ liệu.
 */
export default function ConsentFormPage() {
  return (
    <>
      <div className="mb-4 flex flex-wrap items-center gap-3 print:hidden">
        <Link href="/privacy">
          <Button variant="outline" size="sm">
            <ArrowLeft /> Về Chính sách
          </Button>
        </Link>
        <Button size="sm" onClick={() => window.print()}>
          <Printer /> In mẫu
        </Button>
        <p className="text-xs text-muted-foreground">
          Biểu mẫu trắng — nhà trường in, phát cho gia đình ký và lưu cùng hồ sơ người học.
        </p>
      </div>

      <article className="rounded-xl border bg-background p-6 text-sm leading-relaxed shadow-sm print:border-none print:p-0 print:shadow-none sm:p-10">
        <header className="text-center">
          <p className="font-bold uppercase">Cộng hòa xã hội chủ nghĩa Việt Nam</p>
          <p className="font-semibold">Độc lập – Tự do – Hạnh phúc</p>
          <p className="mx-auto mt-1 w-40 border-b" />
          <h1 className="mt-6 text-lg font-bold uppercase">
            Văn bản đồng ý cho phép thu thập, xử lý dữ liệu cá nhân
          </h1>
          <p className="mt-1 text-muted-foreground">(Dành cho người học và cha mẹ, người giám hộ)</p>
        </header>

        <p className="mt-6">
          <b>Kính gửi:</b> Ban Giám hiệu Trường
          <Blank className="w-[22rem]" />
        </p>

        <p className="mt-3">Căn cứ:</p>
        <ul className="list-disc space-y-1 pl-6">
          <li>{PDPL_LAW};</li>
          <li>{PDPL_DECREE};</li>
          <li>
            Chính sách bảo vệ dữ liệu cá nhân của Hệ thống hỗ trợ quản lý an toàn thực phẩm học
            đường (SFMS), phiên bản {PRIVACY_POLICY_VERSION}, hiệu lực từ ngày{" "}
            {PRIVACY_POLICY_EFFECTIVE_DATE}.
          </li>
        </ul>

        <Section title="I. Thông tin người đồng ý">
          <Field label="Họ và tên" />
          <div className="flex flex-wrap gap-x-8">
            <Field label="Năm sinh" className="w-56" />
            <Field label="Số CCCD" className="w-64" />
          </div>
          <Field label="Quan hệ với người học (cha / mẹ / người giám hộ hợp pháp)" />
          <div className="flex flex-wrap gap-x-8">
            <Field label="Số điện thoại" className="w-64" />
            <Field label="Thư điện tử" className="w-72" />
          </div>
        </Section>

        <Section title="II. Thông tin người học">
          <Field label="Họ và tên" />
          <div className="flex flex-wrap gap-x-8">
            <Field label="Ngày sinh" className="w-56" />
            <Field label="Lớp" className="w-40" />
            <Field label="Mã người học" className="w-56" />
          </div>
        </Section>

        <Section title="III. Nội dung đồng ý">
          <p>
            Sau khi được thông báo đầy đủ và đã đọc Chính sách bảo vệ dữ liệu cá nhân nêu trên, tôi
            cho ý kiến về từng hoạt động xử lý dữ liệu như sau (đánh dấu <b>X</b> vào ô tương ứng):
          </p>
          <div className="mt-3 overflow-x-auto">
            <table className="w-full border-collapse">
              <thead>
                <tr className="bg-muted/60 print:bg-transparent">
                  <th className="w-8 border px-2 py-2 text-center font-semibold">TT</th>
                  <th className="border px-3 py-2 text-left font-semibold">
                    Hoạt động xử lý dữ liệu và mục đích
                  </th>
                  <th className="w-20 border px-2 py-2 text-center font-semibold">Đồng ý</th>
                  <th className="w-24 border px-2 py-2 text-center font-semibold">Không đồng ý</th>
                </tr>
              </thead>
              <tbody>
                <ConsentRow
                  no={1}
                  required
                  title="Dữ liệu định danh cơ bản của người học"
                  detail="Họ tên, ngày sinh, lớp, trường — để lập danh sách suất ăn và bàn giao đúng người. Không có dữ liệu này thì không thể cung cấp dịch vụ bữa ăn qua Hệ thống."
                />
                <ConsentRow
                  no={2}
                  title="Khai báo dị ứng thực phẩm và bệnh lý mạn tính"
                  detail="Để đối chiếu với thành phần món ăn, cảnh báo trước khi duyệt thực đơn và chuẩn bị suất ăn riêng. Không đồng ý thì Hệ thống không cảnh báo được nguy cơ dị ứng cho suất ăn của con tôi."
                />
                <ConsentRow
                  no={3}
                  title="Chỉ số thể chất và hồ sơ y tế đính kèm"
                  detail="Chiều cao, cân nặng, nhóm máu, giấy tờ y tế chứng minh tình trạng dị ứng, bệnh lý — để theo dõi dinh dưỡng và xác minh nhu cầu ăn đặc biệt."
                />
                <ConsentRow
                  no={4}
                  required
                  title="Xử lý dữ liệu khi có sự cố an toàn thực phẩm"
                  detail="Ghi nhận triệu chứng, diễn biến xử trí và cung cấp cho cơ sở y tế, cơ quan quản lý nhà nước khi cần điều tra nguyên nhân. Đây là hoạt động bảo vệ tính mạng, sức khỏe theo quy định của pháp luật."
                />
                <ConsentRow
                  no={5}
                  title="Nhận thông báo về bữa ăn và sự cố"
                  detail="Gửi thông báo qua thư điện tử, tin nhắn hoặc thông báo đẩy trên ứng dụng tới số điện thoại và địa chỉ thư điện tử tôi cung cấp."
                />
                <ConsentRow
                  no={6}
                  title="Lưu trữ, xử lý dữ liệu trên hạ tầng đặt ngoài lãnh thổ Việt Nam"
                  detail="Hệ thống sử dụng dịch vụ điện toán đám mây của nhà cung cấp nước ngoài; dữ liệu được mã hóa khi truyền và bên nhận có cam kết bảo vệ tương đương (Điều 12 Chính sách)."
                />
              </tbody>
            </table>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">
            Mục đánh dấu <b>(*)</b> là điều kiện để nhà trường cung cấp dịch vụ bữa ăn học đường
            qua Hệ thống; nếu không đồng ý, gia đình vui lòng làm việc trực tiếp với nhà trường về
            phương án thay thế.
          </p>
        </Section>

        <Section title="IV. Cam kết và ghi nhận quyền">
          <ol className="list-decimal space-y-2 pl-5">
            <li>
              Tôi xác nhận đã được thông báo rõ về loại dữ liệu được thu thập, mục đích xử lý, các
              bên được chia sẻ dữ liệu và thời hạn lưu trữ.
            </li>
            <li>
              Tôi cam kết các thông tin khai báo, đặc biệt là thông tin dị ứng và bệnh lý, là đúng
              sự thật và sẽ cập nhật kịp thời khi có thay đổi.
            </li>
            <li>
              Tôi được biết mình có quyền xem, chỉnh sửa, yêu cầu xóa dữ liệu, rút lại sự đồng ý,
              hạn chế hoặc phản đối việc xử lý dữ liệu bất cứ lúc nào; việc rút lại sự đồng ý không
              ảnh hưởng đến tính hợp pháp của việc xử lý trước đó.
            </li>
            <li>
              Tôi được biết yêu cầu của mình sẽ được phản hồi trong 02 ngày làm việc và hoàn thành
              theo thời hạn tại Điều 9 Chính sách.
            </li>
          </ol>
        </Section>

        <Section title="V. Ý kiến của người học từ đủ 07 tuổi đến dưới 16 tuổi">
          <p>
            Theo Điều 24 Luật 91, hoạt động xử lý dữ liệu của người học trong độ tuổi này phải có
            sự đồng ý của cả người học và cha mẹ, người giám hộ hợp pháp. Người học từ đủ 16 tuổi
            trở lên tự quyết định và ký tại mục này thay cho phần trên.
          </p>
          <p className="mt-3">
            Em đã được giải thích và <b>đồng ý</b> cho nhà trường thu thập, sử dụng thông tin của
            em cho các mục đích nêu trên:
            <Blank className="w-40" />
            <span className="ml-2 text-muted-foreground">(ghi rõ &ldquo;Đồng ý&rdquo;)</span>
          </p>
        </Section>

        <p className="mt-8 text-right italic">
          ……………, ngày …… tháng …… năm 20……
        </p>
        <div className="mt-4 grid grid-cols-2 gap-8 text-center">
          <div>
            <p className="font-semibold">Người học</p>
            <p className="text-xs text-muted-foreground">(ký, ghi rõ họ tên)</p>
            <div className="h-24" />
          </div>
          <div>
            <p className="font-semibold">Cha mẹ / Người giám hộ hợp pháp</p>
            <p className="text-xs text-muted-foreground">(ký, ghi rõ họ tên)</p>
            <div className="h-24" />
          </div>
        </div>

        <p className="mt-8 border-t pt-3 text-xs text-muted-foreground">
          Bản đồng ý này được lưu cùng hồ sơ người học tại nhà trường. Mọi yêu cầu liên quan đến dữ
          liệu cá nhân gửi về cán bộ bảo vệ dữ liệu (DPO) của nhà trường theo Điều 19 Chính sách.
        </p>
      </article>
    </>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="mt-6">
      <h2 className="font-bold uppercase">{title}</h2>
      <div className="mt-2 space-y-2">{children}</div>
    </section>
  );
}

/** Dòng kẻ để viết tay — bản in cần chỗ trống thật, không phải ô nhập liệu. */
function Blank({ className = "w-64" }: { className?: string }) {
  return <span className={`ml-2 inline-block border-b border-dotted align-bottom ${className}`} />;
}

function Field({ label, className }: { label: string; className?: string }) {
  return (
    <p className={className}>
      {label}:<Blank className={className ? "w-24" : "w-[26rem]"} />
    </p>
  );
}

function ConsentRow({
  no,
  title,
  detail,
  required,
}: {
  no: number;
  title: string;
  detail: string;
  required?: boolean;
}) {
  return (
    <tr>
      <td className="border px-2 py-2 text-center align-top">{no}</td>
      <td className="border px-3 py-2 align-top">
        <p className="font-medium">
          {title}
          {required && <span className="text-destructive"> (*)</span>}
        </p>
        <p className="mt-0.5 text-xs text-muted-foreground">{detail}</p>
      </td>
      <td className="border px-2 py-2 align-top" />
      <td className="border px-2 py-2 align-top" />
    </tr>
  );
}
