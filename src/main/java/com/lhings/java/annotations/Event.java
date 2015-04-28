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


package com.lhings.java.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * This annotation is used to define the events of your device. By convention, it
 * should annotate fields of type String. You can make your device send events
 * defined with this annotation using the method {@link com.lhings.java.LhingsDevice#sendEvent(java.lang.String) LhingsDevice.sendEvent()}.
 * The name parameter is optional, if not provided the name of the field is used instead. Only alphanumeric characters
 * and underscore can be used for the name of the event.
 * 
 * If this event will be sent with a <a href = "http://support.lhings.com/Event-Payload.html">structured payload</a>, 
 * component_names and component_types arrays must be used to define its components. For instance, if the device is a car
 * that sends an event called "fuel_low", and its payload has two components, "distance_for_refuelling" of type integer and
 * "gps_position" of type geolocation, then the annotation will be defined like this:
 * <code>
 * @Event(name = "fuel_low", component_names = { "distance_for_refuelling", "gps_position" }, component_types = { "integer", "geolocation"})
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Event {
	String name() default "";
	String[] component_names() default {};
	String[] component_types() default {};
}
