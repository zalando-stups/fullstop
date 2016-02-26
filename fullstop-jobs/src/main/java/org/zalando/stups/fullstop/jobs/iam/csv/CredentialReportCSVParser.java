package org.zalando.stups.fullstop.jobs.iam.csv;

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;

import java.util.List;
import java.util.function.Function;

public interface CredentialReportCSVParser extends Function<GetCredentialReportResult, List<CSVReportEntry>> {
}
