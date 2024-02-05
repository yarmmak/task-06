package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.task06.dto.ConfigurationData;
import com.task06.dto.Response;
import com.task06.model.AuditCreatedRecord;
import com.task06.model.AuditUpdatedRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.syndicate.deployment.model.ResourceType.DYNAMODB_TABLE;

@LambdaHandler(lambdaName = "audit_producer",
	roleName = "audit_producer-role"
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 10)
@DependsOn(name = "Configuration", resourceType = DYNAMODB_TABLE)
public class AuditProducer implements RequestHandler<DynamodbEvent, Response>
{
	private static final String AWS_REGION = "eu-central-1";
	private static final String KEY = "key";
	private static final String VALUE = "value";

	private AmazonDynamoDB amazonDynamoDB;
	private DynamoDBMapper dbMapper;
	private LambdaLogger logger;

	public Response handleRequest(DynamodbEvent request, Context context)
	{
		this.initDynamoDbClient();
		this.initDynamoDbMapper();
		this.initLambdaLogger(context);

		processRecords(request.getRecords());

		return buildResponse();
	}

	private void processRecords(final List<DynamodbEvent.DynamodbStreamRecord> records)
	{
		for (DynamodbEvent.DynamodbStreamRecord record : records)
		{
			String eventName = record.getEventName();

			if ("INSERT".equals(eventName))
			{
				logger.log(String.format("Processing INSERT event: %s", record));
				processInsertEvent(record);
			}
			else if ("MODIFY".equals(eventName))
			{
				logger.log(String.format("Processing MODIFY event: %s", record));
				processUpdateEvent(record);
			}
		}
	}

	private void processInsertEvent(final DynamodbEvent.DynamodbStreamRecord record)
	{
		Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();

		ConfigurationData configurationData = ConfigurationData.builder()
				.key(newImage.get(KEY).getS())
				.value(newImage.get(VALUE).getS())
				.build();

		AuditCreatedRecord auditCreatedRecord = AuditCreatedRecord.builder()
				.id(getUUID())
				.itemKey(newImage.get(KEY).getS())
				.modificationTime(getModificationTime())
				.newValue(configurationData)
				.build();

		dbMapper.save(auditCreatedRecord);
	}

	private void processUpdateEvent(final DynamodbEvent.DynamodbStreamRecord record)
	{
		Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
		Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

		AuditUpdatedRecord auditUpdatedRecord = AuditUpdatedRecord.builder()
				.id(getUUID())
				.itemKey(newImage.get(KEY).getS())
				.modificationTime(getModificationTime())
				.updatedAttribute("value")
				.newValue(newImage.get(VALUE).getS())
				.oldValue(oldImage.get(VALUE).getS())
				.build();

		dbMapper.save(auditUpdatedRecord);
	}

	private String getUUID()
	{
		return UUID.randomUUID().toString();
	}

	private String getModificationTime()
	{
		final DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

		return LocalDateTime.now().format(formatter);
	}

	private Response buildResponse()
	{
		return Response.builder()
				.statusCode("201")
				.message("Events processed successfully!")
				.build();
	}

	private void initDynamoDbClient()
	{
		this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion(AWS_REGION)
				.build();
	}

	private void initDynamoDbMapper()
	{
		this.dbMapper = new DynamoDBMapper(amazonDynamoDB);
	}

	private void initLambdaLogger(final Context context)
	{
		this.logger = context.getLogger();
	}
}
