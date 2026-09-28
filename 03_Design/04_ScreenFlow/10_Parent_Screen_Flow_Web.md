# Screen Flow — Parent (Web) 🆕

> Platform: Web (Next.js) — bản mới bổ sung 2026-07-01 theo hướng "all web + Mobile song song" (`00_UI_Design_Documentation_Plan.md` mục 0, 5, 8b). Vào sau khi qua [Common Entry Flow](13_Guest_Common_Screen_Flow.md).
> **Nguyên tắc bắt buộc**: dùng chung 100% business logic/UC/BR/MSG với [05_Parent_Screen_Flow.md](05_Parent_Screen_Flow.md) (Mobile) — không thêm chức năng mới.
> **Độ tin cậy**: Trung bình — Parent là actor phụ huynh, phần lớn thao tác (xem menu, phản hồi bữa ăn) vẫn ưu tiên trải nghiệm Mobile trong thực tế; bản Web phù hợp cho phụ huynh muốn xem trên máy tính (ít khẩn cấp hơn cảnh báo dị ứng thời gian thực).

## Sidebar (2 cấp, gọn — theo Navigation_Guidelines.md mục 1.1)

| Nhóm | Màn hình con |
| --- | --- |
| Thực đơn & Dinh dưỡng | Weekly Menu, Nutrition Detail, Nutrition History |
| Sức khỏe & Dị ứng | Student Health Information, Submit/Update Health Information, Submit/Update Allergy Information, Special Meal Arrangements, Special Meal Records |
| Bữa ăn của con | School Meals Dashboard, Meal History, Meal History Detail |
| Phản hồi & Sự cố | Feedback Center, Submit Meal Feedback, Submit Food Quality Feedback, Report Food Safety Concern |
| Cá nhân | Profile, Notification Settings |

## Sơ đồ điều hướng

```mermaid
flowchart TD
    Dash["Parent Dashboard (Web)"]
    Dash --> ChildSel["Child Selector\n(dropdown ở Topbar, không phải Dashboard riêng)"]
    Dash -->|"Allergy Threat banner"| ConflictAlert["Allergy Conflict Alert"]
    Dash --> WeeklyMenu["Weekly Menu"]
    Dash --> HealthHub["Student Health Information"]
    Dash --> SchoolMeals["School Meals Dashboard"]
    Dash --> FeedbackCenter["Feedback Center"]
    Dash --> MealHistLog["Meal History"]
    Dash --> ReportSafety["Report Food Safety Concern"]
    Dash --> Notif["Notifications"]

    HealthHub --> UpdateHealth["Submit/Update Health Information"]
    HealthHub --> AddEditAllergy["Submit/Update Allergy Information"]

    WeeklyMenu -->|"select dish"| DishDetails["Nutrition Detail"]
    WeeklyMenu --> NutritionHistory["Nutrition History"]
    WeeklyMenu -->|"allergy conflict"| ConflictAlert
    DishDetails -->|"Give Feedback"| MealFeedback["Submit Meal Feedback"]

    FeedbackCenter --> MealFeedback
    FeedbackCenter --> FoodQualityFeedback["Submit Food Quality Feedback"]
    FeedbackCenter --> ReportSafety

    MealHistLog --> WeeklyCharts["Nutrition History (charts)"]
    MealHistLog --> SpecialMealHist["Special Meal Records"]
    MealHistLog --> MealHistDetail["Meal History Detail"]

    HealthHub --> SpecialMealArr["Special Meal Arrangements"]

    Dash --> Prof["Profile Management"]
```

## Khác biệt so với bản Mobile

- **Push notification thời gian thực** (cảnh báo dị ứng, thông báo khẩn) vẫn ưu tiên Mobile — bản Web chỉ hiển thị Notification Center khi phụ huynh chủ động đăng nhập, không có push background như app di động (giới hạn kỹ thuật trình duyệt/PWA, cần xác nhận với team nếu có ý định dùng Web Push API).
- **Report Food Safety Concern**: trên Mobile là nút khẩn cấp nổi bật (bypass duyệt theo BR-138); trên Web nên đặt cố định dễ thấy trong sidebar/topbar, không chỉ ẩn trong menu, để giữ đúng tính khẩn cấp.
- **Offline**: không áp dụng (Web luôn cần kết nối).

## Nguồn dữ liệu
- Business logic/UC/BR/MSG: dùng chung 100% với [05_Parent_Screen_Flow.md](05_Parent_Screen_Flow.md).
- Bố cục Web: `00_Foundation/Layout_Guidelines.md`, `00_Foundation/Navigation_Guidelines.md` mục 1.
