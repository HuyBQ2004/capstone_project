# Quyết định 39/2006/QĐ-BYT — "Quy chế điều tra ngộ độc thực phẩm"

> **Loại tài liệu**: văn bản quy phạm pháp luật (Quyết định của Bộ trưởng Bộ Y tế, ban hành 13/12/2006).
> **Mục đích sử dụng trong dự án**: căn cứ pháp lý cho khung thời gian mặc định **48 giờ** khi Medical Staff chọn "món ăn nghi vấn" để xét nghiệm/cách ly ở `26_IncidentDetail.md` (trạng thái điều tra sự cố ATTP) — xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 10.5.
> **Nguồn tra cứu**: không truy cập được toàn văn Quyết định 39/2006/QĐ-BYT trực tiếp (thuvienphapluat.vn chặn fetch tự động). Nội dung dưới đây trích dẫn qua tài liệu triển khai công khai của Chi cục An toàn vệ sinh thực phẩm tỉnh Quảng Trị (dẫn chiếu trực tiếp Quyết định 39/2006/QĐ-BYT làm tài liệu viện dẫn), cùng phần tóm tắt từ các nguồn tin tức pháp luật khác. Khuyến nghị: nếu cần trích dẫn chính thức trong hồ sơ pháp lý, nên đối chiếu lại bản gốc trên Công báo/Cổng thông tin Bộ Y tế.

## Nội dung liên quan (trích dẫn qua tài liệu triển khai)

Nguồn: "Quy trình điều tra và xử lý ngộ độc thực phẩm" (Chi cục ATVSTP Quảng Trị), mục 3 "Tài liệu viện dẫn" ghi rõ: *"Quyết định số 39/2006/QĐ-BYT ngày 13/12/2006 về việc ban hành 'Quy chế điều tra ngộ độc thực phẩm'"*. Mục 5.2.4 "Điều tra ngộ độc thực phẩm tại thực địa" quy định:

> "Điều tra những người bị ngộ độc về tình hình ăn uống **trong vòng 24 đến 48 giờ**, ăn cùng ai... xác nhận có bữa ăn chung không... theo biểu mẫu BM-QLNĐ-16-04 (tổng hợp từ bước 1; bước 3.1; 3.2 trong quy chế điều tra ngộ độc thực phẩm)."

Các nguồn tin tức pháp luật khác (thuvienphapluat.vn, baochinhphu.vn, nhandan.vn) xác nhận Quyết định 39/2006/QĐ-BYT là văn bản gốc ban hành "Quy chế điều tra ngộ độc thực phẩm", áp dụng cho cơ sở xảy ra ngộ độc, người bị ảnh hưởng, và cơ quan y tế các cấp — quy trình gồm 11 bước điều tra, trong đó có bước xác định "bữa ăn nguyên nhân" và "thức ăn nguyên nhân" dựa trên đối chiếu thời gian ăn uống với thời điểm phát bệnh.

## Đối chiếu với thiết kế SFMS

| Điểm trong quy chế | Ứng dụng trong SFMS |
| --- | --- |
| Điều tra tình hình ăn uống trong vòng 24-48 giờ trước khi phát bệnh | Khi Medical Staff chọn "Món ăn nghi vấn" (multi-select) ở `26_IncidentDetail.md` trạng thái đánh giá, danh sách món **mặc định lọc theo món chế biến trong 48 giờ** tính từ thời điểm biên bản/báo cáo sự cố được tạo |
| Không có mốc cứng tuyệt đối — quy chế chỉ đưa ra khung tham chiếu cho điều tra viên | Bổ sung nút **"Mở rộng phạm vi ngày"** để Medical Staff tự nới rộng ngoài 48h khi đánh giá lâm sàng nghi ngờ tác nhân có thời gian ủ bệnh dài hơn (xem ghi chú y văn quốc tế bên dưới) — không chặn cứng |

## Lưu ý bổ sung (y văn dịch tễ quốc tế, không thuộc văn bản pháp luật Việt Nam)

Nghiên cứu CDC/PMC về thời gian ủ bệnh các vụ ngộ độc thực phẩm tại Mỹ (1998–2013) cho thấy thời gian ủ bệnh trung vị khác nhau đáng kể theo tác nhân: độc tố vi khuẩn có sẵn (Staphylococcus aureus, Bacillus cereus, Clostridium perfringens) trung vị 4–10 giờ; Norovirus/Salmonella/Shigella trung vị 32–45 giờ; Campylobacter và E. coli sinh độc tố Shiga (STEC) trung vị **62–87 giờ** — vượt mốc 48h của quy chế Việt Nam. Đây là lý do thiết kế không khoá cứng 48h mà cho phép mở rộng phạm vi thủ công.

*(Nguồn: "Incubation periods of enteric illnesses in foodborne outbreaks, United States, 1998–2013", PMC6805792.)*
