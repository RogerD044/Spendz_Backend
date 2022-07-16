package com.example.spendz.Controller;


import com.example.spendz.Model.Response.MonthlySpendStatResponse;
import com.example.spendz.Service.MainPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/landingPage/")
public class MainPageController {

    @Autowired
    MainPageService mainPageService;

    @GetMapping("/details")
    public MonthlySpendStatResponse getDetails() {
        return mainPageService.getDetails();
    }
}
