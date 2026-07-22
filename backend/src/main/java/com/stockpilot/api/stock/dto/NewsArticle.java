package com.stockpilot.api.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsArticle {
    private String title;
    private String publisher;
    private String link;
    private Long publishTime;
}
