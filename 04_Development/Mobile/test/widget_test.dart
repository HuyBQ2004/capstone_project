import 'package:flutter_test/flutter_test.dart';
import 'package:sfms_mobile/main.dart';

void main() {
  testWidgets('SFMS Mobile App smoke test', (WidgetTester tester) async {
    await tester.pumpWidget(const SfmsMobileApp());
    expect(find.byType(SfmsMobileApp), findsOneWidget);
  });
}
