/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import edu.utah.hsir.javaRedcap.enums.REDCapApiAction;
import edu.utah.hsir.javaRedcap.enums.REDCapApiContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiCsvDelimiter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDateFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDecimalCharacter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiLogType;
import edu.utah.hsir.javaRedcap.enums.REDCapApiOverwriteBehavior;
import edu.utah.hsir.javaRedcap.enums.REDCapApiParameter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.REDCapApiReturnContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiType;

@SuppressWarnings("javadoc")
public class REDCapApiRequestTest {
	protected static final String TEST_TOKEN = "12345678901234567890123456789012";

	protected Map<REDCapApiParameter, Object> testDataMap = new HashMap<>();
	protected REDCapApiRequest testParams;

	@Before
	public void setup() throws JavaREDCapException {
		testDataMap.clear();
		testDataMap.put(REDCapApiParameter.TOKEN, TEST_TOKEN);
		testDataMap.put(REDCapApiParameter.CONTENT, REDCapApiContent.VERSION);
		testDataMap.put(REDCapApiParameter.FORMAT, REDCapApiFormat.JSON);
		
		testParams = new REDCapApiRequest(testDataMap);
	}

	@Test
	public void testCtor_nullMap() {
    	assertThrows(NullPointerException.class, () -> {
    		new REDCapApiRequest(null);
    	});
	}

	@Test
	public void testCtor_emptyMap() {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest(new HashMap<REDCapApiParameter, Object>());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}

	@Test
	public void testCtor_baseMap_MissingFormat() throws JavaREDCapException {
		testDataMap.remove(REDCapApiParameter.FORMAT);

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest(testDataMap);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}	

	@Test
	public void testCtor_baseMap_MissingContent() throws JavaREDCapException {
		testDataMap.remove(REDCapApiParameter.CONTENT);

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest(testDataMap);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}	

	@Test
	public void testCtor_baseMap_NullValue() throws JavaREDCapException {
		testDataMap.put(REDCapApiParameter.RECORDS, null);

		REDCapApiRequest data = new REDCapApiRequest(testDataMap);
		
		assertNotNull(data);
	}	

	@Test
	public void testCtor_baseMap() throws JavaREDCapException {
		REDCapApiRequest data = new REDCapApiRequest(testDataMap);
		
		assertNotNull(data);
		assertEquals(testDataMap.size() + 1, data.paramMap.size());

		// the entries from the map pass in should all be present
		for (Entry<REDCapApiParameter, Object> entry : testDataMap.entrySet()) {
			Object value = data.paramMap.get(entry.getKey());
			assertNotNull(value);
			assertEquals(entry.getValue(), value);
		}

		// the return format should be set
		assertNotNull(data.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		assertEquals(REDCapApiFormat.JSON, data.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		
		assertNotNull(data.errorHandler);
	}
	
	@Test
	public void testCtor_nullParams() {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest (null, null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_emptyToken() {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest ("  ", null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_nullContent() {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCapApiRequest (TEST_TOKEN, null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_nullHandler() throws JavaREDCapException {
    	REDCapApiRequest data = new REDCapApiRequest (TEST_TOKEN, REDCapApiContent.FORM_EVENT_MAPPING, null);
    	
    	assertNotNull(data.errorHandler);
	}

	@Test
	public void testCtor_withHandler() throws JavaREDCapException {
		ErrorHandlerInterface handler = Mockito.mock(ErrorHandlerInterface.class);

		REDCapApiRequest data = new REDCapApiRequest (TEST_TOKEN, REDCapApiContent.ARM, handler);
    	
		assertSame(handler, data.errorHandler);
	}

	@Test
	public void testCtor_goodParams() throws JavaREDCapException {
		String testToken = new StringBuilder(TEST_TOKEN).reverse().toString();

		REDCapApiRequest data = new REDCapApiRequest (testToken, REDCapApiContent.EVENT, null);
		
		assertNotNull(data);
		assertEquals(4, data.paramMap.size());	// expect token, content, format & returnFormat
		
		assertEquals(testToken, data.paramMap.get(REDCapApiParameter.TOKEN));
		assertEquals(REDCapApiContent.EVENT, data.paramMap.get(REDCapApiParameter.CONTENT));
		assertEquals(REDCapApiFormat.JSON, data.paramMap.get(REDCapApiParameter.FORMAT));
		assertEquals(REDCapApiFormat.JSON, data.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		assertNotNull(data.errorHandler);
	}

	@Test
	public void testAddFormat_nullParam() {
		int preSize = testParams.legalFormats.size();
		
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		testParams.addFormat(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

    	assertEquals(preSize, testParams.legalFormats.size());
	}

	@Test
	public void testAddFormat_defaultFormat() throws JavaREDCapException {
		int preSize = testParams.legalFormats.size();
		
		testParams.addFormat(REDCapApiFormat.JSON);
    	
    	assertEquals(preSize, testParams.legalFormats.size());
	}

	@Test
	public void testAddFormat_newFormat() throws JavaREDCapException {
		int preSize = testParams.legalFormats.size();
		
		testParams.addFormat(REDCapApiFormat.ODM);
    	
    	assertEquals(preSize + 1, testParams.legalFormats.size());
    	assertNotNull(testParams.legalFormats.contains(REDCapApiFormat.ODM));
	}

	@Test
	public void testSetAction_nullParam() {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		testParams.setAction(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("action"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ACTION));
	}

	@Test
	public void testSetAction() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ACTION));

		testParams.setAction(REDCapApiAction.IMPORT);
		
		assertEquals(REDCapApiAction.IMPORT, testParams.paramMap.get(REDCapApiParameter.ACTION));
	}

	@Test
	public void testSetAllRecords() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ALL_RECORDS));
		
		testParams.setAllRecords(false);

		// Parameter is only used when set to true
		assertNull(testParams.paramMap.get(REDCapApiParameter.ALL_RECORDS));
		
		testParams.setAllRecords(true);
		
		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.ALL_RECORDS));
	}

	@Test
	public void testSetArm_nullParam() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARM));

		testParams.setArm(null);
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARM));
	}

	@Test
	public void testSetArm_negativeValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARM));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setArm(-19);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arm"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARM));
	}

	@Test
	public void testSetArm_goodValue() throws JavaREDCapException {
		int testArm = 5;

		assertNull(testParams.paramMap.get(REDCapApiParameter.ARM));

		testParams.setArm(testArm);
    	
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.ARM));
    	assertEquals(testArm, (int)testParams.paramMap.get(REDCapApiParameter.ARM));
	}

	@Test
	public void testSetArms_nullSet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));

		// no arms provided but not required
		testParams.setArms(null, false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
		
    	// no arms provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setArms(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arms"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
	}

	@Test
	public void testSetArms_emptyArms() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));

		// no arms provided but not required
		testParams.setArms(new HashSet<Integer>(), false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
		
    	// no arms provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setArms(new HashSet<Integer>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arms"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
	}

	@Test
	public void testSetArms_negativeValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(1, 2, -4));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setArms(testArms, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
	}

	@Test
	public void testSetArms_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(1, 2, null));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setArms(testArms, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
	}

	@Test
	public void testSetArms_goodSet_notRequried() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(3, 6, 9));

		testParams.setArms(testArms, false);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.ARMS));
    	
    	@SuppressWarnings("unchecked")
		Set<Integer> retrievedArms = (Set<Integer>)testParams.paramMap.get(REDCapApiParameter.ARMS);
    	
    	assertEquals(testArms.size(), retrievedArms.size());
    	
    	for (Integer arm : retrievedArms) {
    		assertTrue(testArms.contains(arm));
    	}
	}
	
	@Test
	public void testCheckDateRangeArgument_nullValue() {
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.checkDateRangeArgument(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
	}
	
	@Test
	public void testCheckDateRangeArgument_blankValue() {
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.checkDateRangeArgument("");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
	}
	
	@Test
	public void testCheckDateRangeArgument_invalidFormat() {
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.checkDateRangeArgument("12/25/2020");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
	}
	
	@Test
	public void testCheckDateRangeArgument_validFormat() throws JavaREDCapException {
		String testDate = "2020-01-31 00:00:00";
		String result = testParams.checkDateRangeArgument(testDate);
		
		assertEquals(testDate, result);
	}
	
	@Test
	public void testSetBeginTime_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));

		testParams.setBeginTime(null);

		assertNull(testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));
	}
	
	@Test
	public void testSetBeginTime_invalidFormat() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setBeginTime("2/2/2002");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

		assertNull(testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));
	}
	
	@Test
	public void testSetBeginTime_validFormat() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));

		String testDate = "2017-07-31 00:15:00";
		testParams.setBeginTime(testDate);

		assertEquals(testDate, (String)testParams.paramMap.get(REDCapApiParameter.BEGIN_TIME));
	}
	
	@Test
	public void testSetEndTime_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.END_TIME));

		testParams.setEndTime(null);
    	
		assertNull(testParams.paramMap.get(REDCapApiParameter.END_TIME));
	}
	
	@Test
	public void testSetEndTime_invalidFormat() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.END_TIME));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setEndTime("1/5/2004");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

		assertNull(testParams.paramMap.get(REDCapApiParameter.END_TIME));
	}
	
	@Test
	public void testEndBeginTime_validFormat() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.END_TIME));

		String testDate = "2014-02-28 14:00:00";
		testParams.setEndTime(testDate);

		assertEquals(testDate, (String)testParams.paramMap.get(REDCapApiParameter.END_TIME));
	}

	@Test
	public void testSetCompactDisplay() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.COMPACT_DISPLAY));
		
		testParams.setCompactDisplay(false);

		// Parameter is only used when set to true
		assertNull(testParams.paramMap.get(REDCapApiParameter.COMPACT_DISPLAY));

		testParams.setCompactDisplay(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.COMPACT_DISPLAY));
	}

	@Test
	public void testSetContent_nullValue() {
		Object currentContent = testParams.paramMap.get(REDCapApiParameter.CONTENT);
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setContent(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertEquals(currentContent, testParams.paramMap.get(REDCapApiParameter.CONTENT));
	}
	
	@Test
	public void testSetConent_validValue() throws JavaREDCapException {
		assertNotEquals(REDCapApiContent.INSTRUMENT, testParams.paramMap.get(REDCapApiParameter.CONTENT));
		
		testParams.setContent(REDCapApiContent.INSTRUMENT);
		
		assertEquals(REDCapApiContent.INSTRUMENT, testParams.paramMap.get(REDCapApiParameter.CONTENT));
	}
	
	@Test
	public void testSetCsvDelimiter_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.CSV_DELIMITER));

		testParams.setCsvDelimiter(null);
    	
		assertEquals(REDCapApiCsvDelimiter.COMMA, testParams.paramMap.get(REDCapApiParameter.CSV_DELIMITER));
	}

	@Test
	public void testSetCsvDelimiter_validValue() throws JavaREDCapException {
		REDCapApiCsvDelimiter testDelimiter = REDCapApiCsvDelimiter.CARET;

		assertNull(testParams.paramMap.get(REDCapApiParameter.CSV_DELIMITER));

		testParams.setCsvDelimiter(testDelimiter);

		assertEquals(testDelimiter, (REDCapApiCsvDelimiter)testParams.paramMap.get(REDCapApiParameter.CSV_DELIMITER));
	}

	@Test
	public void testSetDag_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAG));

		testParams.setDag(null);

		assertNull(testParams.paramMap.get(REDCapApiParameter.DAG));
	}

	@Test
	public void testSetDag_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAG));

		testParams.setDag(" ");

		assertNull(testParams.paramMap.get(REDCapApiParameter.DAG));
	}

	@Test
	public void testSetDag_validValue() throws JavaREDCapException {
		String testDag = " some_dag ";

		assertNull(testParams.paramMap.get(REDCapApiParameter.DAG));

		testParams.setDag(testDag);

		assertEquals(testDag.trim(), (String)testParams.paramMap.get(REDCapApiParameter.DAG));
	}

	@Test
	public void testSetDags_nullDags() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));

		// no dags provided but not required
		testParams.setDags(null, false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
		
    	// no dags provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDags(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("dags"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
	}

	@Test
	public void testSetDags_emptyDags() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));

		// no dags provided but not required
		testParams.setDags(new HashSet<String>(), false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
		
    	// no dags provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDags(new HashSet<String>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("dags"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
	}

	@Test
	public void testSetDags_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", null));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDags(testDags, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
	}

	@Test
	public void testSetDags_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", ""));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDags(testDags, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
	}

	@Test
	public void testSetDags_goodSet_notRequried() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", "dag_3"));

		testParams.setDags(testDags, false);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.DAGS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedDags = (Set<String>)testParams.paramMap.get(REDCapApiParameter.DAGS);
    	
    	assertEquals(testDags.size(), retrievedDags.size());
    	
    	for (String dag : retrievedDags) {
    		assertTrue(testDags.contains(dag));
    	}
	}

	@Test
	public void testSetData_nullValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATA));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setData(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATA));
	}
	
	@Test
	public void testSetData() throws JavaREDCapException {
		String testData = "[{\"recordid\": \"1\"}]";

		assertNull(testParams.paramMap.get(REDCapApiParameter.DATA));
		
		testParams.setData(testData);

		assertSame(testData, testParams.paramMap.get(REDCapApiParameter.DATA));
	}

	@Test
	public void testSetDateFormat_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_FORMAT));
		
		testParams.setDateFormat(null);
		
		assertEquals(REDCapApiDateFormat.YMD, testParams.paramMap.get(REDCapApiParameter.DATE_FORMAT));
	}

	@Test
	public void testSetDateFormat_goodValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_FORMAT));
		
		testParams.setDateFormat(REDCapApiDateFormat.MDY);
		
		assertEquals(REDCapApiDateFormat.MDY, testParams.paramMap.get(REDCapApiParameter.DATE_FORMAT));
	}

	@Test
	public void testSetDateRangeBegin_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
		
		testParams.setDateRangeBegin(null);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeBegin_blankValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDateRangeBegin("                 ");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeBegin_invalidFormat() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDateRangeBegin("April 1, 2000 11:00pm");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
	}


	@Test
	public void testSetDateRangeBegin_validFormat() throws JavaREDCapException {
		String testDate = "1999-12-31 23:59:59";

		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
		
		testParams.setDateRangeBegin(testDate);
    	
    	assertEquals(testDate, (String)testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeEnd_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
		
		testParams.setDateRangeEnd(null);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_blankValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDateRangeEnd("");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_invalidFormat() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setDateRangeEnd("March 23, 1976 1:00am");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

    	assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_validFormat() throws JavaREDCapException {
		String testDate = "1972-08-23 03:09:19";

		assertNull(testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
		
		testParams.setDateRangeEnd(testDate);
    	
    	assertEquals(testDate, (String)testParams.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
	}
	
	@Test
	public void testSetDecimalCharacter_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DECIMAL_CHARACTER));
		
		testParams.setDecimalCharacter(null);
    	
		assertNull(testParams.paramMap.get(REDCapApiParameter.DECIMAL_CHARACTER));
	}
	
	@Test
	public void testSetDecimalCharacter_goodValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.DECIMAL_CHARACTER));
		
		testParams.setDecimalCharacter(REDCapApiDecimalCharacter.PERIOD);
    	
		assertEquals(REDCapApiDecimalCharacter.PERIOD, testParams.paramMap.get(REDCapApiParameter.DECIMAL_CHARACTER));
	}


	@Test
	public void testSetEvent_null() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENT));

		testParams.setEvent(null);
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENT));
	}

	@Test
	public void testSetEvent_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENT));

		testParams.setEvent(" ");
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENT));
	}

	@Test
	public void testSetEvent_goodValue() throws JavaREDCapException {
		String testEvent = "initial_visit";

		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENT));

		testParams.setEvent(testEvent);
    	
    	assertEquals(testEvent, (String)testParams.paramMap.get(REDCapApiParameter.EVENT));
	}

	@Test
	public void testSetEvents_nullEvents() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));

		// no events provided but not required
		testParams.setEvents(null, false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
		
    	// no events provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setEvents(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("events"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
	}

	@Test
	public void testSetEvents_emptyEvents() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));

		// no events provided but not required
		testParams.setEvents(new HashSet<String>(), false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
		
    	// no events provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setEvents(new HashSet<String>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("events"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
	}

	@Test
	public void testSetEvents_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("annual_visit_1", null));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setEvents(testEvents, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
	}

	@Test
	public void testSetEvents_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("annual_visit_1", " "));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setEvents(testEvents, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
	}

	@Test
	public void testSetEvents_goodSet_notRequried() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("initial_visit", "annual_visit"));

		testParams.setEvents(testEvents, false);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.EVENTS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedEvents = (Set<String>)testParams.paramMap.get(REDCapApiParameter.EVENTS);
    	
    	assertEquals(testEvents.size(), retrievedEvents.size());
    	
    	for (String event : retrievedEvents) {
    		assertTrue(testEvents.contains(event));
    	}
	}

	@Test
	public void testSetExportCheckboxLabel() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EXPORT_CHECKBOX_LABEL));
		
		testParams.setExportCheckboxLabel(false);
		
		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_CHECKBOX_LABEL));
		
		testParams.setExportCheckboxLabel(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_CHECKBOX_LABEL));
	}

	@Test
	public void testSetExportDataAccessGroups() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS));
		
		testParams.setExportDataAccessGroups(false);
		
		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS));
		
		testParams.setExportDataAccessGroups(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS));
	}

	@Test
	public void testSetExportFiles() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EXPORT_FILES));
		
		testParams.setExportFiles(false);
		
		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_FILES));
		
		testParams.setExportFiles(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_FILES));
	}

	@Test
	public void testSetExportSurveyFields() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.EXPORT_SURVEY_FIELDS));
		
		testParams.setExportSurveryFields(false);
		
		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_SURVEY_FIELDS));
		
		testParams.setExportSurveryFields(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.EXPORT_SURVEY_FIELDS));
	}

	@Test
	public void testSetField_nullValue_notRequired() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));
		
		testParams.setField(null, false);

		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));
	}

	@Test
	public void testSetField_nullValue_required() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setField(null, true);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));
	}

	@Test
	public void testSetField_blankValue_required() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setField("", true);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));
	}

	@Test
	public void testSetField_validValue() throws JavaREDCapException {
		String testField = "test_field";

		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELD));

		testParams.setField(testField, true);

		assertEquals(testField, testParams.paramMap.get(REDCapApiParameter.FIELD));
	}

	@Test
	public void testSetFields_nullFields() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));

		// no events provided but not required
		testParams.setFields(null);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
	}

	@Test
	public void testSetFields_emptyEvents() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));

		testParams.setFields(new HashSet<String>());

    	assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
	}

	@Test
	public void testSetFields_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));

		Set<String> testFields = new HashSet<String>(Arrays.asList("recordid", "age", null));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFields(testFields);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
	}

	@Test
	public void testSetFields_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
		
		Set<String> testFields = new HashSet<String>(Arrays.asList("  ", "recordid", "dob"));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFields(testFields);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
	}

	@Test
	public void testSetEvents_goodSet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
		
		Set<String> testFields = new HashSet<String>(Arrays.asList("recordid", "age", "dob", "time_in_service"));

		testParams.setFields(testFields);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.FIELDS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedFields = (Set<String>)testParams.paramMap.get(REDCapApiParameter.FIELDS);
    	
    	assertEquals(testFields.size(), retrievedFields.size());
    	
    	for (String field : retrievedFields) {
    		assertTrue(testFields.contains(field));
    	}
	}

	@Test
	public void testSetFile_nullValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFile(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no filename"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
	}

	@Test
	public void testSetFile_blankValue() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFile("   ");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no filename"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
	}
	
	@Test
	public void testSetFile_missingFile() throws IOException {
	    File testFile = File.createTempFile("setfile_missing", null);
	    testFile.delete();

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFile(testFile.getAbsolutePath());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INPUT_FILE_NOT_FOUND, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("not"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("found"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
	}
	
	@Test
    public void testSetFile_fileUnreadable() throws IOException
    {
		// Skip the test on Windows
		Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));

	    File testFile = File.createTempFile("setfile_unreadable", null);
	    testFile.deleteOnExit();
	    testFile.setReadable(false);

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFile(testFile.getAbsolutePath());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INPUT_FILE_UNREADABLE, exception.getCode());
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("unreadable"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
    }
	
	
	@Test
    public void testSetFile_goodFile() throws IOException, JavaREDCapException
    {
	    File testFile = File.createTempFile("setfile_goodfile", null);
	    testFile.deleteOnExit();

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILE));
		
		testParams.setFile(testFile.getAbsolutePath());
    	
    	assertEquals(Paths.get(testFile.getAbsolutePath()), testParams.paramMap.get(REDCapApiParameter.FILE));
    }

	@Test
	public void testSetFilterLogic_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
		
		testParams.setFilterLogic(null);

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
	}

	@Test
	public void testSetFilterLogic_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
		
		testParams.setFilterLogic("");

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
	}

	@Test
	public void testSetFilterLogic_goodValue() throws JavaREDCapException {
		String testLogic = "[age]=\"25\"";

		assertNull(testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
		
		testParams.setFilterLogic(testLogic);

		assertEquals(testLogic, testParams.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
	}
	
	@Test
	public void testSetForceAutoNumber() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORCE_AUTO_NUMBER));
		
		testParams.setForceAutoNumber(false);

		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.FORCE_AUTO_NUMBER));

		testParams.setForceAutoNumber(true);

		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.FORCE_AUTO_NUMBER));
	}

	@Test
	public void testSetFormat() throws JavaREDCapException {
		Object currentFormat = testParams.paramMap.get(REDCapApiParameter.FORMAT);
		assertNotEquals(REDCapApiFormat.XML, currentFormat);

		testParams.setFormat(REDCapApiFormat.XML);
		
		assertEquals(REDCapApiFormat.XML, testParams.paramMap.get(REDCapApiParameter.FORMAT));

		testParams.setFormat(null);
		
		assertEquals(REDCapApiFormat.JSON, testParams.paramMap.get(REDCapApiParameter.FORMAT));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setFormat(REDCapApiFormat.ODM);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("invalid"));
    	
    	testParams.addFormat(REDCapApiFormat.ODM);
		testParams.setFormat(REDCapApiFormat.ODM);
    	
		assertEquals(REDCapApiFormat.ODM, testParams.paramMap.get(REDCapApiParameter.FORMAT));
	}

	@Test
	public void testSetForms_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
		
		testParams.setForms(null);
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
	}

	@Test
	public void testSetForms_emptySet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
		
		testParams.setForms(new HashSet<String>());
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
	}

	@Test
	public void testSetForms_nullEntry() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));

		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "medical_history", null));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setForms(testForms);
		});

		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
	}
	
	@Test
	public void testSetForms_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
		
		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "  ", "medical_history", null));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setForms(testForms);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
	}

	@Test
	public void testSetForms_goodSet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
		
		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "medical_history", "physical_activity"));

		testParams.setForms(testForms);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.FORMS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedForms = (Set<String>)testParams.paramMap.get(REDCapApiParameter.FORMS);
    	
    	assertEquals(testForms.size(), retrievedForms.size());
    	
    	for (String form : retrievedForms) {
    		assertTrue(testForms.contains(form));
    	}
	}

	@Test
	public void testSetInstrument_nullValue_notRequired() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));

		testParams.setInstrument(null, false);
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_nullValue_required() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setInstrument(null, true);
		});
    	
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
    	assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_blankValue_notRequired() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));

		testParams.setInstrument("     ", false);
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_blankValue_required() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setInstrument("   ", true);
		});
    	
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
    	assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_goodValue() throws JavaREDCapException {
		String testInstrument = "activities";

		assertNull(testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));

		testParams.setInstrument(testInstrument, false);
    	
    	assertEquals(testInstrument, testParams.paramMap.get(REDCapApiParameter.INSTRUMENT));
	}

	@Test
	public void testSetLogType_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.LOGTYPE));
		
		testParams.setLogType(null);
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.LOGTYPE));
	}


	@Test
	public void testSetLogType() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.LOGTYPE));
		
		testParams.setLogType(REDCapApiLogType.RECORD);
		
		assertEquals(REDCapApiLogType.RECORD, testParams.paramMap.get(REDCapApiParameter.LOGTYPE));

		testParams.setLogType(REDCapApiLogType.USER);
		
		assertEquals(REDCapApiLogType.USER, testParams.paramMap.get(REDCapApiParameter.LOGTYPE));
	}

	@Test
	public void testSetOdm_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ODM));
		
		testParams.setOdm(null);
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.ODM));
	}

	@Test
	public void testSetOdm_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.ODM));

		testParams.setOdm(" ");
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.ODM));
	}

	@Test
	public void testSetOdm_goodValue() throws JavaREDCapException {
		String testOdmStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		assertNull(testParams.paramMap.get(REDCapApiParameter.ODM));
			
		testParams.setOdm(testOdmStr);
			
		assertEquals(testOdmStr, testParams.paramMap.get(REDCapApiParameter.ODM));
	}

	@Test
	public void testSetOverride() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.OVERRIDE));
		
		testParams.setOverride(false);
		
		assertEquals(0, testParams.paramMap.get(REDCapApiParameter.OVERRIDE));

		testParams.setOverride(true);
		
		assertEquals(1, testParams.paramMap.get(REDCapApiParameter.OVERRIDE));
	}

	@Test
	public void testSetOverwriteBehavior() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.OVERWRITE_BEHAVIOR));
		
		testParams.setOverwriteBehavior(null);
		
		assertEquals(REDCapApiOverwriteBehavior.NORMAL, testParams.paramMap.get(REDCapApiParameter.OVERWRITE_BEHAVIOR));
		
		testParams.setOverwriteBehavior(REDCapApiOverwriteBehavior.OVERWRITE);
		
		assertEquals(REDCapApiOverwriteBehavior.OVERWRITE, testParams.paramMap.get(REDCapApiParameter.OVERWRITE_BEHAVIOR));

		testParams.setOverwriteBehavior(REDCapApiOverwriteBehavior.NORMAL);
		
		assertEquals(REDCapApiOverwriteBehavior.NORMAL, testParams.paramMap.get(REDCapApiParameter.OVERWRITE_BEHAVIOR));
	}

	@Test
	public void testSetRawOrLabel() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(null);
		
		assertEquals(REDCapApiRawOrLabel.RAW, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(REDCapApiRawOrLabel.LABEL);
		
		assertEquals(REDCapApiRawOrLabel.LABEL, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(REDCapApiRawOrLabel.RAW);
		
		assertEquals(REDCapApiRawOrLabel.RAW, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL));
	}

	@Test
	public void testSetRawOrLabelHeaders() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(null);
		
		assertEquals(REDCapApiRawOrLabel.RAW, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(REDCapApiRawOrLabel.LABEL);
		
		assertEquals(REDCapApiRawOrLabel.LABEL, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(REDCapApiRawOrLabel.RAW);
		
		assertEquals(REDCapApiRawOrLabel.RAW, testParams.paramMap.get(REDCapApiParameter.RAW_OR_LABEL_HEADERS));
	}

	@Test
	public void testSetRecord_nullRecord() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));

		// no record provided but not required
		testParams.setRecord(null, false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
		
    	// no record provided but they are required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setRecord(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("record"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
	}

	@Test
	public void testSetRecord_emptyRecord() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));

		// no record provided but not required
		testParams.setRecord("  ", false);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
		
    	// no record provided but is required
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setRecord("", true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("record"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
	}

	@Test
	public void testSetRecord_goodValue_notRequried() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
		
		String testRecordId = "ABC123 ";

		testParams.setRecord(testRecordId, false);
    	
    	assertEquals(testRecordId.trim(), testParams.paramMap.get(REDCapApiParameter.RECORD));
	}

	@Test
	public void testSetRecord_goodValue_requried() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORD));
		
		String testRecordId = " XYZPDQ";

		testParams.setRecord(testRecordId, true);
    	
    	assertEquals(testRecordId.trim(), testParams.paramMap.get(REDCapApiParameter.RECORD));
	}

	@Test
	public void testSetRecords_nullSet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));

		// no records provided
		testParams.setRecords(null);

    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
	}

	@Test
	public void testSetRecords_emptySet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));

		// no records provided
		testParams.setRecords(new HashSet<String>());

    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
	}

	@Test
	public void testSetRecords_nullValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("ABC987", "BDE414", null));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setRecords(testRecordIds);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
	}

	@Test
	public void testSetRecords_blankValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("jaf42", "k7sfa", " "));

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setRecords(testRecordIds);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
	}

	@Test
	public void testSetRecords_goodSet() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("ab431", "kgh47", "afe898"));

		testParams.setRecords(testRecordIds);
		
    	assertNotNull(testParams.paramMap.get(REDCapApiParameter.RECORDS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedRecordIds = (Set<String>)testParams.paramMap.get(REDCapApiParameter.RECORDS);
    	
    	assertEquals(testRecordIds.size(), retrievedRecordIds.size());
    	
    	for (String recordId : retrievedRecordIds) {
    		assertTrue(testRecordIds.contains(recordId));
    	}
	}

	@Test
	public void testSetRepeatInstance_nullParam() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
		
		testParams.setRepeatInstance(null);

		assertNull(testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
	}
	
	@Test
	public void testSetRepeatInstance_negativeParam() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setRepeatInstance(-3);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
	}
	
	@Test
	public void testSetRepeatInstance_goodValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
		
		int testInstance = 4;
		
		testParams.setRepeatInstance(testInstance);
		
		assertEquals(testInstance, testParams.paramMap.get(REDCapApiParameter.REPEAT_INSTANCE));
	}


	@Test
	public void testSetReportId_nullParam() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setReportId(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertNull(testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
	}
	
	@Test
	public void testSetReportId_negativeParam() {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setReportId(-7);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
	}
	
	@Test
	public void testSetReportId_goodValue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
		
		int testReportId = 5;
		
		testParams.setReportId(testReportId);
		
		assertEquals(testReportId, testParams.paramMap.get(REDCapApiParameter.REPORT_ID));
	}
	
	@Test
	public void testSetReturnContent_nullParam() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
		
		testParams.setReturnContent(null, false);
		
		assertEquals(REDCapApiReturnContent.COUNT, testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_autoIds_forceFalse() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
		
		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			testParams.setReturnContent(REDCapApiReturnContent.AUTO_IDS, false);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("auto_ids"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("forceAutoNumber"));
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_autoIds_forceTrue() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
		
		testParams.setReturnContent(REDCapApiReturnContent.AUTO_IDS, true);
    	
		assertEquals(REDCapApiReturnContent.AUTO_IDS, testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_goodValues() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
		
		testParams.setReturnContent(REDCapApiReturnContent.COUNT, true);
    	
		assertEquals(REDCapApiReturnContent.COUNT, testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
		
		testParams.setReturnContent(REDCapApiReturnContent.IDS, true);
    	
		assertEquals(REDCapApiReturnContent.IDS, testParams.paramMap.get(REDCapApiParameter.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnFormat() throws JavaREDCapException {
		REDCapApiFormat currentFormat = (REDCapApiFormat)testParams.paramMap.get(REDCapApiParameter.RETURN_FORMAT);
		
		assertNotEquals(REDCapApiFormat.XML, currentFormat);

		testParams.setReturnFormat(REDCapApiFormat.XML);

		assertEquals(REDCapApiFormat.XML, testParams.paramMap.get(REDCapApiParameter.RETURN_FORMAT));

		testParams.setReturnFormat(null);
		
		assertEquals(REDCapApiFormat.JSON, testParams.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		
		testParams.setReturnFormat(REDCapApiFormat.CSV);

		assertEquals(REDCapApiFormat.CSV, testParams.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		
		testParams.setReturnFormat(REDCapApiFormat.JSON);

		assertEquals(REDCapApiFormat.JSON, testParams.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
	}

	@Test
	public void testSetReturnMetadataOnly() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.RETURN_METADATA_ONLY));
		
		testParams.setReturnMetadataOnly(false);
		
		assertFalse((boolean)testParams.paramMap.get(REDCapApiParameter.RETURN_METADATA_ONLY));
		
		testParams.setReturnMetadataOnly(true);
		
		assertTrue((boolean)testParams.paramMap.get(REDCapApiParameter.RETURN_METADATA_ONLY));
	}

	@Test
	public void testSetType() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.TYPE));
		
		testParams.setType(null);
		
		assertEquals(REDCapApiType.FLAT, testParams.paramMap.get(REDCapApiParameter.TYPE));
		
		testParams.setType(REDCapApiType.EAV);

		assertEquals(REDCapApiType.EAV, testParams.paramMap.get(REDCapApiParameter.TYPE));
		
		testParams.setType(REDCapApiType.FLAT);

		assertEquals(REDCapApiType.FLAT, testParams.paramMap.get(REDCapApiParameter.TYPE));
	}

	@Test
	public void testSetUser() throws JavaREDCapException {
		assertNull(testParams.paramMap.get(REDCapApiParameter.USER));
		
		testParams.setUser(null);
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.USER));
		
		testParams.setUser("        ");
		
		assertNull(testParams.paramMap.get(REDCapApiParameter.USER));

		String testUser = "fred";
		
		testParams.setUser(testUser);
		
		assertEquals(testUser, testParams.paramMap.get(REDCapApiParameter.USER));
	}
	
	@Test
	public void testToFormData_defaultTestParams() {
		String data = testParams.toWwwFormUrlencoded();
		
		Map<String, Object> dataMap = formStrToMap(data);
		
		assertTrue(dataMap.keySet().contains(REDCapApiParameter.FORMAT.getLabel()));
		assertEquals(REDCapApiFormat.JSON.getLabel(), dataMap.get(REDCapApiParameter.FORMAT.getLabel()));

		assertTrue(dataMap.keySet().contains(REDCapApiParameter.RETURN_FORMAT.getLabel()));
		assertEquals(REDCapApiFormat.JSON.getLabel(), dataMap.get(REDCapApiParameter.RETURN_FORMAT.getLabel()));
		
		assertTrue(dataMap.keySet().contains(REDCapApiParameter.CONTENT.getLabel()));
		assertEquals(REDCapApiContent.VERSION.getLabel(), dataMap.get(REDCapApiParameter.CONTENT.getLabel()));

		assertTrue(dataMap.keySet().contains(REDCapApiParameter.TOKEN.getLabel()));
		assertEquals(TEST_TOKEN, dataMap.get(REDCapApiParameter.TOKEN.getLabel()));
	}

	@Test
	public void testToFormData_withIntegerSet() throws JavaREDCapException {
		Set<Integer> testArms = Set.of(Integer.valueOf(101), Integer.valueOf(201), Integer.valueOf(301));

		testParams.setArms(testArms, false);
		
		String data = testParams.toWwwFormUrlencoded();
		
		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.ARMS.getLabel() + "[]";

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertTrue(dataMap.get(fieldKey) instanceof Set<?>);

		@SuppressWarnings("unchecked")
		Set<String> armSet = (Set<String>) dataMap.get(fieldKey);
		MatcherAssert.assertThat(armSet, Matchers.hasSize(testArms.size()));

		for (Integer arm : testArms) {
			MatcherAssert.assertThat(armSet, Matchers.hasItem(arm.toString()));
		}
	}

	@Test
	public void testToFormData_withStringSet() throws JavaREDCapException {
		Set<String> testEvents = Set.of("initial_visit", "annual_visit_1", "annual_visit_2", "annual_visit_3");

		testParams.setEvents(testEvents, false);
		
		String data = testParams.toWwwFormUrlencoded();
		
		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.EVENTS.getLabel() + "[]";

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertTrue(dataMap.get(fieldKey) instanceof Set<?>);

		@SuppressWarnings("unchecked")
		Set<String> eventSet = (Set<String>) dataMap.get(fieldKey);
		MatcherAssert.assertThat(eventSet, Matchers.hasSize(testEvents.size()));

		for (String event : testEvents) {
			MatcherAssert.assertThat(eventSet, Matchers.hasItem(event));
		}
	}
	
	@Test
	public void testToFormData_withBooleanTrue() throws JavaREDCapException {
		boolean exportDags = true;

		testParams.setExportDataAccessGroups(exportDags);

		String data = testParams.toWwwFormUrlencoded();

		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS.getLabel();

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertEquals(Boolean.valueOf(exportDags).toString(), dataMap.get(fieldKey));
	}

	@Test
	public void testToFormData_withBooleanFalse() throws JavaREDCapException {
		Boolean exportCheckboxLabel = false;

		testParams.setExportCheckboxLabel(exportCheckboxLabel);

		String data = testParams.toWwwFormUrlencoded();

		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.EXPORT_CHECKBOX_LABEL.getLabel();

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertEquals(exportCheckboxLabel.toString(), dataMap.get(fieldKey));
	}

	@Test
	public void testToFormData_withInteger() throws JavaREDCapException {
		Integer repeatInstance = 25;

		testParams.setRepeatInstance(repeatInstance);

		String data = testParams.toWwwFormUrlencoded();

		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.REPEAT_INSTANCE.getLabel();

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertEquals(repeatInstance.toString(), dataMap.get(fieldKey));
	}

	@Test
	public void testToFormData_withNullValue() throws JavaREDCapException {
		Map<REDCapApiParameter, Object> map = new HashMap<>(testDataMap);
		map.put(REDCapApiParameter.RECORDS, null);

		REDCapApiRequest params = new REDCapApiRequest(map);
		
		String data = params.toWwwFormUrlencoded();

		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = REDCapApiParameter.RECORDS.getLabel();

		assertFalse(dataMap.keySet().contains(fieldKey));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> formStrToMap(String formStr) {
		Map<String, Object> map = new HashMap<String, Object>();

		String[] pairs = formStr.split("\\&");

		try {
			for (String pair : pairs) {
				String[] fields = pair.split("=");
				
				String name = URLDecoder.decode(fields[0], "UTF-8");
				String value = URLDecoder.decode(fields[1], "UTF-8");
				
				if (map.containsKey(name)) {
					Object mapValue = map.get(name);

					if (mapValue instanceof String) {
						// Convert map entry from string to list
						Set<String> strList = new HashSet<String>();
						
						strList.add((String)mapValue);	// add original value
						strList.add(value);				// add new value

						map.put(name, strList);			// replace the string with the list in the map
					}
					else {
						((Set<String>)mapValue).add(value);
					}
				}
				else {
					map.put(name, value);
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			fail("Exception parsing form data: " + e.getMessage());
		}

		return map;
	}
}
