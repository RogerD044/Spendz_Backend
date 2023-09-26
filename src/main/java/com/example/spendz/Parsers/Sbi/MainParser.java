package com.example.spendz.Parsers.Sbi;

import com.example.spendz.Model.Spend;
import com.example.spendz.Model.Tag;
import com.example.spendz.Repo.SpendRepo;
import com.example.spendz.Repo.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Date;
import java.util.HashMap;

@Component
public class MainParser {

    private String fileLocation = "src/main/java/com/example/spendz/Parsers/Sbi/Aug.tsv";
    private final String REGEX = "\t";
    private final static String BANK_TYPE = "SBI";
    private static final String STARTING_TEXT = "TxnDateValueDateDescriptionRefNo./ChequeNo.DebitCreditBalance";
    private static final String DIR = "src/main/resources/data";
    private static final String ERROR_FILE = "src/main/resources/data/error.txt";
    private static final HashMap<String, Integer> CATEGORY_MAPPER = new HashMap<String, Integer>(){{
        put("cab",2);
        put("ola",2);
        put("uber",2);
        put("food",7);
        put("shop",11);
        put("grocery",12);
        put("medicine",13);
        put("bulk posting",4);
    }};

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
        for (int i = 0; i < contents.length; i++) {
            String fileName = DIR + "/" + contents[i];
            File file = new File(fileName);
            fileName = fileName.replace(".xls", ".tsv");
            File rename = new File(fileName);
            file.renameTo(rename);

            // Parse Spend
            try {
                BufferedReader objReader = new BufferedReader(new FileReader(fileName));
                String fileText = "";

                while ((fileText = objReader.readLine()) != null) {
                    fileText = fileText.replace(" ", "");
                    fileText = fileText.replace("\t", "");
                    if (fileText.equals(STARTING_TEXT))
                        break;
                }

                while ((fileText = objReader.readLine()) != null) {
                    if (fileText.equals(""))
                        break;
                    parseToSpend(fileText);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Delete File
            rename.delete();
        }
    }

    public void parseToSpend(String strCurrentLine) {
        try {
            String[] str = strCurrentLine.split("\t");

            Date txDate = dateParser.parseDate(str[0]);
            String desc = str[2].trim();
            double debit = amountParser.parseAmount(str[4]);
            double credit = amountParser.parseAmount(str[5]);
            double balance = amountParser.parseAmount(str[6]);
            String info = descParser.extractInfoFromDescription(desc);
            String displayInfo = (info.split("@").length==1) ? info : info.split("@")[0];
            String categoryComment = (info.split("@").length==1) ? "" : info.split("@")[1];
            String paymentVia = descParser.paymentVia(desc);

//            System.out.println(txDate + " | " +desc+ " | " +debit+ " | " +credit+ " | " + balance);

            Spend existingSpend = spendRepo.findByRawDescAndBalance(desc, balance);
            if (null != existingSpend)
                return;

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
        for(String unlistedCategory : CATEGORY_MAPPER.keySet()) {
            if(unlistedCategory.startsWith(comment.toLowerCase()))
                return CATEGORY_MAPPER.get(unlistedCategory);
        }

        return 1;
    }

}
