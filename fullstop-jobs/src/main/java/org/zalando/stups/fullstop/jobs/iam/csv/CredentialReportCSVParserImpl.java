/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Optional;
import java.util.stream.Stream;

import static com.amazonaws.services.identitymanagement.model.ReportFormatType.Textcsv;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.csv.QuoteMode.MINIMAL;

@Component
public class CredentialReportCSVParserImpl implements CredentialReportCSVParser {

    public static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(',')
            .withQuoteMode(MINIMAL)
            .withNullString("not_supported")
            .withHeader("user", "password_enabled")
            .withSkipHeaderRecord()
            .withAllowMissingColumnNames();

    @Override
    public Stream<User> apply(GetCredentialReportResult report) {
        Assert.state(Textcsv.toString().equals(report.getReportFormat()), "unknown credential report format: " + report.getReportFormat());

        try (final Reader r = new BufferedReader(new InputStreamReader(new ByteBufferBackedInputStream(report.getContent())))) {
            final CSVParser parser = new CSVParser(r, CSV_FORMAT);
            return stream(parser.spliterator(), false).map(this::toUser);
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
