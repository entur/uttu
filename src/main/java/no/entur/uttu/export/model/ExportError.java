package no.entur.uttu.export.model;

import java.text.MessageFormat;

public class ExportError {

    private String message;

    public ExportError(String message, Object... params) {
        this.message = MessageFormat.format(message, params);
    }

    public String getMessage() {
        return message;
    }
}
