import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../../core/constants/api_constants.dart';
import '../../../core/models/user_model.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../auth/data/auth_models.dart';
import '../../auth/data/auth_repository.dart';
import '../../auth/presentation/login_screen.dart';
import '../data/profile_repository.dart';
import 'change_password_screen.dart';

/// BR-012: +84xxxxxxxxx hoặc 0xxxxxxxxx (9–10 số sau đầu số).
final _phoneRe = RegExp(r'^(\+84|0)\d{9,10}$');

/// UC-097/UC-059/UC-035 — đối chiếu dropdown "Hồ sơ cá nhân" trong
/// `user-menu.tsx` (bản web), dựng lại thành 1 màn full-screen thay vì dialog.
/// KHÔNG port mục "Cài đặt thông báo" — bên web đó là mock chưa nối API thật.
class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();

  Profile? _profile;
  XFile? _pendingAvatar;
  bool _loading = true;
  bool _saving = false;
  bool _loggingOut = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final profile = await ProfileRepository.instance.fetchMine();
      _nameController.text = profile.name;
      _phoneController.text = profile.phone ?? '';
      if (mounted) setState(() => _profile = profile);
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không tải được hồ sơ. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _pickAvatar() async {
    final file = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 90);
    if (file != null) setState(() => _pendingAvatar = file);
  }

  bool get _phoneValid => _phoneController.text.isEmpty || _phoneRe.hasMatch(_phoneController.text);

  Future<void> _save() async {
    if (!_formKey.currentState!.validate() || !_phoneValid) return;

    setState(() {
      _saving = true;
      _error = null;
    });

    try {
      var profile = await ProfileRepository.instance.updateProfile(
        name: _nameController.text.trim(),
        phone: _phoneController.text.trim().replaceAll(RegExp(r'[\s.]'), ''),
      );
      if (_pendingAvatar != null) {
        profile = await ProfileRepository.instance.uploadAvatar(_pendingAvatar!.path);
        _pendingAvatar = null;
      }
      if (mounted) {
        setState(() => _profile = profile);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Cập nhật hồ sơ thành công.')),
        );
      }
    } on ApiException catch (e) {
      setState(() => _error = e.message);
    } catch (_) {
      setState(() => _error = 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.');
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _confirmLogout() async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Đăng xuất'),
        content: const Text('Bạn có chắc muốn đăng xuất? Phiên làm việc hiện tại sẽ kết thúc.'),
        actions: [
          TextButton(onPressed: () => Navigator.of(ctx).pop(false), child: const Text('Hủy')),
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(true),
            child: const Text('Đăng xuất', style: TextStyle(color: AppTheme.dangerRose)),
          ),
        ],
      ),
    );
    if (confirmed != true) return;

    setState(() => _loggingOut = true);
    await AuthRepository.instance.logout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Hồ sơ cá nhân')),
      body: SafeArea(
        child: _loading
            ? const Center(child: CircularProgressIndicator())
            : _profile == null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(24),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(_error ?? 'Không tải được hồ sơ.', textAlign: TextAlign.center),
                          const SizedBox(height: 12),
                          ElevatedButton(onPressed: _load, child: const Text('Thử lại')),
                        ],
                      ),
                    ),
                  )
                : _buildForm(context),
      ),
    );
  }

  Widget _buildForm(BuildContext context) {
    final profile = _profile!;
    return SingleChildScrollView(
      padding: const EdgeInsets.all(24),
      child: Form(
        key: _formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Center(
              child: GestureDetector(
                onTap: _saving ? null : _pickAvatar,
                child: Stack(
                  children: [
                    CircleAvatar(
                      radius: 44,
                      backgroundColor: AppTheme.primarySky.withValues(alpha: 0.15),
                      backgroundImage: _pendingAvatar != null
                          ? FileImage(File(_pendingAvatar!.path)) as ImageProvider
                          : (profile.avatarUrl != null
                              ? NetworkImage(ApiConstants.resolveMediaUrl(profile.avatarUrl!))
                              : null),
                      child: (_pendingAvatar == null && profile.avatarUrl == null)
                          ? Text(
                              profile.name.isNotEmpty ? profile.name[0].toUpperCase() : '?',
                              style: const TextStyle(fontSize: 32, color: AppTheme.primarySky, fontWeight: FontWeight.bold),
                            )
                          : null,
                    ),
                    Positioned(
                      right: 0,
                      bottom: 0,
                      child: Container(
                        padding: const EdgeInsets.all(6),
                        decoration: const BoxDecoration(color: AppTheme.primarySky, shape: BoxShape.circle),
                        child: const Icon(Icons.camera_alt, size: 16, color: Colors.white),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 6),
            Center(
              child: Text(
                'JPG/PNG, tối đa 10MB (BR-013)',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.white54),
              ),
            ),
            const SizedBox(height: 24),

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
              controller: _nameController,
              enabled: !_saving,
              decoration: const InputDecoration(labelText: 'Họ và tên', prefixIcon: Icon(Icons.badge_outlined)),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Họ tên là bắt buộc' : null,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _phoneController,
              enabled: !_saving,
              keyboardType: TextInputType.phone,
              decoration: InputDecoration(
                labelText: 'Số điện thoại',
                hintText: '+84xxxxxxxxx hoặc 0xxxxxxxxx',
                prefixIcon: const Icon(Icons.phone_outlined),
                errorText: _phoneValid ? null : 'Định dạng không hợp lệ (+84 hoặc 0xx — BR-012)',
              ),
              onChanged: (_) => setState(() {}),
            ),
            const SizedBox(height: 16),
            TextFormField(
              initialValue: profile.email,
              enabled: false,
              decoration: const InputDecoration(labelText: 'Email (không thể sửa)', prefixIcon: Icon(Icons.email_outlined)),
            ),
            const SizedBox(height: 16),
            TextFormField(
              initialValue: profile.school ?? profile.role.displayName,
              enabled: false,
              decoration: const InputDecoration(labelText: 'Vai trò · Trường', prefixIcon: Icon(Icons.apartment_outlined)),
            ),
            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: _saving ? null : _save,
              child: _saving
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                    )
                  : const Text('Lưu hồ sơ'),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              onPressed: _saving
                  ? null
                  : () => Navigator.of(context).push(
                        MaterialPageRoute(builder: (_) => const ChangePasswordScreen()),
                      ),
              icon: const Icon(Icons.key_outlined),
              label: const Text('Đổi mật khẩu'),
            ),
            const SizedBox(height: 24),
            TextButton.icon(
              onPressed: _loggingOut ? null : _confirmLogout,
              icon: _loggingOut
                  ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.logout, color: AppTheme.dangerRose),
              label: const Text('Đăng xuất', style: TextStyle(color: AppTheme.dangerRose)),
            ),
          ],
        ),
      ),
    );
  }
}
