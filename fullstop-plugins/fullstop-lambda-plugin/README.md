## AWS Lambda Plugin

### Aim of plugin
This plugin is going to validate if the Lambda function was created or updated from the known s3 bucket. 

### Reacts on

```
   lambda.amazonaws.com
    - CreateFunction.*
    - UpdateFunctionCode.*
```

### Configuration

List of S3 buckets that are whitelisted for executing a lambda function. 
