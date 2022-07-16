package com.example.spendz.Controller;

import com.example.spendz.Model.SpendTag;
import com.example.spendz.Service.SpendTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/spendTags")
public class SpendTagController {

    @Autowired
    SpendTagService spendTagService;

    @GetMapping("")
    public List<SpendTag> getAllSpendTags() {
        return spendTagService.getAllSpendTags();
    }
}
