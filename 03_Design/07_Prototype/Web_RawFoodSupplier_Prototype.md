# Prototype — Web App — Raw Food Supplier

**Link công cụ**: [Stitch Web Project](https://stitch.withgoogle.com/projects/17295592011637202401)
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
    Dash["Supplier Dashboard (Web) ✅"]

    Dash --> PartnerD["Partnership Detail ✅"]
    PartnerD --> PartnerApp["Supplier Partnership Application Form ✅"]
    PartnerApp --> VD["Verification Documents Upload ✅"]
    PartnerD --> PartnerHist["Partnership Approval History (read-only) ✅"]

    Dash --> PRList["Purchase Request List ✅"]
    PRList --> PRDetail["Purchase Request Detail ✅"]
    PRDetail --> PRResponse["Confirm Quantity & Delivery Date ✅"]

    Dash --> DelMgmt["Delivery Management ✅"]
    DelMgmt --> ShipForm["Raw Material Shipment Form ✅"]
    ShipForm --> ShipDocs["Attach Delivery Documents ✅"]
    ShipForm --> ShipTrack["Shipment Tracking / Status ✅"]
    ShipTrack -.->|"received by Caterer"| CatererRecv["→ Service Caterers:\nReceive Ingredients Workspace ⬜"]

    Dash --> CompRec["Food Safety Compliance Record ✅"]
    CompRec --> FSCert["Food Safety Certificate Detail ✅"]
    CompRec --> OriginCert["Product Origin Certification Detail ✅"]
    CompRec --> UploadCert["Upload Certificates Form ✅"]

    Dash --> BatchList["Ingredient Batches List ✅"]
    BatchList --> BatchDetail["Ingredient Batch Detail ✅"]
    BatchDetail --> UploadCert["Upload Certificates Form ✅"]

    Dash --> Prof["Profile Management ⬜"]
```

## Ghi chú review
*(Sẽ cập nhật sau khi chạy qua checklist 00_Review_Checklist.md)*
