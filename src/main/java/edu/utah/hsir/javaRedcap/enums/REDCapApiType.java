package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of format types used in importing and export records. 
 *
 */
public enum REDCapApiType
	implements REDCapApiEnum
{
	/** Use a EAV format */ 
	EAV("eav"),
	/** Use a flat format */
	FLAT("flat");

    private final String label;

    private REDCapApiType(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
