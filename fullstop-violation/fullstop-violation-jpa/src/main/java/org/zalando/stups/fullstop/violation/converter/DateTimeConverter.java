package org.zalando.stups.fullstop.violation.converter;

import org.joda.time.DateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;

/**
 * @author ahartmann
 */
@Converter(autoApply = true)
public class DateTimeConverter implements AttributeConverter<DateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(final DateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return new Timestamp(dateTime.getMillis());
    }

    @Override
    public DateTime convertToEntityAttribute(final Timestamp date) {
        if (date == null) {
            return null;
        }

        return new DateTime(date.getTime());
    }

}
