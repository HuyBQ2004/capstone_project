/// `MealRecordResponse` (backend, MF09) — đối chiếu
/// `04_Development/Frontend/src/app/(app)/teacher/meal-records/page.tsx`
/// (UC-056/UC-080). `consumption` là chuỗi enum
/// `FULLY|PARTIALLY|NOT_CONSUMED`.
class MealRecord {
  final String id;
  final String? studentId;
  final String? student;
  final String? classroomId;
  final String? date;
  final String? mealType;
  final String? dishes;
  final String? consumption;
  final String? specialDiet;
  final String? specialMealServed;
  final String? recordedAt;

  MealRecord({
    required this.id,
    required this.studentId,
    required this.student,
    required this.classroomId,
    required this.date,
    required this.mealType,
    required this.dishes,
    required this.consumption,
    required this.specialDiet,
    required this.specialMealServed,
    required this.recordedAt,
  });

  factory MealRecord.fromJson(Map<String, dynamic> json) => MealRecord(
        id: json['id'] as String,
        studentId: json['studentId'] as String?,
        student: json['student'] as String?,
        classroomId: json['class'] as String?,
        date: json['date'] as String?,
        mealType: json['mealType'] as String?,
        dishes: json['dishes'] as String?,
        consumption: json['consumption'] as String?,
        specialDiet: json['specialDiet'] as String?,
        specialMealServed: json['specialMealServed'] as String?,
        recordedAt: json['recordedAt'] as String?,
      );
}

const kConsumptionOptions = <String>['FULLY', 'PARTIALLY', 'NOT_CONSUMED'];

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
