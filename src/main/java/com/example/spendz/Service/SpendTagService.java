package com.example.spendz.Service;

import com.example.spendz.Model.SpendTag;
import com.example.spendz.Repo.SpendTagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpendTagService {

    @Autowired
    SpendTagRepo spendTagRepo;

    public List<SpendTag> getAllSpendTags() {
        return spendTagRepo.findAll();
    }
}
