# Prototype — Web App — Guest & Common

**Link công cụ**: [Stitch Project Link](https://stitch.withgoogle.com/projects/8460166033796048457)
**Phạm vi màn hình**: 
- `01_Login.md`
- `02_ForgotPassword.md`
- `03_ResetPassword.md`
- `04_VerifyOTP.md`
- `05_ProfileManagement.md`
- `06_ChangePassword.md`
- `07_NotificationCenterList.md`
- `08_NotificationDetail.md`
- `09_NotificationSettings.md`

**Ngày dựng**: 2026-07-02 | **Trạng thái**: Draft cho Review

## Luồng điều hướng đã hiện thực trong Prototype

```mermaid
flowchart TD
    LoginWeb["Login (Web)"] -->|"success"| DashboardWeb["Actor Dashboard (Web)\n(System Admin / School Admin / bản Web mới của 5 role Mobile)"]
    LoginWeb -->|"Forgot Password?"| ForgotWeb["Forgot Password (Web)"]
    ForgotWeb --> OTPWeb["Verify OTP (Web)"]
    OTPWeb --> ResetWeb["Reset Password (Web)"]
    ResetWeb --> LoginWeb
    DashboardWeb --> ProfileWeb["Profile dropdown (Topbar)"]
    ProfileWeb --> ChangePwWeb["Change Password (modal)"]
    ProfileWeb --> NotifSettingsWeb["Notification Settings (trang riêng)"]
    DashboardWeb --> NotifCenterWeb["Notification Center\n(dropdown chuông + trang đầy đủ)"]
```

## Ghi chú review
*(Sẽ được điền sau khi hoàn thành UAT và rà soát chéo)*
