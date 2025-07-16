package com.son.bookhaven.data.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Publisher {
    private int publisherId;
    private String publisherName;
    private String contactInfo;
    private String createdAt;
    private List<BookVariant> variants;
}
