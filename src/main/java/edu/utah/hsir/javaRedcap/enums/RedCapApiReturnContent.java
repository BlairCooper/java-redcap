package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiReturnContent
	implements RedCapApiEnum
{
	COUNT("count"),
	AUTO_IDS("auto_ids"),
	IDS("ids");

    public final String label;

    private RedCapApiReturnContent(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
