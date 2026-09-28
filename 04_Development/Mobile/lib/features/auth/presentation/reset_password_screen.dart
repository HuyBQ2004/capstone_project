import 'package:flutter/material.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/auth_models.dart';
import '../data/auth_repository.dart';
import 'login_screen.dart';

/// UC-032 bước cuối — đối chiếu bản web `(auth)/reset-password/page.tsx`.
/// Web đọc token từ query string `?token=` (mở từ link email trên desktop);
/// mobile chưa có deep link nên người dùng dán tay — ô token luôn hiện, không
/// ẩn kiểu "đã tự điền" như web.
class ResetPasswordScreen extends StatefulWidget {
  const ResetPasswordScreen({super.key});

  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  final _formKey = GlobalKey<FormState>();
  final _tokenController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmController = TextEditingController();

  PublicPasswordPolicy _policy = PublicPasswordPolicy.fallback;
  bool _submitting = false;
  bool _done = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    AuthRepository.instance.fetchPublicPasswordPolicy().then((p) {
      if (mounted) setState(() => _policy = p);
    }).catchError((_) {
      // Giữ nguyên fallback — server vẫn là nguồn thật kiểm tra lúc submit.
    });
    _passwordController.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _tokenController.dispose();
    _passwordController.dispose();
    _confirmController.dispose();
    super.dispose();
  }

  List<_PasswordRule> get _rules {
    final pw = _passwordController.text;
    return [
      _PasswordRule('Tối thiểu ${_policy.passwordMinLength} ký tự', pw.length >= _policy.passwordMinLength),
      if (_policy.requireMixedCase)
        _PasswordRule('Có chữ hoa và chữ thường', RegExp(r'[a-z]').hasMatch(pw) && RegExp(r'[A-Z]').hasMatch(pw)),
      if (_policy.requireNumber) _PasswordRule('Có chữ số', RegExp(r'\d').hasMatch(pw)),
      if (_policy.requireSpecialChar)
        _PasswordRule('Có ký tự đặc biệt', RegExp(r'[^A-Za-z0-9]').hasMatch(pw)),
    ];
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    if (_rules.any((r) => !r.ok)) {
      setState(() => _error = 'Mật khẩu chưa đáp ứng chính sách bảo mật (BR-006).');
      return;
    }
    if (_passwordController.text != _confirmController.text) {
      setState(() => _error = 'Mật khẩu xác nhận không khớp.');
      return;
    }

    setState(() {
      _submitting = true;
      _error = null;
    });

    try {
      await AuthRepository.instance.resetPassword(_tokenController.text.trim(), _passwordController.text);
      if (mounted) setState(() => _done = true);
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
      appBar: AppBar(title: const Text('Đặt lại mật khẩu')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: _done ? _buildDoneState(context) : _buildForm(context),
        ),
      ),
    );
  }

  Widget _buildForm(BuildContext context) {
    return Form(
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
            controller: _tokenController,
            enabled: !_submitting,
            decoration: const InputDecoration(
              labelText: 'Token đặt lại mật khẩu',
              hintText: 'Dán token từ link trong email',
              prefixIcon: Icon(Icons.vpn_key_outlined),
            ),
            validator: (v) => (v == null || v.trim().isEmpty) ? 'Nhập token' : null,
          ),
          const SizedBox(height: 16),
          TextFormField(
            controller: _passwordController,
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
                    style: TextStyle(
                      fontSize: 12,
                      color: r.ok ? AppTheme.successEmerald : Colors.white54,
                    ),
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
              labelText: 'Xác nhận mật khẩu',
              prefixIcon: Icon(Icons.lock_outline),
            ),
            validator: (v) => (v == null || v.isEmpty) ? 'Nhập lại mật khẩu' : null,
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
                : const Text('Đặt lại mật khẩu'),
          ),
        ],
      ),
    );
  }

  Widget _buildDoneState(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 12),
        const Icon(Icons.check_circle_outline, color: AppTheme.successEmerald, size: 56),
        const SizedBox(height: 16),
        Text(
          'Đổi mật khẩu thành công! Bạn có thể dùng mật khẩu mới để đăng nhập.',
          textAlign: TextAlign.center,
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        const SizedBox(height: 24),
        ElevatedButton(
          onPressed: () => Navigator.of(context).pushAndRemoveUntil(
            MaterialPageRoute(builder: (_) => const LoginScreen()),
            (route) => false,
          ),
          child: const Text('Đăng nhập'),
        ),
      ],
    );
  }
}

class _PasswordRule {
  final String label;
  final bool ok;
  _PasswordRule(this.label, this.ok);
}
