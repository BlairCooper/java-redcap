package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of characters used as the separator in numeric values.
 * 
 */
public enum REDCapApiDecimalCharacter
	implements REDCapApiEnum
{
	/** A Comma */
	COMMA(","),
	/** A Dot/Period/Full Stop */
	PERIOD(".");

	private final String label;

    private REDCapApiDecimalCharacter(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
