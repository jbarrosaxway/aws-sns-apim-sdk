# Publish SNS Message Integration for Axway API Gateway

This document describes how to integrate AWS SNS with Axway API Gateway using Groovy scripts, including configuration for Kubernetes environments.

## Overview

The integration allows publishing messages to AWS SNS topics directly from Axway API Gateway through Groovy script filters, offering flexibility for authentication and credential configuration.

## Tested Version

✅ **Tested and validated on Axway API Gateway version 7.7.0.20240830**

## Prerequisites

- Axway API Gateway 7.7.0.20240830 (tested)
- AWS SDK for Java 1.12.314 (aws-java-sdk-sns, aws-java-sdk-core)
- Jackson (included in the gateway)
- Access to AWS SNS topics
- Configured AWS credentials

## Configuration

### 1. Dependencies

The script uses the following dependencies that must be available in the classpath:

#### Required JARs (Tested Versions):
- `aws-java-sdk-sns-1.12.314.jar`
- `aws-java-sdk-core-1.12.314.jar`
- Jackson (included in the gateway - no additional JARs required)

#### JAR Location:
The JARs must be in the gateway's `ext/lib` directory. Example structure:
```
<VORDEL_HOME>/groups/group-<X>/instance-<Y>/ext/lib/
```

**Example**: `/opt/axway/Axway-7.7.0.20240830/apigateway/groups/group-2/instance-1/ext/lib/`

**Note**: The path may vary depending on your installation. Adjust as needed.

### 2. Policy Studio Configuration

**IMPORTANT**: JARs in the `ext/lib` directory are not automatically included in the Policy Studio classpath. You must add them manually:

1. Open Policy Studio
2. Go to **Window > Preferences > Runtime Dependencies**
3. Click **Add** and navigate to the `ext/lib` directory
4. Select the required JARs:
   - `aws-java-sdk-sns-1.12.314.jar`
   - `aws-java-sdk-core-1.12.314.jar`
5. Click **Apply** to save
6. Restart Policy Studio with the `-clean` option

### 3. Installation

1. Copy the contents of the `aws-sns-filter.groovy` file to the Policy Studio script filter
2. Configure the required parameters
3. Configure AWS credentials
4. Test the integration

## Groovy Script for Publish SNS Message

### Main Script

The main script is available in the `aws-sns-filter.groovy` file. This script implements:

- Flexible AWS authentication (environment variables, credentials file, IAM Roles)
- Dynamic configuration via message attributes
- Automatic retry system
- HTTP request processing
- SNS message publishing
- JSON and non-JSON message handling
- Detailed logging for troubleshooting
- Proper resource management

To use the script:

1. Open the `aws-sns-filter.groovy` file in a text editor
2. Copy all the contents of the file
3. In Policy Studio, create a script filter and paste the contents
4. Configure the required parameters
5. Test the integration

### Configuration Parameters

The script accepts the following parameters via message attributes:

#### Required Parameters:

| Parameter | Type | Description |
|-----------|------|-------------|
| `aws.sns.topic.arn` | String | SNS topic ARN |

#### Optional Parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `aws.sns.region` | String | `AWS_DEFAULT_REGION` | AWS region |
| `aws.sns.message` | String | `content.body` or `{}` | Message to publish |
| `aws.sns.subject` | String | `null` | Message subject |
| `aws.sns.message.structure` | String | `null` | Message structure (json, etc.) |
| `aws.sns.message.attributes` | String | `null` | JSON format message attributes |
| `aws.sns.retry.delay` | Integer | `1000` | Retry delay in milliseconds |
| `aws.sns.max.retries` | Integer | `3` | Maximum retry attempts |

### AWS Authentication

The script supports multiple authentication methods:

#### 1. Environment Variables (Recommended)
```bash
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1
```

#### 2. Credentials File
```bash
# ~/.aws/credentials
[default]
aws_access_key_id = your_access_key
aws_secret_access_key = your_secret_key
```

#### 3. IAM Role (EC2, ECS, Lambda)
The script automatically detects and uses IAM roles when available.

### Example Usage

#### Basic SNS Message Publishing:
```groovy
// Set required parameters
msg.put("aws.sns.topic.arn", "arn:aws:sns:us-east-1:123456789012:my-topic")
msg.put("aws.sns.region", "us-east-1")
msg.put("aws.sns.message", "Hello from Axway API Gateway!")
msg.put("aws.sns.subject", "Test Message")
```

#### Advanced Configuration:
```groovy
// Advanced configuration with retry and attributes
msg.put("aws.sns.topic.arn", "arn:aws:sns:us-east-1:123456789012:my-topic")
msg.put("aws.sns.region", "us-east-1")
msg.put("aws.sns.message", '{"key": "value", "timestamp": "' + System.currentTimeMillis() + '"}')
msg.put("aws.sns.subject", "API Gateway Notification")
msg.put("aws.sns.message.structure", "json")
msg.put("aws.sns.message.attributes", '{"Type": {"DataType": "String", "StringValue": "Notification"}}')
msg.put("aws.sns.retry.delay", "2000")
msg.put("aws.sns.max.retries", "5")
```

## Error Handling

The script includes comprehensive error handling:

### Retry Logic
- Automatic retry on transient failures
- Configurable retry delay and max attempts
- Exponential backoff for repeated failures

### Error Categories
- **Authentication Errors**: Invalid credentials or permissions
- **Network Errors**: Connection timeouts or network issues
- **SNS Errors**: Invalid topic ARN, message format issues
- **Configuration Errors**: Missing required parameters

### Logging
The script provides detailed logging for troubleshooting:
- Configuration validation
- AWS client initialization
- SNS operation details
- Error details with stack traces
- Performance metrics

## Performance Considerations

### Best Practices
1. **Use IAM Roles** when possible for better security
2. **Configure appropriate timeouts** for your network
3. **Monitor retry settings** to avoid excessive delays
4. **Use message attributes** for structured data
5. **Implement proper error handling** in your policies

### Monitoring
- Monitor SNS publish success rates
- Track message delivery times
- Monitor AWS API usage and costs
- Set up CloudWatch alarms for SNS metrics

## Troubleshooting

### Common Issues

#### 1. ClassNotFoundException
**Problem**: `java.lang.ClassNotFoundException: com.amazonaws.services.sns.AmazonSNS`
**Solution**: Ensure AWS SDK JARs are in the classpath

#### 2. Invalid Topic ARN
**Problem**: `InvalidParameter: Invalid parameter: TopicArn`
**Solution**: Verify the topic ARN format and permissions

#### 3. Authentication Errors
**Problem**: `com.amazonaws.AmazonServiceException: The security token included in the request is invalid`
**Solution**: Check AWS credentials and permissions

#### 4. Network Timeouts
**Problem**: `com.amazonaws.SdkClientException: Unable to execute HTTP request`
**Solution**: Check network connectivity and firewall settings

### Debug Mode
Enable debug logging by setting:
```groovy
msg.put("aws.sns.debug", "true")
```

## Security Considerations

### Credential Management
- Use IAM roles when possible
- Rotate access keys regularly
- Use least privilege principle
- Monitor AWS CloudTrail logs

### Message Security
- Validate message content
- Use message attributes for metadata
- Implement proper access controls
- Monitor SNS access patterns

## Integration Examples

### REST API Integration
```groovy
// Publish message from REST API call
def messageBody = msg.get("content.body")
def topicArn = msg.get("aws.sns.topic.arn")

msg.put("aws.sns.message", messageBody)
msg.put("aws.sns.subject", "API Gateway Notification")
```

### Event-Driven Integration
```groovy
// Publish event notifications
def eventType = msg.get("event.type")
def eventData = msg.get("event.data")

msg.put("aws.sns.message", eventData)
msg.put("aws.sns.subject", "Event: " + eventType)
```

### Batch Processing
```groovy
// Process multiple messages
def messages = msg.get("batch.messages")
messages.each { message ->
    msg.put("aws.sns.message", message)
    // Publish each message
}
```

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review AWS SNS documentation
3. Verify Axway API Gateway logs
4. Test with AWS CLI for comparison

## Version History

- **v1.0.0**: Initial release with SNS integration
- Support for multiple authentication methods
- Comprehensive error handling and retry logic
- Detailed logging and monitoring capabilities 