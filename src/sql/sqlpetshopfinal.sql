/* =====================================================================
   PET SHOP PRO - SQL SERVER (TAO MOI + DU LIEU MAU)
   Tong hop tu 6 file do nguoi dung cung cap:
   1. SQLPetShopPro.sql
   2. MigrationHanSuDung.sql
   3. MigrationMaHoaDon.sql
   4. SQLPetShopPro_update_voucher.sql
   5. capnhatmatkhau.sql
   6. MigrationGoiYThucAn.sql

   LUU Y:
   - Script TAO MOI tren database 'ptudpetshop'. Khong xoa du lieu dang co.
   - Neu database da co bang du lieu, script se TU CHOI chay de tranh
     ghi de/chen them du lieu mau. Dung file migration rieng de nang cap.
   - KhachHang chi co: maKhachHang, hoTen, soDienThoai.
   - Khong tao TheThanhVien; voucher cu VC002 = ngung ap dung.
   - HoaDon.maHoaDon VARCHAR(15); co BoDemHoaDon.
   - matKhau NVARCHAR(255) ho tro Unicode, nhung ung dung nen bam mat khau.
   - Ngay het han PN001 duoc tao theo ngay chay (du lieu demo).
   ===================================================================== */

IF DB_ID(N'ptudpetshop') IS NULL
    EXEC(N'CREATE DATABASE [ptudpetshop]');
GO
USE [00];
GO
SET NOCOUNT ON;
SET XACT_ABORT ON;

-- Dung truoc khi tao bang neu DB khong con trong (bao ve du lieu co san).
IF EXISTS (
    SELECT 1 FROM sys.tables t
    WHERE t.is_ms_shipped = 0 AND SCHEMA_NAME(t.schema_id) = N'dbo'
)
    THROW 51001, N'Database ptudpetshop da co bang. Hay sao luu va dung script nang cap DB hien co, khong chay script tao moi.', 1;

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
('SP008', N'Pate mèo cá ngừ', 25000, 39000, 'DM002', 'NCC001');
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

('HD0030710260003', NULL, 'DV001', NULL, 1, 120000, 15),
('HD0030710260003', NULL, 'DV002', NULL, 1, 180000, 15),

('HD0040610260004', NULL, NULL, 'SP005', 1, 145000, 0),
('HD0040610260004', NULL, NULL, 'SP006', 1, 150000, 0),

('HD0050610260005', NULL, NULL, 'SP004', 1, 120000, 0),
('HD0050610260005', NULL, NULL, 'SP007', 1, 99000, 0);
INSERT INTO PhieuNhapKho
(maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
VALUES
('PN001', '2026-10-01 08:00:00', 1, 'NV003', 'NCC001'),
('PN002', '2026-10-03 09:00:00', 1, 'NV003', 'NCC002'),
('PN003', '2026-10-05 10:00:00', 1, 'NV003', 'NCC003');
INSERT INTO ChiTietPhieuNhap
(maPhieuNhap, maSanPham, soLuongNhap, donGiaNhap, trangThai)
VALUES
('PN001', 'SP001', 30, 180000, 1),
('PN001', 'SP002', 40, 90000, 1),
('PN001', 'SP008', 50, 25000, 1),
('PN002', 'SP003', 20, 35000, 1),
('PN002', 'SP004', 15, 70000, 1),
('PN002', 'SP007', 20, 60000, 1),
('PN003', 'SP005', 12, 80000, 1),
('PN003', 'SP006', 18, 95000, 1);
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

/* Ngày hết hạn demo: giá trị tương đối theo ngày chạy. */
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
