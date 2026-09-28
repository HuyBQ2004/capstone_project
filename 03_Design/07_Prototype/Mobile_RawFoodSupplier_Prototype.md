# Prototype — Mobile App — Raw Food Supplier

**Link công cụ**: [Stitch Mobile Project](https://stitch.withgoogle.com/projects/15299343906664661446)
**Phạm vi màn hình**:
- 01_SupplierDashboard
- 02_SelectSchoolWorkspace
- 03_SchoolPartnerships
- 04_ProductOriginDocuments
- 05_FoodSafetyCertificates
- 06_UploadSupplierCertificates
- 07_PartnershipApplication
- 08_ApplicationStatus
- 09_PurchaseRequests
- 10_PurchaseRequestList
- 11_PurchaseRequestDetail
- 12_IngredientBatches
- 13_BatchList
- 14_BatchDetail
- 15_SubmitBatchInformation
- 16_UploadBatchCertificates
- 17_ViewBatchHistory
- 18_Deliveries
- 19_DeliveryList
- 20_DeliveryDetail
- 21_SubmitDeliveryInformation
- 22_ConfirmDeliverySchedule
- 23_UpdateDeliveryStatus
- 24_ViewDeliveryBatchRecords
- 25_UpdateDocuments
- 26_ComplianceDocuments

**Ngày dựng**: 2026-07-02 | **Trạng thái**: Draft cho Review

## Luồng điều hướng đã hiện thực trong Prototype

Dưới đây là sơ đồ luồng điều hướng đã được cấu hình click-through hoàn chỉnh trong dự án Stitch:

```mermaid
flowchart TD
    Dash["Supplier Dashboard ✅"]

    %% Partnership — Workflow 1
    Dash -->|"Partnership status card"| PartnerD["Partnership Detail ✅"]
    PartnerD -->|"submit application"| PartnerApp["Supplier Partnership Application Form ✅"]
    PartnerApp --> VD["Verification Documents Upload\n(Food Safety Certificate, Business License...) ✅"]
    PartnerD --> PartnerHist["Partnership Approval History\n(read-only) ✅"]

    %% Purchase Request — Workflow 2
    Dash -->|"Purchase Request button"| PRList["Purchase Request Screen ✅"]
    PRList -->|"select request"| PRDetail["Purchase Request Detail ✅"]
    PRDetail -->|"Accept/Reject"| PRResponse["Confirm Quantity & Delivery Date ✅"]

    %% Delivery / Shipment — Workflow 2
    Dash -->|"Delivery Management button"| DelMgmt["Delivery Management ✅"]
    DelMgmt -->|"create shipment"| ShipForm["Raw Material Shipment Form ✅"]
    ShipForm -->|"attach docs"| ShipDocs["Attach Delivery Documents\n(invoice, origin cert) ✅"]
    ShipForm -->|"submit"| ShipTrack["Shipment Tracking / Status ✅"]
    ShipTrack -.->|"received by Caterer"| CatererRecv["→ Service Caterers:\nReceive Ingredients Workspace ⬜"]

    %% Compliance — Workflow 1 & 2
    Dash -->|"Food Safety Compliance Record button"| CompRec["Food Safety Compliance Record ✅"]
    CompRec --> FSCert["Food Safety Certificate Detail ✅"]
    CompRec --> OriginCert["Product Origin Certification Detail ✅"]
    CompRec -->|"upload new"| UploadCert["Supplier Upload Certificates Form ✅"]

    %% Ingredient Batches — Workflow 2
    Dash -->|"Ingredient Batches"| BatchList["Ingredient Batches List ✅"]
    BatchList --> BatchDetail["Ingredient Batch Detail ✅"]
    BatchDetail -->|"upload cert for this batch"| UploadCert["Supplier Upload Certificates Form ✅"]
```

## Ghi chú review
*(Sẽ cập nhật sau khi chạy qua checklist 00_Review_Checklist.md)*
