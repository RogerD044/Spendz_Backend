package com.example.spendz.Parsers.Sbi;

import org.springframework.stereotype.Component;

@Component
public class DescParser {
    private static final String UPI_TRANSFER_DR = "UPI/DR";
    private static final String UPI_TRANSFER_CR = "UPI/CR";
    private static final String ATM_WITHDRAWAL = "ATM WDL";
    private static final String DEBIT_CARD = "by debit card";
    private static final String INTERNET_BANKING = "TRANSFER-INB";
    private static final String WITHDRAWAL = "WITHDRAWAL TRANSFER";
    private static final String NEFT = "TRANSFER-NEFT";
    private static final String SALARY = "NEFT*HDFC";
    private static final String DIVIDENDS = "CEMTEX";

    public String paymentVia(String desc) {
        if (desc.contains(UPI_TRANSFER_DR) || desc.contains(UPI_TRANSFER_CR))
            return "UPI_TRANSFER";
        else if (desc.contains(ATM_WITHDRAWAL))
            return "ATM_WITHDRAWAL";
        else if (desc.contains(DEBIT_CARD))
            return "DEBIT_CARD";
        else if (desc.contains(INTERNET_BANKING))
            return "INTERNET_BANKING";
        else if (desc.contains(WITHDRAWAL))
            return "TRANSFER";
        else if (desc.contains(NEFT))
            return "NEFT";

        return "OTHERS";
    }

    public String extractInfoFromDescription(String desc) {
        String info = "";
        if (desc.contains(UPI_TRANSFER_DR) || desc.contains(UPI_TRANSFER_CR))
            info = extractUpiTransferInfoFromDesc(desc);
        else if (desc.contains(SALARY))
            info = SALARY;
        else if (desc.contains(DIVIDENDS))
            info = DIVIDENDS;
        else if (desc.contains(ATM_WITHDRAWAL))
            info = ATM_WITHDRAWAL;
        else if (desc.contains(DEBIT_CARD))
            info = extractDebitCardInfoFromDesc(desc);
        else if (desc.contains(INTERNET_BANKING))
            info = extractIntBankingInfoFromDesc(desc);
        else if (desc.contains(WITHDRAWAL))
            info = desc;
        else if (desc.contains(NEFT))
            info = extractNeftInfoFromDesc(desc);
        else
            info = desc;

        info = info.replace("--", "");
        return info;
    }

    public String extractUpiTransferInfoFromDesc(String desc) {
        try {
            String upiConstant = desc.contains(UPI_TRANSFER_DR) ? UPI_TRANSFER_DR : UPI_TRANSFER_CR;
            String receiver = desc.substring(desc.indexOf(upiConstant) + upiConstant.length()).split("/")[2];
            String comments =  desc.substring(desc.indexOf(upiConstant) + upiConstant.length()).split("/")[5];
            comments = comments.split(" ")[0];
            return receiver + "@" + comments;
        } catch (Exception e) {
            return desc;
        }
    }

    public String extractDebitCardInfoFromDesc(String desc) {
        try {
            String newStr = desc.substring(desc.indexOf(DEBIT_CARD) + DEBIT_CARD.length());
            return newStr.substring(newStr.indexOf(" ")).trim();
        } catch (Exception e) {
            return desc;
        }
    }

    public String extractIntBankingInfoFromDesc(String desc) {
        try {
            return desc.substring(desc.indexOf(INTERNET_BANKING) + INTERNET_BANKING.length()).trim();
        } catch (Exception e) {
            return desc;
        }
    }

    public String extractNeftInfoFromDesc(String desc) {
        try {
            return desc.substring(desc.lastIndexOf("*") + 1);
        } catch (Exception e) {
            return desc;
        }
    }
}
