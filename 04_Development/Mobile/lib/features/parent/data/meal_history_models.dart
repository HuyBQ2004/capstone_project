/// `ServingDishView` (backend, MF09) — món trong một lần phục vụ, có thể là
/// suất thay thế cho dị ứng/chế độ ăn riêng.
class ServingDish {
  final String? dishName;
  final bool isSubstitute;
  final String? substitutedForDishName;

  ServingDish({required this.dishName, required this.isSubstitute, required this.substitutedForDishName});

  factory ServingDish.fromJson(Map<String, dynamic> json) => ServingDish(
        dishName: json['dishName'] as String?,
        isSubstitute: json['isSubstitute'] as bool? ?? false,
        substitutedForDishName: json['substitutedForDishName'] as String?,
      );
}

/// `MealHistoryEntryResponse` (backend, MF09 — `GET /students/{code}/meal-history`).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/parent/meal-history/page.tsx`
/// (UC-133, BR-121).
class MealHistoryEntry {
  final String id;
  final String? date;
  final String? mealType;
  final String? consumption;
  final String? catererName;
  final List<ServingDish> dishes;

  MealHistoryEntry({
    required this.id,
    required this.date,
    required this.mealType,
    required this.consumption,
    required this.catererName,
    required this.dishes,
  });

  factory MealHistoryEntry.fromJson(Map<String, dynamic> json) => MealHistoryEntry(
        id: json['id'] as String,
        date: json['date'] as String?,
        mealType: json['mealType'] as String?,
        consumption: json['consumption'] as String?,
        catererName: json['catererName'] as String?,
        dishes: ((json['dishes'] as List?) ?? const [])
            .map((e) => ServingDish.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

const kConsumptionOptions = ['FULLY', 'PARTIALLY', 'NOT_CONSUMED'];

String consumptionLabel(String? value) {
  switch (value) {
    case 'FULLY':
      return 'Ăn hết';
    case 'PARTIALLY':
      return 'Ăn một phần';
    case 'NOT_CONSUMED':
      return 'Không ăn';
    default:
      return 'Chưa ghi nhận';
  }
}
