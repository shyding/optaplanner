/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.holder;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.drools.core.common.AgendaItem;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;

/**
 * Abstract superclass for {@link ScoreHolder}.
 * @param <Score_> the {@link Score} type
 */
public abstract class AbstractScoreHolder<Score_ extends Score<Score_>> implements ScoreHolder<Score_>, Serializable {

    protected final boolean constraintMatchEnabled;
    protected final Map<String, ConstraintMatchTotal<Score_>> constraintMatchTotalMap;
    protected final Map<Object, Indictment<Score_>> indictmentMap;
    protected final Score_ zeroScore;

    protected AbstractScoreHolder(boolean constraintMatchEnabled, Score_ zeroScore) {
        this.constraintMatchEnabled = constraintMatchEnabled;
        // TODO Can we set the initial capacity of this map more accurately? For example: number of rules
        constraintMatchTotalMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        // TODO Can we set the initial capacity of this map more accurately by using entitySize?
        indictmentMap = constraintMatchEnabled ? new LinkedHashMap<>() : null;
        this.zeroScore = zeroScore;
    }

    @Override
    public boolean isConstraintMatchEnabled() {
        return constraintMatchEnabled;
    }

    @Override
    public Collection<ConstraintMatchTotal<Score_>> getConstraintMatchTotals() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        return constraintMatchTotalMap.values();
    }

    @Override
    public Map<Object, Indictment<Score_>> getIndictmentMap() {
        if (!isConstraintMatchEnabled()) {
            throw new IllegalStateException("When constraintMatchEnabled (" + isConstraintMatchEnabled()
                    + ") is disabled in the constructor, this method should not be called.");
        }
        return indictmentMap;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void configureConstraintWeight(Rule rule, Score_ constraintWeight) {
        if (constraintWeight.getInitScore() != 0) {
            throw new IllegalStateException("The initScore (" + constraintWeight.getInitScore() + ") must be 0.");
        }
        if (constraintMatchEnabled) {
            String constraintPackage = rule.getPackageName();
            String constraintName = rule.getName();
            String constraintId = constraintPackage + "/" + constraintName;
            constraintMatchTotalMap.put(constraintId,
                    new ConstraintMatchTotal<>(constraintPackage, constraintName, constraintWeight, zeroScore));
        }
    }

    protected void registerConstraintMatch(RuleContext kcontext,
            final Runnable constraintUndoListener, Supplier<Score_> scoreSupplier) {
        AgendaItem<?> agendaItem = (AgendaItem) kcontext.getMatch();
        ConstraintActivationUnMatchListener constraintActivationUnMatchListener
                = new ConstraintActivationUnMatchListener(constraintUndoListener);
        agendaItem.setCallback(constraintActivationUnMatchListener);
        if (constraintMatchEnabled) {
            List<Object> justificationList = extractJustificationList(kcontext);
            // Not needed in fast code: Add ConstraintMatch
            constraintActivationUnMatchListener.constraintMatchTotal = findConstraintMatchTotal(kcontext);
            ConstraintMatch<Score_> constraintMatch = constraintActivationUnMatchListener.constraintMatchTotal
                    .addConstraintMatch(justificationList, scoreSupplier.get());
            List<Indictment<Score_>> indictmentList = justificationList.stream()
                    .distinct() // One match might have the same justification twice
                    .map(justification -> {
                        Indictment<Score_> indictment = indictmentMap.computeIfAbsent(justification,
                                k -> new Indictment<>(justification, zeroScore));
                        indictment.addConstraintMatch(constraintMatch);
                        return indictment;
                    }).collect(Collectors.toList());
            constraintActivationUnMatchListener.constraintMatch = constraintMatch;
            constraintActivationUnMatchListener.indictmentList = indictmentList;
        }
    }

    private ConstraintMatchTotal<Score_> findConstraintMatchTotal(RuleContext kcontext) {
        Rule rule = kcontext.getRule();
        String constraintPackage = rule.getPackageName();
        String constraintName = rule.getName();
        String constraintId = constraintPackage + "/" + constraintName;
        return constraintMatchTotalMap.computeIfAbsent(constraintId,
                k -> new ConstraintMatchTotal<>(constraintPackage, constraintName, null, zeroScore));
    }

    protected List<Object> extractJustificationList(RuleContext kcontext) {
        // Unlike kcontext.getMatch().getObjects(), this includes the matches of accumulate and exists
        return ((org.drools.core.spi.Activation) kcontext.getMatch()).getObjectsDeep();
    }

    public class ConstraintActivationUnMatchListener implements Runnable {

        private final Runnable constraintUndoListener;

        private ConstraintMatchTotal<Score_> constraintMatchTotal;
        private List<Indictment<Score_>> indictmentList;
        private ConstraintMatch<Score_> constraintMatch;

        public ConstraintActivationUnMatchListener(Runnable constraintUndoListener) {
            this.constraintUndoListener = constraintUndoListener;
        }

        @Override
        public final void run() {
            constraintUndoListener.run();
            if (constraintMatchEnabled) {
                // Not needed in fast code: Remove ConstraintMatch
                constraintMatchTotal.removeConstraintMatch(constraintMatch);
                for (Indictment<Score_> indictment : indictmentList) {
                    indictment.removeConstraintMatch(constraintMatch);
                    if (indictment.getConstraintMatchSet().isEmpty()) {
                        indictmentMap.remove(indictment.getJustification());
                    }
                }
            }
        }

    }

}
