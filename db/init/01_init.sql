CREATE TABLE tenant (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  ten_trung_tam text NOT NULL,
  ngay_tao timestamp NOT NULL DEFAULT now(),
  trang_thai text NOT NULL DEFAULT 'HOAT_DONG'
);
