/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.device.binding.openhab.util.configgen.xmlpaser;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import nu.xom.Node;
import nu.xom.Nodes;

/**
 *
 * @author divine
 */
public class OverissueNodeException extends XMLParsingException {

	public OverissueNodeException(String nodeName, Nodes childNodes, Node parent, Exception e) {
		super("Expected one Node[" + nodeName + "] but found " + childNodes.size() + " childs of parent Element[" + parent.getBaseURI() + "].", parent.getBaseURI(), e);
	}

	public OverissueNodeException(String nodeName, Nodes childElements, Node parent) {
		super("Expected one Node[" + nodeName + "] but found " + childElements.size() + " childs of parent Element[" + parent.getBaseURI() + "].");
	}
}