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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Date;

@Service
public class TransactionService {

    @Autowired
    SpendRepo spendRepo;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    TagRepo tagRepo;

    private static final Long SALARY_CATEGORY_ID = 14L;
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
            spendRepo.save(spend);

            // Changing upcoming category for given spend info
            if (request.isAllowUpcomingCategoryChanges()) {
                Tag tag = tagRepo.findByInfo(spend.getInfo());

                // Insert new tag
                if (null == tag)
                    tag = Tag.builder().info(spend.getInfo()).build();

                tag.setCategoryId(request.getCategoryId());
                tagRepo.save(tag);

                updateAllFutureTransactionsFromCurrentTransaction(spend.getTxDate(),spend.getInfo(), request.getCategoryId());
            }
        }
    }

    public void updateAllFutureTransactionsFromCurrentTransaction(Date startDate, String info, Long categoryId) {
        List<Spend> spends = spendRepo.findByTxDateGreaterThanEqualAndInfo(startDate, info);
        for(Spend spend : spends) {
            spend.setCategoryId(categoryId);
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
        double totalIncome = 0.0;
        double miscIncome = 0.0;
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
            if (!spend.isExcludeFromExpense() && spend.getType() == Spend.SpendType.D) {
                totalAmt += spend.getAmount();

                if (categorySpendMap.containsKey(spend.getCategoryId())) {
                    CategoryWiseResponseData categoryWiseResponseData = categorySpendMap.get(spend.getCategoryId());
                    categoryWiseResponseData.setAmount((categoryWiseResponseData.getAmount() + spend.getAmount()));
                    categoryWiseResponseData.setNoOfSpend(categoryWiseResponseData.getNoOfSpend() + 1);
                } else {
                    Category currentCategory = hashMap.get(spend.getCategoryId());
                    CategoryWiseResponseData categoryWiseData = CategoryWiseResponseData.builder()
                            .amount(spend.getAmount())
                            .categoryName(currentCategory.getCategory())
                            .color(currentCategory.getRgbColour())
                            .id(currentCategory.getId())
                            .noOfSpend(1)
                            .build();
                    categorySpendMap.put(currentCategory.getId(), categoryWiseData);
                }
            }

            // Credit Calculation
            if (!spend.isExcludeFromExpense() && spend.getType() == Spend.SpendType.C) {
                totalIncome += spend.getAmount();

                if (spend.getCategoryId() != SALARY_CATEGORY_ID && spend.getCategoryId() != INVESTMENT_CATEGORY_ID)
                    miscIncome += spend.getAmount();
            }
        }

        for (CategoryWiseResponseData entity : categorySpendMap.values()) {
            entity.setPercentile((entity.getAmount() / totalAmt) * 100);
            entity.setAmount(Math.round(entity.getAmount()));
            categoryWiseResponseDataList.add(entity);
        }

        return TransactionResponse.builder()
                .allTransactions(allTransactionResponses)
                .categoryWiseResponseData(categoryWiseResponseDataList)
                .totalSpend(Math.round(totalAmt))
                .totalIncome(Math.round(totalIncome))
                .investment(Math.round(categorySpendMap.get(INVESTMENT_CATEGORY_ID) == null ? 0.0 : categorySpendMap.get(INVESTMENT_CATEGORY_ID).getAmount()))
                .miscIncome(Math.round(miscIncome))
                .build();
    }
}
