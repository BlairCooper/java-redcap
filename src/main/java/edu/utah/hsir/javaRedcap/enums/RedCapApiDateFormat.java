package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiDateFormat
	implements RedCapApiEnum
{
	MDY("MDY"),
	DMY("DMY"),
	YMD("YMD");

	public final String label;

    private RedCapApiDateFormat(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
