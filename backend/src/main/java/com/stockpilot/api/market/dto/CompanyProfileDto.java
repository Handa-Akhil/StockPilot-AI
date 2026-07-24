package com.stockpilot.api.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProfileDto {
    private String symbol;
    private String name;
    private String sector;
    private String industry;
    private String description;
    private String ceo;
    private String website;
    private Long employees;
    private Long marketCap;
    private Double pe;
    private Double dividendYield;
    private Double fiftyTwoWeekHigh;
    private Double fiftyTwoWeekLow;
    private String country;
}
