package com.example.spendz;

import com.example.spendz.Parsers.Sbi.MainParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/insert/")
public class Read {
    @Autowired
    MainParser mainParser;

    @GetMapping("data")
    public void addDataFromFiles() {
        mainParser.addDataFromFiles();
    }

    @GetMapping("errdata")
    public void insertErrorData(@RequestBody String str) {
        mainParser.parseToSpend(str);
    }
}