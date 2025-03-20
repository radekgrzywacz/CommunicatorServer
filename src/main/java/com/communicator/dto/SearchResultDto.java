package com.communicator.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class SearchResultDto {

    private Page<SearchUserResults> results;
    private String message;
}
