package org.zalando.stups.fullstop.clients.kio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author  jbellmann
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Version extends VersionBase {

    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

}
