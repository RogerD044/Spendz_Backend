package com.example.spendz.Service;


import com.example.spendz.Model.Category;
import com.example.spendz.Model.Response.AllTransactionResponse;
import com.example.spendz.Model.Response.CategoryWiseResponseData;
import com.example.spendz.Model.Response.MonthlySpendStatResponse;
import com.example.spendz.Model.Spend;
import com.example.spendz.Repo.CategoryRepo;
import com.example.spendz.Repo.SpendRepo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MainPageService {

    @Autowired
    SpendRepo spendRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    SafeSpendService safeSpendService;

    private HashMap<Long, Category> catgeoryMap = new HashMap<>();

    public MonthlySpendStatResponse getDetails() {
        System.out.println("REQUEST");

        // Add all categories
        categoryRepo.findAll().forEach(category -> catgeoryMap.put(category.getId(), category));

        Date startDateOfCurrMonth = DateUtils.setDays(getZeroDate(), 1);
        Date startDateOfNextMonth = DateUtils.addMonths(DateUtils.setDays(getZeroDate(), 1), 1);
        Date currDate = getZeroDate();
        Date tomorrowDate = DateUtils.addDays(getZeroDate(), 1);
        Date sevenDaysBefore = DateUtils.addDays(currDate, -6);

        // Get Monthly Spend
        List<Spend> monthlySpends = spendRepo.findByTypeAndTxDateGreaterThanEqualAndTxDateLessThanOrderByAmountDesc(Spend.SpendType.D, startDateOfCurrMonth, tomorrowDate);
        double monthSpend = monthlySpends.stream().map(Spend::getAmount).reduce(0.0, Double::sum);
        List<AllTransactionResponse> topSixSpends = monthlySpends.subList(0, Math.min(monthlySpends.size(), 6)).stream().map(this::build).collect(Collectors.toList());

        HashMap<Long, CategoryWiseResponseData> categorySpendMap = new HashMap<>();
        List<CategoryWiseResponseData> topSixCategorySpends = new ArrayList<>();
        for (Spend spend : monthlySpends) {
            CategoryWiseResponseData res = categorySpendMap.getOrDefault(spend.getCategoryId(), CategoryWiseResponseData.builder()
                    .id(spend.getCategoryId())
                    .noOfSpend(0)
                    .categoryName(catgeoryMap.get(spend.getCategoryId()).getCategory())
                    .color(catgeoryMap.get(spend.getCategoryId()).getRgbColour())
                    .amount(0)
                    .build());

            res.setAmount(res.getAmount() + spend.getAmount());
            res.setNoOfSpend(res.getNoOfSpend() + 1);

            categorySpendMap.put(spend.getCategoryId(), res);
        }
        topSixCategorySpends = new ArrayList<>(categorySpendMap.values());
        topSixCategorySpends.sort(Comparator.comparing(CategoryWiseResponseData::getAmount, Comparator.reverseOrder()));
        topSixCategorySpends = topSixCategorySpends.subList(0, Math.min(topSixCategorySpends.size(), 6));


        // Get Last 7 days Spend
        List<Spend> last7DaysSpend = spendRepo.findByTypeAndTxDateGreaterThanEqualAndTxDateLessThan(Spend.SpendType.D, sevenDaysBefore, tomorrowDate);
        double lastWeekTotalSpend = last7DaysSpend.stream().map(Spend::getAmount).reduce(0.0, Double::sum);
        TreeMap<Date, Double> lastSevenDaySpendMap = new TreeMap<>();
        for (int i = 0; i < 7; ++i)
            lastSevenDaySpendMap.put(DateUtils.addDays(currDate, -i), 0.0);

        last7DaysSpend.forEach(spend -> {
            Double spendAmt = lastSevenDaySpendMap.get(spend.getTxDate()) + spend.getAmount();
            lastSevenDaySpendMap.put(spend.getTxDate(), spendAmt);
        });

        return MonthlySpendStatResponse.builder()
                .spendLimit(safeSpendService.getSafeSpendLimit())
                .topSixTransactions(topSixSpends)
                .topSixCategorySpends(topSixCategorySpends)
                .monthlySpend(monthSpend)
                .lastWeekSpend(lastWeekTotalSpend)
                .lastSevenDaySpends(lastSevenDaySpendMap)
                .build();
    }

    public Date getZeroDate() {
        Date date = new Date();
        date = DateUtils.setMilliseconds(date, 0);
        date = DateUtils.setSeconds(date, 0);
        date = DateUtils.setMinutes(date, 0);
        date = DateUtils.setHours(date, 0);

        return date;
    }

    public AllTransactionResponse build(Spend spend) {
        return AllTransactionResponse.builder()
                .amount(spend.getAmount())
                .id(spend.getId())
                .category(catgeoryMap.get(spend.getCategoryId()))
                .rawDesc(spend.getRawDesc())
                .info(spend.getInfo())
                .displayInfo(spend.getDisplayInfo())
                .txDate(spend.getTxDate())
                .build();
    }


//    public Pair<Date, Date> getDateRange() {
//        Date beginning, end;
//
//        {
//            Calendar calendar = getCalendarForNow();
//            calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
//            setTimeToBeginningOfDay(calendar);
//            beginning = calendar.getTime();
//        }
//
//        {
//            Calendar calendar = getCalendarForNow();
//            calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
//            setTimeToEndofDay(calendar);
//            end = calendar.getTime();
//        }
//
//        return Pair.of(beginning, end);
//    }
//
//    private static Calendar getCalendarForNow() {
//        Calendar calendar = GregorianCalendar.getInstance();
//        calendar.setTime(new Date());
//        return calendar;
//    }
//
//    private static void setTimeToBeginningOfDay(Calendar calendar) {
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//    }
//
//    private static void setTimeToEndofDay(Calendar calendar) {
//        calendar.set(Calendar.HOUR_OF_DAY, 23);
//        calendar.set(Calendar.MINUTE, 59);
//        calendar.set(Calendar.SECOND, 59);
//        calendar.set(Calendar.MILLISECOND, 999);
//    }
}
