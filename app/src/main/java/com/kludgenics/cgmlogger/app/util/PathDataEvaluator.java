/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.kludgenics.cgmlogger.app.util;
import android.animation.TypeEvaluator;

    /**
      * PathDataEvaluator is used to interpolate between two paths which are
      * represented in the same format but different control points' values.
      * The path is represented as an array of PathDataNode here, which is
      * fundamentally an array of floating point numbers.
      */
    public class PathDataEvaluator implements TypeEvaluator<PathParser.PathDataNode[]> {
    private PathParser.PathDataNode[] mNodeArray;

    /**
     * Create a PathParser.PathDataNode[] that does not reuse the animated value.
     * Care must be taken when using this option because on every evaluation
     * a new <code>PathParser.PathDataNode[]</code> will be allocated.
     */
    private PathDataEvaluator() {
    }

    /**
     * Create a PathDataEvaluator that reuses <code>nodeArray</code> for every evaluate() call.
     * Caution must be taken to ensure that the value returned from
     * {@link android.animation.ValueAnimator#getAnimatedValue()} is not cached, modified, or
     * used across threads. The value will be modified on each <code>evaluate()</code> call.
     *
     * @param nodeArray The array to modify and return from <code>evaluate</code>.
     */
    public PathDataEvaluator(PathParser.PathDataNode[] nodeArray) {
        mNodeArray = nodeArray;
    }

    @Override
    public PathParser.PathDataNode[] evaluate(float fraction,
                                              PathParser.PathDataNode[] startPathData,
                                              PathParser.PathDataNode[] endPathData) {
        if (!PathParser.canMorph(startPathData, endPathData)) {
            throw new IllegalArgumentException("Can't interpolate between"
                    + " two incompatible pathData");
        }

        if (mNodeArray == null || !PathParser.canMorph(mNodeArray, startPathData)) {
            mNodeArray = PathParser.deepCopyNodes(startPathData);
        }

        for (int i = 0; i < startPathData.length; i++) {
            mNodeArray[i].interpolatePathDataNode(startPathData[i],
                    endPathData[i], fraction);
        }

        return mNodeArray;
    }
}
