package org.eclipse.dltk.dbgp.debugger.packet.sender.response;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.debugger.IVariableAdder;

public class EvalPacket extends DbgpXmlResponsePacket implements IVariableAdder  {

	public EvalPacket(DbgpRequest command) {
		super(command);
		super.addAttribute("success", "1");
	}
	
	public void addProperty(PropertyPacket property) {
		super.addElement(property);
	}

	public void addVariable(String name, String type, String value) {
		addProperty(new PropertyPacket(name, type, value));		
	}
}

