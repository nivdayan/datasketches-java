/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.req;

import static java.lang.Math.max;
import static org.apache.datasketches.Util.LS;
import static org.apache.datasketches.Util.TAB;
import static org.apache.datasketches.req.ReqSketch.MIN_K;

/**
 * For building a new ReqSketch
 *
 * @author Lee Rhodes
 */
public class ReqSketchBuilder {
  private final static int DEFAULT_K = 12;
  private int bK = DEFAULT_K;
  private boolean bHRA;
  private boolean bLtEq;
  private boolean bCompatible;
  private ReqDebug bReqDebug;

  /**
   * Constructor for the ReqSketchBuilder.
   */
  public ReqSketchBuilder() {
    bK = DEFAULT_K;
    bHRA = true;
    bLtEq = false;
    bCompatible = true;
    bReqDebug = null;
  }

  /**
   * Returns a new ReqSketch with the current configuration of the builder.
   * @return a new ReqSketch
   */
  public ReqSketch build() {
    final ReqSketch sk = new ReqSketch(bK, bHRA);
    sk.setLessThanOrEqual(bLtEq);
    sk.setCompatible(bCompatible);
    if (bReqDebug != null) { sk.setReqDebug(bReqDebug); }
    return sk;
  }

  /**
   * gets the builder configured value of compatible.
   * @return the builder configured value of compatible.
   */
  public boolean getCompatible() {
    return bCompatible;
  }

  /**
   * Gets the builder confibured value of High Rank Accuracy.
   * @return the builder confibured value of High Rank Accuracy.
   */
  public boolean getHighRankAccuracy() {
    return bHRA;
  }

  /**
   * Gets the builder configured value of k.
   * @return the builder configured value of k.
   */
  public int getK() {
    return bK;
  }

  /**
   * Gets the builder configured value of Less-Than-Or-Equal.
   * @return the builder confibured value of Less-Than-Or-Equal
   */
  public boolean getLessThanOrEqual() {
    return bLtEq;
  }

  /**
   * Gets the builder configured value of ReqDebug
   * @return the builder configured value of ReqDebug, or null.
   */
  public ReqDebug getReqDebug() {
    return bReqDebug;
  }

  /**
   * Sets the parameter compatible. This parameter can also be modified after the sketch has
   * been constructed. It is included here for convenience.
   * @param compatible See {@link ReqSketch#setCompatible(boolean)}.
   * @return this
   */
  public ReqSketchBuilder setCompatible(final boolean compatible) {
    bCompatible = compatible;
    return this;
  }

  /**
   * This sets the parameter highRankAccuracy.
   * @param hra See {@link ReqSketch#ReqSketch(int, boolean)}
   * @return this
   */
  public ReqSketchBuilder setHighRankAccuracy(final boolean hra) {
    bHRA = hra;
    return this;
  }

  /**
   * This sets the parameter k.
   * @param k See {@link ReqSketch#ReqSketch(int, boolean)}
   * @return this
   */
  public ReqSketchBuilder setK(final int k) {
    bK = max(k & -2, MIN_K); //make even and no smaller than MIN_K
    return this;
  }

  /**
   * Sets the parameter lessThanOrEquals. This parameter can also be modified after the sketch has
   * been constructed. It is included here for convenience.
   * @param ltEq See {@link ReqSketch#setLessThanOrEqual(boolean)}
   * @return this
   */
  public ReqSketchBuilder setLessThanOrEqual(final boolean ltEq) {
    bLtEq = ltEq;
    return this;
  }

  /**
   * This sets the parameter reqDebug.
   * This parameter can also be  modified after the sketch has been constructed.
   * It is included here for convenience.
   * @param reqDebug See {@link ReqSketch#setReqDebug(ReqDebug)}
   * @return this
   */
  public ReqSketchBuilder setReqDebug(final ReqDebug reqDebug) {
    bReqDebug = reqDebug;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("ReqSketchBuilder configuration:").append(LS);
    sb.append("K:").append(TAB).append(bK).append(LS);
    sb.append("HRA:").append(TAB).append(bHRA).append(LS);
    sb.append("LtEq").append(TAB).append(bLtEq).append(LS);
    sb.append("Compatible:").append(TAB).append(bCompatible).append(LS);
    final String valid = bReqDebug != null ? "valid" : "invalid";
    sb.append("ReqDebug:").append(TAB).append(valid).append(LS);
    return sb.toString();
  }

}
