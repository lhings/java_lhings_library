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
 * <code>@Payload</code> annotation is used to specify that the parameter of a method
 * annotated with <code>@Action</code> should carry the raw payload of the event. When 
 * the corresponding action is linked to an event through a rule, the library will store
 * the raw payload of the event in the parameter annotated with <code>@Payload</code>.
 * 
 * Currently for an action to receive payload it must have only one parameter and it
 * has to be annotated with payload. In this case the <code>argumentNames</code>
 * parameter of the {@link com.lhings.java.annotations.Action @Action} annotation
 * must be empty. <code>@Payload</code> annotation will not work as expected if the
 * method has multiple parameters.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Documented
public @interface Payload {
    
}
