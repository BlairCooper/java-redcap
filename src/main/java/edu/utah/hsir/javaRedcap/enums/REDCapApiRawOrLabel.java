package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of values used when exporting records in CSV/FLAT format to
 * set the format for the CSV headers.
 *  
 */
public enum REDCapApiRawOrLabel
	implements REDCapApiEnum
{
	/** Use the raw value */
	RAW("raw"),
	/** user the label */
	LABEL("label");

    private final String label;

    private REDCapApiRawOrLabel(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
