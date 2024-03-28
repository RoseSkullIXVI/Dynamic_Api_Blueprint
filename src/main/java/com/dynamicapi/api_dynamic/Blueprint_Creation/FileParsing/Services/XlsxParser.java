package com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.Services;

import java.io.InputStream;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.dynamicapi.api_dynamic.Blueprint_Creation.FileParsing.ParsingInterface.FileParsingInterface;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class XlsxParser implements FileParsingInterface{
     @Override
    public String FileParser(InputStream file) {
        HashMap<String, StringBuilder> excel_data = new HashMap<String,StringBuilder>();
        String keywords = "";
        try{
            Workbook workbook = WorkbookFactory.create(file);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheet_name = sheet.getSheetName();
                StringBuilder sheet_data = new StringBuilder();
                Row header_row = sheet.getRow(0);
                if (header_row != null) {
                     for (Cell cell : header_row) { // Iterate through each cell of the first row
                    // Process the cell value based on its type
                        switch (cell.getCellType()) {
                            case STRING:
                                sheet_data.append(cell.getStringCellValue() + "\n");
                                break;
                            case NUMERIC:
                                sheet_data.append(cell.getNumericCellValue() + "\n");
                                break;
                            case BOOLEAN:
                                sheet_data.append(cell.getBooleanCellValue() + "\n");
                                break;
                            default:
                                sheet_data.append("Unknown Type\t");
                                break;
                        }
                    }
                }
                excel_data.put(sheet_name, sheet_data);                
            }
            Gson gson = new GsonBuilder().create();
            keywords = gson.toJson(excel_data);
            
            
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Error parsing file";
        }
       return keywords;
    }

    @Override
    public boolean supports(String type) {
        return type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }


    
}
