/* Chạy một lần trên database ptudpetshop để bật cảnh báo hạn dùng. */
IF COL_LENGTH('dbo.ChiTietPhieuNhap', 'hanSuDung') IS NULL
    ALTER TABLE dbo.ChiTietPhieuNhap ADD hanSuDung DATE NULL;
GO

/* Ngày mẫu tương đối với ngày chạy, để bản dữ liệu demo hiện cả hai trạng thái. */
UPDATE dbo.ChiTietPhieuNhap
SET hanSuDung = CASE maSanPham
    WHEN 'SP001' THEN DATEADD(day, 12, CAST(GETDATE() AS date))
    WHEN 'SP002' THEN DATEADD(day, -5, CAST(GETDATE() AS date))
    WHEN 'SP008' THEN DATEADD(day, 75, CAST(GETDATE() AS date))
    ELSE hanSuDung
END
WHERE maPhieuNhap = 'PN001'
  AND maSanPham IN ('SP001', 'SP002', 'SP008')
  AND hanSuDung IS NULL;
GO
