package com.son.bookhaven.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublisherResponse {
    private int publisherId;
    private String publisherName;
    private String contactInfo;
    private String createdAt;
}
