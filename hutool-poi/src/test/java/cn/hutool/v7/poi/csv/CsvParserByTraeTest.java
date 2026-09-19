/*
 * Copyright (c) 2026 Hutool Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hutool.v7.poi.csv;

import cn.hutool.v7.core.io.IORuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class CsvParserByTraeTest {
	private CsvParser csvParser;
	private CsvReadConfig config;

	@BeforeEach
	public void setUp() {
		config = CsvReadConfig.of();
	}

	@Test
	public void testBasicParsing() {
		final String csvData = "name,age,city\nJohn,25,NYC\nJane,30,LA";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals(3, row1.size());
		assertEquals("name", row1.get(0));
		assertEquals("age", row1.get(1));
		assertEquals("city", row1.get(2));

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals(3, row2.size());
		assertEquals("John", row2.get(0));
		assertEquals("25", row2.get(1));
		assertEquals("NYC", row2.get(2));

		final CsvRow row3 = csvParser.nextRow();
		assertNotNull(row3);
		assertEquals(3, row3.size());
		assertEquals("Jane", row3.get(0));
		assertEquals("30", row3.get(1));
		assertEquals("LA", row3.get(2));

		assertNull(csvParser.nextRow()); // End of data
	}

	@Test
	public void testWithCustomHeaderLine() {
		final String csvData = "John,25,NYC\nJane,30,LA\nBob,35,Chicago";
		//config.setHeaderLineNo(0); // First row is header
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals(3, row1.size());
		assertEquals("John", row1.get(0)); // This becomes the first data row
		assertEquals("25", row1.get(1)); // Second row
		assertEquals("NYC", row1.get(2)); // Third row

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals(3, row2.size());
		assertEquals("Jane", row2.get(0)); // This becomes the first data row
		assertEquals("30", row2.get(1)); // Second row
		assertEquals("LA", row2.get(2)); // Third row

		final CsvRow row3 = csvParser.nextRow();
		assertNotNull(row3);
		assertEquals(3, row3.size());
		assertEquals("Bob", row3.get(0)); // This becomes the first data row
		assertEquals("35", row3.get(1)); // Second row
		assertEquals("Chicago", row3.get(2)); // Third row

		assertNull(csvParser.nextRow()); // No more data
	}

	@Test
	public void testQuotedFields() {
		final String csvData = "\"name with,comma\",\"quoted \"\"word\"\"\",text\nJohn Doe,\"Jane \"\"Jo\"\" Bloggs\",simple";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
		assertEquals(3, header.size());
		assertEquals("name with,comma", header.get(0));
		assertEquals("quoted \"word\"", header.get(1));
		assertEquals("text", header.get(2));

		final CsvRow data = csvParser.nextRow();
		assertNotNull(data);
		assertEquals(3, data.size());
		assertEquals("John Doe", data.get(0));
		assertEquals("Jane \"Jo\" Bloggs", data.get(1));
		assertEquals("simple", data.get(2));

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testSkipEmptyRows() {
		config.setSkipEmptyRows(true);
		final String csvData = "name,age\nJohn,25\n\nJane,30\n\n";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
		assertEquals("name", header.get(0));
		assertEquals("age", header.get(1));

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals("John", row1.get(0));
		assertEquals("25", row1.get(1));

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals("Jane", row2.get(0));
		assertEquals("30", row2.get(1));

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testFieldWithNewlines() {
		final String csvData = "name,description\nJohn,\"A description\nwith multiple\nlines\"\nJane,Simple";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
		assertEquals("name", header.get(0));
		assertEquals("description", header.get(1));

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals("John", row1.get(0));
		assertEquals("A description\nwith multiple\nlines", row1.get(1));

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals("Jane", row2.get(0));
		assertEquals("Simple", row2.get(1));

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testDifferentFieldCountError() {
		config.setErrorOnDifferentFieldCount(true);
		final String csvData = "name,age\ntest\none,two,three";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);

		assertThrows(IORuntimeException.class, () -> {
			csvParser.nextRow();
		});
	}

	@Test
	public void testTrimFieldOption() {
		config.setTrimField(true);
		final String csvData = " name , age \n John , 25 ";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
		assertEquals("name", header.get(0));
		assertEquals("age", header.get(1));

		final CsvRow row = csvParser.nextRow();
		assertNotNull(row);
		assertEquals("John", row.get(0));
		assertEquals("25", row.get(1));
	}

	@Test
	public void testCommentLines() {
		config.setCommentCharacter('#');
		final String csvData = "# This is a comment\nname,age\nJohn,25\n# Another comment\nJane,30";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
		assertEquals("name", header.get(0));
		assertEquals("age", header.get(1));

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals("John", row1.get(0));
		assertEquals("25", row1.get(1));

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals("Jane", row2.get(0));
		assertEquals("30", row2.get(1));

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testRangeReading() {
		config.setBeginLineNo(1); // Start from second line
		config.setEndLineNo(2);   // End at third line
		final String csvData = "name,age,city\nJohn,25,NYC\nJane,30,LA\nBob,35,Chicago\nTom,40,Seattle";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow row1 = csvParser.nextRow();
		assertNotNull(row1);
		assertEquals("John", row1.get(0));
		assertEquals("25", row1.get(1));
		assertEquals("NYC", row1.get(2));

		final CsvRow row2 = csvParser.nextRow();
		assertNotNull(row2);
		assertEquals("Jane", row2.get(0));
		assertEquals("30", row2.get(1));
		assertEquals("LA", row2.get(2));

		assertNull(csvParser.nextRow()); // Should stop after line 2 (0-indexed)
	}

	@Test
	public void testEmptyInput() {
		final String csvData = "";
		csvParser = new CsvParser(new StringReader(csvData), config);

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testSingleRow() {
		final String csvData = "name,age";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow row = csvParser.nextRow();
		assertNotNull(row);
		assertEquals("name", row.get(0));
		assertEquals("age", row.get(1));

		assertNull(csvParser.nextRow());
	}

	@Test
	public void testCloseMethod() {
		final String csvData = "name,age\nJohn,25";
		csvParser = new CsvParser(new StringReader(csvData), config);

		final CsvRow header = csvParser.nextRow();
		assertNotNull(header);
	}

	@Test
	public void testGetHeaderWithoutHeaderParsing() {
		final String csvData = "name,age\nJohn,25";
		csvParser = new CsvParser(new StringReader(csvData), config);

		assertThrows(IllegalStateException.class, () -> {
			csvParser.getHeader();
		});
	}
}
