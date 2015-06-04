/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.violation.converter;

import org.joda.time.DateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;

/**
 * @author  ahartmann
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
