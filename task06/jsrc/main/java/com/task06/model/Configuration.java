package com.task06.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@DynamoDBTable(tableName = "cmtr-52e956b4-Configuration-test")
@Builder
@Getter @Setter
@ToString
public class Configuration {
    @DynamoDBHashKey(attributeName = "key") private String key;
    @DynamoDBAttribute(attributeName = "value") private String value;
}
