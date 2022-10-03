package com.example.spendz.Service;

import com.example.spendz.Model.Category;
import com.example.spendz.Model.Requests.Trends.TrendRequest;
import com.example.spendz.Model.Response.Trends.*;
import com.example.spendz.Model.Spend;
import com.example.spendz.Repo.CategoryRepo;
import com.example.spendz.Repo.SpendRepo;
import com.example.spendz.Repo.SpendTagRepo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrendService {

    @Autowired
    SpendRepo spendRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    SpendTagRepo spendTagRepo;

    private final static String[] monthNames = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private final static int defaultMonthlyPeriod = 12;
    private final static int javaDateYearOffset = 1900;
    private static HashMap<Long, String> categoryMap = new HashMap<>();
    private static HashMap<Long, String> tagMap = new HashMap<>();

    private final static String SALARY_CATEGORY = "Salary";
    private final static String OTHER_CATEGORY = "Others";
    private final static String INVESTMENT_CATEGORY = "Investment";

    public Date getZeroDate() {
        Date date = new Date();
        date = DateUtils.setDays(date, 1);
        date = DateUtils.setHours(date, 0);
        date = DateUtils.setMinutes(date, 0);
        date = DateUtils.setSeconds(date, 0);
        date = DateUtils.setMilliseconds(date, 0);

        return date;
    }

    public List<CumulativeResponse> calculateData() {
        List<CumulativeResponse> responses = new ArrayList<>();

        Date firstDayOfMonth = getZeroDate();
        Date firstDayOfNextMonth = DateUtils.addMonths(getZeroDate(), 1);

        for (int it = 0; it < 8; ++it) {
            // TODO : OPTIMIZE
            List<Spend> monthlySpend = spendRepo.findByTxDateGreaterThanEqualAndTxDateLessThan(firstDayOfMonth, firstDayOfNextMonth);
            responses.add(generateCumulativeDataResponseMonthWise(monthlySpend, firstDayOfMonth));

            firstDayOfMonth = DateUtils.addMonths(firstDayOfMonth, -1);
            firstDayOfNextMonth = DateUtils.addMonths(firstDayOfNextMonth, -1);
        }

        return responses;
    }

    public CumulativeResponse generateCumulativeDataResponseMonthWise(List<Spend> monthlySpends, Date currMonthDate) {
        HashMap<Long, Double> income = new HashMap<>();
        HashMap<Long, Double> categoryExpense = new HashMap<>();
        HashMap<Long, Double> tagExpense = new HashMap<>();
        Date latestMonthDate = new Date(0, 0, 1);
        double closingAmount = 0.0;

        for (Spend spend : monthlySpends) {
            // Calc closing Amount
            if (latestMonthDate.before(spend.getTxDate())) {
                latestMonthDate = spend.getTxDate();
                closingAmount = spend.getBalance();
            }

            // Skip in case of excluded spend
            if (spend.isExcludeFromExpense()) continue;

            if (spend.getType().equals(Spend.SpendType.C)) {
                Double categoryAmt = income.getOrDefault(spend.getCategoryId(), 0.0) + spend.getAmount();
                income.put(spend.getCategoryId(), categoryAmt);
            } else {
                Double categoryAmt = categoryExpense.getOrDefault(spend.getCategoryId(), 0.0) + spend.getAmount();
                // Unplanned Expense : Working code
                // categoryAmt = categoryAmt - (spend.getSpendTags().contains(5) ? spend.getAmount() : 0.0);
                categoryExpense.put(spend.getCategoryId(), categoryAmt);

                spend.getSpendTags().forEach(tagId -> {
                    Double tagAmt = tagExpense.getOrDefault(tagId, 0.0) + spend.getAmount();
                    tagExpense.put(Long.valueOf(tagId), tagAmt);
                });
            }
        }

        CumulativeResponse response = CumulativeResponse.builder()
                .monthDate(currMonthDate)
                .label(monthNames[currMonthDate.getMonth()] + " " + (currMonthDate.getYear() + javaDateYearOffset))
                .income(income)
                .categoryExpense(categoryExpense)
                .tagExpense(tagExpense)
                .closingAmount(closingAmount)
                .build();

        return response;
    }

    public TrendResponse getTrendResponse(TrendRequest request) {
        // Fetch all Categories & Tags
        if (categoryMap.size() == 0)
            categoryRepo.findAll().forEach(category -> categoryMap.put(category.getId(), category.getCategory()));
        if (tagMap.size() == 0)
            spendTagRepo.findAll().forEach(tag -> tagMap.put(tag.getId(), tag.getTagName()));

        // Calculate Raw Monthly Response
        List<CumulativeResponse> rawResponse = calculateData();
        Collections.reverse(rawResponse);

        TrendResponse response;
        switch (request.getTrendType()) {
            case INCOME_EXPENSE:
                response = getIncomeExpenseResponse(rawResponse, request);
                break;

            case SAVING:
                response = getSavingsResponse(rawResponse, request);
                break;

            case CATEGORY_EXPENSE:
                response = getCategoryResponse(rawResponse, request);
                break;

            default:
                response = null;
        }

        return response;
    }

    public TrendResponse getIncomeExpenseResponse(List<CumulativeResponse> rawResponse, TrendRequest request) {
        ArrayList<IncomeExpenseResponse> incomeExpenseResponse = new ArrayList<>();

        double fixedIncome = 0.0, passiveIncome = 0.0, miscIncome = 0.0, totalSpend = 0.0;
        int timePeriod = request.getTimePeriod().getValue();
        String label = convertDateToLabel(rawResponse.get(0).getMonthDate());
        Date startDateOfInterval = rawResponse.get(0).getMonthDate();
        boolean intervalLeft = true;

        for (int i = 0; i < rawResponse.size(); ++i) {
            intervalLeft = true;
            CumulativeResponse response = rawResponse.get(i);

            // Calc Income
            for (Long categoryId : response.getIncome().keySet()) {
                if (categoryMap.get(categoryId).equals(SALARY_CATEGORY))
                    fixedIncome += response.getIncome().get(categoryId);
                else if (categoryMap.get(categoryId).equals(OTHER_CATEGORY) || categoryMap.get(categoryId).equals(INVESTMENT_CATEGORY))
                    passiveIncome += response.getIncome().get(categoryId);
                else
                    miscIncome += response.getIncome().get(categoryId);
            }

            // Calculate Expense
            // totalSpend = response.getCategoryExpense().values().stream().mapToDouble(expense -> expense).sum();
            totalSpend += response.getCategoryExpense().values().stream().mapToDouble(expense -> expense).sum();

            timePeriod--;

            if (timePeriod == 0) {
                incomeExpenseResponse.add(IncomeExpenseResponse.builder()
                        .startDate(startDateOfInterval)
                        .label(label)
                        .fixedIncome(fixedIncome)
                        .passiveIncome(passiveIncome)
                        .miscIncome(miscIncome)
                        .totalSpend(totalSpend)
                        .build());

                fixedIncome = 0.0;
                passiveIncome = 0.0;
                miscIncome = 0.0;
                totalSpend = 0.0;
                timePeriod = request.getTimePeriod().getValue();

                if (i + 1 != rawResponse.size()) {
                    label = convertDateToLabel(rawResponse.get(i + 1).getMonthDate());
                    startDateOfInterval = rawResponse.get(i + 1).getMonthDate();
                }
                intervalLeft = false;
            }
        }

        if (intervalLeft)
            incomeExpenseResponse.add(IncomeExpenseResponse.builder()
                    .startDate(startDateOfInterval)
                    .label(label)
                    .fixedIncome(fixedIncome)
                    .passiveIncome(passiveIncome)
                    .miscIncome(miscIncome)
                    .totalSpend(totalSpend)
                    .build());

        return IncomeExpenseCumulativeResponse.builder()
                .incomeExpenseResponse(incomeExpenseResponse)
                .build();
    }

    public TrendResponse getSavingsResponse(List<CumulativeResponse> rawResponse, TrendRequest request) {
        ArrayList<SavingsResponse> savingsResponses = new ArrayList<>();

        int timePeriod = request.getTimePeriod().getValue();

        for (int i = timePeriod - 1; i < rawResponse.size(); i += timePeriod) {
            savingsResponses.add(SavingsResponse.builder()
                    .amount(rawResponse.get(i).getClosingAmount())
                    .startDate(rawResponse.get(i).getMonthDate())
                    .label(convertDateToLabel(rawResponse.get(i - timePeriod + 1).getMonthDate()))
                    .build());
        }

        if (rawResponse.size() % timePeriod != 0)
            savingsResponses.add(SavingsResponse.builder()
                    .amount(rawResponse.get(rawResponse.size() - 1).getClosingAmount())
                    .startDate(rawResponse.get(rawResponse.size() - 1).getMonthDate())
                    .label(convertDateToLabel(rawResponse.get(rawResponse.size() - (rawResponse.size() % timePeriod)).getMonthDate()))
                    .build());

        return SavingsCumulativeResponse.builder()
                .savings(savingsResponses)
                .build();
    }

    public TrendResponse getCategoryResponse(List<CumulativeResponse> rawResponse, TrendRequest request) {
        ArrayList<CategoryTrendResponse> categoryTrendResponses = new ArrayList<>();

        int timePeriod = request.getTimePeriod().getValue();
        String label = convertDateToLabel(rawResponse.get(0).getMonthDate());
        Date startDateOfInterval = rawResponse.get(0).getMonthDate();
        boolean intervalLeft = true;

        HashMap<String, Double> categoryExpense = new HashMap<>();

        for (int i = 0; i < rawResponse.size(); ++i) {
            intervalLeft = true;
            CumulativeResponse response = rawResponse.get(i);

            for (String category : request.getCategories()) {
                if (!categoryExpense.containsKey(category))
                    categoryExpense.put(category, 0.0);

                Long categoryId = 0L;
                for (Long catId : categoryMap.keySet()) {
                    if (categoryMap.get(catId).equals(category)) {
                        categoryId = catId;
                        break;
                    }
                }
                // NET CALCULATION
                Double amt = categoryExpense.getOrDefault(category, 0.0) + response.getCategoryExpense().getOrDefault(categoryId, 0.0) - response.getIncome().getOrDefault(categoryId, 0.0);

                // TOTAL CALCULATION
//                Double amt = categoryExpense.getOrDefault(category,0.0) + response.getCategoryExpense().getOrDefault(categoryId,0.0);
                categoryExpense.put(category, amt);
            }


            timePeriod--;

            if (timePeriod == 0) {
                categoryTrendResponses.add(CategoryTrendResponse.builder()
                        .startDate(startDateOfInterval)
                        .label(label)
                        .categorySpend(categoryExpense)
                        .totalAmount(categoryExpense.values().stream().mapToDouble(expense -> expense).sum())
                        .build());

                categoryExpense = new HashMap<>();
                timePeriod = request.getTimePeriod().getValue();

                if (i + 1 != rawResponse.size()) {
                    label = convertDateToLabel(rawResponse.get(i + 1).getMonthDate());
                    startDateOfInterval = rawResponse.get(i + 1).getMonthDate();
                }
                intervalLeft = false;
            }
        }

        if (intervalLeft)
            categoryTrendResponses.add(CategoryTrendResponse.builder()
                    .startDate(startDateOfInterval)
                    .label(label)
                    .categorySpend(categoryExpense)
                    .totalAmount(categoryExpense.values().stream().mapToDouble(expense -> expense).sum())
                    .build());


        // Calculate Average
        HashMap<String, Double> categoryAvgExpense = new HashMap<>();
        for (CategoryTrendResponse res : categoryTrendResponses) {
            for (String category : res.getCategorySpend().keySet()) {
                Double amt = categoryAvgExpense.getOrDefault(category, 0.0) + res.getCategorySpend().get(category);
                categoryAvgExpense.put(category, amt);
            }
        }
        for (String category : categoryAvgExpense.keySet()) {
            Double amt = categoryAvgExpense.get(category);
            amt = amt / categoryTrendResponses.size();
            categoryAvgExpense.put(category, (double) Math.round(amt));
        }

        // Make categoryExpense net positive
        for (CategoryTrendResponse response : categoryTrendResponses) {
            for (String category : response.getCategorySpend().keySet()) {
                Double amt = response.getCategorySpend().get(category);
                response.getCategorySpend().put(category, Math.max(0.0, amt));
            }
        }

        return CategoryCumulativeResponse.builder()
                .categoryTrendResponse(categoryTrendResponses)
                .categoryAvgExpense(categoryAvgExpense)
                .build();
    }

    private String convertDateToLabel(Date date) {
        int year = date.getYear() + javaDateYearOffset;
        return monthNames[date.getMonth()] + "'" + Integer.toString(year % 100);
    }
}
