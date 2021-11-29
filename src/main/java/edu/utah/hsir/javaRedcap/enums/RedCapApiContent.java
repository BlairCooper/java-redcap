package edu.utah.hsir.javaRedcap.enums;

public enum RedCapApiContent
	implements RedCapApiEnum
{
	ARM("arm"),
	DAG("dag"),
	EVENT("event"),
    EXPORT_FIELD_NAMES("exportFieldNames"),
    FILE("file"),
    FORM_EVENT_MAPPING("formEventMapping"),
	GENERATE_NEXT_RECORD_NAME("generateNextRecordName"),
    INSTRUMENT("instrument"),
    LOG("log"),
    METADATA("metadata"),
    PARTICIPANT_LIST("participantList"),
    PDF("pdf"),
	PROJECT("project"),
	PROJECT_SETTINGS("project_settings"),
	PROJECT_XML("project_xml"),
	RECORD("record"),
	REPORT("report"),
	REPEATING_FORMS_EVENTS("repeatingFormsEvents"),
	SURVEY_LINK("surveyLink"),
	SURVEY_QUEUE_LINK("surveyQueueLink"),
	SURVEY_RETURN_CODE("surveyReturnCode"),
	USER("user"),
	USER_DAG_MAPPING("userDagMapping"),
	VERSION("version");

    public final String label;

    private RedCapApiContent(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
