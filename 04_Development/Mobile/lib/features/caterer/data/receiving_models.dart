/// Nhận nguyên liệu (MF06 — UC-044/060/072/123). Đối chiếu backend
/// `com.sfms.mf06.dto.{DeliveryDtos,ReceivingDtos,PurchaseRequestDtos,SupplierBatchDtos}`
/// và `04_Development/Frontend/src/app/(app)/service-caterers/receiving/`.

/// `DeliveryResponse` — chuyến giao của NCC tới trường.
class Delivery {
  final String id;
  final String? purchaseRequestId;
  final String? scheduledAt;
  final String? driverId;
  final String? vehiclePlate;
  final String? status; // SCHEDULED | DELIVERED | COMPLETED...
  final int batches;

  Delivery({
    required this.id,
    required this.purchaseRequestId,
    required this.scheduledAt,
    required this.driverId,
    required this.vehiclePlate,
    required this.status,
    required this.batches,
  });

  factory Delivery.fromJson(Map<String, dynamic> json) => Delivery(
        id: json['id'] as String? ?? '',
        purchaseRequestId: json['purchaseRequestId'] as String?,
        scheduledAt: json['scheduledAt'] as String?,
        driverId: json['driverId'] as String?,
        vehiclePlate: json['vehiclePlate'] as String?,
        status: json['status'] as String?,
        batches: (json['batches'] as num?)?.toInt() ?? 0,
      );
}

/// `DeliveryPersonResponse` — tài xế giao hàng.
class DeliveryPerson {
  final String id;
  final String? fullName;
  final String? vehiclePlate;
  final String? biometricStatus;

  DeliveryPerson({required this.id, required this.fullName, required this.vehiclePlate, required this.biometricStatus});

  factory DeliveryPerson.fromJson(Map<String, dynamic> json) => DeliveryPerson(
        id: json['id'] as String? ?? '',
        fullName: json['fullName'] as String?,
        vehiclePlate: json['vehiclePlate'] as String?,
        biometricStatus: json['biometricStatus'] as String?,
      );
}

/// `PurchaseRequestResponse.ItemResponse` — dòng nguyên liệu của đơn đặt hàng,
/// đã được NCC gán lô (`supplierBatchId`) khi xác nhận lịch giao (BR-071).
class PurchaseRequestItem {
  final String ingredientId;
  final String? ingredient;
  final String? supplierBatchId;

  PurchaseRequestItem({required this.ingredientId, required this.ingredient, required this.supplierBatchId});

  factory PurchaseRequestItem.fromJson(Map<String, dynamic> json) => PurchaseRequestItem(
        ingredientId: json['ingredientId'] as String? ?? '',
        ingredient: json['ingredient'] as String?,
        supplierBatchId: json['supplierBatchId'] as String?,
      );
}

class PurchaseRequestDetail {
  final String id;
  final String? supplier;
  final List<PurchaseRequestItem> items;

  PurchaseRequestDetail({required this.id, required this.supplier, required this.items});

  factory PurchaseRequestDetail.fromJson(Map<String, dynamic> json) => PurchaseRequestDetail(
        id: json['id'] as String? ?? '',
        supplier: json['supplier'] as String?,
        items: ((json['items'] as List?) ?? const [])
            .map((e) => PurchaseRequestItem.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// `SupplierBatchResponse` — lô hàng NCC đã đăng ký (HSD, NSX, số lượng…).
class SupplierBatch {
  final String id;
  final String? ingredient;
  final String? lotNumber;
  final String? manufacturingDate;
  final String? expirationDate;
  final double? quantity;
  final String? unit;

  SupplierBatch({
    required this.id,
    required this.ingredient,
    required this.lotNumber,
    required this.manufacturingDate,
    required this.expirationDate,
    required this.quantity,
    required this.unit,
  });

  factory SupplierBatch.fromJson(Map<String, dynamic> json) => SupplierBatch(
        id: json['id'] as String? ?? '',
        ingredient: json['ingredient'] as String?,
        lotNumber: json['lotNumber'] as String?,
        manufacturingDate: json['manufacturingDate'] as String?,
        expirationDate: json['expirationDate'] as String?,
        quantity: (json['quantity'] as num?)?.toDouble(),
        unit: json['unit'] as String?,
      );

  bool get isExpired {
    if (expirationDate == null) return false;
    final exp = DateTime.tryParse(expirationDate!);
    if (exp == null) return false;
    return exp.isBefore(DateTime.now());
  }
}

/// Một dòng đối chiếu lô hàng trong phiên nhận đang soạn (state cục bộ, chưa
/// gửi lên server — gộp lại `ReceivingLineRequest` lúc submit).
class BatchReview {
  final String ingredientId;
  final String ingredient;
  final String batchId;
  String storageZone; // Dry | Chilled | Frozen (UI label — gửi UPPERCASE)
  bool rejected;
  String rejectReason;
  final List<String> rejectPhotoPaths = [];

  BatchReview({
    required this.ingredientId,
    required this.ingredient,
    required this.batchId,
    this.storageZone = 'Chilled',
    this.rejected = false,
    this.rejectReason = '',
  });
}

/// `ReceivingSessionResponse` — phiên nhận hàng đã hoàn tất.
class ReceivingSessionLine {
  final String? ingredient;
  final String? batchId;
  final bool rejected;
  final String? rejectReason;

  ReceivingSessionLine({required this.ingredient, required this.batchId, required this.rejected, required this.rejectReason});

  factory ReceivingSessionLine.fromJson(Map<String, dynamic> json) => ReceivingSessionLine(
        ingredient: json['ingredient'] as String?,
        batchId: json['batchId'] as String?,
        rejected: json['rejected'] as bool? ?? false,
        rejectReason: json['rejectReason'] as String?,
      );
}

class ReceivingSession {
  final String id;
  final String? supplier;
  final String? supplierStatus;
  final String? receivedAt;
  final int items;
  final List<String> batches;
  final String? status;
  final bool gpsVerified;
  final int photosCount;
  final String? receivedBy;
  final List<ReceivingSessionLine> lines;

  ReceivingSession({
    required this.id,
    required this.supplier,
    required this.supplierStatus,
    required this.receivedAt,
    required this.items,
    required this.batches,
    required this.status,
    required this.gpsVerified,
    required this.photosCount,
    required this.receivedBy,
    required this.lines,
  });

  factory ReceivingSession.fromJson(Map<String, dynamic> json) => ReceivingSession(
        id: json['id'] as String? ?? '',
        supplier: json['supplier'] as String?,
        supplierStatus: json['supplierStatus'] as String?,
        receivedAt: json['receivedAt'] as String?,
        items: (json['items'] as num?)?.toInt() ?? 0,
        batches: (json['batches'] as List?)?.cast<String>() ?? const [],
        status: json['status'] as String?,
        gpsVerified: json['gpsVerified'] as bool? ?? false,
        photosCount: (json['photosCount'] as num?)?.toInt() ?? 0,
        receivedBy: json['receivedBy'] as String?,
        lines: ((json['lines'] as List?) ?? const [])
            .map((e) => ReceivingSessionLine.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}
