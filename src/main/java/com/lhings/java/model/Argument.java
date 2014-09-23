/* Copyright 2014 Lyncos Technologies S. L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */


package com.lhings.java.model;

import java.io.Serializable;


/**
 * Entity implementation class for Entity: Argument
 *
 */

public class Argument implements Serializable {

	private String name;
	
	private String type;
	
//	private StateVar relatedStateVariable;
	
//	private Action action;

//	private String relatedStateVariableAsString;
	
	private static final long serialVersionUID = 1L;

	public Argument(String name, String type) {
		super();
		this.name = name;
		this.type = type;
//		this.relatedStateVariable = relatedStateVariable;
	}
	
//	public Argument(String name, String type, String relatedStateVariable) {
//		super();
//		this.name = name;
//		this.type = type;
//		this.relatedStateVariableAsString = relatedStateVariable;
//	}
	
	public Argument() {
		super();
	}   
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}   
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}   
}
