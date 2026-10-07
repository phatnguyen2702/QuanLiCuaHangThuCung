CREATE TABLE DanhMuc (
    maDanhMuc VARCHAR(10) NOT NULL PRIMARY KEY,
    tenDanhMuc NVARCHAR(50) NULL,
    moTa NVARCHAR(255) NULL
);
GO

CREATE TABLE Nhacungcap (
    maNhaCungCap VARCHAR(10) NOT NULL PRIMARY KEY,
    tenNhaCungCap NVARCHAR(50) NULL,
    soDienThoai VARCHAR(15) NULL,
    email VARCHAR(50) NULL,
    diaChi NVARCHAR(50) NULL
);
GO

CREATE TABLE NhanVien (
    maNhanVien VARCHAR(10) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(50) NULL,
    soDienThoai VARCHAR(15) NULL,
    email VARCHAR(30) NULL,
    matKhau VARCHAR(25) NULL,
    gioiTinh NVARCHAR(10) NULL,
    vaiTro NVARCHAR(50) NULL
);
GO

CREATE TABLE KhachHang (
    maKhachHang VARCHAR(10) NOT NULL PRIMARY KEY,
    hoTen NVARCHAR(50) NULL,
    soDienThoai VARCHAR(15) NULL,
    email VARCHAR(50) NULL,
    diaChi NVARCHAR(50) NULL,
    diemTichLuy INT NULL DEFAULT 0
);
GO

CREATE TABLE ThuCung (
    maThuCung VARCHAR(10) NOT NULL PRIMARY KEY,
    tenThuCung NVARCHAR(50) NULL,
    loai NVARCHAR(25) NULL,
    giong NVARCHAR(25) NULL,
    gioiTinh NVARCHAR(10) NULL,
    canNang DECIMAL(5,2) NULL
);
GO

CREATE TABLE DichVu (
    maDichVu VARCHAR(10) NOT NULL PRIMARY KEY,
    tenDichVu NVARCHAR(50) NULL,
    giaDichVu DECIMAL(18,2) NULL,
    moTa NVARCHAR(255) NULL
);
GO

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
GO

CREATE TABLE Vocher (
    maVoucher VARCHAR(10) NOT NULL PRIMARY KEY,
    tenVoucher NVARCHAR(50) NULL,
    phanTramGiam DECIMAL(5,2) NULL,
    dieuKien NVARCHAR(255) NULL,
    trangThai BIT NULL DEFAULT 1
);
GO

CREATE TABLE PhuongThucThanhToan (
    maPhuongThuc VARCHAR(10) NOT NULL PRIMARY KEY,
    tenPhuongThuc NVARCHAR(50) NULL,
    trangThai BIT NULL DEFAULT 1,
    ghiChu NVARCHAR(255) NULL
);
GO

/* =======================================================
   2. CA LAM
======================================================= */

CREATE TABLE CaLam (
    maCa VARCHAR(10) NOT NULL PRIMARY KEY,
    tenCa NVARCHAR(50) NULL,
    gioBatDau TIME NULL,
    gioKetThuc TIME NULL
);
GO

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
GO

/* =======================================================
   3. THE THANH VIEN
======================================================= */

CREATE TABLE TheThanhVien (
    maThe VARCHAR(255) NOT NULL PRIMARY KEY,
    hangThe NVARCHAR(25) NULL,
    chietKhauPhanTram DECIMAL(5,2) NULL,
    ngayCap DATE NULL,
    maKhachHang VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NOT NULL,

    CONSTRAINT FK_TheThanhVien_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),

    CONSTRAINT FK_TheThanhVien_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

/* =======================================================
   4. LICH HEN / SPA
======================================================= */

CREATE TABLE LichHen (
    maLichHen VARCHAR(10) NOT NULL PRIMARY KEY,
    ngayGioHen DATETIME2 NULL,
    ghiChu NVARCHAR(255) NULL,
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
GO

CREATE TABLE PhieuDichVu (
    maPhieuDichVu VARCHAR(10) NOT NULL PRIMARY KEY,
    trangThai BIT NULL DEFAULT 1,
    maKhachHang VARCHAR(10) NOT NULL,
    maThuCung VARCHAR(10) NOT NULL,
    maNhanVien VARCHAR(10) NOT NULL,

    CONSTRAINT FK_PhieuDichVu_KhachHang
        FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),

    CONSTRAINT FK_PhieuDichVu_ThuCung
        FOREIGN KEY (maThuCung) REFERENCES ThuCung(maThuCung),

    CONSTRAINT FK_PhieuDichVu_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

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
GO

CREATE TABLE ChiTietLichHen (
    maLichHen VARCHAR(10) NOT NULL,
    maDichVu VARCHAR(10) NOT NULL,
    trieuChung NVARCHAR(255) NULL,
    chuanDoan NVARCHAR(255) NULL,
    huongTriLieu NVARCHAR(255) NULL,
    ghiChu NVARCHAR(50) NULL,
    maNhanVien VARCHAR(10) NOT NULL,

    CONSTRAINT PK_ChiTietLichHen
        PRIMARY KEY (maLichHen, maDichVu),

    CONSTRAINT FK_ChiTietLichHen_LichHen
        FOREIGN KEY (maLichHen) REFERENCES LichHen(maLichHen),

    CONSTRAINT FK_ChiTietLichHen_DichVu
        FOREIGN KEY (maDichVu) REFERENCES DichVu(maDichVu),

    CONSTRAINT FK_ChiTietLichHen_NhanVien
        FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);
GO

/* =======================================================
   5. HOA DON
======================================================= */

CREATE TABLE HoaDon (
    maHoaDon VARCHAR(10) NOT NULL PRIMARY KEY,
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
GO

/*
   Trong mo hinh goc, 3 cot maThuCung/maDichVu/maSanPham
   duoc danh dau NOT NULL. De giao dien ban hang co the tao
   dong san pham HOAC dong dich vu, o day cho phep NULL.
*/
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(10) NOT NULL,
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
GO

CREATE INDEX IX_ChiTietHoaDon_HoaDon
    ON ChiTietHoaDon(maHoaDon);
GO

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
GO

CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(10) NOT NULL,
    maSanPham VARCHAR(10) NOT NULL,
    soLuongNhap INT NULL,
    donGiaNhap DECIMAL(18,2) NULL,
    trangThai BIT NULL DEFAULT 1,

    CONSTRAINT PK_ChiTietPhieuNhap
        PRIMARY KEY (maPhieuNhap, maSanPham),

    CONSTRAINT FK_CTPN_PhieuNhap
        FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhapKho(maPhieuNhap),

    CONSTRAINT FK_CTPN_SanPham
        FOREIGN KEY (maSanPham) REFERENCES SanPham(maSanPham)
);
GO

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
GO

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
GO




INSERT INTO NhanVien
(maNhanVien, hoTen, soDienThoai, email, matKhau, gioiTinh, vaiTro)
VALUES
('NV001', N'Đặng Đình An', '01239415224', 'dangdinhan01@gmail.com', '123456', N'Nam', N'Quan ly'),
('NV002', N'Nguyễn Minh Anh', '0901234567', 'minhanh@gmail.com', '123456', N'Nữ', N'Thu ngan'),
('NV003', N'Trần Quốc Bảo', '0912345678', 'quocbao@gmail.com', '123456', N'Nam', N'Nhan vien kho'),
('NV004', N'Lê Ngọc Mai', '0987654321', 'ngocmai@gmail.com', '123456', N'Nữ', N'Nhan vien Spa');
GO

INSERT INTO DanhMuc
(maDanhMuc, tenDanhMuc, moTa)
VALUES
('DM001', N'Thức ăn cho chó', N'Thức ăn khô và ướt cho chó'),
('DM002', N'Thức ăn cho mèo', N'Thức ăn khô và ướt cho mèo'),
('DM003', N'Đồ chơi', N'Đồ chơi cho thú cưng'),
('DM004', N'Phụ kiện', N'Vòng cổ, dây dắt, bát ăn'),
('DM005', N'Chăm sóc', N'Sản phẩm vệ sinh và chăm sóc');
GO

INSERT INTO Nhacungcap
(maNhaCungCap, tenNhaCungCap, soDienThoai, email, diaChi)
VALUES
('NCC001', N'PetFood Việt Nam', '0901111111', 'petfood@gmail.com', N'TP. Hồ Chí Minh'),
('NCC002', N'Pet Care Việt', '0902222222', 'petcare@gmail.com', N'TP. Hồ Chí Minh'),
('NCC003', N'Happy Pet Supply', '0903333333', 'happypet@gmail.com', N'Bình Dương');
GO

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
GO

INSERT INTO KhachHang
(maKhachHang, hoTen, soDienThoai, email, diaChi, diemTichLuy)
VALUES
('KH001', N'Nguyễn Văn Minh', '0905123456', 'minh@gmail.com', N'Quận 1, TP.HCM', 850),
('KH002', N'Trần Thị Lan', '0916123456', 'lan@gmail.com', N'Quận 3, TP.HCM', 420),
('KH003', N'Lê Hoàng Nam', '0927123456', 'nam@gmail.com', N'Quận 10, TP.HCM', 1200),
('KH004', N'Phạm Ngọc Hà', '0938123456', 'ha@gmail.com', N'Thủ Đức, TP.HCM', 180),
('KH005', N'Đỗ Minh Khang', '0949123456', 'khang@gmail.com', N'Bình Thạnh, TP.HCM', 650);
GO

INSERT INTO ThuCung
(maThuCung, tenThuCung, loai, giong, gioiTinh, canNang)
VALUES
('TC001', N'Bông', N'Chó', N'Golden Retriever', N'Đực', 24.50),
('TC002', N'Miu', N'Mèo', N'British Shorthair', N'Cái', 4.20),
('TC003', N'Lucky', N'Chó', N'Poodle', N'Đực', 7.80),
('TC004', N'Nana', N'Mèo', N'Mèo Anh lông ngắn', N'Cái', 3.90),
('TC005', N'Milo', N'Chó', N'Husky', N'Đực', 22.30);
GO

INSERT INTO DichVu
(maDichVu, tenDichVu, giaDichVu, moTa)
VALUES
('DV001', N'Tắm thú cưng', 120000, N'Tắm và sấy cơ bản'),
('DV002', N'Cắt tỉa lông', 180000, N'Cắt tỉa theo yêu cầu'),
('DV003', N'Vệ sinh tai', 60000, N'Vệ sinh tai cho chó mèo'),
('DV004', N'Cắt móng', 50000, N'Cắt móng và vệ sinh chân'),
('DV005', N'Combo Spa Premium', 350000, N'Tắm, sấy, cắt tỉa và vệ sinh');
GO

INSERT INTO Vocher
(maVoucher, tenVoucher, phanTramGiam, dieuKien, trangThai)
VALUES
('VC001', N'WELCOME10', 10, N'Đơn hàng từ 300000', 1),
('VC002', N'MEMBER15', 15, N'Khách hàng thành viên', 1),
('VC003', N'PETDAY20', 20, N'Chương trình Pet Day', 1);
GO

INSERT INTO PhuongThucThanhToan
(maPhuongThuc, tenPhuongThuc, trangThai, ghiChu)
VALUES
('PT001', N'Tiền mặt', 1, N'Thanh toán tiền mặt tại quầy'),
('PT002', N'Chuyển khoản', 1, N'Chuyển khoản ngân hàng'),
('PT003', N'Ví điện tử', 1, N'Momo/ZaloPay');
GO

INSERT INTO CaLam
(maCa, tenCa, gioBatDau, gioKetThuc)
VALUES
('CA001', N'Ca sáng', '06:00', '14:00'),
('CA002', N'Ca chiều', '14:00', '22:00');
GO

INSERT INTO LichPhanCa
(maPhanCa, ngayLamViec, maNhanVien, maCa)
VALUES
('PC001', '2026-10-07', 'NV001', 'CA001'),
('PC002', '2026-10-07', 'NV002', 'CA001'),
('PC003', '2026-10-07', 'NV003', 'CA002'),
('PC004', '2026-10-07', 'NV004', 'CA002');
GO

INSERT INTO TheThanhVien
(maThe, hangThe, chietKhauPhanTram, ngayCap, maKhachHang, maNhanVien)
VALUES
('THE001', N'Silver', 5, '2026-01-10', 'KH001', 'NV001'),
('THE002', N'Gold', 10, '2026-02-15', 'KH002', 'NV001'),
('THE003', N'Platinum', 15, '2026-03-20', 'KH003', 'NV001'),
('THE004', N'Silver', 5, '2026-05-12', 'KH004', 'NV001');
GO

INSERT INTO LichHen
(maLichHen, ngayGioHen, ghiChu, maKhachHang, maNhanVien, maThuCung)
VALUES
('LH001', '2026-10-07 09:00:00', N'Tắm và cắt tỉa', 'KH001', 'NV004', 'TC001'),
('LH002', '2026-10-07 10:30:00', N'Vệ sinh tai', 'KH002', 'NV004', 'TC002'),
('LH003', '2026-10-07 14:00:00', N'Combo Spa', 'KH003', 'NV004', 'TC003'),
('LH004', '2026-10-08 15:00:00', N'Cắt móng', 'KH004', 'NV004', 'TC004');
GO

INSERT INTO ChiTietLichHen
(maLichHen, maDichVu, trieuChung, chuanDoan, huongTriLieu, ghiChu, maNhanVien)
VALUES
('LH001', 'DV001', N'Lông bẩn', N'Bình thường', N'Tắm và sấy', N'Không dùng nước hoa', 'NV004'),
('LH001', 'DV002', N'Lông dài', N'Bình thường', N'Cắt tỉa', N'Cắt gọn', 'NV004'),
('LH002', 'DV003', N'Tai có ráy', N'Ráy tai nhẹ', N'Vệ sinh tai', N'Nhẹ nhàng', 'NV004'),
('LH003', 'DV005', N'Cần chăm sóc toàn diện', N'Bình thường', N'Combo Premium', N'Khách đặt trước', 'NV004'),
('LH004', 'DV004', N'Móng dài', N'Bình thường', N'Cắt móng', N'Kiểm tra chân', 'NV004');
GO

INSERT INTO PhieuDichVu
(maPhieuDichVu, trangThai, maKhachHang, maThuCung, maNhanVien)
VALUES
('PDV001', 1, 'KH001', 'TC001', 'NV004'),
('PDV002', 1, 'KH002', 'TC002', 'NV004'),
('PDV003', 1, 'KH003', 'TC003', 'NV004');
GO

INSERT INTO ChiTietDichVu
(maPhieuDichVu, maDichVu, soLuong, donGia, ghiChu)
VALUES
('PDV001', 'DV001', 1, 120000, N'Đã hoàn thành'),
('PDV001', 'DV002', 1, 180000, N'Đã hoàn thành'),
('PDV002', 'DV003', 1, 60000, N'Đã hoàn thành'),
('PDV003', 'DV005', 1, 350000, N'Đang xử lý');
GO

INSERT INTO HoaDon
(maHoaDon, ngayLap, maPhuongThuc, maKhachHang, maNhanVien, maVoucher)
VALUES
('HD001', '2026-10-07 08:30:00', 'PT001', 'KH001', 'NV002', NULL),
('HD002', '2026-10-07 09:15:00', 'PT002', 'KH002', 'NV002', 'VC001'),
('HD003', '2026-10-07 10:20:00', 'PT001', 'KH003', 'NV002', 'VC002'),
('HD004', '2026-10-06 14:10:00', 'PT003', 'KH004', 'NV002', NULL),
('HD005', '2026-10-06 16:40:00', 'PT001', 'KH005', 'NV002', NULL);
GO

INSERT INTO ChiTietHoaDon
(maHoaDon, maThuCung, maDichVu, maSanPham, soLuong, donGia, phanTramGiam)
VALUES
('HD001', NULL, NULL, 'SP001', 1, 250000, 0),
('HD001', NULL, NULL, 'SP003', 2, 59000, 0),

('HD002', NULL, NULL, 'SP002', 2, 135000, 10),
('HD002', NULL, NULL, 'SP008', 3, 39000, 10),

('HD003', NULL, 'DV001', NULL, 1, 120000, 15),
('HD003', NULL, 'DV002', NULL, 1, 180000, 15),

('HD004', NULL, NULL, 'SP005', 1, 145000, 0),
('HD004', NULL, NULL, 'SP006', 1, 150000, 0),

('HD005', NULL, NULL, 'SP004', 1, 120000, 0),
('HD005', NULL, NULL, 'SP007', 1, 99000, 0);
GO

INSERT INTO PhieuNhapKho
(maPhieuNhap, ngayNhap, trangThai, maNhanVien, maNhaCungCap)
VALUES
('PN001', '2026-10-01 08:00:00', 1, 'NV003', 'NCC001'),
('PN002', '2026-10-03 09:00:00', 1, 'NV003', 'NCC002'),
('PN003', '2026-10-05 10:00:00', 1, 'NV003', 'NCC003');
GO

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
GO

INSERT INTO PhieuKiemKe
(maPhieuKiemKe, ngayKiemKe, trangThai, maNhanVien)
VALUES
('KK001', '2026-10-06 17:00:00', 1, 'NV003');
GO

INSERT INTO ChiTietPhieuKiemKe
(maPhieuKiemKe, maSanPham, soLuongHeThong, soLuongThucTe, trangThai)
VALUES
('KK001', 'SP001', 29, 29, 1),
('KK001', 'SP002', 38, 37, 0),
('KK001', 'SP003', 18, 18, 1),
('KK001', 'SP004', 14, 14, 1),
('KK001', 'SP005', 11, 10, 0),
('KK001', 'SP006', 17, 17, 1);
GO
