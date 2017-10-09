package org.zalando.stups.fullstop.plugin.scm;

import static java.lang.String.format;

class UnknownScmUrlException extends Exception {

    UnknownScmUrlException(String url) {
        super(format("'%s' does not look like a valid git repository", url));
    }
}
