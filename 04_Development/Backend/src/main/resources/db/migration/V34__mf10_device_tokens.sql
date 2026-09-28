-- V34__mf10_device_tokens.sql — FCM push (mobile + web). Không lộ qua path ⇒ không `code`,
-- cùng convention với notification_deliveries (V18).
CREATE TABLE device_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES user_accounts(id),   -- M10-D04 (C2)
  token VARCHAR(400) NOT NULL,
  platform VARCHAR(8) NOT NULL CHECK (platform IN ('ANDROID', 'IOS', 'WEB')),
  last_used_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),         -- D9 / M10-D02
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by VARCHAR(180),
  updated_by VARCHAR(180),
  -- 1 token vật lý (1 browser/1 app install) chỉ gắn với 1 user tại một thời điểm — đăng nhập
  -- account khác trên cùng thiết bị thì upsert lại user_id (xử lý ở service, không ở DB).
  CONSTRAINT uq_devtok_token UNIQUE (token)
);
CREATE INDEX ix_devtok_user ON device_tokens(user_id);
