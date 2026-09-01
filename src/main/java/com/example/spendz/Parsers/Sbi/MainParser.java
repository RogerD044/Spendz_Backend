package com.example.spendz.Parsers.Sbi;

import com.example.spendz.Model.Spend;
import com.example.spendz.Model.Tag;
import com.example.spendz.Repo.SpendRepo;
import com.example.spendz.Repo.TagRepo;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

@Component
public class MainParser {

    private String fileLocation = "src/main/java/com/example/spendz/Parsers/Sbi/Aug.tsv";
    private final static String BANK_TYPE = "SBI";
    private static final String STARTING_TEXT = "Date\tDetails\tRef No/Cheque No\tDebit\tCredit\tBalance\t";
    private static final String DIR = "src/main/resources/data";
    private static final String ERROR_FILE = "src/main/resources/data/error.txt";
    private static final HashMap<String, Integer> CATEGORY_MAPPER = new HashMap<String, Integer>() {
        {
            put("cab", 2);
            put("ola", 2);
            put("uber", 2);
            put("food", 7);
            put("shop", 11);
            put("grocery", 12);
            put("gro", 12);
            put("gr", 12);
            put("medicine", 13);
            put("bulk posting", 15);
            put("inv", 4);
            put("NEFT*HDFC", 14);
            put("CEMTEX",15);
        }
    };

    @Autowired
    DateParser dateParser;

    @Autowired
    AmountParser amountParser;

    @Autowired
    DescParser descParser;

    @Autowired
    SpendRepo spendRepo;

    @Autowired
    TagRepo tagRepo;

    public void addDataFromFiles() {
        //Creating a File object for directory
        File directoryPath = new File(DIR);
        //List of all files and directories
        String contents[] = directoryPath.list();
        if (contents == null) {
            return;
        }

        for (int i = 0; i < contents.length; i++) {
            String fileName = DIR + "/" + contents[i];
            File file = new File(fileName);
//            fileName = fileName.replace(".xls", ".tsv");
//            File rename = new File(fileName);
//            file.renameTo(rename);

            // Parse Spend
            try {
                if (isExcelFile(fileName)) {
                    parseExcelFile(file);
                } else {
                    parseTextFile(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Delete File
//            file.delete();
        }
    }

    private void parseTextFile(File file) throws IOException {
        try (BufferedReader objReader = new BufferedReader(new FileReader(file))) {
            String fileText;
            while ((fileText = objReader.readLine()) != null) {
                if (isHeaderLine(fileText)) {
                    break;
                }
            }

            while ((fileText = objReader.readLine()) != null) {
                if (fileText.trim().isEmpty()) {
                    break;
                }
                parseToSpend(fileText);
            }
        }
    }

    private void parseExcelFile(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.US);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            boolean foundHeader = false;
            for (Row row : sheet) {
                if (isRowEmpty(row)) {
                    if (foundHeader) {
                        break;
                    }
                    continue;
                }

                String rowText = formatRow(row, formatter, evaluator);
                if (!foundHeader) {
                    if (isHeaderLine(rowText)) {
                        foundHeader = true;
                    }
                    continue;
                }

                parseToSpend(rowText);
            }
        }
    }

    private String formatRow(Row row, DataFormatter formatter, FormulaEvaluator evaluator) {
        StringBuilder sb = new StringBuilder();
        int maxColumns = Math.max(7, row.getLastCellNum());
        for (int i = 0; i < maxColumns; i++) {
            if (i > 0) {
                sb.append('\t');
            }

            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                sb.append(formatter.formatCellValue(cell, evaluator));
            }
        }
        return sb.toString();
    }

    private boolean isHeaderLine(String line) {
        if (line == null) {
            return false;
        }
        return line.equals(STARTING_TEXT);
    }

    private boolean isExcelFile(String fileName) {
        String normalized = fileName.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".xlsx") || normalized.endsWith(".xls");
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        int firstCell = row.getFirstCellNum();
        int lastCell = row.getLastCellNum();
        if (firstCell < 0 || lastCell < 0) {
            return true;
        }

        for (int i = firstCell; i < lastCell; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellTypeEnum() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    public void parseToSpend(String strCurrentLine) {
        try {
            String[] str = strCurrentLine.split("\t");

            Date txDate = dateParser.parseDate(str[0]);
            String desc = str[1].trim();
            double debit = amountParser.parseAmount(str[3]);
            double credit = amountParser.parseAmount(str[4]);
            double balance = amountParser.parseAmount(str[5]);
            String info = descParser.extractInfoFromDescription(desc);
            String displayInfo = (info.split("@").length == 1) ? info : info.split("@")[0];
            String categoryComment = (info.split("@").length == 1) ? "" : info.split("@")[1];
            String paymentVia = descParser.paymentVia(desc);

            System.out.println(txDate + " | " +info);
            Spend existingSpend = spendRepo.findByRawDescAndBalance(desc, balance);
            if (null != existingSpend) {
                return;
            }

            Spend spend = (Spend.builder()
                    .amount((debit == 0.0) ? credit : debit)
                    .balance(balance)
                    .rawDesc(desc)
                    .info(info)
                    .txDate(txDate)
                    .type((debit == 0.0) ? Spend.SpendType.C : Spend.SpendType.D)
                    .bankName(BANK_TYPE)
                    .categoryId(categoryMapper(categoryComment))
                    .displayInfo(displayInfo)
                    .excludeFromExpense(false)
                    .paymentVia(paymentVia)
                    .build());
            // Check if this info is already tagged to a category
            Tag tag = tagRepo.findByInfo(spend.getInfo());
            if (null != tag) {
                spend.setCategoryId(tag.getCategoryId());
            }

            spendRepo.save(spend);

        } catch (Exception e) {
            try {
                System.err.println(strCurrentLine);
                BufferedWriter writer = new BufferedWriter(new FileWriter(ERROR_FILE, true));
                writer.write(strCurrentLine);
                writer.write("\n");
                writer.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private int categoryMapper(String comment) {
        for (String unlistedCategory : CATEGORY_MAPPER.keySet()) {
            if (comment.toLowerCase().startsWith(unlistedCategory)) {
                return CATEGORY_MAPPER.get(unlistedCategory);
            }
        }

        return 1;
    }

}
