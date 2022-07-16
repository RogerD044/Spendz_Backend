package com.example.spendz.Service;

import com.example.spendz.Model.Category;
import com.example.spendz.Repo.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepo categoryRepo;

    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }
}
