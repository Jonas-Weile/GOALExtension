package jasonkri;

import java.io.File;

import krTools.parser.SourceInfo;

/**
 * Fake SourceInfo for testing.
 * 
 * @author W.Pasman 10jun15
 *
 */
public class mySourceInfo implements krTools.parser.SourceInfo {

	@Override
	public File getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCharacterPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStartIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStopIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(SourceInfo o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
