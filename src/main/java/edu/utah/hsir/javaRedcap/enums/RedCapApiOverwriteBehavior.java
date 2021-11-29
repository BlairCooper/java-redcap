package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiOverwriteBehavior
	implements RedCapApiEnum
{
	NORMAL("normal"),
	OVERWRITE("overwrite");

    public final String label;

    private RedCapApiOverwriteBehavior(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
