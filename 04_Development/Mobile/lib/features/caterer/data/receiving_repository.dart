import '../../../core/network/api_client.dart';
import 'receiving_models.dart';

/// `deliveries` + `receiving-sessions` + `purchase-requests` + `supplier-batches`
/// + `delivery-persons` (backend MF06). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/receiving/`.
class ReceivingRepository {
  ReceivingRepository._();
  static final ReceivingRepository instance = ReceivingRepository._();

  final _api = ApiClient.instance;

  /// Chuyến giao ĐÃ GIAO (`status=DELIVERED`) — chỉ những chuyến này mở được
  /// phiên nhận hàng.
  Future<List<Delivery>> fetchDeliveredDeliveries() async {
    final json = await _api.get('/deliveries', query: const {'pageSize': '50', 'status': 'DELIVERED'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => Delivery.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<PurchaseRequestDetail> fetchPurchaseRequest(String id) async {
    final json = await _api.get('/purchase-requests/$id');
    return PurchaseRequestDetail.fromJson(json!);
  }

  Future<List<SupplierBatch>> fetchSupplierBatches() async {
    final json = await _api.get('/supplier-batches', query: const {'pageSize': '200'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => SupplierBatch.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<List<ReceivingSession>> fetchHistory({String? dateFrom, String? dateTo}) async {
    final json = await _api.get('/receiving-sessions', query: {
      'pageSize': '100',
      if (dateFrom != null) 'dateFrom': dateFrom,
      if (dateTo != null) 'dateTo': dateTo,
    });
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => ReceivingSession.fromJson(e as Map<String, dynamic>)).toList();
  }

  /// `POST /deliveries/{code}/verify-driver` (multipart, field `image`) —
  /// UC-060 · BR-050/051, FRS thật (DeepFace) so khớp `delivery_persons.face_id`.
  Future<bool> verifyDriverFace(String deliveryCode, String imagePath) async {
    final json = await _api.uploadFile(
      '/deliveries/$deliveryCode/verify-driver',
      fieldName: 'image',
      filePath: imagePath,
    );
    return json?['verified'] as bool? ?? false;
  }

  /// `POST /receiving-sessions/reject-documents` (multipart field `file`) —
  /// trả `documentId` dùng cho `rejectDocumentIds` khi gửi phiên chính.
  Future<String> uploadRejectDocument(String filePath) async {
    final json = await _api.uploadFile('/receiving-sessions/reject-documents', fieldName: 'file', filePath: filePath);
    return json?['documentId'] as String? ?? '';
  }

  /// `POST /receiving-sessions` — ADR-M06-5 (một transaction atomic): BR-070
  /// (NCC Approved), BR-072 (ảnh), BR-073/074 (GPS + geofence).
  Future<ReceivingSession> createSession({
    required String deliveryId,
    required double lat,
    required double lng,
    required int photosCount,
    List<String>? evidencePhotoIds,
    required List<Map<String, dynamic>> lines,
  }) async {
    final json = await _api.post('/receiving-sessions', body: {
      'deliveryId': deliveryId,
      'lat': lat,
      'lng': lng,
      'photosCount': photosCount,
      if (evidencePhotoIds != null) 'evidencePhotoIds': evidencePhotoIds,
      'lines': lines,
    });
    return ReceivingSession.fromJson(json!);
  }
}
