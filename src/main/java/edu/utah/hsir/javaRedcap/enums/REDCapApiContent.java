package edu.utah.hsir.javaRedcap.enums;

/**
 * Enumeration of REDCap API request contents. The content along with an action identifies the
 * type of request being made.
 *
 */
public enum REDCapApiContent
	implements REDCapApiEnum
{
	/** A request related to Arms */
	ARM("arm"),
	/** A request related to DAGs */
	DAG("dag"),
	/** A request related to Events */
	EVENT("event"),
	/** A request related to exporting field names */
    EXPORT_FIELD_NAMES("exportFieldNames"),
	/** A request related to file fields */
    FILE("file"),
	/** A request related to Form/Event mappings */
    FORM_EVENT_MAPPING("formEventMapping"),
	/** A request related to generating the next record id */
	GENERATE_NEXT_RECORD_NAME("generateNextRecordName"),
	/** A request related to Instruments */
    INSTRUMENT("instrument"),
	/** A request related to Logs */
    LOG("log"),
	/** A request related to Metadata */
    METADATA("metadata"),
	/** A request related to Participant Lists */
    PARTICIPANT_LIST("participantList"),
	/** A request related to PDFs */
    PDF("pdf"),
	/** A request related to Projects */
	PROJECT("project"),
	/** A request related to Project Settings */
	PROJECT_SETTINGS("project_settings"),
	/** A request related to Project XML */
	PROJECT_XML("project_xml"),
	/** A request related to Records */
	RECORD("record"),
	/** A request related to Reports */
	REPORT("report"),
	/** A request related to Repeating Forms & Events */
	REPEATING_FORMS_EVENTS("repeatingFormsEvents"),
	/** A request related to Survey Links */
	SURVEY_LINK("surveyLink"),
	/** A request related to Survey Queue Links */
	SURVEY_QUEUE_LINK("surveyQueueLink"),
	/** A request related to Survey Return Codes */
	SURVEY_RETURN_CODE("surveyReturnCode"),
	/** A request related to Users */
	USER("user"),
	/** A request related to User / DAG mappings */
	USER_DAG_MAPPING("userDagMapping"),
	/** A request related to REDCap versions */
	VERSION("version");

    private final String label;

    private REDCapApiContent(String label) {
        this.label = label;
    }

	@Override
	public String getLabel() {
		return label;
	}
}
