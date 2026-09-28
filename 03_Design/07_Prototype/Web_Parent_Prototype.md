# Prototype — Web App — Parent

**Link công cụ**: https://stitch.withgoogle.com/projects/5917795740769680851
**Phạm vi màn hình**: Toàn bộ màn hình trong `06_Wireframes/Web/Parent/`
- Parent Dashboard
- Weekly Menu
- Nutrition Detail
- Nutrition History
- Submit Allergy Information
- Update Allergy Information
- Student Health Information
- Submit Health Information
- Update Health Information
- Special Meal Arrangements
- Daily Meal Details
- Meal Evaluation
- Meal History
- Dietary Summary
- Feedback and Safety Concern Dashboard
- Submit Safety Concern
- Feedback Detail
- School Safety Notifications

**Ngày dựng**: 2026-07-02 | **Trạng thái**: Draft cho Review

## Luồng điều hướng đã hiện thực trong Prototype

```mermaid
flowchart TD
    Dash["⬜ Parent Dashboard (Web)"]
    Dash --> ChildSel["⬜ Child Selector\n(dropdown ở Topbar, không phải Dashboard riêng)"]
    Dash -->|"Allergy Threat banner"| ConflictAlert["⬜ Allergy Conflict Alert"]
    Dash --> WeeklyMenu["⬜ Weekly Menu"]
    Dash --> HealthHub["⬜ Student Health Information"]
    Dash --> SchoolMeals["⬜ School Meals Dashboard"]
    Dash --> FeedbackCenter["⬜ Feedback Center"]
    Dash --> MealHistLog["⬜ Meal History"]
    Dash --> ReportSafety["⬜ Report Food Safety Concern"]
    Dash --> Notif["⬜ Notifications"]

    HealthHub --> UpdateHealth["⬜ Submit/Update Health Information"]
    HealthHub --> AddEditAllergy["⬜ Submit/Update Allergy Information"]

    WeeklyMenu -->|"select dish"| DishDetails["⬜ Nutrition Detail"]
    WeeklyMenu --> NutritionHistory["⬜ Nutrition History"]
    WeeklyMenu -->|"allergy conflict"| ConflictAlert
    DishDetails -->|"Give Feedback"| MealFeedback["⬜ Submit Meal Feedback"]

    FeedbackCenter --> MealFeedback
    FeedbackCenter --> FoodQualityFeedback["⬜ Submit Food Quality Feedback"]
    FeedbackCenter --> ReportSafety

    MealHistLog --> WeeklyCharts["⬜ Nutrition History (charts)"]
    MealHistLog --> SpecialMealHist["⬜ Special Meal Records"]
    MealHistLog --> MealHistDetail["⬜ Meal History Detail"]

    HealthHub --> SpecialMealArr["⬜ Special Meal Arrangements"]

    Dash --> Prof["⬜ Profile Management"]
```

## Ghi chú review
(Sẽ cập nhật tại `07_Prototype/00_Review_Checklist.md`)
