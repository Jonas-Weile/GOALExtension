/*******************************************************************************
 * Copyright (c) 2010 Freemarker Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.dbgp.debugger.packet.sender;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.dltk.dbgp.internal.utils.Base64Helper;

public class DbgpXmlPacket {

	private String name;
	private Map<String, String> attributes = new HashMap<>();
	private String data;
	private boolean encodeData = true;
	private List<DbgpXmlPacket> elements = null;

	public DbgpXmlPacket(String name) {
		this.name = name;
	}

	public void addAttribute(String name, boolean value) {
		addAttribute(name, value + "");
	}

	public void addAttribute(String name, int value) {
		addAttribute(name, value + "");
	}

	public void addAttribute(String name, String value) {
		this.attributes.put(name, value);
	}

	public String getAttribute(String name) {
		return this.attributes.get(name);
	}

	public void removeAttribute(String name) {
		this.attributes.remove(name);
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		setData(data, false);
	}

	public void setData(String data, boolean encode) {
		this.data = data;
		this.encodeData = encode;
	}

	public String toXml() {
		StringWriter xml = new StringWriter();
		elementToXml(this, xml);
		return xml.toString();
	}

	@Override
	public String toString() {
		return toXml();
	}

	private static void elementToXml(DbgpXmlPacket packet, StringWriter xml) {
		try {
			final XMLOutputFactory xof = XMLOutputFactory.newInstance();
			final XMLStreamWriter xsw = xof.createXMLStreamWriter(xml);
			xsw.writeStartElement(packet.name);
			for (String name : packet.attributes.keySet()) {
				xsw.writeAttribute(name, packet.attributes.get(name));
			}
			xsw.writeCharacters("");
			xsw.flush();

			if (packet.data != null) {
				xml.write(packet.prepareData(packet.data));
			} else if (packet.elements != null) {
				for (DbgpXmlPacket child : packet.elements) {
					elementToXml(child, xml);
				}
			}

			xsw.writeEndElement();
			xsw.close();
		} catch (XMLStreamException e) {
			e.printStackTrace(); // TODO
		}
	}

	private String prepareData(String fData) {
		if (this.encodeData) {
			String encoded = Base64Helper.encodeString(fData);
			return "<![CDATA[" + encoded + "]]>";
		}
		return fData;
	}

	protected void addElement(DbgpXmlPacket packet) {
		if (this.elements == null) {
			this.elements = new LinkedList<>();
		}
		this.elements.add(packet);
	}

	protected DbgpXmlPacket addElement(String name) {
		DbgpXmlPacket packet = new DbgpXmlPacket(name);
		addElement(packet);
		return packet;
	}
}
