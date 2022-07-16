package com.example.spendz.Controller;

import com.example.spendz.Model.Requests.Trends.TrendRequest;
import com.example.spendz.Model.Response.Trends.TrendResponse;
import com.example.spendz.Service.TrendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/trends")
public class TrendsController {

    @Autowired
    TrendService trendService;

//    @GetMapping("")
//    public List getCumulativeData() {
//        return trendService.getResponse();
//    }

    @PostMapping("")
    public TrendResponse getTrendResponse(@RequestBody TrendRequest request) {
        System.out.println(request.toString());
        return trendService.getTrendResponse(request);
    }

}
