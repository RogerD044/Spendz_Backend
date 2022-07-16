package com.example.spendz.Controller;

import com.example.spendz.Model.Requests.TransactionRequest;
import com.example.spendz.Model.Requests.TransactionUpdateRequest;
import com.example.spendz.Model.Response.TransactionResponse;
import com.example.spendz.Model.Spend;
import com.example.spendz.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PutMapping("")
    public void updateTransaction(@RequestBody TransactionUpdateRequest request) {
        System.out.println(request.toString());
        transactionService.update(request);
    }

    @PostMapping("")
    public TransactionResponse getAllTransactions(@RequestBody TransactionRequest request) {
        System.out.println(request.toString());
        return transactionService.getAllTransactions(request);
    }
}
