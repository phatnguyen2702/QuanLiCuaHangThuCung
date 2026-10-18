/* Chạy một lần trên database đang dùng để bật lọc theo loài/tuổi/kích thước. */
IF OBJECT_ID('dbo.GoiYThucAn', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.GoiYThucAn (
        maSanPham VARCHAR(10) NOT NULL,
        loaiThuCung NVARCHAR(10) NOT NULL,
        nhomTuoi NVARCHAR(20) NOT NULL,
        kichThuoc NVARCHAR(20) NOT NULL,
        ghiChu NVARCHAR(250) NULL,
        CONSTRAINT FK_GoiYThucAn_SanPham FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham),
        CONSTRAINT PK_GoiYThucAn PRIMARY KEY (maSanPham, loaiThuCung, nhomTuoi, kichThuoc),
        CONSTRAINT CK_GoiYThucAn_Loai CHECK (loaiThuCung IN (N'Chó', N'Mèo')),
        CONSTRAINT CK_GoiYThucAn_Tuoi CHECK (nhomTuoi IN (N'Con non', N'Trưởng thành', N'Cao tuổi', N'Mọi độ tuổi', N'Chưa xác định')),
        CONSTRAINT CK_GoiYThucAn_Size CHECK (kichThuoc IN (N'Nhỏ', N'Trung bình', N'Lớn', N'Mọi kích thước', N'Chưa xác định'))
    );
END;
GO

/* Dùng chính sản phẩm đang có. Chỉ phân loại tuổi/cỡ khi tên ghi rõ; còn lại để nhóm tổng quát. */
INSERT INTO dbo.GoiYThucAn(maSanPham, loaiThuCung, nhomTuoi, kichThuoc, ghiChu)
SELECT sp.maSanPham,
       CASE WHEN dm.tenDanhMuc LIKE N'%chó%' OR sp.tenSanPham LIKE N'%chó%' OR sp.tenSanPham LIKE N'%dog%' THEN N'Chó'
            WHEN dm.tenDanhMuc LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%cat%' THEN N'Mèo' END,
       CASE WHEN sp.tenSanPham LIKE N'%puppy%' OR sp.tenSanPham LIKE N'%kitten%' OR sp.tenSanPham LIKE N'%con non%' THEN N'Con non'
            WHEN sp.tenSanPham LIKE N'%senior%' OR sp.tenSanPham LIKE N'%ageing%' OR sp.tenSanPham LIKE N'%aging%' OR sp.tenSanPham LIKE N'%cao tuổi%' THEN N'Cao tuổi'
            WHEN sp.tenSanPham LIKE N'%adult%' OR sp.tenSanPham LIKE N'%trưởng thành%' THEN N'Trưởng thành'
            ELSE N'Chưa xác định' END,
       CASE WHEN sp.tenSanPham LIKE N'%mini%' OR sp.tenSanPham LIKE N'%small%' OR sp.tenSanPham LIKE N'%nhỏ%' THEN N'Nhỏ'
            WHEN sp.tenSanPham LIKE N'%medium%' OR sp.tenSanPham LIKE N'%trung bình%' THEN N'Trung bình'
            WHEN sp.tenSanPham LIKE N'%maxi%' OR sp.tenSanPham LIKE N'%large%' OR sp.tenSanPham LIKE N'%giant%' OR sp.tenSanPham LIKE N'%lớn%' THEN N'Lớn'
            ELSE N'Chưa xác định' END,
       N'Phân nhóm tự động từ tên/danh mục; đối chiếu bao bì khi tên không ghi đủ.'
FROM dbo.SanPham sp JOIN dbo.DanhMuc dm ON dm.maDanhMuc = sp.maDanhMuc
WHERE (dm.tenDanhMuc LIKE N'%thức ăn%' OR dm.tenDanhMuc LIKE N'%thuc an%')
  AND (dm.tenDanhMuc LIKE N'%chó%' OR dm.tenDanhMuc LIKE N'%mèo%' OR sp.tenSanPham LIKE N'%dog%' OR sp.tenSanPham LIKE N'%cat%'
       OR sp.tenSanPham LIKE N'%chó%' OR sp.tenSanPham LIKE N'%mèo%')
  AND NOT EXISTS (SELECT 1 FROM dbo.GoiYThucAn gy WHERE gy.maSanPham = sp.maSanPham);
GO
