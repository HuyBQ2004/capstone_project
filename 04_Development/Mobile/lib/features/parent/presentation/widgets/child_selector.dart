import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../../data/child_models.dart';

/// Bộ chọn con — chỉ hiện khi phụ huynh có >1 con (đối chiếu Child Switcher ở
/// Topbar bản web, `components/app/child-switcher.tsx`). Dùng ở tất cả màn
/// hiển thị dữ liệu theo con: Dashboard, Thực đơn, Lịch sử bữa ăn, Dị ứng.
class ChildSelector extends StatelessWidget {
  final List<ParentChild> children;
  final ParentChild? active;
  final ValueChanged<String> onSelect;

  const ChildSelector({
    super.key,
    required this.children,
    required this.active,
    required this.onSelect,
  });

  @override
  Widget build(BuildContext context) {
    if (children.length <= 1) return const SizedBox.shrink();

    return SizedBox(
      height: 40,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: children.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final child = children[index];
          final selected = child.id == active?.id;
          return ChoiceChip(
            label: Text('${child.name ?? child.id} · ${child.classroomId ?? '—'}'),
            selected: selected,
            selectedColor: AppTheme.primarySky.withValues(alpha: 0.25),
            onSelected: (_) => onSelect(child.id),
          );
        },
      ),
    );
  }
}
