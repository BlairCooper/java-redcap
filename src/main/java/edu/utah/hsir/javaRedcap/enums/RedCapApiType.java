package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiType
	implements RedCapApiEnum
{
	EAV("eav"),
	FLAT("flat");

    public final String label;

    private RedCapApiType(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
