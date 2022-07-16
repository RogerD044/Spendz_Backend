package com.example.spendz.Repo;

import com.example.spendz.Model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepo extends JpaRepository<Tag, Long> {
    Tag findByInfo(String info);
}
