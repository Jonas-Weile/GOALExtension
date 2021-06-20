package tuprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import krTools.language.Update;
import krTools.parser.Parser;

public class TestKRInterfaceParser {
	@Test
	public void testParseUpdate() throws Exception {
		StringReader reader = new StringReader("on(a,b), on(b,c), on(c,table)");
		Parser parser = new KRInterfaceParser4(reader, new SourceInfoObject(null, 0, 0, 0, 0));
		Update update = parser.parseUpdate();

		assertEquals(",/2", update.getSignature());

		// assertEquals("on(a,b) , on(b,c) , on(c,table)", update.toString());
		// FIXME
	}

	@Test
	public void testParseUpdate_2() throws Exception {
		StringReader reader = new StringReader("zone(ID, Name, X, Y, Neighbours)");
		Parser parser = new KRInterfaceParser4(reader, new SourceInfoObject(null, 0, 0, 0, 0));
		Update update = parser.parseUpdate();

		assertEquals("zone/5", update.getSignature());

		assertEquals("zone(ID,Name,X,Y,Neighbours)", update.toString());
	}

	// @Test
	// public void testParseDBFs() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testParseQuery() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testParseTerms() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testParseTerm() {
	// fail("Not yet implemented");
	// }
	//
	// @Test
	// public void testParseVar() {
	// fail("Not yet implemented");
	// }

}
