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
 * <code>@StatusComponent</code> annotation is used to specify which fields of a
 * LhingsDevice instance are status components of the device. Currently only
 * fields of type int, float, double, boolean, String and java.util.Date (and
 * the corresponding wrapper types) can be annotated with this
 * <code>@StatusComponent</code>. The value of a field annotated in this way
 * will be automatically stored in Lhings when calling the method
 * {@link com.lhings.java.LhingsDevice#storeStatus() storeStatus()}, and its
 * value will be shown in the control panel of the device both in <a
 * href="http://www.lhings.com">Lhings web interface</a> and in Lhings Mobile apps
 * for <a href="https://play.google.com/store/apps/details?id=com.lyncos.lhingsmobile">Android</a>
 * and <a href="https://itunes.apple.com/us/app/lhings-mobile/id895099076?mt=8">iPhone</a>.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface StatusComponent {

    /**
     * The name of the status component. Only alphanumerical characters are allowed.
     * If not provided, it defaults to the name given to the field in Java code.
     * @return 
     */
    String name() default "";
}
