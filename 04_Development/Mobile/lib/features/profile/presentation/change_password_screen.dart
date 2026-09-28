import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../auth/data/auth_models.dart';
import '../../auth/data/auth_repository.dart';
import '../data/profile_repository.dart';

/// UC-011 — đối chiếu dialog "Đổi mật khẩu" trong `user-menu.tsx` (bản web),
/// dựng lại thành màn riêng cho mobile thay vì dialog giữa trang.
class ChangePasswordScreen extends StatefulWidget {
  const ChangePasswordScreen({super.key});

  @override
  State<ChangePasswordScreen> createState() => _ChangePasswordScreenState();
}

class _ChangePasswordScreenState extends State<ChangePasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _currentController = TextEditingController();
  final _newController = TextEditingController();
  final _confirmController = TextEditingController();

  PublicPasswordPolicy _policy = PublicPasswordPolicy.fallback;
  bool _submitting = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    AuthRepository.instance.fetchPublicPasswordPolicy().then((p) {
      if (mounted) setState(() => _policy = p);
    }).catchError((_) {});
    _newController.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _currentController.dispose();
    _newController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  List<_Rule> get _rules {
    final pw = _newController.text;
    return [
      _Rule('Tối thiểu ${_policy.passwordMinLength} ký tự', pw.length >= _policy.passwordMinLength),
      if (_policy.requireMixedCase)
        _Rule('Có chữ hoa và chữ thường', RegExp(r'[a-z]').hasMatch(pw) && RegExp(r'[A-Z]').hasMatch(pw)),
      if (_policy.requireNumber) _Rule('Có chữ số', RegExp(r'\d').hasMatch(pw)),
      if (_policy.requireSpecialChar) _Rule('Có ký tự đặc biệt', RegExp(r'[^A-Za-z0-9]').hasMatch(pw)),
    ];
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_rules.any((r) => !r.ok)) {
      setState(() => _error = 'Mật khẩu mới chưa đạt chính sách bảo mật (BR-006).');
      return;
    }
    if (_newController.text != _confirmController.text) {
      setState(() => _error = 'Xác nhận mật khẩu không khớp.');
      return;
    }

    setState(() {
      _submitting = true;
      _error = null;
    });

    try {
      await ProfileRepository.instance.changePassword(
        currentPassword: _currentController.text,
        newPassword: _newController.text,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Đổi mật khẩu thành công. Các phiên đăng nhập khác đã bị chấm dứt.')),
        );
        Navigator.of(context).pop();
      }
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Đổi mật khẩu')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text(
                  'Mật khẩu mới không được trùng 3 mật khẩu gần nhất (BR-006).',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.white70),
                ),
                const SizedBox(height: 20),
                if (_error != null) ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppTheme.dangerRose.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppTheme.dangerRose.withValues(alpha: 0.4)),
                    ),
                    child: Text(_error!, style: const TextStyle(color: AppTheme.dangerRose, fontSize: 13)),
                  ),
                  const SizedBox(height: 16),
                ],
                TextFormField(
                  controller: _currentController,
                  enabled: !_submitting,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'Mật khẩu hiện tại',
                    prefixIcon: Icon(Icons.lock_clock_outlined),
                  ),
                  validator: (v) => (v == null || v.isEmpty) ? 'Nhập mật khẩu hiện tại' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _newController,
                  enabled: !_submitting,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'Mật khẩu mới',
                    prefixIcon: Icon(Icons.lock_outline),
                  ),
                  validator: (v) => (v == null || v.isEmpty) ? 'Nhập mật khẩu mới' : null,
                ),
                const SizedBox(height: 10),
                ..._rules.map(
                  (r) => Padding(
                    padding: const EdgeInsets.symmetric(vertical: 2),
                    child: Row(
                      children: [
                        Icon(
                          r.ok ? Icons.check_circle : Icons.circle_outlined,
                          size: 14,
                          color: r.ok ? AppTheme.successEmerald : Colors.white38,
                        ),
                        const SizedBox(width: 6),
                        Text(
                          r.label,
                          style: TextStyle(fontSize: 12, color: r.ok ? AppTheme.successEmerald : Colors.white54),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 12),
                TextFormField(
                  controller: _confirmController,
                  enabled: !_submitting,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'Xác nhận mật khẩu mới',
                    prefixIcon: Icon(Icons.lock_outline),
                  ),
                  validator: (v) => (v == null || v.isEmpty) ? 'Nhập lại mật khẩu mới' : null,
                  onFieldSubmitted: (_) => _submit(),
                ),
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: _submitting ? null : _submit,
                  child: _submitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('Lưu mật khẩu'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _Rule {
  final String label;
  final bool ok;
  _Rule(this.label, this.ok);
}
