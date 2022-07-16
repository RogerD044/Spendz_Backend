package com.example.spendz.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
public class SpendTag {
    @Id
    @GeneratedValue
    @JsonProperty("value")
    private Long id;
    @JsonProperty("label")
    private String tagName;
}
