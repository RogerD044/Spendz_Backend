package com.example.spendz.Repo;

import com.example.spendz.Model.Spend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.annotation.Native;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface SpendRepo extends JpaRepository<Spend, Long> {
    List<Spend> findByTxDateBetweenOrderByTxDateDesc(Date startDate, Date endDate);

    List<Spend> findByTxDateBetween(Date startDate, Date endDate);

    List<Spend> findByTxDateGreaterThanEqualAndTxDateLessThan(Date startDate, Date endDate);

    Spend findByRawDesc(String rawDesc);

    Spend findByRawDescAndTxDate(String rawDesc, Date date);

    Spend findByRawDescAndBalance(String rawDesc, double bal);

    List<Spend> findByTypeAndTxDateGreaterThanEqualAndTxDateLessThanOrderByAmountDesc(Spend.SpendType type, Date startDate, Date endDate);

    List<Spend> findByTxDateGreaterThanEqualAndTxDateLessThanOrderByTxDateDesc(Date startDate, Date endDate);

    List<Spend> findByTypeAndTxDateGreaterThanEqualAndTxDateLessThan(Spend.SpendType type, Date startDate, Date endDate);

    List<Spend> findAllByDisplayInfoAndCategoryId(String displayInfo, long categoryId);

    @Query(
            value = "SELECT * FROM Spend s where s.category_id=?1 and s.tx_date>=?2 and s.tx_date<=?3 order by tx_date desc",
            nativeQuery = true)
    List<Spend> findByCategoryIdAndTxDateGreaterThanAndTxDateLessThan(long categoryId, Date startDate, Date endDate);

    @Query(
            value = "SELECT * FROM Spend s where s.category_id=?1 order by tx_date desc limit 1",
            nativeQuery = true)
    Spend getLatestSpendForGivenCategory(long categoryId);
}
