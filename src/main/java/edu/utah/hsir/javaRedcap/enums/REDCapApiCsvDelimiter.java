package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of possible characters to use as the delimiter when importing
 * or exporting records in CSV format.
 *
 */
public enum REDCapApiCsvDelimiter
	implements REDCapApiEnum
{
	/** A Comma */
	COMMA(","),
	/** A Semicolon */
	SEMICOLON(";"),
	/** A tab character (\t) */
	TAB("tab"),
	/** The Pipe character */
	PIPE("|"),
	/** The Caret character */
	CARET("^");

	private final String label;

    private REDCapApiCsvDelimiter(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
