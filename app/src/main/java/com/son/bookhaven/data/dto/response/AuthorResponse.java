package com.son.bookhaven.data.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorResponse {
    private int authorId;
    private String authorName;
    private String bio;
    private String createdAt;
}
