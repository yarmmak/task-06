package com.task06.dto;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
@DynamoDBDocument
public class ConfigurationData
{
    private String key;
    private String value;
}
