package com.example.spendz.Parsers.Sbi;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserExtractionTests {

    private final DateParser dateParser = new DateParser();
    private final AmountParser amountParser = new AmountParser();
    private final DescParser descParser = new DescParser();

    @Test
    void shouldParseFieldsFromTransactionRow() {
        String row = "03/07/2026\tUPI/DR/12345/98765/Amazon/Ref/Shopping--ab\tIGNORED\t1,250.50\t0\t9,999.49";
        String[] str = row.split("\\t");

        Date txDate = dateParser.parseDate(str[0]);
        String desc = str[1].trim();
        double debit = amountParser.parseAmount(str[3]);
        double credit = amountParser.parseAmount(str[4]);
        double balance = amountParser.parseAmount(str[5]);
        String info = descParser.extractInfoFromDescription(desc);
        String displayInfo = (info.split("@").length == 1) ? info : info.split("@")[0];
        String categoryComment = (info.split("@").length == 1) ? "" : info.split("@")[1];
        String paymentVia = descParser.paymentVia(desc);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(txDate);

        assertEquals(2026, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.JULY, calendar.get(Calendar.MONTH));
        assertEquals(3, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));

        assertEquals("UPI/DR/12345/98765/Amazon/Ref/Shopping--ab", desc);
        assertEquals(1250.50, debit, 0.0001);
        assertEquals(0.0, credit, 0.0001);
        assertEquals(9999.49, balance, 0.0001);
        assertEquals("Amazon@Shopping", info);
        assertEquals("Amazon", displayInfo);
        assertEquals("Shopping", categoryComment);
        assertEquals("UPI_TRANSFER", paymentVia);
    }

    @Test
    void shouldExtractInfoForUpiTransfer() {
        String desc = "\" WDL TFR   UPI/DR/528768342704/SOUMEN DAS/BARB/soumen\n" +
                " d044/gr   0097692162094 AT 09009 BARIDIH (JAMSHEDPUR)\"\n";

        String info = descParser.extractInfoFromDescription(desc);
        System.out.println(info);
        assertEquals("ReceiverName@Grocery", info);
    }

    @Test
    void shouldExtractInfoForAtmWithdrawal() {
        String desc = "ATM WDL CASH WD";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("ATM WDL", info);
    }

    @Test
    void shouldExtractInfoForDebitCard() {
        String desc = "POS PURCHASE by debit card RELIANCE FRESH";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("RELIANCE FRESH", info);
    }

    @Test
    void shouldExtractInfoForInternetBanking() {
        String desc = "TRANSFER-INB RENT JULY";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("RENT JULY", info);
    }

    @Test
    void shouldExtractInfoForWithdrawalTransfer() {
        String desc = "WITHDRAWAL TRANSFER TO SAVINGS";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("WITHDRAWAL TRANSFER TO SAVINGS", info);
    }

    @Test
    void shouldExtractInfoForNeft() {
        String desc = "TRANSFER-NEFT*HDFC*LANDLORD";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("LANDLORD", info);
    }

    @Test
    void shouldFallbackToFullDescriptionForOthers() {
        String desc = "ACH CREDIT SALARY";

        String info = descParser.extractInfoFromDescription(desc);

        assertEquals("ACH CREDIT SALARY", info);
    }

    @Test
    void shouldIdentifyPaymentViaForEachKnownType() {
        assertEquals("UPI_TRANSFER", descParser.paymentVia("UPI/DR/1/2/name/ref/test--ab"));
        assertEquals("ATM_WITHDRAWAL", descParser.paymentVia("ATM WDL ATM123"));
        assertEquals("DEBIT_CARD", descParser.paymentVia("by debit card STORE"));
        assertEquals("INTERNET_BANKING", descParser.paymentVia("TRANSFER-INB SOME PAYMENT"));
        assertEquals("TRANSFER", descParser.paymentVia("WITHDRAWAL TRANSFER XYZ"));
        assertEquals("NEFT", descParser.paymentVia("TRANSFER-NEFT*ABC*XYZ"));
        assertEquals("OTHERS", descParser.paymentVia("IMPS CREDIT"));
    }

    @Test
    void shouldReturnZeroForInvalidAmount() {
        assertEquals(0.0, amountParser.parseAmount("not-a-number"), 0.0001);
    }
}
