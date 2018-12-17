/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.constraint;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.director.ScoreDirector;

/**
 * Retrievable from {@link ScoreDirector#getConstraintMatchTotals()}.
 */
public final class ConstraintMatchTotal<Score_ extends Score<Score_>>
        implements Serializable, Comparable<ConstraintMatchTotal<Score_>> {

    private final String constraintPackage;
    private final String constraintName;
    private final Score_ constraintWeight;

    private final Set<ConstraintMatch<Score_>> constraintMatchSet;
    private Score_ score;

    /**
     * @param constraintPackage never null
     * @param constraintName never null
     * @param zeroScore never null
     */
    public ConstraintMatchTotal(String constraintPackage, String constraintName, Score_ zeroScore) {
        this(constraintPackage, constraintName, null, zeroScore);
    }

    /**
     * @param constraintPackage never null
     * @param constraintName never null
     * @param constraintWeight null if {@link ConstraintWeight} isn't used for this constraint
     * @param zeroScore never null
     */
    public ConstraintMatchTotal(String constraintPackage, String constraintName, Score_ constraintWeight, Score_ zeroScore) {
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
        constraintMatchSet = new LinkedHashSet<>();
        this.constraintWeight = constraintWeight;
        score = zeroScore;
    }

    /**
     * @return never null
     */
    public String getConstraintPackage() {
        return constraintPackage;
    }

    /**
     * @return never null
     */
    public String getConstraintName() {
        return constraintName;
    }

    /**
     * The value of the {@link ConstraintWeight} annotated member of the {@link ConstraintConfiguration}.
     * It's independent to the state of the {@link PlanningVariable planning variables}.
     * Do not confuse with {@link #getScore()}.
     * @return null if {@link ConstraintWeight} isn't used for this constraint
     */
    public Score_ getConstraintWeight() {
        return constraintWeight;
    }

    /**
     * @return never null
     */
    public Set<ConstraintMatch<Score_>> getConstraintMatchSet() {
        return constraintMatchSet;
    }

    /**
     * @return {@code >= 0}
     */
    public int getConstraintMatchCount() {
        return getConstraintMatchSet().size();
    }

    /**
     * Sum of the {@link #getConstraintMatchSet()}'s {@link ConstraintMatch#getScore()}.
     * @return never null
     */
    public Score_ getScore() {
        return score;
    }

    /**
     * @return never null
     * @deprecated in favor of {@link #getScore()}
     */
    @Deprecated
    public Score_ getScoreTotal() {
        return getScore();
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public ConstraintMatch<Score_> addConstraintMatch(List<Object> justificationList, Score_ score) {
        this.score = this.score.add(score);
        ConstraintMatch<Score_> constraintMatch = new ConstraintMatch<>(constraintPackage, constraintName,
                justificationList, score);
        boolean added = constraintMatchSet.add(constraintMatch);
        if (!added) {
            throw new IllegalStateException("The constraintMatchTotal (" + this
                    + ") could not add constraintMatch (" + constraintMatch
                    + ") to its constraintMatchSet (" + constraintMatchSet + ").");
        }
        return constraintMatch;
    }

    public void removeConstraintMatch(ConstraintMatch<Score_> constraintMatch) {
        score = score.subtract(constraintMatch.getScore());
        boolean removed = constraintMatchSet.remove(constraintMatch);
        if (!removed) {
            throw new IllegalStateException("The constraintMatchTotal (" + this
                    + ") could not remove constraintMatch (" + constraintMatch
                    + ") from its constraintMatchSet (" + constraintMatchSet + ").");
        }
    }

    // ************************************************************************
    // Infrastructure methods
    // ************************************************************************

    public String getConstraintId() {
        return constraintPackage + "/" + constraintName;
    }

    @Override
    public int compareTo(ConstraintMatchTotal<Score_> other) {
        if (!constraintPackage.equals(other.constraintPackage)) {
            return constraintPackage.compareTo(other.constraintPackage);
        } else if (!constraintName.equals(other.constraintName)) {
            return constraintName.compareTo(other.constraintName);
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ConstraintMatchTotal) {
            ConstraintMatchTotal<Score_> other = (ConstraintMatchTotal<Score_>) o;
            return constraintPackage.equals(other.constraintPackage)
                    && constraintName.equals(other.constraintName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ((17 * 37)
                + constraintPackage.hashCode()) * 37
                + constraintName.hashCode();
    }

    @Override
    public String toString() {
        return getConstraintId() + "=" + score;
    }

}
