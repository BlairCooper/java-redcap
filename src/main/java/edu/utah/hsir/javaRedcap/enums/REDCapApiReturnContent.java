package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of possible values for the returnContent parameter used when
 * importing records.
 *
 */
public enum REDCapApiReturnContent
	implements REDCapApiEnum
{
	/** Return the number of records imported */
	COUNT("count"),
	/** Return an array of comma-separated record ID pairs, with the new ID
	 * 		created and the corresponding ID that was sent, for the records
	 * 		that were imported. This can only be used if the forceAutoNumber
	 * 		parameter is set to true.
	 */
	AUTO_IDS("auto_ids"),
	/** Return an array of the record IDs imported. */
	IDS("ids");

    private final String label;

    private REDCapApiReturnContent(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
