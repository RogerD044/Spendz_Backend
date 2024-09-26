package com.example.spendz.Service;

import com.example.spendz.Model.Category;
import com.example.spendz.Model.Requests.TransactionRequest;
import com.example.spendz.Model.Requests.TransactionUpdateRequest;
import com.example.spendz.Model.Response.AllTransactionResponse;
import com.example.spendz.Model.Response.CategoryWiseResponseData;
import com.example.spendz.Model.Response.TransactionResponse;
import com.example.spendz.Model.Spend;
import com.example.spendz.Model.Tag;
import com.example.spendz.Repo.CategoryRepo;
import com.example.spendz.Repo.SpendRepo;
import com.example.spendz.Repo.TagRepo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TransactionService {

    @Autowired
    SpendRepo spendRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    TagRepo tagRepo;

    private static final Long SALARY_CATEGORY_ID = 14L;
    private static final Long RETURNS_CATEGORY_ID = 15L;
    private static final Long INVESTMENT_CATEGORY_ID = 4L;

    public void create(Spend spend) {
        Spend entity = spendRepo.findByRawDesc(spend.getRawDesc());
        if (null != entity)
            return;

        spendRepo.save(spend);
    }

    public void update(TransactionUpdateRequest request) {
        Spend spend = spendRepo.findById(request.getId()).orElse(null);

        if (null != spend) {
            spend.setDisplayInfo(request.getDisplayInfo());
            spend.setExcludeFromExpense(request.isExcludeFromExpense());
            spend.setCategoryId(request.getCategoryId());
            spend.setSpendTags((null == request.getSpendTags()) ? new HashSet<>() : request.getSpendTags());

            // Changing upcoming category for given spend info
            if (request.isAllowUpcomingCategoryChanges()) {
                Tag tag = tagRepo.findByInfo(spend.getInfo());

                // Insert new tag
                if (null == tag)
                    tag = Tag.builder().info(spend.getInfo()).build();

                tag.setCategoryId(request.getCategoryId());
                tagRepo.save(tag);

                // Updating Category of All such Spends with CategoryId set to Uncategorized
                List<Spend> spendsToChange = spendRepo.findAllByDisplayInfoAndCategoryId(spend.getDisplayInfo(), 1);
                for(Spend sp : spendsToChange) {
                    sp.setCategoryId(request.getCategoryId());
                    spendRepo.save(sp);
                }
            }

            spendRepo.save(spend);
        }
    }

    public TransactionResponse getAllTransactions(TransactionRequest request) {
        List<Spend> spends = spendRepo.findByTxDateBetweenOrderByTxDateDesc(request.getStartDate(), request.getEndDate());
        HashMap<Long, Category> hashMap = new HashMap<>();
        List<Category> categories = categoryRepo.findAll();
        categories.forEach(category -> hashMap.put(category.getId(), category));

        List<AllTransactionResponse> allTransactionResponses = new ArrayList<>();
        List<CategoryWiseResponseData> categoryWiseResponseDataList = new ArrayList<>();
        HashMap<Long, CategoryWiseResponseData> categorySpendMap = new HashMap<>();

        double totalAmt = 0.0;
        double totalSpend = 0.0;
        double netSpend = 0.0;

        double salary = 0.0;
        double returns = 0.0;
        double totalIncome = 0.0;
        double miscIncome = 0.0;
        double savingAndInvestment = 0.0;
        for (Spend spend : spends) {
            // Adding Transaction to the list
            allTransactionResponses.add(AllTransactionResponse.builder()
                    .amount(spend.getAmount())
                    .balance(spend.getBalance())
                    .category(hashMap.get(spend.getCategoryId()))
                    .bankName(spend.getBankName())
                    .displayInfo(spend.getDisplayInfo())
                    .info(spend.getInfo())
                    .additionInfo(spend.getAdditionInfo())
                    .type(spend.getType())
                    .rawDesc(spend.getRawDesc())
                    .txDate(spend.getTxDate())
                    .excludeFromExpense(spend.isExcludeFromExpense())
                    .paymentVia(spend.getPaymentVia())
                    .id(spend.getId())
                    .spendTagIds(spend.getSpendTags())
                    .build());

            // Debit Calculation
            if (!spend.isExcludeFromExpense()) {

                if (categorySpendMap.containsKey(spend.getCategoryId())) {
                    CategoryWiseResponseData categoryWiseResponseData = categorySpendMap.get(spend.getCategoryId());
                    categoryWiseResponseData.setAmount((categoryWiseResponseData.getAmount() + parseSpendAmount(spend)));
                    categoryWiseResponseData.setNoOfSpend(categoryWiseResponseData.getNoOfSpend() + 1);
                } else {
                    Category currentCategory = hashMap.get(spend.getCategoryId());
                    CategoryWiseResponseData categoryWiseData = CategoryWiseResponseData.builder()
                            .amount(parseSpendAmount(spend))
                            .categoryName(currentCategory.getCategory())
                            .color(currentCategory.getRgbColour())
                            .id(currentCategory.getId())
                            .noOfSpend(1)
                            .build();
                    categorySpendMap.put(currentCategory.getId(), categoryWiseData);
                }
            }

            // Credit Calculation
            if (!spend.isExcludeFromExpense() && spend.getType() == Spend.SpendType.C && spend.getCategoryId() == RETURNS_CATEGORY_ID)
                returns += spend.getAmount();
        }

        // Get Salary calculation
        List<Spend> salarySpends = spendRepo.findByCategoryIdAndTxDateGreaterThanAndTxDateLessThan(SALARY_CATEGORY_ID, request.getStartDate(), request.getEndDate());
        if(salarySpends!=null && salarySpends.size()>0 && salarySpends.get(0).getTxDate().getMonth()!=request.getEndDate().getMonth()) {
            salarySpends.add(salarySpends.get(0));
        }
        else if(salarySpends.size()==0) {
            Spend latestSalarySpend = spendRepo.getLatestSpendForGivenCategory(SALARY_CATEGORY_ID);
            if(Objects.nonNull(latestSalarySpend))
                salarySpends.add(spendRepo.getLatestSpendForGivenCategory(SALARY_CATEGORY_ID));
        }

        salary += salarySpends.stream().mapToDouble(Spend::getAmount).sum();


        totalIncome = salary + returns;
        totalSpend += categorySpendMap.values().stream().filter(spendCategory -> spendCategory.getAmount() > 0.0).mapToDouble(CategoryWiseResponseData::getAmount).sum();
        netSpend = totalSpend - (categorySpendMap.get(INVESTMENT_CATEGORY_ID) == null ? 0.0 : categorySpendMap.get(INVESTMENT_CATEGORY_ID).getAmount());
        savingAndInvestment = salary - netSpend;


        for (CategoryWiseResponseData entity : categorySpendMap.values()) {
            if(entity.getAmount()>0)
                entity.setPercentile((entity.getAmount() / totalSpend) * 100);
            entity.setAmount(Math.round(entity.getAmount()));
            categoryWiseResponseDataList.add(entity);
        }

        return TransactionResponse.builder()
                .allTransactions(allTransactionResponses)
                .categoryWiseResponseData(categoryWiseResponseDataList)
                .totalSpend(totalSpend)
                .totalSpendPercentOfSalary(Math.round((totalSpend/salary) * 100))
                .netSpend(netSpend)
                .netSpendPercentOfSalary(Math.round((netSpend/salary) * 100))
                .savingAndInvestment(Math.round(savingAndInvestment))
                .totalSavingPercentOfSalary(Math.round((savingAndInvestment/salary) * 100))
                .totalIncome(Math.round(totalIncome))
                .investment(Math.round(categorySpendMap.get(INVESTMENT_CATEGORY_ID) == null ? 0.0 : categorySpendMap.get(INVESTMENT_CATEGORY_ID).getAmount()))
                .miscIncome(Math.round(miscIncome))
                .build();
    }

    private double parseSpendAmount(Spend spend) {
        if(spend.getType() == Spend.SpendType.D)
            return spend.getAmount();
        return -1 * spend.getAmount();
    }

    private int getMonthBetweenDates(Date startDate, Date endDate) {
        int countOfMonths = 1;
        Date tmpDate = DateUtils.addDays(startDate,0);
        while(tmpDate.getMonth()!=endDate.getMonth() || tmpDate.getYear()!=endDate.getYear()) {
            tmpDate = DateUtils.addMonths(tmpDate,1);
            countOfMonths+=1;
        }

        return countOfMonths;
    }
}
