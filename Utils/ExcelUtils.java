package Utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.util.*;

public class ExcelUtils {

    /**
     * Đọc dữ liệu từ file Excel
     */
    public static List<Map<String, Object>> readExcel(File file, int sheetIndex) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheetAt(sheetIndex);
            if (sheet == null) return data;
            
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return data;
            
            // Lấy headers
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                headers.add(cell != null ? cell.getStringCellValue() : "");
            }
            
            // Đọc dữ liệu từ dòng 1 trở đi
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), getCellValue(cell));
                }
                data.add(rowData);
            }
        }
        return data;
    }

    /**
     * Viết dữ liệu ra file Excel
     */
    public static void writeExcel(File file, List<String> headers, List<List<Object>> rows) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            
            // Tạo header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(createBoldFont(workbook));
            
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            
            // Tạo data rows
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                List<Object> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = rowData.get(j);
                    if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                }
            }
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Lấy giá trị của một cell
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double value = cell.getNumericCellValue();
                    // Nếu là số nguyên, trả về long, nếu không trả về double
                    if (value == Math.floor(value)) {
                        return (long) value;
                    }
                    return value;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return "";
        }
    }

    /**
     * Tạo font bold cho header
     */
    private static Font createBoldFont(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        return font;
    }

    /**
     * Tạo file template cho sản phẩm
     */
    public static File createSanPhamTemplate() throws IOException {
        List<String> headers = Arrays.asList(
            "Code danh mục*",
            "Code sản phẩm*",
            "Tên sản phẩm*",
            "Lợi nhuận*",
            "Giá bán*"
        );
        
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("CAT-001", "SP001", "Ví dụ 1", 20.0, 15000.0));
        rows.add(Arrays.asList("CAT-002", "SP002", "Ví dụ 2", 30.0, 50000.0));
        
        File templateFile = new File(System.getProperty("user.home") + "/Desktop/SanPham_Template.xlsx");
        writeExcel(templateFile, headers, rows);
        return templateFile;
    }

    /**
     * Tạo file template cho phiếu nhập
     */
    public static File createPhieuNhapTemplate() throws IOException {
        List<String> headers = Arrays.asList(
            "Mã nhà cung cấp*",
            "Mã sản phẩm*",
            "Serial sản phẩm*",
            "Đơn giá nhập*",
            "Thời gian BH (tháng)"
        );
        
        List<List<Object>> rows = new ArrayList<>();
        rows.add(Arrays.asList("NCC A", "SP001", "SN001;SN002;SN003", 12000.0, 3));
        rows.add(Arrays.asList("NCC A", "SP002", "SN004;SN005", 45000.0, 7));
        
        File templateFile = new File(System.getProperty("user.home") + "/PhieuNhap_Template.xlsx");
        writeExcel(templateFile, headers, rows);
        return templateFile;
    }
}