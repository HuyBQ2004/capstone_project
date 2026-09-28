import 'package:flutter/material.dart';

import '../../../core/models/user_model.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../caterer/presentation/caterer_dashboard.dart';
import '../../medical_staff/presentation/medical_staff_dashboard.dart';
import '../../parent/presentation/parent_dashboard.dart';
import '../../teacher/presentation/teacher_dashboard.dart';
import '../data/auth_models.dart';
import '../data/auth_repository.dart';
import 'forgot_password_screen.dart';

/// UC-034/UC-191 — đối chiếu bản web `04_Development/Frontend/src/app/(auth)/login/page.tsx`:
/// cùng nghiệp vụ (login thường + Google), bố cục lại 1 cột full-height cho mobile
/// thay vì Card giữa trang.
class LoginScreen extends StatefulWidget {
  /// 13/08/2026 — `session_gate.dart` truyền vào khi khôi phục phiên gặp vai
  /// trò ngoài phạm vi mobile (school-admin/system-admin/supplier), để hiện
  /// đúng thông báo "chưa hỗ trợ" thay vì bật thẳng về màn Login trống trơn
  /// không giải thích gì.
  final String? initialError;

  const LoginScreen({super.key, this.initialError});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _obscurePassword = true;
  bool _submittingPassword = false;
  bool _submittingGoogle = false;
  String? _error;

  bool get _busy => _submittingPassword || _submittingGoogle;

  @override
  void initState() {
    super.initState();
    _error = widget.initialError;
  }

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submitPasswordLogin() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _error = null;
      _submittingPassword = true;
    });

    try {
      final profile = await AuthRepository.instance.loginWithPassword(
        _emailController.text.trim(),
        _passwordController.text,
      );
      _goToDashboard(profile);
    } on ApiException catch (e) {
      setState(() => _error = _friendlyMessage(e));
    } catch (_) {
      setState(() => _error = 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _submittingPassword = false);
    }
  }

  Future<void> _submitGoogleLogin() async {
    setState(() {
      _error = null;
      _submittingGoogle = true;
    });

    try {
      final profile = await AuthRepository.instance.loginWithGoogle();
      if (profile != null) _goToDashboard(profile);
      // profile == null: người dùng tự hủy picker tài khoản — không hiện lỗi.
    } on ApiException catch (e) {
      setState(() => _error = _friendlyMessage(e));
    } catch (_) {
      setState(() => _error = 'Đăng nhập Google thất bại. Thử lại sau.');
    } finally {
      if (mounted) setState(() => _submittingGoogle = false);
    }
  }

  String _friendlyMessage(ApiException e) {
    final attemptsLeft = e.attemptsLeft;
    final suffix = attemptsLeft != null ? ' Còn $attemptsLeft lần thử.' : '';
    switch (e.code) {
      case 'IAM-002':
        return 'Sai email hoặc mật khẩu.$suffix';
      case 'IAM-003':
        return 'Tài khoản chưa xác minh hoặc đã bị vô hiệu hóa.';
      case 'IAM-007':
        return 'Tài khoản tạm khóa do đăng nhập sai nhiều lần. Thử lại sau 15 phút.';
      case 'IAM-102':
        return 'Email Google này chưa được cấp tài khoản SFMS.';
      case 'IAM-103':
        return 'Xác thực Google thất bại. Thử lại.';
      default:
        return e.message;
    }
  }

  void _goToDashboard(Profile profile) {
    final target = _dashboardFor(profile.role);
    if (target == null) {
      setState(() {
        _error =
            'Vai trò "${profile.role.displayName}" chưa hỗ trợ trên app mobile — dùng bản web.';
      });
      return;
    }
    Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => target));
  }

  /// Chỉ 4 role trong `TASKS_MOBILE_SCOPE.md` có dashboard mobile.
  Widget? _dashboardFor(UserRole role) {
    switch (role) {
      case UserRole.teacher:
        return const TeacherDashboard();
      case UserRole.parent:
        return const ParentDashboard();
      case UserRole.medicalStaff:
        return const MedicalStaffDashboard();
      case UserRole.serviceCaterers:
        return const CatererDashboard();
      case UserRole.rawFoodSupplier:
      case UserRole.schoolAdmin:
      case UserRole.systemAdmin:
        return null;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 24),
                Center(
                  child: Container(
                    width: 64,
                    height: 64,
                    decoration: BoxDecoration(
                      color: AppTheme.primarySky.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: const Icon(Icons.restaurant_menu, color: AppTheme.primarySky, size: 32),
                  ),
                ),
                const SizedBox(height: 20),
                Text(
                  'Đăng nhập SFMS',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 6),
                Text(
                  'Nhập email và mật khẩu tài khoản SFMS của bạn',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Colors.white70,
                      ),
                ),
                const SizedBox(height: 28),

                if (_error != null) ...[
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppTheme.dangerRose.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppTheme.dangerRose.withValues(alpha: 0.4)),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.error_outline, color: AppTheme.dangerRose, size: 20),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            _error!,
                            style: const TextStyle(color: AppTheme.dangerRose, fontSize: 13),
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                ],

                TextFormField(
                  controller: _emailController,
                  enabled: !_busy,
                  keyboardType: TextInputType.emailAddress,
                  autofillHints: const [AutofillHints.username],
                  decoration: const InputDecoration(
                    labelText: 'Email',
                    hintText: 'ten.ban@truong.edu.vn',
                    prefixIcon: Icon(Icons.email_outlined),
                  ),
                  validator: (v) {
                    if (v == null || v.trim().isEmpty) return 'Nhập email';
                    if (!v.contains('@')) return 'Email không hợp lệ';
                    return null;
                  },
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _passwordController,
                  enabled: !_busy,
                  obscureText: _obscurePassword,
                  autofillHints: const [AutofillHints.password],
                  decoration: InputDecoration(
                    labelText: 'Mật khẩu',
                    prefixIcon: const Icon(Icons.lock_outline),
                    suffixIcon: IconButton(
                      icon: Icon(_obscurePassword ? Icons.visibility_outlined : Icons.visibility_off_outlined),
                      onPressed: () => setState(() => _obscurePassword = !_obscurePassword),
                    ),
                  ),
                  validator: (v) => (v == null || v.isEmpty) ? 'Nhập mật khẩu' : null,
                  onFieldSubmitted: (_) => _submitPasswordLogin(),
                ),
                const SizedBox(height: 8),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: _busy
                        ? null
                        : () {
                            Navigator.of(context).push(
                              MaterialPageRoute(builder: (_) => const ForgotPasswordScreen()),
                            );
                          },
                    child: const Text('Quên mật khẩu?'),
                  ),
                ),
                const SizedBox(height: 12),

                ElevatedButton(
                  onPressed: _busy ? null : _submitPasswordLogin,
                  child: _submittingPassword
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('Đăng nhập'),
                ),

                const SizedBox(height: 20),
                Row(
                  children: [
                    Expanded(child: Divider(color: AppTheme.borderDark)),
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                      child: Text('HOẶC', style: TextStyle(color: Colors.white54, fontSize: 12)),
                    ),
                    Expanded(child: Divider(color: AppTheme.borderDark)),
                  ],
                ),
                const SizedBox(height: 20),

                OutlinedButton.icon(
                  onPressed: _busy ? null : _submitGoogleLogin,
                  icon: _submittingGoogle
                      ? const SizedBox(
                          width: 16,
                          height: 16,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Icon(Icons.g_mobiledata, size: 24),
                  label: const Text('Đăng nhập bằng Google'),
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    side: const BorderSide(color: AppTheme.borderDark),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
