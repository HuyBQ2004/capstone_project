/// Kiểm thực ba bước (MF07 — UC-052/UC-065/UC-124). Đối chiếu backend
/// `com.sfms.mf07.dto.InspectionDtos` và
/// `04_Development/Frontend/src/app/(app)/service-caterers/inspections/`.
/// Giữ NGUYÊN tên field JSON của backend (D0 chống lệch FE, comment gốc trong
/// `InspectionDtos.java`).

const Map<String, String> mealTypeLabel = {
  'BREAKFAST': 'Bữa sáng',
  'LUNCH': 'Bữa trưa',
  'SNACK': 'Bữa chiều',
};

String stepStatusLabel(String? s) {
  switch (s) {
    case 'PASSED':
      return 'Đạt';
    case 'FAILED':
      return 'Không đạt';
    default:
      return 'Chưa kiểm';
  }
}

/// `Step1FreshRowDto` — Mục I (Mẫu số 1): thực phẩm tươi sống/đông lạnh.
class Step1FreshRow {
  String name;
  String? batchId;
  String receivedAt;
  String quantity;
  String supplierName;
  String supplierContact;
  String deliverer;
  bool hasInvoice;
  bool hasVetCert;
  bool hasQuarantineCert;
  bool sensoryPass;
  String quickTest; // NONE | PASS | FAIL
  String action;
  int photoCount;

  Step1FreshRow({
    this.name = '',
    this.batchId,
    this.receivedAt = '',
    this.quantity = '',
    this.supplierName = '',
    this.supplierContact = '',
    this.deliverer = '',
    this.hasInvoice = false,
    this.hasVetCert = false,
    this.hasQuarantineCert = false,
    this.sensoryPass = true,
    this.quickTest = 'NONE',
    this.action = '',
    this.photoCount = 0,
  });

  bool get fails => !sensoryPass || quickTest == 'FAIL';

  factory Step1FreshRow.fromJson(Map<String, dynamic> json) => Step1FreshRow(
        name: json['name'] as String? ?? '',
        batchId: json['batchId'] as String?,
        receivedAt: json['receivedAt'] as String? ?? '',
        quantity: json['quantity'] as String? ?? '',
        supplierName: json['supplierName'] as String? ?? '',
        supplierContact: json['supplierContact'] as String? ?? '',
        deliverer: json['deliverer'] as String? ?? '',
        hasInvoice: json['hasInvoice'] as bool? ?? false,
        hasVetCert: json['hasVetCert'] as bool? ?? false,
        hasQuarantineCert: json['hasQuarantineCert'] as bool? ?? false,
        sensoryPass: json['sensoryPass'] as bool? ?? true,
        quickTest: json['quickTest'] as String? ?? 'NONE',
        action: json['action'] as String? ?? '',
        photoCount: (json['photoCount'] as num?)?.toInt() ?? 0,
      );

  Map<String, dynamic> toJson() => {
        'name': name,
        if (batchId != null) 'batchId': batchId,
        'receivedAt': receivedAt,
        'quantity': quantity,
        'supplierName': supplierName,
        'supplierContact': supplierContact,
        'deliverer': deliverer,
        'hasInvoice': hasInvoice,
        'hasVetCert': hasVetCert,
        'hasQuarantineCert': hasQuarantineCert,
        'sensoryPass': sensoryPass,
        'quickTest': quickTest,
        'action': action,
        'photoCount': photoCount,
      };
}

/// `Step1DryRowDto` — Mục II (Mẫu số 1): thực phẩm khô/bao gói sẵn.
class Step1DryRow {
  String name;
  String? batchId;
  String producer;
  String producerAddress;
  String receivedAt;
  String quantity;
  String supplierName;
  String deliverer;
  String supplierContact;
  String expiry;
  String storage; // Thường | Lạnh | Đông
  bool hasInvoice;
  bool sensoryPass;
  String action;
  int photoCount;

  Step1DryRow({
    this.name = '',
    this.batchId,
    this.producer = '',
    this.producerAddress = '',
    this.receivedAt = '',
    this.quantity = '',
    this.supplierName = '',
    this.deliverer = '',
    this.supplierContact = '',
    this.expiry = '',
    this.storage = 'Thường',
    this.hasInvoice = false,
    this.sensoryPass = true,
    this.action = '',
    this.photoCount = 0,
  });

  bool get fails => !sensoryPass;

  factory Step1DryRow.fromJson(Map<String, dynamic> json) => Step1DryRow(
        name: json['name'] as String? ?? '',
        batchId: json['batchId'] as String?,
        producer: json['producer'] as String? ?? '',
        producerAddress: json['producerAddress'] as String? ?? '',
        receivedAt: json['receivedAt'] as String? ?? '',
        quantity: json['quantity'] as String? ?? '',
        supplierName: json['supplierName'] as String? ?? '',
        deliverer: json['deliverer'] as String? ?? '',
        supplierContact: json['supplierContact'] as String? ?? '',
        expiry: json['expiry'] as String? ?? '',
        storage: json['storage'] as String? ?? 'Thường',
        hasInvoice: json['hasInvoice'] as bool? ?? false,
        sensoryPass: json['sensoryPass'] as bool? ?? true,
        action: json['action'] as String? ?? '',
        photoCount: (json['photoCount'] as num?)?.toInt() ?? 0,
      );

  Map<String, dynamic> toJson() => {
        'name': name,
        if (batchId != null) 'batchId': batchId,
        'producer': producer,
        'producerAddress': producerAddress,
        'receivedAt': receivedAt,
        'quantity': quantity,
        'supplierName': supplierName,
        'deliverer': deliverer,
        'supplierContact': supplierContact,
        'expiry': expiry,
        'storage': storage,
        'hasInvoice': hasInvoice,
        'sensoryPass': sensoryPass,
        'action': action,
        'photoCount': photoCount,
      };
}

/// `Step2RowDto` — Mẫu số 2: kiểm tra khi chế biến (Điều 4), theo TỪNG MÓN.
class Step2Row {
  String shift;
  String? dishId;
  String dishName;
  String mainIngredients;
  String servings;
  String prepDoneAt;
  String cookDoneAt;
  bool hygieneStaff;
  bool hygieneEquipment;
  bool hygieneArea;
  bool sensoryPass;
  String action;
  int photoCount;

  Step2Row({
    this.shift = '',
    this.dishId,
    this.dishName = '',
    this.mainIngredients = '',
    this.servings = '',
    this.prepDoneAt = '',
    this.cookDoneAt = '',
    this.hygieneStaff = true,
    this.hygieneEquipment = true,
    this.hygieneArea = true,
    this.sensoryPass = true,
    this.action = '',
    this.photoCount = 0,
  });

  bool get fails => !sensoryPass || !hygieneStaff || !hygieneEquipment || !hygieneArea;

  factory Step2Row.fromJson(Map<String, dynamic> json) => Step2Row(
        shift: json['shift'] as String? ?? '',
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String? ?? '',
        mainIngredients: json['mainIngredients'] as String? ?? '',
        servings: json['servings'] as String? ?? '',
        prepDoneAt: json['prepDoneAt'] as String? ?? '',
        cookDoneAt: json['cookDoneAt'] as String? ?? '',
        hygieneStaff: json['hygieneStaff'] as bool? ?? true,
        hygieneEquipment: json['hygieneEquipment'] as bool? ?? true,
        hygieneArea: json['hygieneArea'] as bool? ?? true,
        sensoryPass: json['sensoryPass'] as bool? ?? true,
        action: json['action'] as String? ?? '',
        photoCount: (json['photoCount'] as num?)?.toInt() ?? 0,
      );

  Map<String, dynamic> toJson() => {
        'shift': shift,
        if (dishId != null) 'dishId': dishId,
        'dishName': dishName,
        'mainIngredients': mainIngredients,
        'servings': servings,
        'prepDoneAt': prepDoneAt,
        'cookDoneAt': cookDoneAt,
        'hygieneStaff': hygieneStaff,
        'hygieneEquipment': hygieneEquipment,
        'hygieneArea': hygieneArea,
        'sensoryPass': sensoryPass,
        'action': action,
        'photoCount': photoCount,
      };
}

/// `Step3RowDto` — Mẫu số 3: kiểm tra trước khi ăn (Điều 5), theo TỪNG MÓN.
class Step3Row {
  String shift;
  String? dishId;
  String dishName;
  String servings;
  String dividedDoneAt;
  String eatStartAt;

  /// Mẫu 3 cột 7 — dụng cụ chia/chứa đựng/che đậy **ĐẠT** yêu cầu vệ sinh hay không.
  ///
  /// Trước 09/08/2026 mobile chỉ có [utensils] (ô chữ bắt buộc, gõ tên dụng cụ). Đó vừa sai
  /// biểu mẫu — cột 7 là mục ĐÁNH GIÁ đạt/không đạt, không phải ô liệt kê — vừa làm **hỏng
  /// hẳn việc nộp bước 3**: backend thêm `utensilsPass` (V56) là kiểu nguyên thuỷ, thiếu nó
  /// thì `POST /inspections/{code}/steps/3` trả **422 "malformed request body"** (đo thật).
  bool utensilsPass;

  /// Ghi chú kèm cột 7 — **tuỳ chọn**: loại dụng cụ, hoặc lý do khi Không đạt.
  String utensils;
  bool sensoryPass;
  String action;
  int photoCount;

  Step3Row({
    this.shift = '',
    this.dishId,
    this.dishName = '',
    this.servings = '',
    this.dividedDoneAt = '',
    this.eatStartAt = '',
    this.utensilsPass = true,
    this.utensils = '',
    this.sensoryPass = true,
    this.action = '',
    this.photoCount = 0,
  });

  /// Không đạt ở BẤT KỲ tiêu chí nào cũng bắt buộc ảnh bằng chứng — khớp CHECK
  /// `ck_s3_utensils_evidence` và `ck_s3_evidence` phía DB, để đừng nộp lên rồi mới bị chặn.
  bool get fails => !sensoryPass || !utensilsPass;

  factory Step3Row.fromJson(Map<String, dynamic> json) => Step3Row(
        shift: json['shift'] as String? ?? '',
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String? ?? '',
        servings: json['servings'] as String? ?? '',
        dividedDoneAt: json['dividedDoneAt'] as String? ?? '',
        eatStartAt: json['eatStartAt'] as String? ?? '',
        utensilsPass: json['utensilsPass'] as bool? ?? true,
        utensils: json['utensils'] as String? ?? '',
        sensoryPass: json['sensoryPass'] as bool? ?? true,
        action: json['action'] as String? ?? '',
        photoCount: (json['photoCount'] as num?)?.toInt() ?? 0,
      );

  Map<String, dynamic> toJson() => {
        'shift': shift,
        if (dishId != null) 'dishId': dishId,
        'dishName': dishName,
        'servings': servings,
        'dividedDoneAt': dividedDoneAt,
        'eatStartAt': eatStartAt,
        'utensilsPass': utensilsPass,
        'utensils': utensils,
        'sensoryPass': sensoryPass,
        'action': action,
        'photoCount': photoCount,
      };
}

/// `InspectionSessionResponse` — chi tiết phiên kiểm thực 3 bước.
class InspectionSession {
  final String id;
  final String? date;
  final String? meal;
  final String? step1;
  final String? step2;
  final String? step3;
  final String? overall;
  final double? coreTemp;
  final double? holdingTemp;
  final String? inspector;
  final String? coSigner;
  final int evidencePhotos;
  final List<Step1FreshRow> step1Fresh;
  final List<Step1DryRow> step1Dry;
  final List<Step2Row> step2Rows;
  final List<Step3Row> step3Rows;

  InspectionSession({
    required this.id,
    required this.date,
    required this.meal,
    required this.step1,
    required this.step2,
    required this.step3,
    required this.overall,
    required this.coreTemp,
    required this.holdingTemp,
    required this.inspector,
    required this.coSigner,
    required this.evidencePhotos,
    required this.step1Fresh,
    required this.step1Dry,
    required this.step2Rows,
    required this.step3Rows,
  });

  factory InspectionSession.fromJson(Map<String, dynamic> json) => InspectionSession(
        id: json['id'] as String? ?? '',
        date: json['date'] as String?,
        meal: json['meal'] as String?,
        step1: json['step1'] as String?,
        step2: json['step2'] as String?,
        step3: json['step3'] as String?,
        overall: json['overall'] as String?,
        coreTemp: (json['coreTemp'] as num?)?.toDouble(),
        holdingTemp: (json['holdingTemp'] as num?)?.toDouble(),
        inspector: json['inspector'] as String?,
        coSigner: json['coSigner'] as String?,
        evidencePhotos: (json['evidencePhotos'] as num?)?.toInt() ?? 0,
        step1Fresh: ((json['step1Fresh'] as List?) ?? const [])
            .map((e) => Step1FreshRow.fromJson(e as Map<String, dynamic>))
            .toList(),
        step1Dry: ((json['step1Dry'] as List?) ?? const [])
            .map((e) => Step1DryRow.fromJson(e as Map<String, dynamic>))
            .toList(),
        step2Rows: ((json['step2Rows'] as List?) ?? const [])
            .map((e) => Step2Row.fromJson(e as Map<String, dynamic>))
            .toList(),
        step3Rows: ((json['step3Rows'] as List?) ?? const [])
            .map((e) => Step3Row.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}
