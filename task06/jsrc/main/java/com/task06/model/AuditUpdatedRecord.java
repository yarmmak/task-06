package com.task06.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@DynamoDBTable(tableName = "cmtr-52e956b4-Audit-test")
@Builder
@Getter @Setter
@ToString
public class AuditUpdatedRecord {
    @DynamoDBHashKey(attributeName = "id") private String id;
    @DynamoDBAttribute(attributeName = "itemKey") private String itemKey;
    @DynamoDBAttribute(attributeName = "modificationTime") private String modificationTime;
    @DynamoDBAttribute(attributeName = "updatedAttribute") private String updatedAttribute;
    @DynamoDBAttribute(attributeName = "newValue") private String newValue;
    @DynamoDBAttribute(attributeName = "oldValue") private String oldValue;
}
