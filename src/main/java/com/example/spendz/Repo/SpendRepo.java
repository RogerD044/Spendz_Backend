package com.example.spendz.Repo;

import com.example.spendz.Model.Spend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpendRepo extends JpaRepository<Spend, Long> {
    List<Spend> findByTxDateBetweenOrderByTxDateDesc(Date startDate, Date endDate);

    List<Spend> findByTxDateBetween(Date startDate, Date endDate);

    List<Spend> findByTxDateGreaterThanEqualAndTxDateLessThan(Date startDate, Date endDate);

    List<Spend> findByTxDateGreaterThanEqualAndInfo(Date startDate,String info);

    Spend findByRawDesc(String rawDesc);

    Spend findByRawDescAndTxDate(String rawDesc, Date date);

    Spend findByRawDescAndBalance(String rawDesc, double bal);

    List<Spend> findByTypeAndTxDateGreaterThanEqualAndTxDateLessThanOrderByAmountDesc(Spend.SpendType type, Date startDate, Date endDate);

    List<Spend> findByTxDateGreaterThanEqualAndTxDateLessThanOrderByTxDateDesc(Date startDate, Date endDate);

    List<Spend> findByTypeAndTxDateGreaterThanEqualAndTxDateLessThan(Spend.SpendType type, Date startDate, Date endDate);
}
