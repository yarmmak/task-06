package com.task06.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class Response
{
    private String statusCode;
    private String message;
}
