/// Xuất kho theo bữa (FEFO) — MF06. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/service-caterers/inventory/issue/page.tsx`.
///
/// 13/08/2026 (Nam) — trước đây mobile CHƯA có màn này (`04_Development/Mobile/lib/features/
/// caterer` không hề có file `issue_*` nào) — bếp chỉ xuất kho được qua web. Rút gọn so với
/// bản web: KHÔNG quy đổi đơn vị công thức↔đơn vị kho (`toBaseQuantity`, bảng quy đổi tải từ
/// server) — dùng thẳng đơn vị nguyên liệu của món, đúng nhánh dự phòng mà bản web cũng rơi
/// vào khi bảng quy đổi không có mục khớp (`toIngredientDbQuantity`: đơn vị lệch thì trả
/// nguyên `qty`, không đoán). Dữ liệu thật của trường hiện luôn đặt đơn vị công thức = đơn vị
/// kho nên nhánh dự phòng này đúng trong mọi trường hợp thực tế.

/// `MenuResponse` — chỉ phần cần để biết bữa nào hôm nay có thực đơn đã duyệt.
class IssueMenu {
  final String id;
  final String? grade;
  final String? menuDateIso;
  final String? status;
  final List<String> mealTypes;

  IssueMenu({required this.id, required this.grade, required this.menuDateIso, required this.status, required this.mealTypes});

  factory IssueMenu.fromJson(Map<String, dynamic> json) {
    final blocks = (json['blocksData'] as List?) ?? const [];
    final meals = blocks
        .cast<Map<String, dynamic>>()
        .map((b) => b['mealType'] as String?)
        .whereType<String>()
        .toSet()
        .toList();
    return IssueMenu(
      id: json['id'] as String? ?? '',
      grade: json['grade'] as String?,
      menuDateIso: json['menuDateIso'] as String?,
      status: json['status'] as String?,
      mealTypes: meals,
    );
  }
}

/// `MenuIngredient` trong `DishResponse.ingredients` — dòng nguyên liệu của 1 món.
class IssueDishIngredient {
  final String? ingredientId;
  final String name;
  final double quantity;
  final String unit;

  IssueDishIngredient({required this.ingredientId, required this.name, required this.quantity, required this.unit});

  factory IssueDishIngredient.fromJson(Map<String, dynamic> json) => IssueDishIngredient(
        ingredientId: json['ingredientId'] as String?,
        name: json['name'] as String? ?? '',
        quantity: (json['quantity'] as num?)?.toDouble() ?? 0,
        unit: json['unit'] as String? ?? '',
      );
}

/// `DishResponse` — chỉ phần cần để tính nhu cầu nguyên liệu.
class IssueDish {
  final String id;
  final List<IssueDishIngredient> ingredients;

  IssueDish({required this.id, required this.ingredients});

  factory IssueDish.fromJson(Map<String, dynamic> json) => IssueDish(
        id: json['id'] as String? ?? '',
        ingredients: ((json['ingredients'] as List?) ?? const [])
            .map((e) => IssueDishIngredient.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// `PlannedDishesResponse.byDish` — `GET /student-distributions/planned-dishes` (bỏ `class`
/// để gộp TOÀN TRƯỜNG). Đúng dữ liệu trả lời "hôm nay cần nấu món gì, bao nhiêu suất".
class IssuePlannedDish {
  final String? dishId;
  final String? dishName;
  final int count;
  final int substituteCount;
  final int blockedCount;

  IssuePlannedDish({required this.dishId, required this.dishName, required this.count, required this.substituteCount, required this.blockedCount});

  factory IssuePlannedDish.fromJson(Map<String, dynamic> json) => IssuePlannedDish(
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String?,
        count: (json['count'] as num?)?.toInt() ?? 0,
        substituteCount: (json['substituteCount'] as num?)?.toInt() ?? 0,
        blockedCount: (json['blockedCount'] as num?)?.toInt() ?? 0,
      );
}

/// `AllocationLine` — 1 lô được phân bổ (FEFO) cho 1 dòng nguyên liệu.
class IssueAllocation {
  final String? batchId;
  final double take;
  final String? expiryDate;

  IssueAllocation({required this.batchId, required this.take, required this.expiryDate});

  factory IssueAllocation.fromJson(Map<String, dynamic> json) => IssueAllocation(
        batchId: json['batchId'] as String?,
        take: (json['take'] as num?)?.toDouble() ?? 0,
        expiryDate: json['expiryDate'] as String?,
      );
}

/// `IssuePlanLine` — 1 dòng nguyên liệu trong kế hoạch xuất kho.
class IssuePlanLine {
  final String? dishId;
  final String? dishName;
  final String? ingredientId;
  final String ingredientName;
  final double needed;
  final String unit;
  final List<IssueAllocation> allocation;
  final double shortfall;
  final bool unknownIngredient;

  IssuePlanLine({
    required this.dishId,
    required this.dishName,
    required this.ingredientId,
    required this.ingredientName,
    required this.needed,
    required this.unit,
    required this.allocation,
    required this.shortfall,
    required this.unknownIngredient,
  });

  factory IssuePlanLine.fromJson(Map<String, dynamic> json) => IssuePlanLine(
        dishId: json['dishId'] as String?,
        dishName: json['dishName'] as String?,
        ingredientId: json['ingredientId'] as String?,
        ingredientName: json['ingredientName'] as String? ?? '',
        needed: (json['needed'] as num?)?.toDouble() ?? 0,
        unit: json['unit'] as String? ?? '',
        allocation: ((json['allocation'] as List?) ?? const [])
            .map((e) => IssueAllocation.fromJson(e as Map<String, dynamic>))
            .toList(),
        shortfall: (json['shortfall'] as num?)?.toDouble() ?? 0,
        unknownIngredient: json['unknownIngredient'] as bool? ?? false,
      );
}

class IssueShortage {
  final String? ingredientId;
  final String ingredientName;
  final double needed;
  final double available;
  final double shortfall;
  final String unit;

  IssueShortage({required this.ingredientId, required this.ingredientName, required this.needed, required this.available, required this.shortfall, required this.unit});

  factory IssueShortage.fromJson(Map<String, dynamic> json) => IssueShortage(
        ingredientId: json['ingredientId'] as String?,
        ingredientName: json['ingredientName'] as String? ?? '',
        needed: (json['needed'] as num?)?.toDouble() ?? 0,
        available: (json['available'] as num?)?.toDouble() ?? 0,
        shortfall: (json['shortfall'] as num?)?.toDouble() ?? 0,
        unit: json['unit'] as String? ?? '',
      );
}

/// `IssuePlanResponse` — `GET /inventory/issue-plan`.
class IssuePlan {
  final List<IssuePlanLine> lines;
  final List<IssueShortage> shortages;
  final List<String> unknownIngredients;

  IssuePlan({required this.lines, required this.shortages, required this.unknownIngredients});

  factory IssuePlan.fromJson(Map<String, dynamic> json) => IssuePlan(
        lines: ((json['lines'] as List?) ?? const []).map((e) => IssuePlanLine.fromJson(e as Map<String, dynamic>)).toList(),
        shortages: ((json['shortages'] as List?) ?? const []).map((e) => IssueShortage.fromJson(e as Map<String, dynamic>)).toList(),
        unknownIngredients: ((json['unknownIngredients'] as List?) ?? const []).cast<String>(),
      );
}
