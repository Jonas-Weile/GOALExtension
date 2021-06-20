package swiprolog.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;

import org.junit.Test;

import krTools.language.Update;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;
import swiprolog.SwiPrologInterface;

public class TestKRInterfaceParser {
	private static final SourceInfoObject INFO = new SourceInfoObject("test", 1, 1, 1, 1);

	@Test
	public void testParseUpdate() throws Exception {
		new SwiPrologInterface();
		StringReader reader = new StringReader("on(a,b), on(b,c), on(c,table)");
		Parser parser = new KRInterfaceParser4(reader, INFO);
		Update update = parser.parseUpdate();

		assertEquals(",/2", update.getSignature());
		assertEquals("on(a,b) , on(b,c) , on(c,table)", update.toString());
	}

	@Test
	public void testParseUpdate_2() throws Exception {
		new SwiPrologInterface();
		StringReader reader = new StringReader("zone(ID, Name, X, Y, Neighbours)");
		Parser parser = new KRInterfaceParser4(reader, INFO);
		Update update = parser.parseUpdate();

		assertEquals("zone/5", update.getSignature());
		assertEquals("zone(ID,Name,X,Y,Neighbours)", update.toString());
	}

	// @Test FIXME: this fails atm
	public void parseLessEqualError() throws Exception {
		// X <= 3 is not correct because "<=" does not exist in prolog.
		new SwiPrologInterface();
		StringReader reader = new StringReader("X <= 3");
		Parser parser = new KRInterfaceParser4(reader, INFO);
		parser.parseQuery();
		for (SourceInfo e : parser.getErrors()) {
			System.out.println(e);
			assertFalse(e.toString().contains("basic term 'nothing'"));
		}
	}
}
