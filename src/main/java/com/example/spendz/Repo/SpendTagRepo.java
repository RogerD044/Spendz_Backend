package com.example.spendz.Repo;

import com.example.spendz.Model.SpendTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpendTagRepo extends JpaRepository<SpendTag, Long> {
}
