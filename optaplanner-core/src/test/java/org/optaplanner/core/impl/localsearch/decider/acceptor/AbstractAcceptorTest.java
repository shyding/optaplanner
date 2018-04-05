/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.localsearch.decider.acceptor;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.heuristic.move.Move;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchMoveScope;
import org.optaplanner.core.impl.localsearch.scope.LocalSearchStepScope;

import static org.mockito.Mockito.*;

public abstract class AbstractAcceptorTest {

    protected <Solution_> LocalSearchMoveScope<Solution_> buildMoveScope(
            LocalSearchStepScope<Solution_> stepScope, int score) {
        Move<Solution_> move = mock(Move.class);
        LocalSearchMoveScope<Solution_> moveScope = new LocalSearchMoveScope<>(stepScope, 0, move);
        moveScope.setScore(SimpleScore.valueOf(score));
        return moveScope;
    }

}
