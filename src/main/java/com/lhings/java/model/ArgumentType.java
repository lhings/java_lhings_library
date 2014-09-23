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

public enum ArgumentType {
	INTEGER, FLOAT, STRING, BOOLEAN, TIMESTAMP;
	
	public String toString(){
		switch(this){
		case INTEGER:
			return "integer";
		case FLOAT:
			return "float";
		case STRING:
			return "string";
		case BOOLEAN:
			return "boolean";
		case TIMESTAMP:
			return "timestamp";
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static ArgumentType getType(String stringRepresentationOfType){
		if(stringRepresentationOfType.equalsIgnoreCase("integer"))
			return INTEGER;
		else if (stringRepresentationOfType.equalsIgnoreCase("float"))
			return FLOAT;
		else if (stringRepresentationOfType.equalsIgnoreCase("string"))
			return STRING;
		else if (stringRepresentationOfType.equalsIgnoreCase("boolean"))
			return BOOLEAN;
		else if (stringRepresentationOfType.equalsIgnoreCase("timestamp"))
			return TIMESTAMP;
		else 
			return null;
	}
}
