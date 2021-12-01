package edu.utah.hsir.javaRedcap.enums;

/**
 * Interface to be implemented by all JavaREDCap enumerations
 *
 */
public interface REDCapApiEnum {
	/**
	 * Retrieve the label associated with an enumeration value.
	 * 
	 * @return The label for an enumeration value.
	 */
	public String getLabel();
}
