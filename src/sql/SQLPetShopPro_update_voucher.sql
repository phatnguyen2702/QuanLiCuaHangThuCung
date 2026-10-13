USE ptudpetshop;
GO

/* Bang Vocher chua co ngay hieu luc va gioi han luot dung */
IF COL_LENGTH('Vocher', 'ngayBatDau') IS NULL
    ALTER TABLE Vocher ADD ngayBatDau DATE NULL;
GO
IF COL_LENGTH('Vocher', 'ngayKetThuc') IS NULL
    ALTER TABLE Vocher ADD ngayKetThuc DATE NULL;
GO
IF COL_LENGTH('Vocher', 'soLuongToiDa') IS NULL
    ALTER TABLE Vocher ADD soLuongToiDa INT NULL;   -- NULL = khong gioi han
GO

/* (Tuy chon) du lieu mau de thay cac trang thai tren giao dien */
UPDATE Vocher SET ngayBatDau = '2026-09-01', ngayKetThuc = '2026-12-31', soLuongToiDa = 200
WHERE maVoucher = 'VC001' AND ngayBatDau IS NULL;
UPDATE Vocher SET ngayBatDau = '2026-01-01', ngayKetThuc = '2026-12-31', soLuongToiDa = NULL
WHERE maVoucher = 'VC002' AND ngayBatDau IS NULL;
UPDATE Vocher SET ngayBatDau = '2026-11-01', ngayKetThuc = '2026-11-30', soLuongToiDa = 100
WHERE maVoucher = 'VC003' AND ngayBatDau IS NULL;
GO
