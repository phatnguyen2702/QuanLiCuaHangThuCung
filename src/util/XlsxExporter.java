package util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Xuat .xlsx chuan Office Open XML, khong can thu vien ben ngoai. */
public final class XlsxExporter {
    // Ẩn hàm khởi tạo vì lớp chỉ cung cấp các hàm tiện ích dùng chung.
    private XlsxExporter() {
    }

    // Xuất dữ liệu ra tệp theo định dạng do lớp cung cấp.
    // Tham số tep: tệp đầu ra.
    // Tham số tenCot: các tiêu đề cột.
    // Tham số duLieu: dữ liệu.
    public static void xuat(Path tep, String[] tenCot, List<String[]> duLieu) throws IOException {
        if (tenCot == null || tenCot.length == 0)
            throw new IllegalArgumentException("Thiếu cột Excel");
        try (OutputStream out = Files.newOutputStream(tep);
                ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            ghi(zip, "[Content_Types].xml",
                    """
                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                            <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                            <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                            <Default Extension="xml" ContentType="application/xml"/>
                            <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                            <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                            <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
                            </Types>
                            """);
            ghi(zip, "_rels/.rels",
                    """
                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                            </Relationships>
                            """);
            ghi(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets><sheet name="Lịch phân ca" sheetId="1" r:id="rId1"/></sheets>
                    </workbook>
                    """);
            ghi(zip, "xl/_rels/workbook.xml.rels",
                    """
                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                            <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                            <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
                            </Relationships>
                            """);
            ghi(zip, "xl/styles.xml",
                    """
                            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                            <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                            <fonts count="2"><font><sz val="11"/><name val="Calibri"/></font>
                            <font><b/><color rgb="FFFFFFFF"/><sz val="11"/><name val="Calibri"/></font></fonts>
                            <fills count="2"><fill><patternFill patternType="none"/></fill>
                            <fill><patternFill patternType="solid"><fgColor rgb="FF123052"/><bgColor indexed="64"/></patternFill></fill></fills>
                            <borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders>
                            <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
                            <cellXfs count="2"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
                            <xf numFmtId="0" fontId="1" fillId="1" borderId="0" xfId="0" applyFont="1" applyFill="1"/></cellXfs>
                            <cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>
                            </styleSheet>
                            """);
            // sb: Bộ đệm ghép các đoạn chuỗi để tạo bộ đệm chuỗi.
            StringBuilder sb = new StringBuilder(8192);
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                    .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
            sb.append(
                    "<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>");
            sb.append("<cols>");
            for (int i = 0; i < tenCot.length; i++) {
                sb.append("<col min=\"").append(i + 1).append("\" max=\"").append(i + 1)
                        .append("\" width=\"").append(i == 3 || i == 5 ? 29 : 19).append("\" customWidth=\"1\"/>");
            }
            sb.append("</cols><sheetData>");
            ghiDong(sb, 1, tenCot, true);
            for (int i = 0; i < duLieu.size(); i++)
                ghiDong(sb, i + 2, duLieu.get(i), false);
            sb.append("</sheetData><autoFilter ref=\"A1:").append(cotExcel(tenCot.length))
                    .append(Math.max(1, duLieu.size() + 1)).append("\"/></worksheet>");
            ghi(zip, "xl/worksheets/sheet1.xml", sb.toString());
        }
    }

    // Ghi dữ liệu một dòng vào XML của trang tính.
    // Tham số out: đầu ra.
    // Tham số row: chỉ số dòng.
    // Tham số ds: danh sách.
    // Tham số tieuDe: tiêu đề.
    private static void ghiDong(StringBuilder out, int row, String[] ds, boolean tieuDe) {
        out.append("<row r=\"").append(row).append("\">");
        for (int col = 0; col < ds.length; col++) {
            out.append("<c r=\"").append(cotExcel(col + 1)).append(row)
                    .append("\" t=\"inlineStr\"");
            if (tieuDe)
                out.append(" s=\"1\"");
            out.append("><is><t xml:space=\"preserve\">")
                    .append(xml(ds[col] == null ? "" : ds[col]))
                    .append("</t></is></c>");
        }
        out.append("</row>");
    }

    // Chuyển chỉ số cột sang tên cột Excel như A, B hoặc AA.
    // Tham số i: i.
    // Kết quả: chuỗi kết quả.
    private static String cotExcel(int i) {
        // s: Bộ đệm ghép các đoạn chuỗi để tạo chuỗi đầu vào.
        StringBuilder s = new StringBuilder();
        while (i > 0) {
            i--;
            s.insert(0, (char) ('A' + i % 26));
            i /= 26;
        }
        return s.toString();
    }

    // Mã hóa ký tự đặc biệt để tạo nội dung XML hợp lệ.
    // Tham số s: chuỗi đầu vào.
    // Kết quả: chuỗi kết quả.
    private static String xml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    // Ghi một mục nội dung vào tệp ZIP cấu thành tệp XLSX.
    // Tham số zip: tệp ZIP.
    // Tham số name: tên.
    // Tham số text: nội dung chữ.
    private static void ghi(ZipOutputStream zip, String name, String text) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
