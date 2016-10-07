package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionRequest;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionResult;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.StringJoiner;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;

@Component
public class LambdaPermissionValidator {

  private static final String FUNCTION_ARN_JSON_PATH = "$.functionName";
  private static final String LATEST_TAG = "$LATEST";
  private final ViolationSink violationSink;
  // TODO Define type
  private static final String VIOLATION_TYPE = "PermissionViolation";
  private final ClientProvider clientProvider;


  public LambdaPermissionValidator(ViolationSink violationSink, ClientProvider clientProvider) {
    this.violationSink = violationSink;
    this.clientProvider = clientProvider;
  }

  public void validateEvent(final CloudTrailEvent event) {
    String elems = event.getEventData().getRequestParameters();
    List<String> functionArns = CloudTrailEventSupport.read(elems, FUNCTION_ARN_JSON_PATH);
    String functionArn = functionArns.get(0);
    Region region = getRegion(fromName(event.getEventData().getAwsRegion()));
    String account = event.getEventData().getAccountId();
    boolean functionVersionExists = validateFunctionVersion(functionArn, region, account);
    if (!functionVersionExists) {
      Violation vio = violationFor(event).withType(VIOLATION_TYPE).build();
      violationSink.put(vio);
    }
  }


  boolean validateFunctionVersion(final String funcitonArn, final Region region,
      final String accountId) {
    String[] parts = funcitonArn.split(":");
    if (parts.length != 7) {
      // function version is not set
      return false;
    } else if (LATEST_TAG.equals(parts[6])) {
      // LATEST VERSION USED
      return false;
    } else {
      String functionName = buildFunctionName(parts);
      String version = parts[7];
      return validateFunctionVersion(functionName, version, region, accountId);
    }

  }

  private boolean validateFunctionVersion(final String functionName, final String version,
      final Region region, final String accountId) {
    AWSLambdaClient client = clientProvider.getClient(AWSLambdaClient.class, accountId, region);
    String nextMarker;
    ListVersionsByFunctionRequest lvbfr =
        new ListVersionsByFunctionRequest().withFunctionName(functionName);

    do {
      ListVersionsByFunctionResult listVersionsByFunctionResult =
          client.listVersionsByFunction(lvbfr);
      nextMarker = listVersionsByFunctionResult.getNextMarker();

      for (FunctionConfiguration next : listVersionsByFunctionResult.getVersions()) {
        if (next.getVersion().equals(version))
          return true;

      }
      lvbfr = lvbfr.withMarker(nextMarker);
    } while (!(nextMarker == null) && !nextMarker.isEmpty());

    return false;
  }

  private String buildFunctionName(final String[] parts) {
    StringJoiner strj = new StringJoiner(":");
    for (int i = 0; i < 6; i ++) {
      strj = strj.add(parts[i]);
    }
    return strj.toString();
  }

}
