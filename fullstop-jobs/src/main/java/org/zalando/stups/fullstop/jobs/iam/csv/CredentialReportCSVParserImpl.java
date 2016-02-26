package org.zalando.stups.fullstop.jobs.iam.csv;

import com.amazonaws.services.identitymanagement.model.GetCredentialReportResult;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.amazonaws.services.identitymanagement.model.ReportFormatType.Textcsv;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.csv.QuoteMode.MINIMAL;

@Component
public class CredentialReportCSVParserImpl implements CredentialReportCSVParser {

    public static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(',')
            .withQuoteMode(MINIMAL)
            .withHeader()
            .withSkipHeaderRecord()
            .withNullString("not_supported")
            .withAllowMissingColumnNames();

    @Override
    public List<CSVReportEntry> apply(GetCredentialReportResult report) {
        Assert.state(Textcsv.toString().equals(report.getReportFormat()), "unknown credential report format: " + report.getReportFormat());

        try (final Reader r = new BufferedReader(new InputStreamReader(new ByteBufferBackedInputStream(report.getContent())))) {
            final CSVParser parser = new CSVParser(r, CSV_FORMAT);
            final Map<String, Integer> headers = parser.getHeaderMap();

            Assert.state(headers.containsKey("user"), "Header 'user' not found in CSV");
            Assert.state(headers.containsKey("arn"), "Header 'arn' not found in CSV");
            Assert.state(headers.containsKey("password_enabled"), "Header 'password_enabled' not found in CSV");
            Assert.state(headers.containsKey("mfa_active"), "Header 'mfa_active' not found in CSV");
            Assert.state(headers.containsKey("access_key_1_active"), "Header 'access_key_1_active' not found in CSV");
            Assert.state(headers.containsKey("access_key_2_active"), "Header 'access_key_2_active' not found in CSV");

            return stream(parser.spliterator(), false).map(this::toCSVReportEntry).filter(Objects::nonNull).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not read csv report", e);
        }
    }

    private CSVReportEntry toCSVReportEntry(CSVRecord record) {
        final Optional<String> user = Optional.ofNullable(record.get("user"));
        final Optional<String> arn = Optional.ofNullable(record.get("arn"));
        final Optional<Boolean> passwordEnabled = Optional.ofNullable(record.get("password_enabled")).map(Boolean::valueOf);
        final Optional<Boolean> mfaActive = Optional.ofNullable(record.get("mfa_active")).map(Boolean::valueOf);
        final Optional<Boolean> accessKey1Active = Optional.ofNullable(record.get("access_key_1_active")).map(Boolean::valueOf);
        final Optional<Boolean> accessKey2Active = Optional.ofNullable(record.get("access_key_2_active")).map(Boolean::valueOf);

        if (user.isPresent() && arn.isPresent() && passwordEnabled.isPresent() && mfaActive.isPresent() && accessKey1Active.isPresent() && accessKey2Active.isPresent()) {
            return new CSVReportEntry(user.get(), arn.get(), passwordEnabled.get(), mfaActive.get(), accessKey1Active.get(), accessKey2Active.get());
        } else {
            return null;
        }
    }
}
