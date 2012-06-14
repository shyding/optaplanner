/*
 * Copyright 2012 JBoss Inc
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

package org.drools.planner.core.heuristic.selector.move.composite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.drools.planner.core.heuristic.selector.Selector;
import org.drools.planner.core.heuristic.selector.cached.FixedSelectorProbabilityWeightFactory;
import org.drools.planner.core.heuristic.selector.move.DummyMoveSelector;
import org.drools.planner.core.heuristic.selector.move.MoveSelector;
import org.drools.planner.core.move.DummyMove;
import org.drools.planner.core.move.Move;
import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.phase.step.AbstractStepScope;
import org.drools.planner.core.solver.DefaultSolverScope;
import org.junit.Ignore;
import org.junit.Test;

import static org.drools.planner.core.testdata.util.PlannerAssert.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnionMoveSelectorTest {

    @Test
    public void originSelection() {
        ArrayList<MoveSelector> childMoveSelectorList = new ArrayList<MoveSelector>();
        childMoveSelectorList.add(new DummyMoveSelector(
                Arrays.<Move>asList(new DummyMove("a1"), new DummyMove("a2"), new DummyMove("a3"))));
        childMoveSelectorList.add(new DummyMoveSelector(
                Arrays.<Move>asList(new DummyMove("a4"), new DummyMove("a5"))));
        UnionMoveSelector moveSelector = new UnionMoveSelector(childMoveSelectorList, false);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        moveSelector.solvingStarted(solverScope);
        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        moveSelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getSolverPhaseScope()).thenReturn(phaseScopeA);
        moveSelector.stepStarted(stepScopeA1);

        assertEquals(false, moveSelector.isContinuous());
        assertEquals(false, moveSelector.isNeverEnding());
        assertEquals(5L, moveSelector.getSize());
        Iterator<Move> iterator = moveSelector.iterator();
        assertTrue(iterator.hasNext());
        assertCode("a1", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a2", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a3", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a4", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a5", iterator.next());
        assertFalse(iterator.hasNext());

        moveSelector.stepEnded(stepScopeA1);
        moveSelector.phaseEnded(phaseScopeA);
        moveSelector.solvingEnded(solverScope);
    }

    @Test
    public void randomSelection() {
        ArrayList<MoveSelector> childMoveSelectorList = new ArrayList<MoveSelector>();
        Map<Selector, Double> fixedProbabilityWeightMap = new HashMap<Selector, Double>();
        DummyMoveSelector moveSelector1 = new DummyMoveSelector(
                Arrays.<Move>asList(new DummyMove("a1"), new DummyMove("a2"), new DummyMove("a3")));
        childMoveSelectorList.add(moveSelector1);
        fixedProbabilityWeightMap.put(moveSelector1, 1000.0);
        DummyMoveSelector moveSelector2 = new DummyMoveSelector(
                Arrays.<Move>asList(new DummyMove("a4"), new DummyMove("a5")));
        childMoveSelectorList.add(moveSelector2);
        fixedProbabilityWeightMap.put(moveSelector2, 20.0);
        UnionMoveSelector moveSelector = new UnionMoveSelector(childMoveSelectorList, true,
                new FixedSelectorProbabilityWeightFactory(fixedProbabilityWeightMap));

        Random workingRandom = mock(Random.class);
        when(workingRandom.nextDouble()).thenReturn(1.0 / 1020.0, 1019.0 / 1020.0, 1000.0 / 1020.0, 0.0, 999.0 / 1020.0);

        DefaultSolverScope solverScope = mock(DefaultSolverScope.class);
        when(solverScope.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.solvingStarted(solverScope);
        AbstractSolverPhaseScope phaseScopeA = mock(AbstractSolverPhaseScope.class);
        when(phaseScopeA.getSolverScope()).thenReturn(solverScope);
        when(phaseScopeA.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.phaseStarted(phaseScopeA);
        AbstractStepScope stepScopeA1 = mock(AbstractStepScope.class);
        when(stepScopeA1.getSolverPhaseScope()).thenReturn(phaseScopeA);
        when(stepScopeA1.getWorkingRandom()).thenReturn(workingRandom);
        moveSelector.stepStarted(stepScopeA1);

        assertEquals(false, moveSelector.isContinuous());
        assertEquals(false, moveSelector.isNeverEnding()); // A union of ending MoveSelectors does end
        assertEquals(5L, moveSelector.getSize());
        Iterator<Move> iterator = moveSelector.iterator();
        assertTrue(iterator.hasNext());
        assertCode("a1", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a4", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a5", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a2", iterator.next());
        assertTrue(iterator.hasNext());
        assertCode("a3", iterator.next());
        assertFalse(iterator.hasNext());

        moveSelector.stepEnded(stepScopeA1);
        moveSelector.phaseEnded(phaseScopeA);
        moveSelector.solvingEnded(solverScope);
    }

}
