import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.xssf.usermodel.*;
import java.io.*;


public class AnalyzeRef {
    public static void main(String[] args) throws Exception {
        File f = new File("C:\\Users\\esantana\\Desktop\\RECEIPTS\\RECIBO_DE_BODEGA_AWB 406-02529962.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(f);
        XSSFSheet sh = wb.getSheetAt(0);
        
        System.out.println("=== SHEET ===");
        System.out.println("Sheet name: " + sh.getSheetName());
        System.out.println("Physical rows: " + sh.getPhysicalNumberOfRows());
        
        System.out.println("\n=== MERGE REGIONS (" + sh.getNumMergedRegions() + ") ===");
        for (int i = 0; i < sh.getNumMergedRegions(); i++) {
            CellRangeAddress mr = sh.getMergedRegion(i);
            System.out.println("  " + mr.formatAsString());
        }
        
        System.out.println("\n=== COLUMN WIDTHS ===");
        for (int c = 0; c <= 14; c++) {
            if (c >= sh.getLastRowNum()) break;
            int w = sh.getColumnWidth(c);
            System.out.println("  Col " + c + ": width=" + w + " chars=" + (w/256.0) + " hidden=" + sh.isColumnHidden(c));
        }
        
        System.out.println("\n=== ALL CELLS ===");
        for (int r = 0; r <= sh.getLastRowNum(); r++) {
            Row row = sh.getRow(r);
            if (row == null) { System.out.println("Row " + r + ": NULL"); continue; }
            System.out.print("Row " + r + ": h=" + row.getHeightInPoints() + "pt");
            for (int c = 0; c < 14; c++) {
                Cell cell = row.getCell(c);
                if (cell == null) continue;
                String val = "";
                if (cell.getCellType() == CellType.STRING) val = cell.getStringCellValue();
                else if (cell.getCellType() == CellType.NUMERIC) val = String.valueOf(cell.getNumericCellValue());
                else if (cell.getCellType() == CellType.BOOLEAN) val = String.valueOf(cell.getBooleanCellValue());
                else if (cell.getCellType() == CellType.FORMULA) val = cell.getCellFormula();
                if (val.length() > 40) val = val.substring(0, 40);
                System.out.print(" " + (char)('A'+c) + "=" + val);
            }
            System.out.println();
        }
        
        System.out.println("\n=== STYLES ===");
        for (int i = 0; i < wb.getNumCellStyles(); i++) {
            XSSFCellStyle cs = wb.getStylesSource().getStyleAt(i);
            XSSFFont fnt = cs.getFont();
            System.out.println("Style " + i + ": font=" + fnt.getFontName() + " " + fnt.getFontHeightInPoints() + "pt bold=" + fnt.getBold()
                + " align=" + cs.getAlignment() + " fillFg=" + cs.getFillForegroundColor() + " fillBg=" + cs.getFillBackgroundColor()
                + " bL=" + cs.getBorderLeft() + " bR=" + cs.getBorderRight() + " bT=" + cs.getBorderTop() + " bB=" + cs.getBorderBottom()
                + " locked=" + cs.getLocked());
        }
        
        System.out.println("\n=== PROTECTION ===");
        System.out.println("Protected: " + sh.getProtect());
        
        wb.close();
    }
}