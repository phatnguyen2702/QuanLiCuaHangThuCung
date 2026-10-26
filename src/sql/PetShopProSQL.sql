
USE [00];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Dung truoc khi tao bang neu DB khong con trong (bao ve du lieu co san).
IF EXISTS (
    SELECT 1 FROM sys.tables t
    WHERE t.is_ms_shipped = 0 AND SCHEMA_NAME(t.schema_id) = N'dbo'
)
    THROW 51001, N'Database [00] da co bang. Hay sao luu va dung script nang cap DB hien co, khong chay script tao moi.', 1;

BEGIN TRY
    BEGIN TRANSACTION;


CREATE TABLE DanhMuc (
    maDanhMuc VARCHAR(10) NOT NULL PRIMARY KEY,
    tenDanhMuc NVARCHAR(50) NULL,
    moTa NVARCHAR(255) NULL
);
CREATE TABLE Nhacungcap (
    maNhaCungCap VARCHAR(10) NOT NULL PRIMARY KEY,
    tenNhaCungCap NVARCHAR(50) NULL,
    soDienThoai VARCHAR(15) NULL,
    email VARCHAR(50) NULL,
    diaChi NVARCHAR(50) NULL
);
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(10) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(50) NULL,
    soDienThoai VARCHAR(15) NULL,
    email VARCHAR(30) NULL,
    matKhau NVARCHAR(255) NULL,
    gioiTinh NVARCHAR(10) NULL,
    vaiTro NVARCHAR(50) NULL
);
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(10) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(50) NOT NULL,
    soDienThoai VARCHAR(15) NOT NULL
);
CREATE TABLE ThuCung (
    maThuCung VARCHAR(10) NOT NULL PRIMARY KEY,
    tenThuCung NVARCHAR(50) NULL,
    loai NVARCHAR(25) NULL,
    giong NVARCHAR(25) NULL,
    gioiTinh NVARCHAR(10) NULL,
    canNang DECIMAL(5,2) NULL,
    maKhachHang VARCHAR(10) NOT NULL,
    trangThai NVARCHAR(50) NOT NULL DEFAULT N'Đang hoạt động',
    CONSTRAINT FK_ThuCung_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang)
);
CREATE TABLE DichVu (
    maDichVu VARCHAR(10) NOT NULL PRIMARY KEY,
    tenDichVu NVARCHAR(50) NULL,
    giaDichVu DECIMAL(18,2) NULL,
    moTa NVARCHAR(255) NULL
);
CREATE TABLE SanPham (
    maSanPham VARCHAR(10) NOT NULL PRIMARY KEY,
    tenSanPham NVARCHAR(50) NULL,
    giaNhap DECIMAL(18,2) NULL,
    giaBan DECIMAL(18,2) NULL,
    maDanhMuc VARCHAR(10) NOT NULL,
    maNhaCungCap VARCHAR(10) NOT NULL,

    CONSTRAINT FK_SanPham_DanhMuc
        FOREIGN KEY (maDanhMuc) REFERENCES DanhMuc(maDanhMuc),

    CONSTRAINT FK_SanPham_NhaCungCap
        FOREIGN KEY (maNhaCungCap) REFERENCES Nhacungcap(maNhaCungCap)
);
CREATE TABLE Vocher (
    maVoucher VARCHAR(10) NOT NULL PRIMARY KEY,
    tenVoucher NVARCHAR(50) NULL,
    phanTramGiam DECIMAL(5,2) NULL,
    dieuKien NVARCHAR(255) NULL,
    trangThai BIT NULL DEFAULT 1,
    ngayBatDau DATE NULL,
    ngayKetThuc DATE NULL,
    soLuongToiDa INT NULL
);
CREATE TABLE PhuongThucThanhToan (
    maPhuongThuc VARCHAR(10) NOT NULL PRIMARY KEY,
    tenPhuongThuc NVARCHAR(50) NULL,
    trangThai BIT NULL DEFAULT 1,
    ghiChu NVARCHAR(255) NULL
);
/* =======================================================
   2. CA LAM
======================================================= */

CREATE TABLE CaLam (
    maCa VARCHAR(10) NOT NULL PRIMARY KEY,
    tenCa NVARCHAR(50) NULL,
    gioBatDau TIME NULL,
    gioKetThuc TIME NULL
);
CREATE TABLE LichPhanCa (
    maPhanCa VARCHAR(10) NOT NULL PRIMARY KEY,
    ngayLamViec DATE NULL,
    maNhanVien VARCHAR(10) NOT NULL,
    maCa VARCHAR(10) NOT NULL,

    CONSTRAINT FK_LichPhanCa_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),

    CONSTRAINT FK_LichPhanCa_CaLam
        FOREIGN KEY (maCa) REFERENCES CaLam(maCa)
);
/* =======================================================
   3. KHACH HANG KHONG DUNG THE THANH VIEN
   - Khong tao bang TheThanhVien hay thong tin diem tich luy.
======================================================= */

/* =======================================================
   4. LICH HEN / SPA
======================================================= */

CREATE TABLE LichHen (
    maLichHen VARCHAR(10) NOT NULL PRIMARY KEY,
    ngayGioHen DATETIME2 NULL,
    ghiChu NVARCHAR(255) NULL,
    trangThai NVARCHAR(50) NULL DEFAULT N'Xác nhận',
    maKhachHang VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NOT NULL,
    maThuCung VARCHAR(10) NOT NULL,

    CONSTRAINT FK_LichHen_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),

    CONSTRAINT FK_LichHen_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),

    CONSTRAINT FK_LichHen_ThuCung
        FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung)
);
CREATE TABLE PhieuDichVu (
    maPhieuDichVu VARCHAR(10) NOT NULL PRIMARY KEY,
    trangThai BIT NULL DEFAULT 1,
    maKhachHang VARCHAR(10) NOT NULL,
    maThuCung VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NOT NULL,
    maLichHen VARCHAR(10) NULL,

    CONSTRAINT FK_PhieuDichVu_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),

    CONSTRAINT FK_PhieuDichVu_ThuCung
        FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),

    CONSTRAINT FK_PhieuDichVu_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),

    CONSTRAINT FK_PhieuDichVu_LichHen
        FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen)
);
CREATE TABLE ChiTietDichVu (
    maPhieuDichVu VARCHAR(10) NOT NULL,
    maDichVu VARCHAR(10) NOT NULL,
    soLuong INT NULL,
    donGia DECIMAL(18,2) NULL,
    ghiChu NVARCHAR(255) NULL,

    CONSTRAINT PK_ChiTietDichVu
        PRIMARY KEY (maPhieuDichVu, maDichVu),

    CONSTRAINT FK_ChiTietDichVu_Phieu
        FOREIGN KEY (maPhieuDichVu) REFERENCES PhieuDichVu(maPhieuDichVu),

    CONSTRAINT FK_ChiTietDichVu_DichVu
        FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu)
);
CREATE TABLE ChiTietLichHen (
    maLichHen VARCHAR(10) NOT NULL,
    maDichVu VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NULL,
    ghiChu NVARCHAR(255) NULL,

    CONSTRAINT PK_ChiTietLichHen
        PRIMARY KEY (maLichHen, maDichVu),

    CONSTRAINT FK_ChiTietLichHen_LichHen
        FOREIGN KEY (maLichHen)
        REFERENCES LichHen(maLichHen),

    CONSTRAINT FK_ChiTietLichHen_DichVu
        FOREIGN KEY (maDichVu)
        REFERENCES DichVu(maDichVu),

    CONSTRAINT FK_ChiTietLichHen_NhanVien
        FOREIGN KEY (maNhanVien)
        REFERENCES NhanVien(maNhanVien)
);
/* =======================================================
   5. HOA DON
======================================================= */

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(15) NOT NULL PRIMARY KEY,
    ngayLap DATETIME2 NULL,
    maPhuongThuc VARCHAR(10) NOT NULL,
    maKhachHang VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NOT NULL,
    maVoucher VARCHAR(10) NULL,

    CONSTRAINT FK_HoaDon_PhuongThuc
        FOREIGN KEY (maPhuongThuc) REFERENCES PhuongThucThanhToan(maPhuongThuc),

    CONSTRAINT FK_HoaDon_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),

    CONSTRAINT FK_HoaDon_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),

    CONSTRAINT FK_HoaDon_Voucher
        FOREIGN KEY (maVoucher) REFERENCES Vocher(maVoucher)
);
CREATE TABLE BoDemHoaDon (
    id INT NOT NULL PRIMARY KEY,
    giaTri INT NOT NULL,
    CONSTRAINT CK_BoDemHoaDon_id CHECK (id = 1),
    CONSTRAINT CK_BoDemHoaDon_giaTri CHECK (giaTri BETWEEN 0 AND 9999)
);
INSERT INTO BoDemHoaDon (id, giaTri) VALUES (1, 0);
/*
   Trong mo hinh goc, 3 cot maThuCung/maDichVu/maSanPham
   duoc danh dau NOT NULL. De giao dien ban hang co the tao
   dong san pham HOAC dong dich vu, o day cho phep NULL.
*/
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(15) NOT NULL,
    maThuCung VARCHAR(10) NULL,
    maDichVu VARCHAR(10) NULL,
    maSanPham VARCHAR(10) NULL,
    soLuong INT NULL,
    donGia DECIMAL(18,2) NULL,
    phanTramGiam DECIMAL(5,2) NULL DEFAULT 0,

    CONSTRAINT FK_CTHD_HoaDon
        FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),

    CONSTRAINT FK_CTHD_ThuCung
        FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),

    CONSTRAINT FK_CTHD_DichVu
        FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),

    CONSTRAINT FK_CTHD_SanPham
        FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
CREATE INDEX IX_ChiTietHoaDon_HoaDon
    ON ChiTietHoaDon(maHoaDon);
/* =======================================================
   6. NHAP KHO
======================================================= */

CREATE TABLE PhieuNhapKho (
    maPhieuNhap VARCHAR(10) NOT NULL PRIMARY KEY,
    ngayNhap DATETIME2 NULL,
    trangThai BIT NULL DEFAULT 1,
    maNhanVien VARCHAR(10) NOT NULL,
    maNhaCungCap VARCHAR(10) NOT NULL,

    CONSTRAINT FK_PhieuNhap_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),

    CONSTRAINT FK_PhieuNhap_NhaCungCap
        FOREIGN KEY (maNhaCungCap) REFERENCES Nhacungcap(maNhaCungCap)
);
CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(10) NOT NULL,
    maSanPham VARCHAR(10) NOT NULL,
    soLuongNhap INT NULL,
    donGiaNhap DECIMAL(18,2) NULL,
    trangThai BIT NULL DEFAULT 1,
    hanSuDung DATE NULL,

    CONSTRAINT PK_ChiTietPhieuNhap
        PRIMARY KEY (maPhieuNhap, maSanPham),

    CONSTRAINT FK_CTPN_PhieuNhap
        FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhapKho(maPhieuNhap),

    CONSTRAINT FK_CTPN_SanPham
        FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
/* =======================================================
   7. KIEM KE
======================================================= */

CREATE TABLE PhieuKiemKe (
    maPhieuKiemKe VARCHAR(10) NOT NULL PRIMARY KEY,
    ngayKiemKe DATETIME2 NULL,
    trangThai BIT NULL DEFAULT 1,
    maNhanVien VARCHAR(10) NOT NULL,

    CONSTRAINT FK_PhieuKiemKe_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
CREATE TABLE ChiTietPhieuKiemKe (
    maPhieuKiemKe VARCHAR(10) NOT NULL,
    maSanPham VARCHAR(10) NOT NULL,
    soLuongHeThong INT NULL,
    soLuongThucTe INT NULL,
    trangThai BIT NULL,

    CONSTRAINT PK_ChiTietPhieuKiemKe
        PRIMARY KEY (maPhieuKiemKe, maSanPham),

    CONSTRAINT FK_CTPKK_PhieuKiemKe
        FOREIGN KEY (maPhieuKiemKe) REFERENCES PhieuKiemKe(maPhieuKiemKe),

    CONSTRAINT FK_CTPKK_SanPham
        FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
/* =======================================================
   GOI Y THUC AN - TICH HOP MigrationGoiYThucAn.sql
======================================================= */
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

INSERT INTO NhanVien
(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro)
VALUES
('NV001', N'Đặng Đình An', '01239415224', 'dangdinhan01@gmail.com', '123456', N'Nam', N'Quan ly'),
('NV002', N'Nguyễn Minh Anh', '0901234567', 'minhanh@gmail.com', '123456', N'Nữ', N'Thu ngan'),
('NV003', N'Trần Quốc Bảo', '0912345678', 'quocbao@gmail.com', '123456', N'Nam', N'Nhan vien kho'),
('NV004', N'Lê Ngọc Mai', '0987654321', 'ngocmai@gmail.com', '123456', N'Nữ', N'Nhan vien Spa');
INSERT INTO DanhMuc
(maDanhMuc, tenDanhMuc, moTa)
VALUES
('DM001', N'Thức ăn cho chó', N'Thức ăn khô và ướt cho chó'),
('DM002', N'Thức ăn cho mèo', N'Thức ăn khô và ướt cho mèo'),
('DM003', N'Đồ chơi', N'Đồ chơi cho thú cưng'),
('DM004', N'Phụ kiện', N'Vòng cổ, dây dắt, bát ăn'),
('DM005', N'Chăm sóc', N'Sản phẩm vệ sinh và chăm sóc');
INSERT INTO Nhacungcap
(maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi)
VALUES
('NCC001', N'PetFood Việt Nam', '0901111111', 'petfood@gmail.com', N'TP. Hồ Chí Minh'),
('NCC002', N'Pet Care Việt', '0902222222', 'petcare@gmail.com', N'TP. Hồ Chí Minh'),
('NCC003', N'Happy Pet Supply', '0903333333', 'happypet@gmail.com', N'Bình Dương');
INSERT INTO SanPham
(maSanPham, tenSanPham, giaNhap, giaBan, maDanhMuc, maNhaCungCap)
VALUES
('SP001', N'Thức ăn chó Royal Canin', 180000, 250000, 'DM001', 'NCC001'),
('SP002', N'Thức ăn mèo Whiskas', 90000, 135000, 'DM002', 'NCC001'),
('SP003', N'Bóng cao su cho chó', 35000, 59000, 'DM003', 'NCC002'),
('SP004', N'Vòng cổ da cao cấp', 70000, 120000, 'DM004', 'NCC002'),
('SP005', N'Sữa tắm chó mèo', 80000, 145000, 'DM005', 'NCC003'),
('SP006', N'Cát vệ sinh cho mèo', 95000, 150000, 'DM005', 'NCC003'),
('SP007', N'Dây dắt thú cưng', 60000, 99000, 'DM004', 'NCC002'),
('SP008', N'Pate mèo cá ngừ', 25000, 39000, 'DM002', 'NCC001'),
('SP009', N'Royal Canin Mini Puppy cho chó con', 320000, 420000, 'DM001', 'NCC001'),
('SP010', N'Pedigree Puppy gà, gạo và rau củ', 115000, 155000, 'DM001', 'NCC001'),
('SP011', N'Pedigree Adult gà và rau củ', 125000, 169000, 'DM001', 'NCC001'),
('SP012', N'Royal Canin Maxi Adult cho chó lớn', 390000, 510000, 'DM001', 'NCC001'),
('SP013', N'WHISKAS Kitten cá biển và sữa', 95000, 129000, 'DM002', 'NCC001'),
('SP014', N'WHISKAS Kitten vị cá thu', 95000, 129000, 'DM002', 'NCC001'),
('SP015', N'Royal Canin Indoor 27 cho mèo trưởng thành', 340000, 450000, 'DM002', 'NCC001');
INSERT INTO KhachHang
(maKhachHang, hoTen, soDienThoai)
VALUES
('KH001', N'Nguyễn Văn Minh', '0905123456'),
('KH002', N'Trần Thị Lan', '0916123456'),
('KH003', N'Lê Hoàng Nam', '0927123456'),
('KH004', N'Phạm Ngọc Hà', '0938123456'),
('KH005', N'Đỗ Minh Khang', '0949123456');
INSERT INTO ThuCung
(maThuCung, tenThuCung, loai, giong, gioiTinh, canNang, maKhachHang)
VALUES
('TC001', N'Bông', N'Chó', N'Golden Retriever', N'Đực', 24.50, 'KH001'),
('TC002', N'Miu', N'Mèo', N'British Shorthair', N'Cái', 4.20, 'KH002'),
('TC003', N'Lucky', N'Chó', N'Poodle', N'Đực', 7.80, 'KH003'),
('TC004', N'Nana', N'Mèo', N'Mèo Anh lông ngắn', N'Cái', 3.90, 'KH004'),
('TC005', N'Milo', N'Chó', N'Husky', N'Đực', 22.30, 'KH005');
INSERT INTO DichVu
(maDichVu, tenDichVu, giaDichVu, moTa)
VALUES
('DV001', N'Tắm thú cưng', 120000, N'Tắm và sấy cơ bản'),
('DV002', N'Cắt tỉa lông', 180000, N'Cắt tỉa theo yêu cầu'),
('DV003', N'Vệ sinh tai', 60000, N'Vệ sinh tai cho chó mèo'),
('DV004', N'Cắt móng', 50000, N'Cắt móng và vệ sinh chân'),
('DV005', N'Combo Spa Premium', 350000, N'Tắm, sấy, cắt tỉa và vệ sinh');
-- VC002 chi duoc giu lai de bao toan hoa don mau cu, khong ap dung cho don moi.
INSERT INTO Vocher
(maVoucher, tenVoucher, phanTramGiam, dieuKien, trangThai,
 ngayBatDau, ngayKetThuc, soLuongToiDa)
VALUES
('VC001', N'WELCOME10', 10, N'Đơn hàng từ 300000', 1,
 '2026-09-01', '2026-12-31', 200),
('VC002', N'Ưu đãi cũ 15%', 15, N'Đã ngừng áp dụng', 0,
 '2026-01-01', '2026-12-31', NULL),
('VC003', N'PETDAY20', 20, N'Chương trình Pet Day', 1,
 '2026-11-01', '2026-11-30', 100);
INSERT INTO PhuongThucThanhToan
(maPhuongThuc, tenPhuongThuc, trangThai, ghiChu)
VALUES
('PT001', N'Tiền mặt', 1, N'Thanh toán tiền mặt tại quầy'),
('PT002', N'Chuyển khoản', 1, N'Chuyển khoản ngân hàng'),
('PT003', N'Khác', 1, N'Giữ dữ liệu mẫu của hóa đơn HD004; cần xác nhận loại thực tế');
INSERT INTO CaLam
(maCa, tenCa, gioBatDau, gioKetThuc)
VALUES
('CA001', N'Ca sáng', '06:00', '14:00'),
('CA002', N'Ca chiều', '14:00', '22:00');
INSERT INTO LichPhanCa
(maPhanCa, ngayLamViec, maNhanVien, maCa)
VALUES
('PC001', '2026-10-07', 'NV001', 'CA001'),
('PC002', '2026-10-07', 'NV002', 'CA001'),
('PC003', '2026-10-07', 'NV003', 'CA002'),
('PC004', '2026-10-07', 'NV004', 'CA002');
INSERT INTO LichHen
(maLichHen, ngayGioHen, ghiChu, maKhachHang, maNhanVien, maThuCung)
VALUES
('LH001', '2026-10-07 09:00:00', N'Tắm và cắt tỉa', 'KH001', 'NV004', 'TC001'),
('LH002', '2026-10-07 10:30:00', N'Vệ sinh tai', 'KH002', 'NV004', 'TC002'),
('LH003', '2026-10-07 14:00:00', N'Combo Spa', 'KH003', 'NV004', 'TC003'),
('LH004', '2026-10-08 15:00:00', N'Cắt móng', 'KH004', 'NV004', 'TC004');
INSERT INTO ChiTietLichHen
    (maLichHen, maDichVu, maNhanVien, ghiChu)
VALUES
    ('LH001', 'DV001', 'NV004', N'Không dùng nước hoa'),
    ('LH001', 'DV002', 'NV004', N'Cắt gọn'),
    ('LH002', 'DV003', 'NV004', N'Nhẹ nhàng'),
    ('LH003', 'DV005', 'NV004', N'Khách đặt trước'),
    ('LH004', 'DV004', 'NV004', N'Kiểm tra chân');
INSERT INTO PhieuDichVu
(maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien, maLichHen)
VALUES
('PDV001', 1, 'KH001', 'TC001', 'NV004', 'LH001'),
('PDV002', 1, 'KH002', 'TC002', 'NV004', NULL),
('PDV003', 1, 'KH003', 'TC003', 'NV004', NULL);
INSERT INTO ChiTietDichVu
(maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu)
VALUES
('PDV001', 'DV001', 1, 120000, N'Đã hoàn thành'),
('PDV001', 'DV002', 1, 180000, N'Đã hoàn thành'),
('PDV002', 'DV003', 1, 60000, N'Đã hoàn thành'),
('PDV003', 'DV005', 1, 350000, N'Đang xử lý');
INSERT INTO HoaDon
(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, maNhanVien, maVoucher)
VALUES
('HD0010710260001', '2026-10-07 08:30:00', 'PT001', 'KH001', 'NV002', NULL),
('HD0020710260002', '2026-10-07 09:15:00', 'PT002', 'KH002', 'NV002', 'VC001'),
('HD0030710260003', '2026-10-07 10:20:00', 'PT001', 'KH003', 'NV002', 'VC002'),
('HD0040610260004', '2026-10-06 14:10:00', 'PT003', 'KH004', 'NV002', NULL),
('HD0050610260005', '2026-10-06 16:40:00', 'PT001', 'KH005', 'NV002', NULL);
UPDATE BoDemHoaDon
SET giaTri = (
    SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(maHoaDon, 4))), 0)
    FROM HoaDon
)
WHERE id = 1;
INSERT INTO ChiTietHoaDon
(maHoaDon, maThuCung, maDichVu, maSanPham, soLuong, donGia, phanTramGiam)
VALUES
('HD0010710260001', NULL, NULL, 'SP001', 1, 250000, 0),
('HD0010710260001', NULL, NULL, 'SP003', 2, 59000, 0),

('HD0020710260002', NULL, NULL, 'SP002', 2, 135000, 10),
('HD0020710260002', NULL, NULL, 'SP008', 3, 39000, 10),

('HD0030710260003', 'TC003', 'DV001', NULL, 1, 120000, 15),
('HD0030710260003', 'TC003', 'DV002', NULL, 1, 180000, 15),

('HD0040610260004', NULL, NULL, 'SP005', 1, 145000, 0),
('HD0040610260004', NULL, NULL, 'SP006', 1, 150000, 0),

('HD0050610260005', NULL, NULL, 'SP004', 1, 120000, 0),
('HD0050610260005', NULL, NULL, 'SP007', 1, 99000, 0);
INSERT INTO PhieuNhapKho
(maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
VALUES
('PN001', '2026-10-01 08:00:00', 1, 'NV003', 'NCC001'),
('PN002', '2026-10-03 09:00:00', 1, 'NV003', 'NCC002'),
('PN003', '2026-10-05 10:00:00', 1, 'NV003', 'NCC003'),
('PN004', GETDATE(), 1, 'NV003', 'NCC001');
INSERT INTO ChiTietPhieuNhap
(maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai, hanSuDung)
VALUES
('PN001', 'SP001', 30, 180000, 1, DATEADD(day, 12, CAST(GETDATE() AS date))),
('PN001', 'SP002', 40, 90000, 1, DATEADD(day, -5, CAST(GETDATE() AS date))),
('PN001', 'SP008', 50, 25000, 1, DATEADD(day, 75, CAST(GETDATE() AS date))),
('PN002', 'SP003', 20, 35000, 1, NULL),
('PN002', 'SP004', 15, 70000, 1, NULL),
('PN002', 'SP007', 20, 60000, 1, NULL),
('PN003', 'SP005', 12, 80000, 1, NULL),
('PN003', 'SP006', 18, 95000, 1, NULL),
('PN004', 'SP009', 20, 320000, 1, DATEADD(day, 5, CAST(GETDATE() AS date))),
('PN004', 'SP010', 20, 115000, 1, DATEADD(day, 10, CAST(GETDATE() AS date))),
('PN004', 'SP011', 20, 125000, 1, DATEADD(day, 15, CAST(GETDATE() AS date))),
('PN004', 'SP012', 20, 390000, 1, DATEADD(day, 20, CAST(GETDATE() AS date))),
('PN004', 'SP013', 20, 95000, 1, DATEADD(day, -2, CAST(GETDATE() AS date))),
('PN004', 'SP014', 20, 95000, 1, DATEADD(day, 25, CAST(GETDATE() AS date))),
('PN004', 'SP015', 20, 340000, 1, DATEADD(day, 30, CAST(GETDATE() AS date)));
INSERT INTO PhieuKiemKe
(maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien)
VALUES
('KK001', '2026-10-06 17:00:00', 1, 'NV003');
INSERT INTO ChiTietPhieuKiemKe
(maPhieuKiemKe, maSanPham, soLuongHeThong, soLuongThucTe, trangThai)
VALUES
('KK001', 'SP001', 29, 29, 1),
('KK001', 'SP002', 38, 37, 0),
('KK001', 'SP003', 18, 18, 1),
('KK001', 'SP004', 14, 14, 1),
('KK001', 'SP005', 11, 10, 0),
('KK001', 'SP006', 17, 17, 1);

/* Phan loai ro 5 san pham cho va 5 san pham meo cho man hinh goi y. */
INSERT INTO dbo.GoiYThucAn(maSanPham, loaiThuCung, nhomTuoi, kichThuoc, ghiChu)
VALUES
('SP001', N'Chó', N'Mọi độ tuổi', N'Mọi kích thước', N'Thức ăn chó; xem hướng dẫn độ tuổi trên bao bì.'),
('SP009', N'Chó', N'Con non', N'Nhỏ', N'Royal Canin Mini Puppy; tên dòng tham khảo từ hãng.'),
('SP010', N'Chó', N'Con non', N'Mọi kích thước', N'Pedigree Puppy; tên dòng tham khảo từ hãng.'),
('SP011', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Pedigree Adult; tên dòng tham khảo từ hãng.'),
('SP012', N'Chó', N'Trưởng thành', N'Lớn', N'Royal Canin Maxi Adult; tên dòng tham khảo từ hãng.'),
('SP002', N'Mèo', N'Mọi độ tuổi', N'Mọi kích thước', N'Thức ăn mèo; xem hướng dẫn độ tuổi trên bao bì.'),
('SP008', N'Mèo', N'Mọi độ tuổi', N'Mọi kích thước', N'Pate mèo; kiểm tra hướng dẫn trên bao bì.'),
('SP013', N'Mèo', N'Con non', N'Mọi kích thước', N'WHISKAS Kitten Ocean Fish with Milk; tên dòng tham khảo từ hãng.'),
('SP014', N'Mèo', N'Con non', N'Mọi kích thước', N'WHISKAS Kitten Mackerel; tên dòng tham khảo từ hãng.'),
('SP015', N'Mèo', N'Trưởng thành', N'Mọi kích thước', N'Royal Canin Indoor 27; sản phẩm mèo trưởng thành trong nhà.');


/* =======================================================
   BO SUNG MIGRATION GOI Y THUC AN - PHAN LOAI TU DONG
   Chi them san pham thuc an CHUA NAM trong du lieu mau o tren.
   Khong tao bang/chen lai 10 ban ghi duoc khai bao truoc do.
======================================================= */
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

    COMMIT TRANSACTION;
    PRINT N'Tao database Pet Shop Pro va nap du lieu mau thanh cong.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- KIEM TRA CAU TRUC VA SO BAN GHI
SELECT c.name AS tenCot, ty.name AS kieuDuLieu, c.max_length AS soByte
FROM sys.columns AS c
JOIN sys.types AS ty ON c.user_type_id = ty.user_type_id
WHERE c.object_id = OBJECT_ID(N'dbo.KhachHang')
ORDER BY c.column_id;
SELECT N'KhachHang' AS bang, COUNT(*) AS soDong FROM dbo.KhachHang
UNION ALL SELECT N'NhanVien', COUNT(*) FROM dbo.NhanVien
UNION ALL SELECT N'HoaDon', COUNT(*) FROM dbo.HoaDon
UNION ALL SELECT N'GoiYThucAn', COUNT(*) FROM dbo.GoiYThucAn;
GO


-- ===============================================================
-- PET SHOP PRO - BO SUNG DU LIEU MAU CHO DATABASE HIEN CO
-- Duoc tao tu: QuanLiCuaHangThuCung(8).zip / PetShopPro_final.sql
-- 12 dong cho 22 bang nghiep vu; bang BoDemHoaDon giu 1 dong.
-- Idempotent: chay lai khong chen trung theo khoa mau.
-- KHONG DROP TABLE / KHONG DELETE / KHONG UPDATE du lieu nghiep vu cu.
-- TEST DATA ONLY: nhan vien moi co mat khau mau Demo@2026.
-- Lich Spa tuong lai duoc tinh theo ngay chay SQL Server.
-- ===============================================================
USE [00];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.NhanVien', 'U') IS NULL OR OBJECT_ID(N'dbo.GoiYThucAn', 'U') IS NULL OR OBJECT_ID(N'dbo.BoDemHoaDon', 'U') IS NULL
    THROW 51042, N'Chua co day du bang Pet Shop Pro. Hay chay script tao database goc truoc.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    -- DanhMuc : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.DanhMuc AS d
        INNER JOIN (VALUES
            (N'DM006', N'Thức ăn cho chó con'),
            (N'DM007', N'Thức ăn cho chó trưởng thành'),
            (N'DM008', N'Thức ăn cho mèo con'),
            (N'DM009', N'Thức ăn cho mèo trưởng thành'),
            (N'DM010', N'Đồ chơi vận động'),
            (N'DM011', N'Phụ kiện đi dạo'),
            (N'DM012', N'Chăm sóc răng miệng'),
            (N'DM013', N'Dụng cụ ăn uống'),
            (N'DM014', N'Giường và nệm'),
            (N'DM015', N'Chăm sóc lông'),
            (N'DM016', N'Đồ chơi mèo'),
            (N'DM017', N'Vệ sinh thú cưng')
        ) AS v([maDanhMuc], [tenDanhMuc])
        ON d.[maDanhMuc] = v.[maDanhMuc]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenDanhMuc]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenDanhMuc]), N'')))
        THROW 51044, N'Ma du lieu mau o bang DanhMuc da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.DanhMuc ([maDanhMuc], [tenDanhMuc], [moTa])
    SELECT v.[maDanhMuc], v.[tenDanhMuc], v.[moTa]
    FROM (VALUES
        (N'DM006', N'Thức ăn cho chó con', N'Thức ăn chuyên cho chó con'),
        (N'DM007', N'Thức ăn cho chó trưởng thành', N'Thức ăn dành cho chó trưởng thành'),
        (N'DM008', N'Thức ăn cho mèo con', N'Dinh dưỡng cho mèo con'),
        (N'DM009', N'Thức ăn cho mèo trưởng thành', N'Dinh dưỡng cho mèo trưởng thành'),
        (N'DM010', N'Đồ chơi vận động', N'Bóng và đồ chơi hoạt động'),
        (N'DM011', N'Phụ kiện đi dạo', N'Dây dắt và phụ kiện'),
        (N'DM012', N'Chăm sóc răng miệng', N'Bàn chải và sản phẩm vệ sinh'),
        (N'DM013', N'Dụng cụ ăn uống', N'Bát ăn và bình nước'),
        (N'DM014', N'Giường và nệm', N'Nệm nằm cho thú cưng'),
        (N'DM015', N'Chăm sóc lông', N'Lược và phụ kiện grooming'),
        (N'DM016', N'Đồ chơi mèo', N'Đồ chơi cho mèo'),
        (N'DM017', N'Vệ sinh thú cưng', N'Sản phẩm vệ sinh')
    ) AS v([maDanhMuc], [tenDanhMuc], [moTa])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.DanhMuc AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maDanhMuc] = v.[maDanhMuc]);

    -- Nhacungcap : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.Nhacungcap AS d
        INNER JOIN (VALUES
            ('NCC004', N'Hải Âu Pet Supply'),
            ('NCC005', N'Ngôi Nhà Thú Cưng'),
            ('NCC006', N'Mây Pet Distribution'),
            ('NCC007', N'Minh Châu PetCare'),
            ('NCC008', N'Bốn Chân Supply'),
            ('NCC009', N'Lá Xanh Pet Foods'),
            ('NCC010', N'An Nhiên Pet Shop'),
            ('NCC011', N'Chó Mèo Việt Supply'),
            ('NCC012', N'Cún Miu Logistics'),
            ('NCC013', N'Biển Xanh Pet Food'),
            ('NCC014', N'Pet Home Distribution'),
            ('NCC015', N'Mầm Xanh Pets')
        ) AS v([maNhaCungCap], [tenNhaCungCap])
        ON d.[maNhaCungCap] = v.[maNhaCungCap]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenNhaCungCap]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenNhaCungCap]), N'')))
        THROW 51044, N'Ma du lieu mau o bang Nhacungcap da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.Nhacungcap ([maNhaCungCap], [tenNhaCungCap], [soDienThoai], [email], [diaChi])
    SELECT v.[maNhaCungCap], v.[tenNhaCungCap], v.[soDienThoai], v.[email], v.[diaChi]
    FROM (VALUES
        ('NCC004', N'Hải Âu Pet Supply', '0988000004', 'ncc004@example.com', N'Bình Dương'),
        ('NCC005', N'Ngôi Nhà Thú Cưng', '0988000005', 'ncc005@example.com', N'TP. Hồ Chí Minh'),
        ('NCC006', N'Mây Pet Distribution', '0988000006', 'ncc006@example.com', N'Bình Dương'),
        ('NCC007', N'Minh Châu PetCare', '0988000007', 'ncc007@example.com', N'TP. Hồ Chí Minh'),
        ('NCC008', N'Bốn Chân Supply', '0988000008', 'ncc008@example.com', N'Bình Dương'),
        ('NCC009', N'Lá Xanh Pet Foods', '0988000009', 'ncc009@example.com', N'TP. Hồ Chí Minh'),
        ('NCC010', N'An Nhiên Pet Shop', '0988000010', 'ncc010@example.com', N'Bình Dương'),
        ('NCC011', N'Chó Mèo Việt Supply', '0988000011', 'ncc011@example.com', N'TP. Hồ Chí Minh'),
        ('NCC012', N'Cún Miu Logistics', '0988000012', 'ncc012@example.com', N'Bình Dương'),
        ('NCC013', N'Biển Xanh Pet Food', '0988000013', 'ncc013@example.com', N'TP. Hồ Chí Minh'),
        ('NCC014', N'Pet Home Distribution', '0988000014', 'ncc014@example.com', N'Bình Dương'),
        ('NCC015', N'Mầm Xanh Pets', '0988000015', 'ncc015@example.com', N'TP. Hồ Chí Minh')
    ) AS v([maNhaCungCap], [tenNhaCungCap], [soDienThoai], [email], [diaChi])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Nhacungcap AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maNhaCungCap] = v.[maNhaCungCap]);

    -- NhanVien : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.NhanVien AS d
        INNER JOIN (VALUES
            ('NV005', N'Nguyễn Hà Vy'),
            ('NV006', N'Phạm Gia Hân'),
            ('NV007', N'Bùi Tuấn Kiệt'),
            ('NV008', N'Hoàng Minh Tâm'),
            ('NV009', N'Đinh Thu Trang'),
            ('NV010', N'Đỗ Quốc Việt'),
            ('NV011', N'Trương Anh Khoa'),
            ('NV012', N'Võ Thanh Hà'),
            ('NV013', N'Lâm Bảo Trân'),
            ('NV014', N'Nguyễn Anh Thư'),
            ('NV015', N'Trần Gia Phúc'),
            ('NV016', N'Huỳnh Ngọc Bích')
        ) AS v([maNhanVien], [hoTen])
        ON d.[maNhanVien] = v.[maNhanVien]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[hoTen]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[hoTen]), N'')))
        THROW 51044, N'Ma du lieu mau o bang NhanVien da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.NhanVien ([maNhanVien], [hoTen], [soDienThoai], [email], [matKhau], [gioiTinh], [vaiTro])
    SELECT v.[maNhanVien], v.[hoTen], v.[soDienThoai], v.[email], v.[matKhau], v.[gioiTinh], v.[vaiTro]
    FROM (VALUES
        ('NV005', N'Nguyễn Hà Vy', '0971000005', 'nv005@example.com', N'Demo@2026', N'Nam', N'Quan ly'),
        ('NV006', N'Phạm Gia Hân', '0971000006', 'nv006@example.com', N'Demo@2026', N'Nữ', N'Thu ngan'),
        ('NV007', N'Bùi Tuấn Kiệt', '0971000007', 'nv007@example.com', N'Demo@2026', N'Nam', N'Thu ngan'),
        ('NV008', N'Hoàng Minh Tâm', '0971000008', 'nv008@example.com', N'Demo@2026', N'Nữ', N'Thu ngan'),
        ('NV009', N'Đinh Thu Trang', '0971000009', 'nv009@example.com', N'Demo@2026', N'Nam', N'Thu ngan'),
        ('NV010', N'Đỗ Quốc Việt', '0971000010', 'nv010@example.com', N'Demo@2026', N'Nữ', N'Nhan vien kho'),
        ('NV011', N'Trương Anh Khoa', '0971000011', 'nv011@example.com', N'Demo@2026', N'Nam', N'Nhan vien kho'),
        ('NV012', N'Võ Thanh Hà', '0971000012', 'nv012@example.com', N'Demo@2026', N'Nữ', N'Nhan vien kho'),
        ('NV013', N'Lâm Bảo Trân', '0971000013', 'nv013@example.com', N'Demo@2026', N'Nam', N'Nhan vien Spa'),
        ('NV014', N'Nguyễn Anh Thư', '0971000014', 'nv014@example.com', N'Demo@2026', N'Nữ', N'Nhan vien Spa'),
        ('NV015', N'Trần Gia Phúc', '0971000015', 'nv015@example.com', N'Demo@2026', N'Nam', N'Nhan vien Spa'),
        ('NV016', N'Huỳnh Ngọc Bích', '0971000016', 'nv016@example.com', N'Demo@2026', N'Nữ', N'Nhan vien Spa')
    ) AS v([maNhanVien], [hoTen], [soDienThoai], [email], [matKhau], [gioiTinh], [vaiTro])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.NhanVien AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maNhanVien] = v.[maNhanVien]);

    -- KhachHang : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.KhachHang AS d
        INNER JOIN (VALUES
            ('KH006', N'Lê Thanh Bình'),
            ('KH007', N'Đỗ Khánh Ngọc'),
            ('KH008', N'Nguyễn Quốc Huy'),
            ('KH009', N'Trần Mỹ Linh'),
            ('KH010', N'Phạm Duy Long'),
            ('KH011', N'Vũ Thu Hà'),
            ('KH012', N'Ngô Minh Đức'),
            ('KH013', N'Lý Bảo Châu'),
            ('KH014', N'Hồ Anh Quân'),
            ('KH015', N'Trịnh Thảo My'),
            ('KH016', N'Phan Việt Anh'),
            ('KH017', N'Dương Hoài Nam')
        ) AS v([maKhachHang], [hoTen])
        ON d.[maKhachHang] = v.[maKhachHang]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[hoTen]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[hoTen]), N'')))
        THROW 51044, N'Ma du lieu mau o bang KhachHang da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.KhachHang ([maKhachHang], [hoTen], [soDienThoai])
    SELECT v.[maKhachHang], v.[hoTen], v.[soDienThoai]
    FROM (VALUES
        ('KH006', N'Lê Thanh Bình', '0962000006'),
        ('KH007', N'Đỗ Khánh Ngọc', '0962000007'),
        ('KH008', N'Nguyễn Quốc Huy', '0962000008'),
        ('KH009', N'Trần Mỹ Linh', '0962000009'),
        ('KH010', N'Phạm Duy Long', '0962000010'),
        ('KH011', N'Vũ Thu Hà', '0962000011'),
        ('KH012', N'Ngô Minh Đức', '0962000012'),
        ('KH013', N'Lý Bảo Châu', '0962000013'),
        ('KH014', N'Hồ Anh Quân', '0962000014'),
        ('KH015', N'Trịnh Thảo My', '0962000015'),
        ('KH016', N'Phan Việt Anh', '0962000016'),
        ('KH017', N'Dương Hoài Nam', '0962000017')
    ) AS v([maKhachHang], [hoTen], [soDienThoai])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.KhachHang AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maKhachHang] = v.[maKhachHang]);

    -- ThuCung : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ThuCung AS d
        INNER JOIN (VALUES
            ('TC006', N'Mochi', 'KH006'),
            ('TC007', N'Bắp', 'KH007'),
            ('TC008', N'Bơ', 'KH008'),
            ('TC009', N'Luna', 'KH009'),
            ('TC010', N'Mít', 'KH010'),
            ('TC011', N'Sữa', 'KH011'),
            ('TC012', N'Toto', 'KH012'),
            ('TC013', N'Gấu', 'KH013'),
            ('TC014', N'Mây', 'KH014'),
            ('TC015', N'Nâu', 'KH015'),
            ('TC016', N'Nấm', 'KH016'),
            ('TC017', N'Ken', 'KH017')
        ) AS v([maThuCung], [tenThuCung], [maKhachHang])
        ON d.[maThuCung] = v.[maThuCung]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenThuCung]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenThuCung]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maKhachHang]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maKhachHang]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ThuCung da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ThuCung ([maThuCung], [tenThuCung], [loai], [giong], [gioiTinh], [canNang], [maKhachHang], [trangThai])
    SELECT v.[maThuCung], v.[tenThuCung], v.[loai], v.[giong], v.[gioiTinh], v.[canNang], v.[maKhachHang], v.[trangThai]
    FROM (VALUES
        ('TC006', N'Mochi', N'Chó', N'Poodle', N'Đực', 3.2, 'KH006', N'Đang hoạt động'),
        ('TC007', N'Bắp', N'Chó', N'Golden Retriever', N'Cái', 4.85, 'KH007', N'Đang hoạt động'),
        ('TC008', N'Bơ', N'Chó', N'Corgi', N'Đực', 6.5, 'KH008', N'Đang hoạt động'),
        ('TC009', N'Luna', N'Mèo', N'Mèo Anh lông ngắn', N'Cái', 3.52, 'KH009', N'Đang hoạt động'),
        ('TC010', N'Mít', N'Chó', N'Beagle', N'Đực', 9.8, 'KH010', N'Đang hoạt động'),
        ('TC011', N'Sữa', N'Mèo', N'Ragdoll', N'Cái', 3.8, 'KH011', N'Đang hoạt động'),
        ('TC012', N'Toto', N'Chó', N'Pomeranian', N'Đực', 13.1, 'KH012', N'Đang hoạt động'),
        ('TC013', N'Gấu', N'Chó', N'Husky', N'Cái', 14.75, 'KH013', N'Đang hoạt động'),
        ('TC014', N'Mây', N'Mèo', N'Mèo Ba Tư', N'Đực', 4.22, 'KH014', N'Đang hoạt động'),
        ('TC015', N'Nâu', N'Chó', N'Chihuahua', N'Cái', 18.05, 'KH015', N'Đang hoạt động'),
        ('TC016', N'Nấm', N'Mèo', N'Scottish Fold', N'Đực', 4.5, 'KH016', N'Đang hoạt động'),
        ('TC017', N'Ken', N'Chó', N'Shiba Inu', N'Cái', 21.35, 'KH017', N'Đang hoạt động')
    ) AS v([maThuCung], [tenThuCung], [loai], [giong], [gioiTinh], [canNang], [maKhachHang], [trangThai])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ThuCung AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maThuCung] = v.[maThuCung]);

    -- DichVu : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.DichVu AS d
        INNER JOIN (VALUES
            ('DV006', N'Vắt tuyến hôi'),
            ('DV007', N'Gỡ rối lông'),
            ('DV008', N'Dưỡng lông chuyên sâu'),
            ('DV009', N'Spa thư giãn'),
            ('DV010', N'Tắm khử mùi'),
            ('DV011', N'Vệ sinh mắt'),
            ('DV012', N'Đánh răng thú cưng'),
            ('DV013', N'Cắt tỉa tạo kiểu'),
            ('DV014', N'Vệ sinh chân'),
            ('DV015', N'Chăm sóc da'),
            ('DV016', N'Tắm mèo'),
            ('DV017', N'Combo Spa cơ bản')
        ) AS v([maDichVu], [tenDichVu])
        ON d.[maDichVu] = v.[maDichVu]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenDichVu]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenDichVu]), N'')))
        THROW 51044, N'Ma du lieu mau o bang DichVu da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.DichVu ([maDichVu], [tenDichVu], [giaDichVu], [moTa])
    SELECT v.[maDichVu], v.[tenDichVu], v.[giaDichVu], v.[moTa]
    FROM (VALUES
        ('DV006', N'Vắt tuyến hôi', 75000, N'Chăm sóc vệ sinh cơ bản'),
        ('DV007', N'Gỡ rối lông', 160000, N'Gỡ rối trước khi tắm'),
        ('DV008', N'Dưỡng lông chuyên sâu', 190000, N'Dưỡng lông sau khi tắm'),
        ('DV009', N'Spa thư giãn', 230000, N'Massage nhẹ nhàng và làm sạch'),
        ('DV010', N'Tắm khử mùi', 150000, N'Tắm và khử mùi'),
        ('DV011', N'Vệ sinh mắt', 45000, N'Vệ sinh mắt cơ bản'),
        ('DV012', N'Đánh răng thú cưng', 65000, N'Vệ sinh răng miệng'),
        ('DV013', N'Cắt tỉa tạo kiểu', 250000, N'Tạo kiểu lông theo yêu cầu'),
        ('DV014', N'Vệ sinh chân', 55000, N'Làm sạch bàn chân'),
        ('DV015', N'Chăm sóc da', 200000, N'Chăm sóc da thông thường'),
        ('DV016', N'Tắm mèo', 170000, N'Tắm sấy nhẹ nhàng cho mèo'),
        ('DV017', N'Combo Spa cơ bản', 280000, N'Tắm, sấy và vệ sinh tai')
    ) AS v([maDichVu], [tenDichVu], [giaDichVu], [moTa])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.DichVu AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maDichVu] = v.[maDichVu]);

    -- SanPham : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.SanPham AS d
        INNER JOIN (VALUES
            ('SP016', N'Thức ăn chó con vị gà 1kg'),
            ('SP017', N'Thức ăn chó con vị cá 1kg'),
            ('SP018', N'Thức ăn chó trưởng thành 2kg'),
            ('SP019', N'Pate chó trưởng thành 400g'),
            ('SP020', N'Thức ăn mèo con vị cá 1kg'),
            ('SP021', N'Hạt mèo con vị gà 1kg'),
            ('SP022', N'Hạt mèo trưởng thành 2kg'),
            ('SP023', N'Pate mèo trưởng thành 400g'),
            ('SP024', N'Bóng nảy cho chó cỡ nhỏ'),
            ('SP025', N'Dây dắt phản quang'),
            ('SP026', N'Bàn chải răng thú cưng'),
            ('SP027', N'Bát inox chống trượt')
        ) AS v([maSanPham], [tenSanPham])
        ON d.[maSanPham] = v.[maSanPham]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenSanPham]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenSanPham]), N'')))
        THROW 51044, N'Ma du lieu mau o bang SanPham da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.SanPham ([maSanPham], [tenSanPham], [giaNhap], [giaBan], [maDanhMuc], [maNhaCungCap])
    SELECT v.[maSanPham], v.[tenSanPham], v.[giaNhap], v.[giaBan], v.[maDanhMuc], v.[maNhaCungCap]
    FROM (VALUES
        ('SP016', N'Thức ăn chó con vị gà 1kg', 80000, 119000, 'DM006', 'NCC004'),
        ('SP017', N'Thức ăn chó con vị cá 1kg', 87000, 129000, 'DM006', 'NCC005'),
        ('SP018', N'Thức ăn chó trưởng thành 2kg', 160000, 219000, 'DM007', 'NCC006'),
        ('SP019', N'Pate chó trưởng thành 400g', 32000, 49000, 'DM007', 'NCC007'),
        ('SP020', N'Thức ăn mèo con vị cá 1kg', 89000, 135000, 'DM008', 'NCC008'),
        ('SP021', N'Hạt mèo con vị gà 1kg', 92000, 139000, 'DM008', 'NCC009'),
        ('SP022', N'Hạt mèo trưởng thành 2kg', 170000, 235000, 'DM009', 'NCC010'),
        ('SP023', N'Pate mèo trưởng thành 400g', 34000, 52000, 'DM009', 'NCC011'),
        ('SP024', N'Bóng nảy cho chó cỡ nhỏ', 25000, 45000, 'DM010', 'NCC012'),
        ('SP025', N'Dây dắt phản quang', 65000, 105000, 'DM011', 'NCC013'),
        ('SP026', N'Bàn chải răng thú cưng', 35000, 60000, 'DM012', 'NCC014'),
        ('SP027', N'Bát inox chống trượt', 48000, 79000, 'DM013', 'NCC015')
    ) AS v([maSanPham], [tenSanPham], [giaNhap], [giaBan], [maDanhMuc], [maNhaCungCap])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.SanPham AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maSanPham] = v.[maSanPham]);

    -- Vocher : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.Vocher AS d
        INNER JOIN (VALUES
            ('VC004', N'DEMO5_004'),
            ('VC005', N'DEMO7_005'),
            ('VC006', N'DEMO8_006'),
            ('VC007', N'DEMO10_007'),
            ('VC008', N'DEMO12_008'),
            ('VC009', N'DEMO15_009'),
            ('VC010', N'DEMO5_010'),
            ('VC011', N'DEMO10_011'),
            ('VC012', N'DEMO6_012'),
            ('VC013', N'DEMO8_013'),
            ('VC014', N'DEMO12_014'),
            ('VC015', N'DEMO20_015')
        ) AS v([maVoucher], [tenVoucher])
        ON d.[maVoucher] = v.[maVoucher]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenVoucher]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenVoucher]), N'')))
        THROW 51044, N'Ma du lieu mau o bang Vocher da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.Vocher ([maVoucher], [tenVoucher], [phanTramGiam], [dieuKien], [trangThai], [ngayBatDau], [ngayKetThuc], [soLuongToiDa])
    SELECT v.[maVoucher], v.[tenVoucher], v.[phanTramGiam], v.[dieuKien], v.[trangThai], v.[ngayBatDau], v.[ngayKetThuc], v.[soLuongToiDa]
    FROM (VALUES
        ('VC004', N'DEMO5_004', 5, N'Đơn hàng từ 150000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 50),
        ('VC005', N'DEMO7_005', 7, N'Đơn hàng từ 250000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 60),
        ('VC006', N'DEMO8_006', 8, N'Đơn hàng từ 350000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 70),
        ('VC007', N'DEMO10_007', 10, N'Đơn hàng từ 450000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 80),
        ('VC008', N'DEMO12_008', 12, N'Đơn hàng từ 550000', 0, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 90),
        ('VC009', N'DEMO15_009', 15, N'Đơn hàng từ 150000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 100),
        ('VC010', N'DEMO5_010', 5, N'Đơn hàng từ 250000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 110),
        ('VC011', N'DEMO10_011', 10, N'Đơn hàng từ 350000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 120),
        ('VC012', N'DEMO6_012', 6, N'Đơn hàng từ 450000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 130),
        ('VC013', N'DEMO8_013', 8, N'Đơn hàng từ 550000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 140),
        ('VC014', N'DEMO12_014', 12, N'Đơn hàng từ 150000', 0, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 150),
        ('VC015', N'DEMO20_015', 20, N'Đơn hàng từ 250000', 1, DATEADD(DAY, -2, CAST(GETDATE() AS date)), DATEADD(DAY, 45, CAST(GETDATE() AS date)), 160)
    ) AS v([maVoucher], [tenVoucher], [phanTramGiam], [dieuKien], [trangThai], [ngayBatDau], [ngayKetThuc], [soLuongToiDa])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.Vocher AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maVoucher] = v.[maVoucher]);

    -- PhuongThucThanhToan : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.PhuongThucThanhToan AS d
        INNER JOIN (VALUES
            ('PT004', N'Thẻ ghi nợ (chưa hỗ trợ)'),
            ('PT005', N'Thẻ tín dụng (chưa hỗ trợ)'),
            ('PT006', N'Ví điện tử A (chưa hỗ trợ)'),
            ('PT007', N'Ví điện tử B (chưa hỗ trợ)'),
            ('PT008', N'Thanh toán QR tự động (dự kiến)'),
            ('PT009', N'Cổng thẻ POS (dự kiến)'),
            ('PT010', N'Thanh toán trả góp (dự kiến)'),
            ('PT011', N'Thanh toán qua đối tác (dự kiến)'),
            ('PT012', N'Thanh toán bằng điểm (dự kiến)'),
            ('PT013', N'Chuyển khoản tự đối soát (dự kiến)'),
            ('PT014', N'Thanh toán quốc tế (dự kiến)'),
            ('PT015', N'Cổng thanh toán web (dự kiến)')
        ) AS v([maPhuongThuc], [tenPhuongThuc])
        ON d.[maPhuongThuc] = v.[maPhuongThuc]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenPhuongThuc]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenPhuongThuc]), N'')))
        THROW 51044, N'Ma du lieu mau o bang PhuongThucThanhToan da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.PhuongThucThanhToan ([maPhuongThuc], [tenPhuongThuc], [trangThai], [ghiChu])
    SELECT v.[maPhuongThuc], v.[tenPhuongThuc], v.[trangThai], v.[ghiChu]
    FROM (VALUES
        ('PT004', N'Thẻ ghi nợ (chưa hỗ trợ)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT005', N'Thẻ tín dụng (chưa hỗ trợ)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT006', N'Ví điện tử A (chưa hỗ trợ)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT007', N'Ví điện tử B (chưa hỗ trợ)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT008', N'Thanh toán QR tự động (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT009', N'Cổng thẻ POS (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT010', N'Thanh toán trả góp (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT011', N'Thanh toán qua đối tác (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT012', N'Thanh toán bằng điểm (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT013', N'Chuyển khoản tự đối soát (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT014', N'Thanh toán quốc tế (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt'),
        ('PT015', N'Cổng thanh toán web (dự kiến)', 0, N'Dữ liệu thử nghiệm - chưa kích hoạt')
    ) AS v([maPhuongThuc], [tenPhuongThuc], [trangThai], [ghiChu])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.PhuongThucThanhToan AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhuongThuc] = v.[maPhuongThuc]);

    -- CaLam : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.CaLam AS d
        INNER JOIN (VALUES
            ('CA003', N'Ca 08-12'),
            ('CA004', N'Ca 12-16'),
            ('CA005', N'Ca 16-20'),
            ('CA006', N'Ca 07-11'),
            ('CA007', N'Ca 11-15'),
            ('CA008', N'Ca 15-19'),
            ('CA009', N'Ca 09-13'),
            ('CA010', N'Ca 13-17'),
            ('CA011', N'Ca 10-14'),
            ('CA012', N'Ca 14-18'),
            ('CA013', N'Ca 18-22'),
            ('CA014', N'Ca 06-10')
        ) AS v([maCa], [tenCa])
        ON d.[maCa] = v.[maCa]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[tenCa]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[tenCa]), N'')))
        THROW 51044, N'Ma du lieu mau o bang CaLam da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.CaLam ([maCa], [tenCa], [gioBatDau], [gioKetThuc])
    SELECT v.[maCa], v.[tenCa], v.[gioBatDau], v.[gioKetThuc]
    FROM (VALUES
        ('CA003', N'Ca 08-12', '08:00', '12:00'),
        ('CA004', N'Ca 12-16', '12:00', '16:00'),
        ('CA005', N'Ca 16-20', '16:00', '20:00'),
        ('CA006', N'Ca 07-11', '07:00', '11:00'),
        ('CA007', N'Ca 11-15', '11:00', '15:00'),
        ('CA008', N'Ca 15-19', '15:00', '19:00'),
        ('CA009', N'Ca 09-13', '09:00', '13:00'),
        ('CA010', N'Ca 13-17', '13:00', '17:00'),
        ('CA011', N'Ca 10-14', '10:00', '14:00'),
        ('CA012', N'Ca 14-18', '14:00', '18:00'),
        ('CA013', N'Ca 18-22', '18:00', '22:00'),
        ('CA014', N'Ca 06-10', '06:00', '10:00')
    ) AS v([maCa], [tenCa], [gioBatDau], [gioKetThuc])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.CaLam AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maCa] = v.[maCa]);

    -- LichPhanCa : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.LichPhanCa AS d
        INNER JOIN (VALUES
            ('PC005', 'NV013', 'CA003'),
            ('PC006', 'NV014', 'CA004'),
            ('PC007', 'NV015', 'CA005'),
            ('PC008', 'NV016', 'CA003'),
            ('PC009', 'NV013', 'CA004'),
            ('PC010', 'NV014', 'CA005'),
            ('PC011', 'NV015', 'CA003'),
            ('PC012', 'NV016', 'CA004'),
            ('PC013', 'NV013', 'CA005'),
            ('PC014', 'NV014', 'CA003'),
            ('PC015', 'NV015', 'CA004'),
            ('PC016', 'NV016', 'CA005')
        ) AS v([maPhanCa], [maNhanVien], [maCa])
        ON d.[maPhanCa] = v.[maPhanCa]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maCa]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maCa]), N'')))
        THROW 51044, N'Ma du lieu mau o bang LichPhanCa da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.LichPhanCa ([maPhanCa], [ngayLamViec], [maNhanVien], [maCa])
    SELECT v.[maPhanCa], v.[ngayLamViec], v.[maNhanVien], v.[maCa]
    FROM (VALUES
        ('PC005', DATEADD(DAY, 2, CAST(GETDATE() AS date)), 'NV013', 'CA003'),
        ('PC006', DATEADD(DAY, 3, CAST(GETDATE() AS date)), 'NV014', 'CA004'),
        ('PC007', DATEADD(DAY, 4, CAST(GETDATE() AS date)), 'NV015', 'CA005'),
        ('PC008', DATEADD(DAY, 5, CAST(GETDATE() AS date)), 'NV016', 'CA003'),
        ('PC009', DATEADD(DAY, 6, CAST(GETDATE() AS date)), 'NV013', 'CA004'),
        ('PC010', DATEADD(DAY, 7, CAST(GETDATE() AS date)), 'NV014', 'CA005'),
        ('PC011', DATEADD(DAY, 8, CAST(GETDATE() AS date)), 'NV015', 'CA003'),
        ('PC012', DATEADD(DAY, 9, CAST(GETDATE() AS date)), 'NV016', 'CA004'),
        ('PC013', DATEADD(DAY, 10, CAST(GETDATE() AS date)), 'NV013', 'CA005'),
        ('PC014', DATEADD(DAY, 11, CAST(GETDATE() AS date)), 'NV014', 'CA003'),
        ('PC015', DATEADD(DAY, 12, CAST(GETDATE() AS date)), 'NV015', 'CA004'),
        ('PC016', DATEADD(DAY, 13, CAST(GETDATE() AS date)), 'NV016', 'CA005')
    ) AS v([maPhanCa], [ngayLamViec], [maNhanVien], [maCa])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.LichPhanCa AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhanCa] = v.[maPhanCa]);

    -- LichHen : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.LichHen AS d
        INNER JOIN (VALUES
            ('LH005', 'NV013', 'KH006', 'TC006'),
            ('LH006', 'NV014', 'KH007', 'TC007'),
            ('LH007', 'NV015', 'KH008', 'TC008'),
            ('LH008', 'NV016', 'KH009', 'TC009'),
            ('LH009', 'NV013', 'KH010', 'TC010'),
            ('LH010', 'NV014', 'KH011', 'TC011'),
            ('LH011', 'NV015', 'KH012', 'TC012'),
            ('LH012', 'NV016', 'KH013', 'TC013'),
            ('LH013', 'NV013', 'KH014', 'TC014'),
            ('LH014', 'NV014', 'KH015', 'TC015'),
            ('LH015', 'NV015', 'KH016', 'TC016'),
            ('LH016', 'NV016', 'KH017', 'TC017')
        ) AS v([maLichHen], [maNhanVien], [maKhachHang], [maThuCung])
        ON d.[maLichHen] = v.[maLichHen]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maKhachHang]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maKhachHang]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maThuCung]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maThuCung]), N'')))
        THROW 51044, N'Ma du lieu mau o bang LichHen da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.LichHen ([maLichHen], [ngayGioHen], [ghiChu], [trangThai], [maKhachHang], [maNhanVien], [maThuCung])
    SELECT v.[maLichHen], v.[ngayGioHen], v.[ghiChu], v.[trangThai], v.[maKhachHang], v.[maNhanVien], v.[maThuCung]
    FROM (VALUES
        ('LH005', DATEADD(MINUTE, 600, DATEADD(DAY, 2, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 005', N'Xác nhận', 'KH006', 'NV013', 'TC006'),
        ('LH006', DATEADD(MINUTE, 840, DATEADD(DAY, 3, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 006', N'Xác nhận', 'KH007', 'NV014', 'TC007'),
        ('LH007', DATEADD(MINUTE, 1020, DATEADD(DAY, 4, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 007', N'Xác nhận', 'KH008', 'NV015', 'TC008'),
        ('LH008', DATEADD(MINUTE, 600, DATEADD(DAY, 5, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 008', N'Xác nhận', 'KH009', 'NV016', 'TC009'),
        ('LH009', DATEADD(MINUTE, 840, DATEADD(DAY, 6, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 009', N'Xác nhận', 'KH010', 'NV013', 'TC010'),
        ('LH010', DATEADD(MINUTE, 1020, DATEADD(DAY, 7, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 010', N'Xác nhận', 'KH011', 'NV014', 'TC011'),
        ('LH011', DATEADD(MINUTE, 600, DATEADD(DAY, 8, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 011', N'Xác nhận', 'KH012', 'NV015', 'TC012'),
        ('LH012', DATEADD(MINUTE, 840, DATEADD(DAY, 9, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 012', N'Xác nhận', 'KH013', 'NV016', 'TC013'),
        ('LH013', DATEADD(MINUTE, 1020, DATEADD(DAY, 10, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 013', N'Xác nhận', 'KH014', 'NV013', 'TC014'),
        ('LH014', DATEADD(MINUTE, 600, DATEADD(DAY, 11, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 014', N'Xác nhận', 'KH015', 'NV014', 'TC015'),
        ('LH015', DATEADD(MINUTE, 840, DATEADD(DAY, 12, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 015', N'Xác nhận', 'KH016', 'NV015', 'TC016'),
        ('LH016', DATEADD(MINUTE, 1020, DATEADD(DAY, 13, CAST(CAST(GETDATE() AS date) AS datetime2))), N'Lịch Spa mẫu 016', N'Xác nhận', 'KH017', 'NV016', 'TC017')
    ) AS v([maLichHen], [ngayGioHen], [ghiChu], [trangThai], [maKhachHang], [maNhanVien], [maThuCung])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.LichHen AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maLichHen] = v.[maLichHen]);

    -- ChiTietLichHen : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ChiTietLichHen AS d
        INNER JOIN (VALUES
            ('LH005', 'DV006', 'NV013'),
            ('LH006', 'DV007', 'NV014'),
            ('LH007', 'DV008', 'NV015'),
            ('LH008', 'DV009', 'NV016'),
            ('LH009', 'DV010', 'NV013'),
            ('LH010', 'DV011', 'NV014'),
            ('LH011', 'DV012', 'NV015'),
            ('LH012', 'DV013', 'NV016'),
            ('LH013', 'DV014', 'NV013'),
            ('LH014', 'DV015', 'NV014'),
            ('LH015', 'DV016', 'NV015'),
            ('LH016', 'DV017', 'NV016')
        ) AS v([maLichHen], [maDichVu], [maNhanVien])
        ON d.[maLichHen] = v.[maLichHen] AND d.[maDichVu] = v.[maDichVu]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ChiTietLichHen da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ChiTietLichHen ([maLichHen], [maDichVu], [maNhanVien], [ghiChu])
    SELECT v.[maLichHen], v.[maDichVu], v.[maNhanVien], v.[ghiChu]
    FROM (VALUES
        ('LH005', 'DV006', 'NV013', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH006', 'DV007', 'NV014', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH007', 'DV008', 'NV015', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH008', 'DV009', 'NV016', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH009', 'DV010', 'NV013', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH010', 'DV011', 'NV014', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH011', 'DV012', 'NV015', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH012', 'DV013', 'NV016', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH013', 'DV014', 'NV013', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH014', 'DV015', 'NV014', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH015', 'DV016', 'NV015', N'Dịch vụ theo lịch hẹn mẫu'),
        ('LH016', 'DV017', 'NV016', N'Dịch vụ theo lịch hẹn mẫu')
    ) AS v([maLichHen], [maDichVu], [maNhanVien], [ghiChu])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietLichHen AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maLichHen] = v.[maLichHen] AND d.[maDichVu] = v.[maDichVu]);

    -- PhieuDichVu : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.PhieuDichVu AS d
        INNER JOIN (VALUES
            ('PDV004', 'NV013', 'KH006', 'TC006'),
            ('PDV005', 'NV014', 'KH007', 'TC007'),
            ('PDV006', 'NV015', 'KH008', 'TC008'),
            ('PDV007', 'NV016', 'KH009', 'TC009'),
            ('PDV008', 'NV013', 'KH010', 'TC010'),
            ('PDV009', 'NV014', 'KH011', 'TC011'),
            ('PDV010', 'NV015', 'KH012', 'TC012'),
            ('PDV011', 'NV016', 'KH013', 'TC013'),
            ('PDV012', 'NV013', 'KH014', 'TC014'),
            ('PDV013', 'NV014', 'KH015', 'TC015'),
            ('PDV014', 'NV015', 'KH016', 'TC016'),
            ('PDV015', 'NV016', 'KH017', 'TC017')
        ) AS v([maPhieuDichVu], [maNhanVien], [maKhachHang], [maThuCung])
        ON d.[maPhieuDichVu] = v.[maPhieuDichVu]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maKhachHang]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maKhachHang]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maThuCung]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maThuCung]), N'')))
        THROW 51044, N'Ma du lieu mau o bang PhieuDichVu da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.PhieuDichVu ([maPhieuDichVu], [trangThai], [maKhachHang], [maThuCung], [maNhanVien], [maLichHen])
    SELECT v.[maPhieuDichVu], v.[trangThai], v.[maKhachHang], v.[maThuCung], v.[maNhanVien], v.[maLichHen]
    FROM (VALUES
        ('PDV004', 0, 'KH006', 'TC006', 'NV013', NULL),
        ('PDV005', 0, 'KH007', 'TC007', 'NV014', NULL),
        ('PDV006', 0, 'KH008', 'TC008', 'NV015', NULL),
        ('PDV007', 0, 'KH009', 'TC009', 'NV016', NULL),
        ('PDV008', 0, 'KH010', 'TC010', 'NV013', NULL),
        ('PDV009', 0, 'KH011', 'TC011', 'NV014', NULL),
        ('PDV010', 0, 'KH012', 'TC012', 'NV015', NULL),
        ('PDV011', 0, 'KH013', 'TC013', 'NV016', NULL),
        ('PDV012', 1, 'KH014', 'TC014', 'NV013', NULL),
        ('PDV013', 1, 'KH015', 'TC015', 'NV014', NULL),
        ('PDV014', 1, 'KH016', 'TC016', 'NV015', NULL),
        ('PDV015', 1, 'KH017', 'TC017', 'NV016', NULL)
    ) AS v([maPhieuDichVu], [trangThai], [maKhachHang], [maThuCung], [maNhanVien], [maLichHen])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.PhieuDichVu AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuDichVu] = v.[maPhieuDichVu]);

    -- ChiTietDichVu : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ChiTietDichVu AS d
        INNER JOIN (VALUES
            ('PDV004', 'DV006', 1, 75000),
            ('PDV005', 'DV007', 1, 160000),
            ('PDV006', 'DV008', 1, 190000),
            ('PDV007', 'DV009', 1, 230000),
            ('PDV008', 'DV010', 1, 150000),
            ('PDV009', 'DV011', 1, 45000),
            ('PDV010', 'DV012', 1, 65000),
            ('PDV011', 'DV013', 1, 250000),
            ('PDV012', 'DV014', 1, 55000),
            ('PDV013', 'DV015', 1, 200000),
            ('PDV014', 'DV016', 1, 170000),
            ('PDV015', 'DV017', 1, 280000)
        ) AS v([maPhieuDichVu], [maDichVu], [soLuong], [donGia])
        ON d.[maPhieuDichVu] = v.[maPhieuDichVu] AND d.[maDichVu] = v.[maDichVu]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[soLuong]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[soLuong]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[donGia]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[donGia]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ChiTietDichVu da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ChiTietDichVu ([maPhieuDichVu], [maDichVu], [soLuong], [donGia], [ghiChu])
    SELECT v.[maPhieuDichVu], v.[maDichVu], v.[soLuong], v.[donGia], v.[ghiChu]
    FROM (VALUES
        ('PDV004', 'DV006', 1, 75000, N'Chưa thanh toán'),
        ('PDV005', 'DV007', 1, 160000, N'Chưa thanh toán'),
        ('PDV006', 'DV008', 1, 190000, N'Chưa thanh toán'),
        ('PDV007', 'DV009', 1, 230000, N'Chưa thanh toán'),
        ('PDV008', 'DV010', 1, 150000, N'Chưa thanh toán'),
        ('PDV009', 'DV011', 1, 45000, N'Chưa thanh toán'),
        ('PDV010', 'DV012', 1, 65000, N'Chưa thanh toán'),
        ('PDV011', 'DV013', 1, 250000, N'Chưa thanh toán'),
        ('PDV012', 'DV014', 1, 55000, N'Đã thanh toán'),
        ('PDV013', 'DV015', 1, 200000, N'Đã thanh toán'),
        ('PDV014', 'DV016', 1, 170000, N'Đã thanh toán'),
        ('PDV015', 'DV017', 1, 280000, N'Đã thanh toán')
    ) AS v([maPhieuDichVu], [maDichVu], [soLuong], [donGia], [ghiChu])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietDichVu AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuDichVu] = v.[maPhieuDichVu] AND d.[maDichVu] = v.[maDichVu]);

    -- HoaDon : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.HoaDon AS d
        INNER JOIN (VALUES
            ('HD0061010260006', 'KH006', 'NV006'),
            ('HD0071110260007', 'KH007', 'NV007'),
            ('HD0081210260008', 'KH008', 'NV008'),
            ('HD0091310260009', 'KH009', 'NV009'),
            ('HD0101410260010', 'KH010', 'NV006'),
            ('HD0111510260011', 'KH011', 'NV007'),
            ('HD0121610260012', 'KH012', 'NV008'),
            ('HD0131710260013', 'KH013', 'NV009'),
            ('HD0141810260014', 'KH014', 'NV006'),
            ('HD0151910260015', 'KH015', 'NV007'),
            ('HD0162010260016', 'KH016', 'NV008'),
            ('HD0172110260017', 'KH017', 'NV009')
        ) AS v([maHoaDon], [maKhachHang], [maNhanVien])
        ON d.[maHoaDon] = v.[maHoaDon]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maKhachHang]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maKhachHang]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')))
        THROW 51044, N'Ma du lieu mau o bang HoaDon da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.HoaDon ([maHoaDon], [ngayLap], [maPhuongThuc], [maKhachHang], [maNhanVien], [maVoucher])
    SELECT v.[maHoaDon], v.[ngayLap], v.[maPhuongThuc], v.[maKhachHang], v.[maNhanVien], v.[maVoucher]
    FROM (VALUES
        ('HD0061010260006', '2026-10-10 09:30:00', 'PT001', 'KH006', 'NV006', NULL),
        ('HD0071110260007', '2026-10-11 10:30:00', 'PT002', 'KH007', 'NV007', NULL),
        ('HD0081210260008', '2026-10-12 11:30:00', 'PT001', 'KH008', 'NV008', NULL),
        ('HD0091310260009', '2026-10-13 12:30:00', 'PT002', 'KH009', 'NV009', NULL),
        ('HD0101410260010', '2026-10-14 13:30:00', 'PT001', 'KH010', 'NV006', NULL),
        ('HD0111510260011', '2026-10-15 14:30:00', 'PT002', 'KH011', 'NV007', NULL),
        ('HD0121610260012', '2026-10-16 15:30:00', 'PT001', 'KH012', 'NV008', NULL),
        ('HD0131710260013', '2026-10-17 16:30:00', 'PT002', 'KH013', 'NV009', NULL),
        ('HD0141810260014', '2026-10-18 09:30:00', 'PT001', 'KH014', 'NV006', NULL),
        ('HD0151910260015', '2026-10-19 10:30:00', 'PT002', 'KH015', 'NV007', NULL),
        ('HD0162010260016', '2026-10-20 11:30:00', 'PT001', 'KH016', 'NV008', NULL),
        ('HD0172110260017', '2026-10-21 12:30:00', 'PT002', 'KH017', 'NV009', NULL)
    ) AS v([maHoaDon], [ngayLap], [maPhuongThuc], [maKhachHang], [maNhanVien], [maVoucher])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.HoaDon AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maHoaDon] = v.[maHoaDon]);

    -- ChiTietHoaDon : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon AS d
        INNER JOIN (VALUES
            ('HD0061010260006', 1, 119000),
            ('HD0071110260007', 2, 129000),
            ('HD0081210260008', 3, 219000),
            ('HD0091310260009', 1, 49000),
            ('HD0101410260010', 2, 135000),
            ('HD0111510260011', 3, 139000),
            ('HD0121610260012', 1, 235000),
            ('HD0131710260013', 2, 52000),
            ('HD0141810260014', 1, 55000),
            ('HD0151910260015', 1, 200000),
            ('HD0162010260016', 1, 170000),
            ('HD0172110260017', 1, 280000)
        ) AS v([maHoaDon], [soLuong], [donGia])
        ON d.[maHoaDon] = v.[maHoaDon]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[soLuong]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[soLuong]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[donGia]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[donGia]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ChiTietHoaDon da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ChiTietHoaDon ([maHoaDon], [maThuCung], [maDichVu], [maSanPham], [soLuong], [donGia], [phanTramGiam])
    SELECT v.[maHoaDon], v.[maThuCung], v.[maDichVu], v.[maSanPham], v.[soLuong], v.[donGia], v.[phanTramGiam]
    FROM (VALUES
        ('HD0061010260006', NULL, NULL, 'SP016', 1, 119000, 0),
        ('HD0071110260007', NULL, NULL, 'SP017', 2, 129000, 0),
        ('HD0081210260008', NULL, NULL, 'SP018', 3, 219000, 0),
        ('HD0091310260009', NULL, NULL, 'SP019', 1, 49000, 0),
        ('HD0101410260010', NULL, NULL, 'SP020', 2, 135000, 0),
        ('HD0111510260011', NULL, NULL, 'SP021', 3, 139000, 0),
        ('HD0121610260012', NULL, NULL, 'SP022', 1, 235000, 0),
        ('HD0131710260013', NULL, NULL, 'SP023', 2, 52000, 0),
        ('HD0141810260014', 'TC014', 'DV014', NULL, 1, 55000, 0),
        ('HD0151910260015', 'TC015', 'DV015', NULL, 1, 200000, 0),
        ('HD0162010260016', 'TC016', 'DV016', NULL, 1, 170000, 0),
        ('HD0172110260017', 'TC017', 'DV017', NULL, 1, 280000, 0)
    ) AS v([maHoaDon], [maThuCung], [maDichVu], [maSanPham], [soLuong], [donGia], [phanTramGiam])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietHoaDon AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maHoaDon] = v.[maHoaDon]);

    -- PhieuNhapKho : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.PhieuNhapKho AS d
        INNER JOIN (VALUES
            ('PN005', 'NCC004', 'NV010'),
            ('PN006', 'NCC005', 'NV011'),
            ('PN007', 'NCC006', 'NV012'),
            ('PN008', 'NCC007', 'NV010'),
            ('PN009', 'NCC008', 'NV011'),
            ('PN010', 'NCC009', 'NV012'),
            ('PN011', 'NCC010', 'NV010'),
            ('PN012', 'NCC011', 'NV011'),
            ('PN013', 'NCC012', 'NV012'),
            ('PN014', 'NCC013', 'NV010'),
            ('PN015', 'NCC014', 'NV011'),
            ('PN016', 'NCC015', 'NV012')
        ) AS v([maPhieuNhap], [maNhaCungCap], [maNhanVien])
        ON d.[maPhieuNhap] = v.[maPhieuNhap]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhaCungCap]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhaCungCap]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')))
        THROW 51044, N'Ma du lieu mau o bang PhieuNhapKho da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.PhieuNhapKho ([maPhieuNhap], [ngayNhap], [trangThai], [maNhanVien], [maNhaCungCap])
    SELECT v.[maPhieuNhap], v.[ngayNhap], v.[trangThai], v.[maNhanVien], v.[maNhaCungCap]
    FROM (VALUES
        ('PN005', '2026-09-20 08:15:00', 1, 'NV010', 'NCC004'),
        ('PN006', '2026-09-21 09:15:00', 1, 'NV011', 'NCC005'),
        ('PN007', '2026-09-22 10:15:00', 1, 'NV012', 'NCC006'),
        ('PN008', '2026-09-23 11:15:00', 1, 'NV010', 'NCC007'),
        ('PN009', '2026-09-24 08:15:00', 1, 'NV011', 'NCC008'),
        ('PN010', '2026-09-25 09:15:00', 1, 'NV012', 'NCC009'),
        ('PN011', '2026-09-26 10:15:00', 1, 'NV010', 'NCC010'),
        ('PN012', '2026-09-27 11:15:00', 1, 'NV011', 'NCC011'),
        ('PN013', '2026-09-28 08:15:00', 1, 'NV012', 'NCC012'),
        ('PN014', '2026-09-29 09:15:00', 1, 'NV010', 'NCC013'),
        ('PN015', '2026-09-30 10:15:00', 1, 'NV011', 'NCC014'),
        ('PN016', '2026-10-01 11:15:00', 1, 'NV012', 'NCC015')
    ) AS v([maPhieuNhap], [ngayNhap], [trangThai], [maNhanVien], [maNhaCungCap])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.PhieuNhapKho AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuNhap] = v.[maPhieuNhap]);

    -- ChiTietPhieuNhap : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ChiTietPhieuNhap AS d
        INNER JOIN (VALUES
            ('PN005', 'SP016', 36, 80000),
            ('PN006', 'SP017', 37, 87000),
            ('PN007', 'SP018', 38, 160000),
            ('PN008', 'SP019', 39, 32000),
            ('PN009', 'SP020', 40, 89000),
            ('PN010', 'SP021', 41, 92000),
            ('PN011', 'SP022', 42, 170000),
            ('PN012', 'SP023', 43, 34000),
            ('PN013', 'SP024', 44, 25000),
            ('PN014', 'SP025', 45, 65000),
            ('PN015', 'SP026', 46, 35000),
            ('PN016', 'SP027', 47, 48000)
        ) AS v([maPhieuNhap], [maSanPham], [soLuongNhap], [donGiaNhap])
        ON d.[maPhieuNhap] = v.[maPhieuNhap] AND d.[maSanPham] = v.[maSanPham]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[soLuongNhap]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[soLuongNhap]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[donGiaNhap]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[donGiaNhap]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ChiTietPhieuNhap da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ChiTietPhieuNhap ([maPhieuNhap], [maSanPham], [soLuongNhap], [donGiaNhap], [trangThai], [hanSuDung])
    SELECT v.[maPhieuNhap], v.[maSanPham], v.[soLuongNhap], v.[donGiaNhap], v.[trangThai], v.[hanSuDung]
    FROM (VALUES
        ('PN005', 'SP016', 36, 80000, 1, DATEADD(DAY, -3, CAST(GETDATE() AS date))),
        ('PN006', 'SP017', 37, 87000, 1, DATEADD(DAY, 9, CAST(GETDATE() AS date))),
        ('PN007', 'SP018', 38, 160000, 1, DATEADD(DAY, 20, CAST(GETDATE() AS date))),
        ('PN008', 'SP019', 39, 32000, 1, DATEADD(DAY, 45, CAST(GETDATE() AS date))),
        ('PN009', 'SP020', 40, 89000, 1, DATEADD(DAY, 60, CAST(GETDATE() AS date))),
        ('PN010', 'SP021', 41, 92000, 1, DATEADD(DAY, 90, CAST(GETDATE() AS date))),
        ('PN011', 'SP022', 42, 170000, 1, DATEADD(DAY, 120, CAST(GETDATE() AS date))),
        ('PN012', 'SP023', 43, 34000, 1, DATEADD(DAY, 180, CAST(GETDATE() AS date))),
        ('PN013', 'SP024', 44, 25000, 1, NULL),
        ('PN014', 'SP025', 45, 65000, 1, NULL),
        ('PN015', 'SP026', 46, 35000, 1, NULL),
        ('PN016', 'SP027', 47, 48000, 1, NULL)
    ) AS v([maPhieuNhap], [maSanPham], [soLuongNhap], [donGiaNhap], [trangThai], [hanSuDung])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietPhieuNhap AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuNhap] = v.[maPhieuNhap] AND d.[maSanPham] = v.[maSanPham]);

    -- PhieuKiemKe : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.PhieuKiemKe AS d
        INNER JOIN (VALUES
            ('KK002', 'NV010'),
            ('KK003', 'NV011'),
            ('KK004', 'NV012'),
            ('KK005', 'NV010'),
            ('KK006', 'NV011'),
            ('KK007', 'NV012'),
            ('KK008', 'NV010'),
            ('KK009', 'NV011'),
            ('KK010', 'NV012'),
            ('KK011', 'NV010'),
            ('KK012', 'NV011'),
            ('KK013', 'NV012')
        ) AS v([maPhieuKiemKe], [maNhanVien])
        ON d.[maPhieuKiemKe] = v.[maPhieuKiemKe]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[maNhanVien]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[maNhanVien]), N'')))
        THROW 51044, N'Ma du lieu mau o bang PhieuKiemKe da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.PhieuKiemKe ([maPhieuKiemKe], [ngayKiemKe], [trangThai], [maNhanVien])
    SELECT v.[maPhieuKiemKe], v.[ngayKiemKe], v.[trangThai], v.[maNhanVien]
    FROM (VALUES
        ('KK002', DATEADD(HOUR, -2, SYSDATETIME()), 1, 'NV010'),
        ('KK003', DATEADD(HOUR, -4, SYSDATETIME()), 1, 'NV011'),
        ('KK004', DATEADD(HOUR, -6, SYSDATETIME()), 1, 'NV012'),
        ('KK005', DATEADD(HOUR, -8, SYSDATETIME()), 1, 'NV010'),
        ('KK006', DATEADD(HOUR, -10, SYSDATETIME()), 1, 'NV011'),
        ('KK007', DATEADD(HOUR, -12, SYSDATETIME()), 1, 'NV012'),
        ('KK008', DATEADD(HOUR, -14, SYSDATETIME()), 1, 'NV010'),
        ('KK009', DATEADD(HOUR, -16, SYSDATETIME()), 1, 'NV011'),
        ('KK010', DATEADD(HOUR, -18, SYSDATETIME()), 1, 'NV012'),
        ('KK011', DATEADD(HOUR, -20, SYSDATETIME()), 1, 'NV010'),
        ('KK012', DATEADD(HOUR, -22, SYSDATETIME()), 1, 'NV011'),
        ('KK013', DATEADD(HOUR, -24, SYSDATETIME()), 1, 'NV012')
    ) AS v([maPhieuKiemKe], [ngayKiemKe], [trangThai], [maNhanVien])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.PhieuKiemKe AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuKiemKe] = v.[maPhieuKiemKe]);

    -- ChiTietPhieuKiemKe : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.ChiTietPhieuKiemKe AS d
        INNER JOIN (VALUES
            ('KK002', 'SP016', 35, 35),
            ('KK003', 'SP017', 35, 34),
            ('KK004', 'SP018', 35, 36),
            ('KK005', 'SP019', 38, 38),
            ('KK006', 'SP020', 38, 38),
            ('KK007', 'SP021', 38, 37),
            ('KK008', 'SP022', 41, 42),
            ('KK009', 'SP023', 41, 41),
            ('KK010', 'SP024', 44, 44),
            ('KK011', 'SP025', 45, 44),
            ('KK012', 'SP026', 46, 47),
            ('KK013', 'SP027', 47, 47)
        ) AS v([maPhieuKiemKe], [maSanPham], [soLuongHeThong], [soLuongThucTe])
        ON d.[maPhieuKiemKe] = v.[maPhieuKiemKe] AND d.[maSanPham] = v.[maSanPham]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[soLuongHeThong]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[soLuongHeThong]), N'')) OR (ISNULL(CONVERT(NVARCHAR(4000), d.[soLuongThucTe]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[soLuongThucTe]), N'')))
        THROW 51044, N'Ma du lieu mau o bang ChiTietPhieuKiemKe da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.ChiTietPhieuKiemKe ([maPhieuKiemKe], [maSanPham], [soLuongHeThong], [soLuongThucTe], [trangThai])
    SELECT v.[maPhieuKiemKe], v.[maSanPham], v.[soLuongHeThong], v.[soLuongThucTe], v.[trangThai]
    FROM (VALUES
        ('KK002', 'SP016', 35, 35, 1),
        ('KK003', 'SP017', 35, 34, 0),
        ('KK004', 'SP018', 35, 36, 0),
        ('KK005', 'SP019', 38, 38, 1),
        ('KK006', 'SP020', 38, 38, 1),
        ('KK007', 'SP021', 38, 37, 0),
        ('KK008', 'SP022', 41, 42, 0),
        ('KK009', 'SP023', 41, 41, 1),
        ('KK010', 'SP024', 44, 44, 1),
        ('KK011', 'SP025', 45, 44, 0),
        ('KK012', 'SP026', 46, 47, 0),
        ('KK013', 'SP027', 47, 47, 1)
    ) AS v([maPhieuKiemKe], [maSanPham], [soLuongHeThong], [soLuongThucTe], [trangThai])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.ChiTietPhieuKiemKe AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maPhieuKiemKe] = v.[maPhieuKiemKe] AND d.[maSanPham] = v.[maSanPham]);

    -- GoiYThucAn : 12 ban ghi
    IF EXISTS (SELECT 1 FROM dbo.GoiYThucAn AS d
        INNER JOIN (VALUES
            ('SP016', N'Chó', N'Con non', N'Nhỏ', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP017', N'Chó', N'Con non', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP018', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP019', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP020', N'Mèo', N'Con non', N'Nhỏ', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP021', N'Mèo', N'Con non', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP022', N'Mèo', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP023', N'Mèo', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
            ('SP009', N'Chó', N'Con non', N'Mọi kích thước', N'Lựa chọn tham khảo theo độ tuổi'),
            ('SP010', N'Chó', N'Con non', N'Nhỏ', N'Lựa chọn tham khảo theo độ tuổi'),
            ('SP011', N'Chó', N'Trưởng thành', N'Trung bình', N'Lựa chọn tham khảo theo độ tuổi'),
            ('SP012', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Lựa chọn tham khảo theo độ tuổi')
        ) AS v([maSanPham], [loaiThuCung], [nhomTuoi], [kichThuoc], [ghiChu])
        ON d.[maSanPham] = v.[maSanPham] AND d.[loaiThuCung] = v.[loaiThuCung] AND d.[nhomTuoi] = v.[nhomTuoi] AND d.[kichThuoc] = v.[kichThuoc]
        WHERE (ISNULL(CONVERT(NVARCHAR(4000), d.[ghiChu]), N'') <> ISNULL(CONVERT(NVARCHAR(4000), v.[ghiChu]), N'')))
        THROW 51044, N'Ma du lieu mau o bang GoiYThucAn da duoc dung cho thong tin khac. Khong chen de tranh sai lien ket.', 1;

    INSERT INTO dbo.GoiYThucAn ([maSanPham], [loaiThuCung], [nhomTuoi], [kichThuoc], [ghiChu])
    SELECT v.[maSanPham], v.[loaiThuCung], v.[nhomTuoi], v.[kichThuoc], v.[ghiChu]
    FROM (VALUES
        ('SP016', N'Chó', N'Con non', N'Nhỏ', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP017', N'Chó', N'Con non', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP018', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP019', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP020', N'Mèo', N'Con non', N'Nhỏ', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP021', N'Mèo', N'Con non', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP022', N'Mèo', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP023', N'Mèo', N'Trưởng thành', N'Mọi kích thước', N'Dữ liệu gợi ý mẫu, cần đối chiếu nhãn sản phẩm'),
        ('SP009', N'Chó', N'Con non', N'Mọi kích thước', N'Lựa chọn tham khảo theo độ tuổi'),
        ('SP010', N'Chó', N'Con non', N'Nhỏ', N'Lựa chọn tham khảo theo độ tuổi'),
        ('SP011', N'Chó', N'Trưởng thành', N'Trung bình', N'Lựa chọn tham khảo theo độ tuổi'),
        ('SP012', N'Chó', N'Trưởng thành', N'Mọi kích thước', N'Lựa chọn tham khảo theo độ tuổi')
    ) AS v([maSanPham], [loaiThuCung], [nhomTuoi], [kichThuoc], [ghiChu])
    WHERE NOT EXISTS (SELECT 1 FROM dbo.GoiYThucAn AS d WITH (UPDLOCK, HOLDLOCK) WHERE d.[maSanPham] = v.[maSanPham] AND d.[loaiThuCung] = v.[loaiThuCung] AND d.[nhomTuoi] = v.[nhomTuoi] AND d.[kichThuoc] = v.[kichThuoc]);

    -- BoDemHoaDon la bang he thong SINGLETON, khong them dong.
    IF NOT EXISTS (SELECT 1 FROM dbo.BoDemHoaDon WHERE id = 1)
        THROW 51043, N'BoDemHoaDon thieu dong id=1; vui long khoi phuc bang dem.', 1;
    UPDATE dbo.BoDemHoaDon
    SET giaTri = CASE WHEN giaTri < q.maxNum THEN q.maxNum ELSE giaTri END
    FROM (SELECT ISNULL(MAX(TRY_CONVERT(INT, RIGHT(maHoaDon, 4))), 0) AS maxNum FROM dbo.HoaDon) AS q
    WHERE id = 1;

    COMMIT TRANSACTION;
    PRINT N'Da bo sung du lieu mau; ban co the chay lai ma khong trung khoa.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

-- KIEM TRA SO BAN GHI SAU KHI BO SUNG (bao gom du lieu cu neu DB da thay doi):
SELECT N'DanhMuc' AS [Bang], COUNT_BIG(*) AS [SoDong] FROM dbo.DanhMuc
UNION ALL SELECT N'Nhacungcap', COUNT_BIG(*) FROM dbo.Nhacungcap
UNION ALL SELECT N'NhanVien', COUNT_BIG(*) FROM dbo.NhanVien
UNION ALL SELECT N'KhachHang', COUNT_BIG(*) FROM dbo.KhachHang
UNION ALL SELECT N'ThuCung', COUNT_BIG(*) FROM dbo.ThuCung
UNION ALL SELECT N'DichVu', COUNT_BIG(*) FROM dbo.DichVu
UNION ALL SELECT N'SanPham', COUNT_BIG(*) FROM dbo.SanPham
UNION ALL SELECT N'Vocher', COUNT_BIG(*) FROM dbo.Vocher
UNION ALL SELECT N'PhuongThucThanhToan', COUNT_BIG(*) FROM dbo.PhuongThucThanhToan
UNION ALL SELECT N'CaLam', COUNT_BIG(*) FROM dbo.CaLam
UNION ALL SELECT N'LichPhanCa', COUNT_BIG(*) FROM dbo.LichPhanCa
UNION ALL SELECT N'LichHen', COUNT_BIG(*) FROM dbo.LichHen
UNION ALL SELECT N'ChiTietLichHen', COUNT_BIG(*) FROM dbo.ChiTietLichHen
UNION ALL SELECT N'PhieuDichVu', COUNT_BIG(*) FROM dbo.PhieuDichVu
UNION ALL SELECT N'ChiTietDichVu', COUNT_BIG(*) FROM dbo.ChiTietDichVu
UNION ALL SELECT N'HoaDon', COUNT_BIG(*) FROM dbo.HoaDon
UNION ALL SELECT N'ChiTietHoaDon', COUNT_BIG(*) FROM dbo.ChiTietHoaDon
UNION ALL SELECT N'PhieuNhapKho', COUNT_BIG(*) FROM dbo.PhieuNhapKho
UNION ALL SELECT N'ChiTietPhieuNhap', COUNT_BIG(*) FROM dbo.ChiTietPhieuNhap
UNION ALL SELECT N'PhieuKiemKe', COUNT_BIG(*) FROM dbo.PhieuKiemKe
UNION ALL SELECT N'ChiTietPhieuKiemKe', COUNT_BIG(*) FROM dbo.ChiTietPhieuKiemKe
UNION ALL SELECT N'GoiYThucAn', COUNT_BIG(*) FROM dbo.GoiYThucAn
UNION ALL SELECT N'BoDemHoaDon', COUNT_BIG(*) FROM dbo.BoDemHoaDon
ORDER BY [Bang];
GO
