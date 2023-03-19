package com.example.spendz;

import com.example.spendz.Parsers.Sbi.MainParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@RestController
@RequestMapping("/insert/")
public class Read {
    @Autowired
    MainParser mainParser;

    private final String uploadDir = "src/main/resources/data";

    @GetMapping("data")
    public void addDataFromFiles() {
        mainParser.addDataFromFiles();
    }

    @PostMapping("upload")
    public void uploadDataFile(@RequestBody MultipartFile file) {
        System.out.println("Request Received");
        Path uploadPath = Paths.get(uploadDir);

        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioe) {
            System.err.println("Could not save file: " + fileName);
        }

        System.out.println("FILE UPLOADED SUCCESSFULLY");
        addDataFromFiles();
    }

    @GetMapping("errdata")
    public void insertErrorData(@RequestBody String str) {
        mainParser.parseToSpend(str);
    }
}