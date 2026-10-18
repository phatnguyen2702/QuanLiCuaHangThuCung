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
    private XlsxExporter() {
    }

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

    private static String cotExcel(int i) {
        StringBuilder s = new StringBuilder();
        while (i > 0) {
            i--;
            s.insert(0, (char) ('A' + i % 26));
            i /= 26;
        }
        return s.toString();
    }

    private static String xml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static void ghi(ZipOutputStream zip, String name, String text) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
