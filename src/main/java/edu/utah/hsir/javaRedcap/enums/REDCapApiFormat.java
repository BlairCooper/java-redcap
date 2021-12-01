package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of formats allowed in REDCap requests.
 *
 */
public enum REDCapApiFormat
	implements REDCapApiEnum
{
	/** CSV / Comma Separated Values format */
	CSV("csv"),
	/** File format used for file fields */
	FILE("file"),
	/** JSON / JavaScript Object Notation format */
	JSON("json"),
	/** CDISC ODM XML format, specifically ODM version 1.3.1 */
	ODM("odm"),
	/** XML / Extensible Markup Language format */
	XML("xml");

    private final String label;

    private REDCapApiFormat(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
