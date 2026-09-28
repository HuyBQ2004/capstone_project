/// `NotificationDtos.NotificationResponse` (backend, MF10).
///
/// ⚠️ `id` thực chất là mã nghiệp vụ (`UserNotification.code`, D1) — dùng đúng
/// giá trị này khi gọi `PATCH /notifications/{id}/read` (xem
/// `NotificationServiceImpl.markRead`, backend nhận path variable tên `code`).
class NotificationItem {
  final String id;
  final String audience;
  final List<String> recipientRoles;
  final String type;
  final String title;
  final String content;
  final DateTime sentAt;
  final bool read;
  final bool critical;
  final String? link;
  final String? linkLabel;

  NotificationItem({
    required this.id,
    required this.audience,
    required this.recipientRoles,
    required this.type,
    required this.title,
    required this.content,
    required this.sentAt,
    required this.read,
    required this.critical,
    required this.link,
    required this.linkLabel,
  });

  factory NotificationItem.fromJson(Map<String, dynamic> json) => NotificationItem(
        id: json['id'] as String,
        audience: json['audience'] as String,
        recipientRoles: (json['recipientRoles'] as List?)?.cast<String>() ?? const [],
        type: json['type'] as String,
        title: json['title'] as String,
        content: json['content'] as String,
        sentAt: DateTime.parse(json['sentAt'] as String),
        read: json['read'] as bool,
        critical: json['critical'] as bool,
        link: json['link'] as String?,
        linkLabel: json['linkLabel'] as String?,
      );

  NotificationItem copyWith({bool? read}) => NotificationItem(
        id: id,
        audience: audience,
        recipientRoles: recipientRoles,
        type: type,
        title: title,
        content: content,
        sentAt: sentAt,
        read: read ?? this.read,
        critical: critical,
        link: link,
        linkLabel: linkLabel,
      );
}

/// `PagedResponse<NotificationResponse>` (đúng shape với `Paged<T>` dùng chung).
class NotificationPage {
  final List<NotificationItem> items;
  final int total;
  final int page;
  final int pageSize;
  final int pageCount;

  NotificationPage({
    required this.items,
    required this.total,
    required this.page,
    required this.pageSize,
    required this.pageCount,
  });

  factory NotificationPage.fromJson(Map<String, dynamic> json) => NotificationPage(
        items: (json['items'] as List).map((e) => NotificationItem.fromJson(e as Map<String, dynamic>)).toList(),
        total: json['total'] as int,
        page: json['page'] as int,
        pageSize: json['pageSize'] as int,
        pageCount: json['pageCount'] as int,
      );
}
