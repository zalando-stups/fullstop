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
    public List<User> apply(GetCredentialReportResult report) {
        Assert.state(Textcsv.toString().equals(report.getReportFormat()), "unknown credential report format: " + report.getReportFormat());

        try (final Reader r = new BufferedReader(new InputStreamReader(new ByteBufferBackedInputStream(report.getContent())))) {
            final CSVParser parser = new CSVParser(r, CSV_FORMAT);
            final Map<String, Integer> headers = parser.getHeaderMap();

            Assert.state(headers.containsKey("user"), "Header 'user' not found in CSV");
            Assert.state(headers.containsKey("password_enabled"), "Header 'password_enabled' not found in CSV");

            return stream(parser.spliterator(), false).map(this::toUser).filter(Objects::nonNull).collect(toList());
        } catch (IOException e) {
            throw new RuntimeException("Could not read csv report", e);
        }
    }

    private User toUser(CSVRecord record) {
        final Optional<Boolean> passwordEnabled = Optional.ofNullable(record.get("password_enabled")).map(Boolean::valueOf);
        if (passwordEnabled.isPresent()) {
            return new User(record.get("user"), passwordEnabled.get());
        } else {
            return null;
        }
    }
}
