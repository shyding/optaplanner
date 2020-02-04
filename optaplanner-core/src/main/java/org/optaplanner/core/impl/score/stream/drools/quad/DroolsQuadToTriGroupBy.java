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

package org.optaplanner.core.impl.score.stream.drools.quad;

import org.optaplanner.core.api.function.QuadFunction;
import org.optaplanner.core.api.score.stream.quad.QuadConstraintCollector;
import org.optaplanner.core.impl.score.stream.drools.common.DroolsAbstractGroupBy;
import org.optaplanner.core.impl.score.stream.drools.common.GroupByAccumulator;
import org.optaplanner.core.impl.score.stream.drools.common.QuadTuple;
import org.optaplanner.core.impl.score.stream.drools.common.TriTuple;

final class DroolsQuadToTriGroupBy<A, B, C, D, NewA, NewB, NewC>
        extends DroolsAbstractGroupBy<QuadTuple<A, B, C, D>, TriTuple<NewA, NewB, NewC>> {

    private final QuadFunction<A, B, C, D, NewA> groupKeyAMapping;
    private final QuadFunction<A, B, C, D, NewB> groupKeyBMapping;
    private final QuadConstraintCollector<A, B, C, D, ?, NewC> collector;

    public DroolsQuadToTriGroupBy(QuadFunction<A, B, C, D, NewA> groupKeyAMapping,
            QuadFunction<A, B, C, D, NewB> groupKeyBMapping, QuadConstraintCollector<A, B, C, D, ?, NewC> collector) {
        this.groupKeyAMapping = groupKeyAMapping;
        this.groupKeyBMapping = groupKeyBMapping;
        this.collector = collector;
    }

    @Override
    protected GroupByAccumulator<QuadTuple<A, B, C, D>, TriTuple<NewA, NewB, NewC>> newAccumulator() {
        return new DroolsQuadToTriGroupByAccumulator<>(groupKeyAMapping, groupKeyBMapping, collector);
    }

}