/// `MenuBlockDish` (backend, MF05) — chỉ phần các màn Thực đơn/Dashboard Parent
/// cần. Đối chiếu `DishForm` (`components/app/menu-form.tsx`, bản web).
class MenuDish {
  final String? dishId;
  final String? name;
  final String? kcal;
  final List<String> manualAllergens;

  MenuDish({required this.dishId, required this.name, required this.kcal, required this.manualAllergens});

  factory MenuDish.fromJson(Map<String, dynamic> json) => MenuDish(
        dishId: json['dishId'] as String?,
        name: json['name'] as String?,
        kcal: json['kcal']?.toString(),
        manualAllergens: (json['manualAllergens'] as List?)?.cast<String>() ?? const [],
      );

  double get kcalValue => double.tryParse(kcal ?? '') ?? 0;
}

/// `MenuBlock` (backend) — một buổi ăn (Sáng/Trưa/Chiều) trong thực đơn ngày.
class MenuBlock {
  final String mealType;
  final List<MenuDish> dishes;

  MenuBlock({required this.mealType, required this.dishes});

  factory MenuBlock.fromJson(Map<String, dynamic> json) => MenuBlock(
        mealType: json['mealType'] as String? ?? '',
        dishes: ((json['dishes'] as List?) ?? const [])
            .map((e) => MenuDish.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

/// `MenuIngredient` trong `DishResponse.ingredients` — 1 dòng nguyên liệu của món.
class DishIngredientLine {
  final String? ingredientId;
  final String name;
  final String? quantity;
  final String? unit;

  DishIngredientLine({required this.ingredientId, required this.name, required this.quantity, required this.unit});

  factory DishIngredientLine.fromJson(Map<String, dynamic> json) => DishIngredientLine(
        ingredientId: json['ingredientId'] as String?,
        name: json['name'] as String? ?? '',
        quantity: json['quantity']?.toString(),
        unit: json['unit'] as String?,
      );
}

/// `DishResponse` đầy đủ (backend, MF05) — dùng cho "Xem chi tiết món" (13/08/2026, Nam):
/// nguyên liệu + ảnh minh hoạ (nếu bếp đã tải lên) + dinh dưỡng. Đối chiếu
/// `04_Development/Frontend/src/app/(app)/parent/weekly-menu/page.tsx` (dialog chi tiết món).
class DishDetail {
  final String id;
  final String? name;
  final String? category;
  final String? kcal;
  final String? protein;
  final String? fat;
  final String? carb;
  final List<String> manualAllergens;
  final List<DishIngredientLine> ingredients;
  final String? imageUrl;
  final String? description;

  DishDetail({
    required this.id,
    required this.name,
    required this.category,
    required this.kcal,
    required this.protein,
    required this.fat,
    required this.carb,
    required this.manualAllergens,
    required this.ingredients,
    required this.imageUrl,
    required this.description,
  });

  factory DishDetail.fromJson(Map<String, dynamic> json) => DishDetail(
        id: json['id'] as String? ?? '',
        name: json['name'] as String?,
        category: json['category'] as String?,
        kcal: json['kcal']?.toString(),
        protein: json['protein']?.toString(),
        fat: json['fat']?.toString(),
        carb: json['carb']?.toString(),
        manualAllergens: (json['manualAllergens'] as List?)?.cast<String>() ?? const [],
        ingredients: ((json['ingredients'] as List?) ?? const [])
            .map((e) => DishIngredientLine.fromJson(e as Map<String, dynamic>))
            .toList(),
        imageUrl: json['imageUrl'] as String?,
        description: json['description'] as String?,
      );
}

const mealTypeLabel = {
  'BREAKFAST': 'Bữa sáng',
  'LUNCH': 'Bữa trưa',
  'SNACK': 'Bữa chiều',
};

/// `MenuResponse` (backend, MF05) — chỉ field các màn Parent cần. Đối chiếu
/// `PersistedMenu` (`components/app/menu-form.tsx`, bản web).
class ParentMenu {
  final String id;
  final String? week;
  final String? grade;
  final String? status;
  final String? menuDateIso;
  final String? audience;
  final List<MenuBlock> blocks;

  ParentMenu({
    required this.id,
    required this.week,
    required this.grade,
    required this.status,
    required this.menuDateIso,
    required this.audience,
    required this.blocks,
  });

  factory ParentMenu.fromJson(Map<String, dynamic> json) => ParentMenu(
        id: json['id'] as String,
        week: json['week'] as String?,
        grade: json['grade'] as String?,
        status: json['status'] as String?,
        menuDateIso: json['menuDateIso'] as String?,
        audience: json['audience'] as String?,
        blocks: ((json['blocksData'] as List?) ?? const [])
            .map((e) => MenuBlock.fromJson(e as Map<String, dynamic>))
            .toList(),
      );

  /// BR-068: thực đơn chỉ hiển thị chính thức cho phụ huynh sau khi công bố.
  bool get isLive => status == 'PUBLISHED';

  /// Thực đơn CHUNG của khối, không phải thực đơn riêng theo nhóm chế độ ăn
  /// (`audience != "ALL"`) — đối chiếu `standardMenus()` (bản web).
  bool get isStandard => audience == null || audience == 'ALL';

  int get totalKcal => blocks.fold(0, (sum, b) => sum + b.dishes.fold(0.0, (s, d) => s + d.kcalValue).round());

  int get totalDishes => blocks.fold(0, (sum, b) => sum + b.dishes.length);
}
