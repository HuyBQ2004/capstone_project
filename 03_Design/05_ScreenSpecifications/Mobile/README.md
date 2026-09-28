# Mobile/ — Screen Specifications (Mobile, Flutter/Material3)

Chứa Screen Specification cho **6 nhóm actor** (không có SystemAdmin/SchoolAdministration — theo ADR-001, 2 actor này chỉ Web):

```
ServiceCaterers/  MedicalStaff/  Parent/  RawFoodSupplier/  Teacher/  GuestCommon/
```

Mỗi file trong đây phải **trùng số thứ tự + trùng tên** với file tương ứng bên `../Web/<CùngActor>/` (theo `Naming_Convention.md` §2) — đây là cặp Web/Mobile của cùng 1 màn hình/UC.

Quy tắc đặt tên, thứ tự đánh số, và phân công phụ trách: xem `../00_Team_Work_Assignment_Plan.md`.
Danh sách đầy đủ màn hình mỗi actor: xem `../../04_ScreenFlow/14_Screen_Authorization_Matrix.md`.
