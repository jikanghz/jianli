package com.jianli.common.excel;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jianli.common.Ext;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Excel {

    private static final String EXCEL_XLS = "xls";
    private static final String EXCEL_XLSX = "xlsx";

    public static Workbook getWorkbok(InputStream in, File file) throws Exception {
        Workbook wb = null;
        if (file.getName().endsWith(EXCEL_XLS)) {  //Excel 2003
            wb = new HSSFWorkbook(in);
        } else if (file.getName().endsWith(EXCEL_XLSX)) {  // Excel 2007/2010
            wb = new XSSFWorkbook(in);
        }

        return wb;
    }

    public static List<List<String>> read(String path) throws Exception {
        List<List<String>> table = new ArrayList<List<String>>();

        File excelFile = new File(path); // 创建文件对象
        FileInputStream in = new FileInputStream(excelFile); // 文件流
        Workbook workbook = getWorkbok(in, excelFile);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row excelRow : sheet) {
            {
                List<String> row = new ArrayList<String>();
                for (int j = excelRow.getFirstCellNum(); j < excelRow.getPhysicalNumberOfCells(); j++) {
                    Cell cell = excelRow.getCell(j);
                    String msg = cell.toString();
                    if (cell.getCellType() == CellType.NUMERIC) {
                        if (DateUtil.isCellDateFormatted(cell)) {
                            Date dateCellValue = cell.getDateCellValue();
                            msg = Ext.toDateString(dateCellValue, "yy-MM-dd");
                        } else {
                            double numericCellValue = cell.getNumericCellValue();
                            msg = String.valueOf(Ext.toBigDecimal(numericCellValue).longValue());

                        }

                    }
                    row.add(msg);
                }
                table.add(row);
            }
        }

        return table;
    }


    public static void write(JSONObject data, OutputStream out) throws Exception {
        short rowHeight = 320;
        short fontHeight = 220;


        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        sheet.setDefaultColumnWidth(16);
        HSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        HSSFFont headFont = workbook.createFont();
        //headFont.setBold(true);
        headStyle.setFont(headFont);
        headFont.setFontHeight(fontHeight);

        HSSFRow headRow = sheet.createRow(0);
        headRow.setHeight(rowHeight);
        JSONArray cols = data.getJSONArray("cols");
        for (int i = 0; i < cols.size(); ++i) {
            JSONObject col = cols.getJSONObject(i);
            HSSFCell headCell = headRow.createCell(i);
            headCell.setCellStyle(headStyle);
            HSSFRichTextString text = new HSSFRichTextString(col.getString("displayName"));
            headCell.setCellValue(text);
            if (col.containsKey("width")) {
                sheet.setColumnWidth(i, col.getInteger("width"));
            }
        }

        HSSFCellStyle bodyStyle = workbook.createCellStyle();
        bodyStyle.setBorderLeft(BorderStyle.THIN);
        bodyStyle.setBorderRight(BorderStyle.THIN);
        bodyStyle.setBorderTop(BorderStyle.THIN);
        bodyStyle.setBorderBottom(BorderStyle.THIN);
        bodyStyle.setAlignment(HorizontalAlignment.LEFT);
        bodyStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        HSSFFont bodyFont = workbook.createFont();
        bodyFont.setBold(false);
        bodyFont.setFontHeight(fontHeight);
        bodyStyle.setFont(bodyFont);

        JSONArray dataRows = data.getJSONArray("entityList");
        for (int i = 0; i < dataRows.size(); ++i) {
            JSONObject dataRow = dataRows.getJSONObject(i);
            HSSFRow bodyRow = sheet.createRow(i + 1);
            bodyRow.setHeight(rowHeight);
            for (int j = 0; j < cols.size(); ++j) {
                JSONObject col = cols.getJSONObject(j);
                HSSFCell cell = bodyRow.createCell(j);
                cell.setCellStyle(bodyStyle);
                String value = "";
                if (dataRow.containsKey(col.getString("fieldName"))) {
                    value = dataRow.getString(col.getString("fieldName"));
                }

                HSSFRichTextString text = new HSSFRichTextString(value);
                cell.setCellValue(text);
            }
        }


        if (data.containsKey("regions")) {
            JSONArray regions = data.getJSONArray("regions");
            for (int i = 0; i < regions.size(); ++i) {
                JSONObject region = regions.getJSONObject(i);
                CellRangeAddress cellRegion = new CellRangeAddress(region.getInteger("startRow"), region.getInteger("endRow"), region.getInteger("startCol"), region.getInteger("endCol"));
                sheet.addMergedRegion(cellRegion);
            }
        }

        /**
         * 添加 Sheet2 存放其它数据
         */
        if (data.containsKey("otherCols")) {
            HSSFSheet otherSheet = workbook.createSheet("Sheet2");
            otherSheet.setDefaultColumnWidth(16);
            HSSFRow otherHeadRow = otherSheet.createRow(0);
            otherHeadRow.setHeight(rowHeight);
            JSONArray otherCols = data.getJSONArray("otherCols");
            for (int i = 0; i < otherCols.size(); ++i) {
                JSONObject col = otherCols.getJSONObject(i);
                HSSFCell headCell = otherHeadRow.createCell(i);
                headCell.setCellStyle(headStyle);
                HSSFRichTextString text = new HSSFRichTextString(col.getString("displayName"));
                headCell.setCellValue(text);
                if (col.containsKey("width")) {
                    otherSheet.setColumnWidth(i, col.getInteger("width"));
                }
            }

            HSSFRow bodyRow = otherSheet.createRow(1);
            bodyRow.setHeight(rowHeight);
            for (int j = 0; j < otherCols.size(); ++j) {
                JSONObject col = otherCols.getJSONObject(j);
                HSSFCell cell = bodyRow.createCell(j);
                cell.setCellStyle(bodyStyle);
                String value = "";
                if (data.containsKey(col.getString("fieldName"))) {
                    value = data.getString(col.getString("fieldName"));
                }
                HSSFRichTextString text = new HSSFRichTextString(value);
                cell.setCellValue(text);
            }
        }


        workbook.write(out);
    }

    public static void write(String text, OutputStream out) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Sheet1");
        sheet.setDefaultColumnWidth(10240);
        HSSFCellStyle headStyle = workbook.createCellStyle();
        headStyle.setBorderBottom(BorderStyle.THIN);
        headStyle.setBorderLeft(BorderStyle.THIN);
        headStyle.setBorderRight(BorderStyle.THIN);
        headStyle.setBorderTop(BorderStyle.THIN);
        headStyle.setAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        HSSFFont headFont = workbook.createFont();
        headFont.setColor(HSSFFont.COLOR_RED);
        short rowHeight = 320;
        short fontHeight = 220;
        headStyle.setFont(headFont);
        headFont.setFontHeight(fontHeight);

        HSSFRow headRow = sheet.createRow(0);
        headRow.setHeight(rowHeight);

        HSSFCell headCell = headRow.createCell(0);
        headCell.setCellStyle(headStyle);
        HSSFRichTextString cellText = new HSSFRichTextString(text);
        headCell.setCellValue(text);
        sheet.setColumnWidth(0, 10240);

        workbook.write(out);

    }


    public static void createComment(Workbook workbook, int rowIndex, int colIndex, String text) {
        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0);

        CellStyle style = workbook.createCellStyle();

        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setFillForegroundColor(IndexedColors.RED1.index);

        sheet.getRow(rowIndex).getCell(colIndex).setCellStyle(style);

        HSSFPatriarch p = sheet.createDrawingPatriarch();
        HSSFComment comment = p.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 3, 3, (short) 5, 6));
        comment.setString(new HSSFRichTextString(text));
        sheet.getRow(rowIndex).getCell(colIndex).setCellComment(comment);
    }


    public static JSONObject createExcelColumn(String fieldName, String displayName) {
        JSONObject col = new JSONObject(true);
        col.put("fieldName", fieldName);
        col.put("displayName", displayName);
        return col;
    }


    public static JSONObject createExcelColumn(String fieldName, String displayName, int width) {
        JSONObject col = new JSONObject(true);
        col.put("fieldName", fieldName);
        col.put("displayName", displayName);
        col.put("width", width);
        return col;
    }


    public static JSONObject createRegion(int startRow, int endRow, int startCol, int endCol) {
        JSONObject region = new JSONObject(true);
        region.put("startRow", startRow);
        region.put("endRow", endRow);
        region.put("startCol", startCol);
        region.put("endCol", endCol);
        return region;
    }
}