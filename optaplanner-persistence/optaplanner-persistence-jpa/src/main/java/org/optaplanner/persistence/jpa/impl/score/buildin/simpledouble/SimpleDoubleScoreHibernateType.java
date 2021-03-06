/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.persistence.jpa.impl.score.buildin.simpledouble;

import org.hibernate.type.StandardBasicTypes;
import org.optaplanner.core.impl.score.buildin.simpledouble.SimpleDoubleScoreDefinition;
import org.optaplanner.persistence.jpa.impl.score.AbstractScoreHibernateType;

@SuppressWarnings("checkstyle:javadocstyle")
/**
 * @deprecated Double-based scores are deprecated as floating point numbers can't represent a decimal number correctly.
 */
@Deprecated(/* forRemoval = true */)
public class SimpleDoubleScoreHibernateType extends AbstractScoreHibernateType {

    public SimpleDoubleScoreHibernateType() {
        scoreDefinition = new SimpleDoubleScoreDefinition();
        type = StandardBasicTypes.DOUBLE;
    }

}
