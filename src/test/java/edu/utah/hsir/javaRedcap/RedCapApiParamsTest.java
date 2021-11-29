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

import edu.utah.hsir.javaRedcap.enums.RedCapApiAction;
import edu.utah.hsir.javaRedcap.enums.RedCapApiContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiCsvDelimiter;
import edu.utah.hsir.javaRedcap.enums.RedCapApiDateFormat;
import edu.utah.hsir.javaRedcap.enums.RedCapApiDecimalCharacter;
import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.RedCapApiLogType;
import edu.utah.hsir.javaRedcap.enums.RedCapApiOverwriteBehavior;
import edu.utah.hsir.javaRedcap.enums.RedCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.RedCapApiReturnContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiType;

public class RedCapApiParamsTest {
	protected static final String TEST_TOKEN = "12345678901234567890123456789012";

	protected Map<String, Object> testDataMap = new HashMap<String, Object>();
	protected RedCapApiParams testParams;

	@Before
	public void setup() throws JavaRedcapException {
		testDataMap.clear();
		testDataMap.put(RedCapApiParams.TOKEN, TEST_TOKEN);
		testDataMap.put(RedCapApiParams.CONTENT, RedCapApiContent.VERSION);
		testDataMap.put(RedCapApiParams.FORMAT, RedCapApiFormat.JSON);
		
		testParams = new RedCapApiParams(testDataMap);
	}

	@Test
	public void testCtor_nullMap() {
    	assertThrows(NullPointerException.class, () -> {
    		new RedCapApiParams(null);
    	});
	}

	@Test
	public void testCtor_emptyMap() {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams(new HashMap<String, Object>());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}

	@Test
	public void testCtor_baseMap_MissingFormat() throws JavaRedcapException {
		testDataMap.remove(RedCapApiParams.FORMAT);

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams(testDataMap);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}	

	@Test
	public void testCtor_baseMap_MissingContent() throws JavaRedcapException {
		testDataMap.remove(RedCapApiParams.CONTENT);

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams(testDataMap);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("missing"));
	}	

	@Test
	public void testCtor_baseMap() throws JavaRedcapException {
		RedCapApiParams data = new RedCapApiParams(testDataMap);
		
		assertNotNull(data);
		assertEquals(testDataMap.size() + 1, data.size());

		// the entries from the map pass in should all be present
		for (Entry<String, Object> entry : testDataMap.entrySet()) {
			Object value = data.get(entry.getKey());
			assertNotNull(value);
			assertEquals(entry.getValue(), value);
		}

		// the return format should be set
		assertNotNull(data.get(RedCapApiParams.RETURN_FORMAT));
		assertEquals(RedCapApiFormat.JSON, data.get(RedCapApiParams.RETURN_FORMAT));
		
		assertNotNull(data.errorHandler);
	}
	
	@Test
	public void testCtor_nullParams() {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams (null, null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_emptyToken() {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams ("  ", null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_nullContent() {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCapApiParams (TEST_TOKEN, null, null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("content"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
	}

	@Test
	public void testCtor_nullHandler() throws JavaRedcapException {
    	RedCapApiParams data = new RedCapApiParams (TEST_TOKEN, RedCapApiContent.FORM_EVENT_MAPPING, null);
    	
    	assertNotNull(data.errorHandler);
	}

	@Test
	public void testCtor_withHandler() throws JavaRedcapException {
		ErrorHandlerInterface handler = Mockito.mock(ErrorHandlerInterface.class);

		RedCapApiParams data = new RedCapApiParams (TEST_TOKEN, RedCapApiContent.ARM, handler);
    	
		assertSame(handler, data.errorHandler);
	}

	@Test
	public void testCtor_goodParams() throws JavaRedcapException {
		String testToken = new StringBuilder(TEST_TOKEN).reverse().toString();

		RedCapApiParams data = new RedCapApiParams (testToken, RedCapApiContent.EVENT, null);
		
		assertNotNull(data);
		assertEquals(4, data.size());	// expect token, content, format & returnFormat
		
		assertEquals(testToken, data.get(RedCapApiParams.TOKEN));
		assertEquals(RedCapApiContent.EVENT, data.get(RedCapApiParams.CONTENT));
		assertEquals(RedCapApiFormat.JSON, data.get(RedCapApiParams.FORMAT));
		assertEquals(RedCapApiFormat.JSON, data.get(RedCapApiParams.RETURN_FORMAT));
		assertNotNull(data.errorHandler);
	}

	@Test
	public void testAddFormat_nullParam() {
		int preSize = testParams.legalFormats.size();
		
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		testParams.addFormat(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

    	assertEquals(preSize, testParams.legalFormats.size());
	}

	@Test
	public void testAddFormat_defaultFormat() throws JavaRedcapException {
		int preSize = testParams.legalFormats.size();
		
		testParams.addFormat(RedCapApiFormat.JSON);
    	
    	assertEquals(preSize, testParams.legalFormats.size());
	}

	@Test
	public void testAddFormat_newFormat() throws JavaRedcapException {
		int preSize = testParams.legalFormats.size();
		
		testParams.addFormat(RedCapApiFormat.ODM);
    	
    	assertEquals(preSize + 1, testParams.legalFormats.size());
    	assertNotNull(testParams.legalFormats.contains(RedCapApiFormat.ODM));
	}

	@Test
	public void testSetAction_nullParam() {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		testParams.setAction(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("action"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.ACTION));
	}

	@Test
	public void testSetAction() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ACTION));

		testParams.setAction(RedCapApiAction.IMPORT);
		
		assertEquals(RedCapApiAction.IMPORT, testParams.get(RedCapApiParams.ACTION));
	}

	@Test
	public void testSetAllRecords() {
		assertNull(testParams.get(RedCapApiParams.ALL_RECORDS));
		
		testParams.setAllRecords(false);

		// Parameter is only used when set to true
		assertNull(testParams.get(RedCapApiParams.ALL_RECORDS));
		
		testParams.setAllRecords(true);
		
		assertTrue((boolean)testParams.get(RedCapApiParams.ALL_RECORDS));
	}

	@Test
	public void testSetArm_nullParam() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARM));

		testParams.setArm(null);
    	
    	assertNull(testParams.get(RedCapApiParams.ARM));
	}

	@Test
	public void testSetArm_negativeValue() {
		assertNull(testParams.get(RedCapApiParams.ARM));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setArm(-19);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arm"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
    	
    	assertNull(testParams.get(RedCapApiParams.ARM));
	}

	@Test
	public void testSetArm_goodValue() throws JavaRedcapException {
		int testArm = 5;

		assertNull(testParams.get(RedCapApiParams.ARM));

		testParams.setArm(testArm);
    	
    	assertNotNull(testParams.get(RedCapApiParams.ARM));
    	assertEquals(testArm, (int)testParams.get(RedCapApiParams.ARM));
	}

	@Test
	public void testSetArms_nullSet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARMS));

		// no arms provided but not required
		testParams.setArms(null, false);

    	assertNull(testParams.get(RedCapApiParams.ARMS));
		
    	// no arms provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setArms(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arms"));
    	
    	assertNull(testParams.get(RedCapApiParams.ARMS));
	}

	@Test
	public void testSetArms_emptyArms() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARMS));

		// no arms provided but not required
		testParams.setArms(new HashSet<Integer>(), false);

    	assertNull(testParams.get(RedCapApiParams.ARMS));
		
    	// no arms provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setArms(new HashSet<Integer>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("arms"));
    	
    	assertNull(testParams.get(RedCapApiParams.ARMS));
	}

	@Test
	public void testSetArms_negativeValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(1, 2, -4));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setArms(testArms, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
    	
    	assertNull(testParams.get(RedCapApiParams.ARMS));
	}

	@Test
	public void testSetArms_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(1, 2, null));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setArms(testArms, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.ARMS));
	}

	@Test
	public void testSetArms_goodSet_notRequried() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.ARMS));
		
		Set<Integer> testArms = new HashSet<Integer>(Arrays.asList(3, 6, 9));

		testParams.setArms(testArms, false);
		
    	assertNotNull(testParams.get(RedCapApiParams.ARMS));
    	
    	@SuppressWarnings("unchecked")
		Set<Integer> retrievedArms = (Set<Integer>)testParams.get(RedCapApiParams.ARMS);
    	
    	assertEquals(testArms.size(), retrievedArms.size());
    	
    	for (Integer arm : retrievedArms) {
    		assertTrue(testArms.contains(arm));
    	}
	}
	
	@Test
	public void testCheckDateRangeArgument_nullValue() {
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.checkDateRangeArgument(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
	}
	
	@Test
	public void testCheckDateRangeArgument_blankValue() {
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.checkDateRangeArgument("");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
	}
	
	@Test
	public void testCheckDateRangeArgument_invalidFormat() {
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.checkDateRangeArgument("12/25/2020");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
	}
	
	@Test
	public void testCheckDateRangeArgument_validFormat() throws JavaRedcapException {
		String testDate = "2020-01-31 00:00:00";
		String result = testParams.checkDateRangeArgument(testDate);
		
		assertEquals(testDate, result);
	}
	
	@Test
	public void testSetBeginTime_nullValue() {
		assertNull(testParams.get(RedCapApiParams.BEGIN_TIME));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setBeginTime(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));

		assertNull(testParams.get(RedCapApiParams.BEGIN_TIME));
	}
	
	@Test
	public void testSetBeginTime_invalidFormat() {
		assertNull(testParams.get(RedCapApiParams.BEGIN_TIME));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setBeginTime("2/2/2002");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

		assertNull(testParams.get(RedCapApiParams.BEGIN_TIME));
	}
	
	@Test
	public void testSetBeginTime_validFormat() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.BEGIN_TIME));

		String testDate = "2017-07-31 00:15:00";
		testParams.setBeginTime(testDate);

		assertEquals(testDate, (String)testParams.get(RedCapApiParams.BEGIN_TIME));
	}
	
	@Test
	public void testSetEndTime_nullValue() {
		assertNull(testParams.get(RedCapApiParams.END_TIME));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEndTime(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));

		assertNull(testParams.get(RedCapApiParams.END_TIME));
	}
	
	@Test
	public void testSetEndTime_invalidFormat() {
		assertNull(testParams.get(RedCapApiParams.END_TIME));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEndTime("1/5/2004");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("date"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

		assertNull(testParams.get(RedCapApiParams.END_TIME));
	}
	
	@Test
	public void testEndBeginTime_validFormat() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.END_TIME));

		String testDate = "2014-02-28 14:00:00";
		testParams.setEndTime(testDate);

		assertEquals(testDate, (String)testParams.get(RedCapApiParams.END_TIME));
	}

	@Test
	public void testSetCompactDisplay() {
		assertNull(testParams.get(RedCapApiParams.COMPACT_DISPLAY));
		
		testParams.setCompactDisplay(false);

		// Parameter is only used when set to true
		assertNull(testParams.get(RedCapApiParams.COMPACT_DISPLAY));

		testParams.setCompactDisplay(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.COMPACT_DISPLAY));
	}

	@Test
	public void testSetContent_nullValue() {
		Object currentContent = testParams.get(RedCapApiParams.CONTENT);
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setContent(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertEquals(currentContent, testParams.get(RedCapApiParams.CONTENT));
	}
	
	@Test
	public void testSetConent_validValue() throws JavaRedcapException {
		assertNotEquals(RedCapApiContent.INSTRUMENT, testParams.get(RedCapApiParams.CONTENT));
		
		testParams.setContent(RedCapApiContent.INSTRUMENT);
		
		assertEquals(RedCapApiContent.INSTRUMENT, testParams.get(RedCapApiParams.CONTENT));
	}
	
	@Test
	public void testSetCsvDelimiter_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.CSV_DELIMITER));

		testParams.setCsvDelimiter(null);
    	
		assertEquals(RedCapApiCsvDelimiter.COMMA, testParams.get(RedCapApiParams.CSV_DELIMITER));
	}

	@Test
	public void testSetCsvDelimiter_validValue() throws JavaRedcapException {
		RedCapApiCsvDelimiter testDelimiter = RedCapApiCsvDelimiter.CARAT;

		assertNull(testParams.get(RedCapApiParams.CSV_DELIMITER));

		testParams.setCsvDelimiter(testDelimiter);

		assertEquals(testDelimiter, (RedCapApiCsvDelimiter)testParams.get(RedCapApiParams.CSV_DELIMITER));
	}

	@Test
	public void testSetDag_nullValue() {
		assertNull(testParams.get(RedCapApiParams.DAG));

		testParams.setDag(null);

		assertNull(testParams.get(RedCapApiParams.DAG));
	}

	@Test
	public void testSetDag_blankValue() {
		assertNull(testParams.get(RedCapApiParams.DAG));

		testParams.setDag(" ");

		assertNull(testParams.get(RedCapApiParams.DAG));
	}

	@Test
	public void testSetDag_validValue() throws JavaRedcapException {
		String testDag = " some_dag ";

		assertNull(testParams.get(RedCapApiParams.DAG));

		testParams.setDag(testDag);

		assertEquals(testDag.trim(), (String)testParams.get(RedCapApiParams.DAG));
	}

	@Test
	public void testSetDags_nullDags() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DAGS));

		// no dags provided but not required
		testParams.setDags(null, false);

    	assertNull(testParams.get(RedCapApiParams.DAGS));
		
    	// no dags provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDags(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("dags"));
    	
    	assertNull(testParams.get(RedCapApiParams.DAGS));
	}

	@Test
	public void testSetDags_emptyDags() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DAGS));

		// no dags provided but not required
		testParams.setDags(new HashSet<String>(), false);

    	assertNull(testParams.get(RedCapApiParams.DAGS));
		
    	// no dags provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDags(new HashSet<String>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("dags"));
    	
    	assertNull(testParams.get(RedCapApiParams.DAGS));
	}

	@Test
	public void testSetDags_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", null));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDags(testDags, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.DAGS));
	}

	@Test
	public void testSetDags_blankValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", ""));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDags(testDags, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.get(RedCapApiParams.DAGS));
	}

	@Test
	public void testSetDags_goodSet_notRequried() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DAGS));
		
		Set<String> testDags = new HashSet<String>(Arrays.asList("dag_1", "dag_2", "dag_3"));

		testParams.setDags(testDags, false);
		
    	assertNotNull(testParams.get(RedCapApiParams.DAGS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedDags = (Set<String>)testParams.get(RedCapApiParams.DAGS);
    	
    	assertEquals(testDags.size(), retrievedDags.size());
    	
    	for (String dag : retrievedDags) {
    		assertTrue(testDags.contains(dag));
    	}
	}

	@Test
	public void testSetData_nullValue() {
		assertNull(testParams.get(RedCapApiParams.DATA));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setData(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.DATA));
	}
	
	@Test
	public void testSetData() throws JavaRedcapException {
		String testData = "[{\"recordid\": \"1\"}]";

		assertNull(testParams.get(RedCapApiParams.DATA));
		
		testParams.setData(testData);

		assertSame(testData, testParams.get(RedCapApiParams.DATA));
	}

	@Test
	public void testSetDateFormat_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DATE_FORMAT));
		
		testParams.setDateFormat(null);
		
		assertEquals(RedCapApiDateFormat.YMD, testParams.get(RedCapApiParams.DATE_FORMAT));
	}

	@Test
	public void testSetDateFormat_goodValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DATE_FORMAT));
		
		testParams.setDateFormat(RedCapApiDateFormat.MDY);
		
		assertEquals(RedCapApiDateFormat.MDY, testParams.get(RedCapApiParams.DATE_FORMAT));
	}

	@Test
	public void testSetDateRangeBegin_nullValue() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeBegin(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeBegin_blankValue() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeBegin("                 ");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeBegin_invalidFormat() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeBegin("April 1, 2000 11:00pm");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
	}


	@Test
	public void testSetDateRangeBegin_validFormat() throws JavaRedcapException {
		String testDate = "1999-12-31 23:59:59";

		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
		
		testParams.setDateRangeBegin(testDate);
    	
    	assertEquals(testDate, (String)testParams.get(RedCapApiParams.DATE_RANGE_BEGIN));
	}

	@Test
	public void testSetDateRangeEnd_nullValue() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeEnd(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_blankValue() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeEnd("");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_invalidFormat() {
		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDateRangeEnd("March 23, 1976 1:00am");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));

    	assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
	}

	@Test
	public void testSetDateRangeEnd_validFormat() throws JavaRedcapException {
		String testDate = "1972-08-23 03:09:19";

		assertNull(testParams.get(RedCapApiParams.DATE_RANGE_END));
		
		testParams.setDateRangeEnd(testDate);
    	
    	assertEquals(testDate, (String)testParams.get(RedCapApiParams.DATE_RANGE_END));
	}
	
	@Test
	public void testSetDecimalCharacter_nullValue() {
		assertNull(testParams.get(RedCapApiParams.DECIMAL_CHARACTER));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setDecimalCharacter(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertNull(testParams.get(RedCapApiParams.DECIMAL_CHARACTER));
	}
	
	@Test
	public void testSetDecimalCharacter_goodValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.DECIMAL_CHARACTER));
		
		testParams.setDecimalCharacter(RedCapApiDecimalCharacter.PERIOD);
    	
		assertEquals(RedCapApiDecimalCharacter.PERIOD, testParams.get(RedCapApiParams.DECIMAL_CHARACTER));
	}


	@Test
	public void testSetEvent_null() {
		assertNull(testParams.get(RedCapApiParams.EVENT));

		testParams.setEvent(null);
    	
    	assertNull(testParams.get(RedCapApiParams.EVENT));
	}

	@Test
	public void testSetEvent_blankValue() {
		assertNull(testParams.get(RedCapApiParams.EVENT));

		testParams.setEvent(" ");
    	
    	assertNull(testParams.get(RedCapApiParams.EVENT));
	}

	@Test
	public void testSetEvent_goodValue() {
		String testEvent = "initial_visit";

		assertNull(testParams.get(RedCapApiParams.EVENT));

		testParams.setEvent(testEvent);
    	
    	assertEquals(testEvent, (String)testParams.get(RedCapApiParams.EVENT));
	}

	@Test
	public void testSetEvents_nullEvents() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.EVENTS));

		// no events provided but not required
		testParams.setEvents(null, false);

    	assertNull(testParams.get(RedCapApiParams.EVENTS));
		
    	// no events provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEvents(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("events"));
    	
    	assertNull(testParams.get(RedCapApiParams.EVENTS));
	}

	@Test
	public void testSetEvents_emptyEvents() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.EVENTS));

		// no events provided but not required
		testParams.setEvents(new HashSet<String>(), false);

    	assertNull(testParams.get(RedCapApiParams.EVENTS));
		
    	// no events provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEvents(new HashSet<String>(), true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("events"));
    	
    	assertNull(testParams.get(RedCapApiParams.EVENTS));
	}

	@Test
	public void testSetEvents_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("annual_visit_1", null));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEvents(testEvents, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.EVENTS));
	}

	@Test
	public void testSetEvents_blankValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("annual_visit_1", " "));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setEvents(testEvents, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.get(RedCapApiParams.EVENTS));
	}

	@Test
	public void testSetEvents_goodSet_notRequried() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.EVENTS));
		
		Set<String> testEvents = new HashSet<String>(Arrays.asList("initial_visit", "annual_visit"));

		testParams.setEvents(testEvents, false);
		
    	assertNotNull(testParams.get(RedCapApiParams.EVENTS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedEvents = (Set<String>)testParams.get(RedCapApiParams.EVENTS);
    	
    	assertEquals(testEvents.size(), retrievedEvents.size());
    	
    	for (String event : retrievedEvents) {
    		assertTrue(testEvents.contains(event));
    	}
	}

	@Test
	public void testSetExportCheckboxLabel() {
		assertNull(testParams.get(RedCapApiParams.EXPORT_CHECKBOX_LABEL));
		
		testParams.setExportCheckboxLabel(false);
		
		assertFalse((boolean)testParams.get(RedCapApiParams.EXPORT_CHECKBOX_LABEL));
		
		testParams.setExportCheckboxLabel(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.EXPORT_CHECKBOX_LABEL));
	}

	@Test
	public void testSetExportDataAccessGroups() {
		assertNull(testParams.get(RedCapApiParams.EXPORT_DATA_ACCESS_GROUPS));
		
		testParams.setExportDataAccessGroups(false);
		
		assertFalse((boolean)testParams.get(RedCapApiParams.EXPORT_DATA_ACCESS_GROUPS));
		
		testParams.setExportDataAccessGroups(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.EXPORT_DATA_ACCESS_GROUPS));
	}

	@Test
	public void testSetExportFiles() {
		assertNull(testParams.get(RedCapApiParams.EXPORT_FILES));
		
		testParams.setExportFiles(false);
		
		assertFalse((boolean)testParams.get(RedCapApiParams.EXPORT_FILES));
		
		testParams.setExportFiles(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.EXPORT_FILES));
	}

	@Test
	public void testSetExportSurveyFields() {
		assertNull(testParams.get(RedCapApiParams.EXPORT_SURVEY_FIELDS));
		
		testParams.setExportSurveryFields(false);
		
		assertFalse((boolean)testParams.get(RedCapApiParams.EXPORT_SURVEY_FIELDS));
		
		testParams.setExportSurveryFields(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.EXPORT_SURVEY_FIELDS));
	}

	@Test
	public void testSetField_nullValue_notRequired() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELD));
		
		testParams.setField(null, false);

		assertNull(testParams.get(RedCapApiParams.FIELD));
	}

	@Test
	public void testSetField_nullValue_required() {
		assertNull(testParams.get(RedCapApiParams.FIELD));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setField(null, true);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertNull(testParams.get(RedCapApiParams.FIELD));
	}

	@Test
	public void testSetField_blankValue_required() {
		assertNull(testParams.get(RedCapApiParams.FIELD));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setField("", true);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));

		assertNull(testParams.get(RedCapApiParams.FIELD));
	}

	@Test
	public void testSetField_validValue() throws JavaRedcapException {
		String testField = "test_field";

		assertNull(testParams.get(RedCapApiParams.FIELD));

		testParams.setField(testField, true);

		assertEquals(testField, testParams.get(RedCapApiParams.FIELD));
	}

	@Test
	public void testSetFields_nullFields() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELDS));

		// no events provided but not required
		testParams.setFields(null);

    	assertNull(testParams.get(RedCapApiParams.FIELDS));
	}

	@Test
	public void testSetFields_emptyEvents() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELDS));

		testParams.setFields(new HashSet<String>());

    	assertNull(testParams.get(RedCapApiParams.FIELDS));
	}

	@Test
	public void testSetFields_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELDS));

		Set<String> testFields = new HashSet<String>(Arrays.asList("recordid", "age", null));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFields(testFields);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.FIELDS));
	}

	@Test
	public void testSetFields_blankValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELDS));
		
		Set<String> testFields = new HashSet<String>(Arrays.asList("  ", "recordid", "dob"));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFields(testFields);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.get(RedCapApiParams.FIELDS));
	}

	@Test
	public void testSetEvents_goodSet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FIELDS));
		
		Set<String> testFields = new HashSet<String>(Arrays.asList("recordid", "age", "dob", "time_in_service"));

		testParams.setFields(testFields);
		
    	assertNotNull(testParams.get(RedCapApiParams.FIELDS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedFields = (Set<String>)testParams.get(RedCapApiParams.FIELDS);
    	
    	assertEquals(testFields.size(), retrievedFields.size());
    	
    	for (String field : retrievedFields) {
    		assertTrue(testFields.contains(field));
    	}
	}

	@Test
	public void testSetFile_nullValue() {
		assertNull(testParams.get(RedCapApiParams.FILE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFile(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no filename"));
    	
    	assertNull(testParams.get(RedCapApiParams.FILE));
	}

	@Test
	public void testSetFile_blankValue() {
		assertNull(testParams.get(RedCapApiParams.FILE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFile("   ");
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no filename"));
    	
    	assertNull(testParams.get(RedCapApiParams.FILE));
	}
	
	@Test
	public void testSetFile_missingFile() throws IOException {
	    File testFile = File.createTempFile("setfile_missing", null);
	    testFile.delete();

		assertNull(testParams.get(RedCapApiParams.FILE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFile(testFile.getAbsolutePath());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INPUT_FILE_NOT_FOUND, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("not"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("found"));
    	
    	assertNull(testParams.get(RedCapApiParams.FILE));
	}
	
	@Test
    public void testSetFile_fileUnreadable() throws IOException
    {
		// Skip the test on Windows
		Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));

	    File testFile = File.createTempFile("setfile_unreadable", null);
	    testFile.deleteOnExit();
	    testFile.setReadable(false);

		assertNull(testParams.get(RedCapApiParams.FILE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFile(testFile.getAbsolutePath());
    	});
    	
    	assertEquals(ErrorHandlerInterface.INPUT_FILE_UNREADABLE, exception.getCode());
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("unreadable"));
    	
    	assertNull(testParams.get(RedCapApiParams.FILE));
    }
	
	
	@Test
    public void testSetFile_goodFile() throws IOException, JavaRedcapException
    {
	    File testFile = File.createTempFile("setfile_goodfile", null);
	    testFile.deleteOnExit();

		assertNull(testParams.get(RedCapApiParams.FILE));
		
		testParams.setFile(testFile.getAbsolutePath());
    	
    	assertEquals(Paths.get(testFile.getAbsolutePath()), testParams.get(RedCapApiParams.FILE));
    }

	@Test
	public void testSetFilterLogic_nullValue() {
		assertNull(testParams.get(RedCapApiParams.FILTER_LOGIC));
		
		testParams.setFilterLogic(null);

		assertNull(testParams.get(RedCapApiParams.FILTER_LOGIC));
	}

	@Test
	public void testSetFilterLogic_blankValue() {
		assertNull(testParams.get(RedCapApiParams.FILTER_LOGIC));
		
		testParams.setFilterLogic("");

		assertNull(testParams.get(RedCapApiParams.FILTER_LOGIC));
	}

	@Test
	public void testSetFilterLogic_goodValue() {
		String testLogic = "[age]=\"25\"";

		assertNull(testParams.get(RedCapApiParams.FILTER_LOGIC));
		
		testParams.setFilterLogic(testLogic);

		assertEquals(testLogic, testParams.get(RedCapApiParams.FILTER_LOGIC));
	}
	
	@Test
	public void testSetForceAutoNumber() {
		assertNull(testParams.get(RedCapApiParams.FORCE_AUTO_NUMBER));
		
		testParams.setForceAutoNumber(false);

		assertFalse((boolean)testParams.get(RedCapApiParams.FORCE_AUTO_NUMBER));

		testParams.setForceAutoNumber(true);

		assertTrue((boolean)testParams.get(RedCapApiParams.FORCE_AUTO_NUMBER));
	}

	@Test
	public void testSetFormat() throws JavaRedcapException {
		Object currentFormat = testParams.get(RedCapApiParams.FORMAT);
		assertNotEquals(RedCapApiFormat.XML, currentFormat);

		testParams.setFormat(RedCapApiFormat.XML);
		
		assertEquals(RedCapApiFormat.XML, testParams.get(RedCapApiParams.FORMAT));

		testParams.setFormat(null);
		
		assertEquals(RedCapApiFormat.JSON, testParams.get(RedCapApiParams.FORMAT));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setFormat(RedCapApiFormat.ODM);
		});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("invalid"));
    	
    	testParams.addFormat(RedCapApiFormat.ODM);
		testParams.setFormat(RedCapApiFormat.ODM);
    	
		assertEquals(RedCapApiFormat.ODM, testParams.get(RedCapApiParams.FORMAT));
	}

	@Test
	public void testSetForms_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FORMS));
		
		testParams.setForms(null);
		
		assertNull(testParams.get(RedCapApiParams.FORMS));
	}

	@Test
	public void testSetForms_emptySet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FORMS));
		
		testParams.setForms(new HashSet<String>());
		
		assertNull(testParams.get(RedCapApiParams.FORMS));
	}

	@Test
	public void testSetForms_nullEntry() {
		assertNull(testParams.get(RedCapApiParams.FORMS));

		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "medical_history", null));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setForms(testForms);
		});

		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
		assertNull(testParams.get(RedCapApiParams.FORMS));
	}
	
	@Test
	public void testSetForms_blankValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FORMS));
		
		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "  ", "medical_history", null));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setForms(testForms);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.get(RedCapApiParams.FORMS));
	}

	@Test
	public void testSetForms_goodSet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.FORMS));
		
		Set<String> testForms = new HashSet<String>(Arrays.asList("demographics", "medical_history", "physical_activity"));

		testParams.setForms(testForms);
		
    	assertNotNull(testParams.get(RedCapApiParams.FORMS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedForms = (Set<String>)testParams.get(RedCapApiParams.FORMS);
    	
    	assertEquals(testForms.size(), retrievedForms.size());
    	
    	for (String form : retrievedForms) {
    		assertTrue(testForms.contains(form));
    	}
	}

	@Test
	public void testSetInstrument_nullValue_notRequired() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.INSTRUMENT));

		testParams.setInstrument(null, false);
    	
    	assertNull(testParams.get(RedCapApiParams.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_nullValue_required() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.INSTRUMENT));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setInstrument(null, true);
		});
    	
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
    	assertNull(testParams.get(RedCapApiParams.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_blankValue_notRequired() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.INSTRUMENT));

		testParams.setInstrument("     ", false);
    	
    	assertNull(testParams.get(RedCapApiParams.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_blankValue_required() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.INSTRUMENT));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setInstrument("   ", true);
		});
    	
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
		
    	assertNull(testParams.get(RedCapApiParams.INSTRUMENT));
	}

	@Test
	public void testSetInstrument_goodValue() throws JavaRedcapException {
		String testInstrument = "activities";

		assertNull(testParams.get(RedCapApiParams.INSTRUMENT));

		testParams.setInstrument(testInstrument, false);
    	
    	assertEquals(testInstrument, testParams.get(RedCapApiParams.INSTRUMENT));
	}

	@Test
	public void testSetLogType_nullValue() {
		assertNull(testParams.get(RedCapApiParams.LOGTYPE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setLogType(null);
		});
    	
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

    	assertNull(testParams.get(RedCapApiParams.LOGTYPE));
	}


	@Test
	public void testSetLogType() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.LOGTYPE));
		
		testParams.setLogType(RedCapApiLogType.RECORD);
		
		assertEquals(RedCapApiLogType.RECORD, testParams.get(RedCapApiParams.LOGTYPE));

		testParams.setLogType(RedCapApiLogType.USER);
		
		assertEquals(RedCapApiLogType.USER, testParams.get(RedCapApiParams.LOGTYPE));
	}

	@Test
	public void testSetOdm_nullValue() {
		assertNull(testParams.get(RedCapApiParams.ODM));
		
		testParams.setOdm(null);
		
		assertNull(testParams.get(RedCapApiParams.ODM));
	}

	@Test
	public void testSetOdm_blankValue() {
		assertNull(testParams.get(RedCapApiParams.ODM));

		testParams.setOdm(" ");
		
		assertNull(testParams.get(RedCapApiParams.ODM));
	}

	@Test
	public void testSetOdm_goodValue() {
		String testOdmStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

		assertNull(testParams.get(RedCapApiParams.ODM));
			
		testParams.setOdm(testOdmStr);
			
		assertEquals(testOdmStr, testParams.get(RedCapApiParams.ODM));
	}

	@Test
	public void testSetOverride() {
		assertNull(testParams.get(RedCapApiParams.OVERRIDE));
		
		testParams.setOverride(false);
		
		assertEquals(0, testParams.get(RedCapApiParams.OVERRIDE));

		testParams.setOverride(true);
		
		assertEquals(1, testParams.get(RedCapApiParams.OVERRIDE));
	}

	@Test
	public void testSetOverwriteBehavior() {
		assertNull(testParams.get(RedCapApiParams.OVERWRITE_BEHAVIOR));
		
		testParams.setOverwriteBehavior(null);
		
		assertEquals(RedCapApiOverwriteBehavior.NORMAL, testParams.get(RedCapApiParams.OVERWRITE_BEHAVIOR));
		
		testParams.setOverwriteBehavior(RedCapApiOverwriteBehavior.OVERWRITE);
		
		assertEquals(RedCapApiOverwriteBehavior.OVERWRITE, testParams.get(RedCapApiParams.OVERWRITE_BEHAVIOR));

		testParams.setOverwriteBehavior(RedCapApiOverwriteBehavior.NORMAL);
		
		assertEquals(RedCapApiOverwriteBehavior.NORMAL, testParams.get(RedCapApiParams.OVERWRITE_BEHAVIOR));
	}

	@Test
	public void testSetRawOrLabel() {
		assertNull(testParams.get(RedCapApiParams.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(null);
		
		assertEquals(RedCapApiRawOrLabel.RAW, testParams.get(RedCapApiParams.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(RedCapApiRawOrLabel.LABEL);
		
		assertEquals(RedCapApiRawOrLabel.LABEL, testParams.get(RedCapApiParams.RAW_OR_LABEL));
		
		testParams.setRawOrLabel(RedCapApiRawOrLabel.RAW);
		
		assertEquals(RedCapApiRawOrLabel.RAW, testParams.get(RedCapApiParams.RAW_OR_LABEL));
	}

	@Test
	public void testSetRawOrLabelHeaders() {
		assertNull(testParams.get(RedCapApiParams.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(null);
		
		assertEquals(RedCapApiRawOrLabel.RAW, testParams.get(RedCapApiParams.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(RedCapApiRawOrLabel.LABEL);
		
		assertEquals(RedCapApiRawOrLabel.LABEL, testParams.get(RedCapApiParams.RAW_OR_LABEL_HEADERS));
		
		testParams.setRawOrLabelHeaders(RedCapApiRawOrLabel.RAW);
		
		assertEquals(RedCapApiRawOrLabel.RAW, testParams.get(RedCapApiParams.RAW_OR_LABEL_HEADERS));
	}

	@Test
	public void testSetRecord_nullRecord() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORD));

		// no record provided but not required
		testParams.setRecord(null, false);

    	assertNull(testParams.get(RedCapApiParams.RECORD));
		
    	// no record provided but they are required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setRecord(null, true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("record"));
    	
    	assertNull(testParams.get(RedCapApiParams.RECORD));
	}

	@Test
	public void testSetRecord_emptyRecord() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORD));

		// no record provided but not required
		testParams.setRecord("  ", false);

    	assertNull(testParams.get(RedCapApiParams.RECORD));
		
    	// no record provided but is required
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setRecord("", true);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("record"));
    	
    	assertNull(testParams.get(RedCapApiParams.RECORD));
	}

	@Test
	public void testSetRecord_goodValue_notRequried() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORD));
		
		String testRecordId = "ABC123 ";

		testParams.setRecord(testRecordId, false);
    	
    	assertEquals(testRecordId.trim(), testParams.get(RedCapApiParams.RECORD));
	}

	@Test
	public void testSetRecord_goodValue_requried() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORD));
		
		String testRecordId = " XYZPDQ";

		testParams.setRecord(testRecordId, true);
    	
    	assertEquals(testRecordId.trim(), testParams.get(RedCapApiParams.RECORD));
	}

	@Test
	public void testSetRecords_nullSet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORDS));

		// no records provided
		testParams.setRecords(null);

    	assertNull(testParams.get(RedCapApiParams.RECORDS));
	}

	@Test
	public void testSetRecords_emptySet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORDS));

		// no records provided
		testParams.setRecords(new HashSet<String>());

    	assertNull(testParams.get(RedCapApiParams.RECORDS));
	}

	@Test
	public void testSetRecords_nullValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("ABC987", "BDE414", null));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setRecords(testRecordIds);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    	
    	assertNull(testParams.get(RedCapApiParams.RECORDS));
	}

	@Test
	public void testSetRecords_blankValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("jaf42", "k7sfa", " "));

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setRecords(testRecordIds);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    	
    	assertNull(testParams.get(RedCapApiParams.RECORDS));
	}

	@Test
	public void testSetRecords_goodSet() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RECORDS));
		
		Set<String> testRecordIds = new HashSet<String>(Arrays.asList("ab431", "kgh47", "afe898"));

		testParams.setRecords(testRecordIds);
		
    	assertNotNull(testParams.get(RedCapApiParams.RECORDS));
    	
    	@SuppressWarnings("unchecked")
		Set<String> retrievedRecordIds = (Set<String>)testParams.get(RedCapApiParams.RECORDS);
    	
    	assertEquals(testRecordIds.size(), retrievedRecordIds.size());
    	
    	for (String recordId : retrievedRecordIds) {
    		assertTrue(testRecordIds.contains(recordId));
    	}
	}

	@Test
	public void testSetRepeatInstance_nullParam() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.REPEAT_INSTANCE));
		
		testParams.setRepeatInstance(null);

		assertNull(testParams.get(RedCapApiParams.REPEAT_INSTANCE));
	}
	
	@Test
	public void testSetRepeatInstance_negativeParam() {
		assertNull(testParams.get(RedCapApiParams.REPEAT_INSTANCE));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setRepeatInstance(-3);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
		
		assertNull(testParams.get(RedCapApiParams.REPEAT_INSTANCE));
	}
	
	@Test
	public void testSetRepeatInstance_goodValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.REPEAT_INSTANCE));
		
		int testInstance = 4;
		
		testParams.setRepeatInstance(testInstance);
		
		assertEquals(testInstance, testParams.get(RedCapApiParams.REPEAT_INSTANCE));
	}


	@Test
	public void testSetReportId_nullParam() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.REPORT_ID));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setReportId(null);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));

		assertNull(testParams.get(RedCapApiParams.REPORT_ID));
	}
	
	@Test
	public void testSetReportId_negativeParam() {
		assertNull(testParams.get(RedCapApiParams.REPORT_ID));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setReportId(-7);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("negative"));
		
		assertNull(testParams.get(RedCapApiParams.REPORT_ID));
	}
	
	@Test
	public void testSetReportId_goodValue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.REPORT_ID));
		
		int testReportId = 5;
		
		testParams.setReportId(testReportId);
		
		assertEquals(testReportId, testParams.get(RedCapApiParams.REPORT_ID));
	}
	
	@Test
	public void testSetReturnContent_nullParam() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RETURN_CONTENT));
		
		testParams.setReturnContent(null, false);
		
		assertEquals(RedCapApiReturnContent.COUNT, testParams.get(RedCapApiParams.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_autoIds_forceFalse() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RETURN_CONTENT));
		
		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testParams.setReturnContent(RedCapApiReturnContent.AUTO_IDS, false);
    	});
    	
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("auto_ids"));
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("forceAutoNumber"));
		
		assertNull(testParams.get(RedCapApiParams.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_autoIds_forceTrue() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RETURN_CONTENT));
		
		testParams.setReturnContent(RedCapApiReturnContent.AUTO_IDS, true);
    	
		assertEquals(RedCapApiReturnContent.AUTO_IDS, testParams.get(RedCapApiParams.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnContent_goodValues() throws JavaRedcapException {
		assertNull(testParams.get(RedCapApiParams.RETURN_CONTENT));
		
		testParams.setReturnContent(RedCapApiReturnContent.COUNT, true);
    	
		assertEquals(RedCapApiReturnContent.COUNT, testParams.get(RedCapApiParams.RETURN_CONTENT));
		
		testParams.setReturnContent(RedCapApiReturnContent.IDS, true);
    	
		assertEquals(RedCapApiReturnContent.IDS, testParams.get(RedCapApiParams.RETURN_CONTENT));
	}
	
	@Test
	public void testSetReturnFormat() {
		RedCapApiFormat currentFormat = (RedCapApiFormat)testParams.get(RedCapApiParams.RETURN_FORMAT);
		
		assertNotEquals(RedCapApiFormat.XML, currentFormat);

		testParams.setReturnFormat(RedCapApiFormat.XML);

		assertEquals(RedCapApiFormat.XML, testParams.get(RedCapApiParams.RETURN_FORMAT));

		testParams.setReturnFormat(null);
		
		assertEquals(RedCapApiFormat.JSON, testParams.get(RedCapApiParams.RETURN_FORMAT));
		
		testParams.setReturnFormat(RedCapApiFormat.CSV);

		assertEquals(RedCapApiFormat.CSV, testParams.get(RedCapApiParams.RETURN_FORMAT));
		
		testParams.setReturnFormat(RedCapApiFormat.JSON);

		assertEquals(RedCapApiFormat.JSON, testParams.get(RedCapApiParams.RETURN_FORMAT));
	}

	@Test
	public void testSetReturnMetadataOnly() {
		assertNull(testParams.get(RedCapApiParams.RETURN_METADATA_ONLY));
		
		testParams.setReturnMetadataOnly(false);
		
		assertFalse((boolean)testParams.get(RedCapApiParams.RETURN_METADATA_ONLY));
		
		testParams.setReturnMetadataOnly(true);
		
		assertTrue((boolean)testParams.get(RedCapApiParams.RETURN_METADATA_ONLY));
	}

	@Test
	public void testSetType() {
		assertNull(testParams.get(RedCapApiParams.TYPE));
		
		testParams.setType(null);
		
		assertEquals(RedCapApiType.FLAT, testParams.get(RedCapApiParams.TYPE));
		
		testParams.setType(RedCapApiType.EAV);

		assertEquals(RedCapApiType.EAV, testParams.get(RedCapApiParams.TYPE));
		
		testParams.setType(RedCapApiType.FLAT);

		assertEquals(RedCapApiType.FLAT, testParams.get(RedCapApiParams.TYPE));
	}

	@Test
	public void testSetUser() {
		assertNull(testParams.get(RedCapApiParams.USER));
		
		testParams.setUser(null);
		
		assertNull(testParams.get(RedCapApiParams.USER));
		
		testParams.setUser("        ");
		
		assertNull(testParams.get(RedCapApiParams.USER));

		String testUser = "fred";
		
		testParams.setUser(testUser);
		
		assertEquals(testUser, testParams.get(RedCapApiParams.USER));
	}
	
	@Test
	public void testGetMediaType() throws JavaRedcapException {
		testParams.setFormat(RedCapApiFormat.JSON);
		assertEquals("application/json", testParams.getMediaType());

		testParams.setFormat(RedCapApiFormat.CSV);
		assertEquals("text/csv", testParams.getMediaType());
		
		testParams.setFormat(RedCapApiFormat.XML);
		assertEquals("application/xml", testParams.getMediaType());
		
		testParams.addFormat(RedCapApiFormat.ODM);
		testParams.setFormat(RedCapApiFormat.ODM);
		assertEquals("application/xml", testParams.getMediaType());
		
		testParams.addFormat(RedCapApiFormat.FILE);
		testParams.setFormat(RedCapApiFormat.FILE);
		assertEquals("text/plain", testParams.getMediaType());
	}

	@Test
	public void testToFormData_defaultTestParams() {
		String data = testParams.toFormData();
		
		Map<String, Object> dataMap = formStrToMap(data);
		
		assertTrue(dataMap.keySet().contains(RedCapApiParams.FORMAT));
		assertEquals(RedCapApiFormat.JSON.label, dataMap.get(RedCapApiParams.FORMAT));

		assertTrue(dataMap.keySet().contains(RedCapApiParams.RETURN_FORMAT));
		assertEquals(RedCapApiFormat.JSON.label, dataMap.get(RedCapApiParams.RETURN_FORMAT));
		
		assertTrue(dataMap.keySet().contains(RedCapApiParams.CONTENT));
		assertEquals(RedCapApiContent.VERSION.label, dataMap.get(RedCapApiParams.CONTENT));

		assertTrue(dataMap.keySet().contains(RedCapApiParams.TOKEN));
		assertEquals(TEST_TOKEN, dataMap.get(RedCapApiParams.TOKEN));
	}

	@Test
	public void testToFormData_withIntegerSet() throws JavaRedcapException {
		Set<Integer> testArms = Set.of(Integer.valueOf(101), Integer.valueOf(201), Integer.valueOf(301));

		testParams.setArms(testArms, false);
		
		String data = testParams.toFormData();
		
		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = RedCapApiParams.ARMS + "[]";

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
	public void testToFormData_withStringSet() throws JavaRedcapException {
		Set<String> testEvents = Set.of("initial_visit", "annual_visit_1", "annual_visit_2", "annual_visit_3");

		testParams.setEvents(testEvents, false);
		
		String data = testParams.toFormData();
		
		Map<String, Object> dataMap = formStrToMap(data);

		String fieldKey = RedCapApiParams.EVENTS + "[]";

		assertTrue(dataMap.keySet().contains(fieldKey));
		assertTrue(dataMap.get(fieldKey) instanceof Set<?>);

		@SuppressWarnings("unchecked")
		Set<String> eventSet = (Set<String>) dataMap.get(fieldKey);
		MatcherAssert.assertThat(eventSet, Matchers.hasSize(testEvents.size()));

		for (String event : testEvents) {
			MatcherAssert.assertThat(eventSet, Matchers.hasItem(event));
		}
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
