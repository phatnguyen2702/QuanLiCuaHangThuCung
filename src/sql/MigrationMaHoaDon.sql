BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE object_id = OBJECT_ID('dbo.ChiTietHoaDon')
          AND name = 'IX_ChiTietHoaDon_HoaDon'
    )
        DROP INDEX IX_ChiTietHoaDon_HoaDon ON dbo.ChiTietHoaDon;

    IF EXISTS (
        SELECT 1
        FROM sys.foreign_keys
        WHERE parent_object_id = OBJECT_ID('dbo.ChiTietHoaDon')
          AND name = 'FK_CTHD_HoaDon'
    )
        ALTER TABLE dbo.ChiTietHoaDon DROP CONSTRAINT FK_CTHD_HoaDon;

    DECLARE @pkHoaDon SYSNAME;
    SELECT @pkHoaDon = name
    FROM sys.key_constraints
    WHERE parent_object_id = OBJECT_ID('dbo.HoaDon')
      AND type = 'PK';

    IF @pkHoaDon IS NOT NULL
    BEGIN
        DECLARE @dropPk NVARCHAR(4000) =
            N'ALTER TABLE dbo.HoaDon DROP CONSTRAINT ' + QUOTENAME(@pkHoaDon);
        EXEC sp_executesql @dropPk;
    END;

    ALTER TABLE dbo.HoaDon ALTER COLUMN maHoaDon VARCHAR(15) NOT NULL;
    ALTER TABLE dbo.ChiTietHoaDon ALTER COLUMN maHoaDon VARCHAR(15) NOT NULL;

    ALTER TABLE dbo.HoaDon
        ADD CONSTRAINT PK_HoaDon PRIMARY KEY (maHoaDon);
    ALTER TABLE dbo.ChiTietHoaDon
        ADD CONSTRAINT FK_CTHD_HoaDon
        FOREIGN KEY (maHoaDon) REFERENCES dbo.HoaDon(maHoaDon);
    CREATE INDEX IX_ChiTietHoaDon_HoaDon
        ON dbo.ChiTietHoaDon(maHoaDon);

    DECLARE @giaTriLonNhat INT;
    SELECT @giaTriLonNhat =
        ISNULL(MAX(TRY_CONVERT(INT, RIGHT(maHoaDon, 4))), 0)
    FROM dbo.HoaDon;

    IF OBJECT_ID('dbo.BoDemHoaDon', 'U') IS NULL
    BEGIN
        CREATE TABLE dbo.BoDemHoaDon (
            id INT NOT NULL PRIMARY KEY,
            giaTri INT NOT NULL,
            CONSTRAINT CK_BoDemHoaDon_id CHECK (id = 1),
            CONSTRAINT CK_BoDemHoaDon_giaTri CHECK (giaTri BETWEEN 0 AND 9999)
        );
    END;

    IF EXISTS (SELECT 1 FROM dbo.BoDemHoaDon WHERE id = 1)
        UPDATE dbo.BoDemHoaDon
        SET giaTri = CASE
            WHEN giaTri < @giaTriLonNhat THEN @giaTriLonNhat
            ELSE giaTri
        END
        WHERE id = 1;
    ELSE
        INSERT INTO dbo.BoDemHoaDon (id, giaTri)
        VALUES (1, @giaTriLonNhat);

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    THROW;
END CATCH;
