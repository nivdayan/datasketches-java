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

package org.apache.datasketches.kll;

import static org.apache.datasketches.kll.KllPreambleUtil.DATA_START_ADR;
import static org.apache.datasketches.kll.KllPreambleUtil.DATA_START_ADR_SINGLE_ITEM;
import static org.apache.datasketches.kll.KllPreambleUtil.SERIAL_VERSION_UPDATABLE;
import static org.apache.datasketches.kll.KllPreambleUtil.getMemorySerVer;
import static org.apache.datasketches.kll.KllSketch.Error.NOT_SINGLE_ITEM;
import static org.apache.datasketches.kll.KllSketch.Error.kllSketchThrow;
import static org.apache.datasketches.kll.KllSketch.SketchType.DOUBLES_SKETCH;

import java.util.Objects;

import org.apache.datasketches.memory.Memory;

/**
 * This class implements an on-heap doubles KllSketch.
 *
 * <p>Please refer to the documentation in the package-info:<br>
 * {@link org.apache.datasketches.kll}</p>
 *
 * @author Lee Rhodes, Kevin Lang
 */
final class KllHeapDoublesSketch extends KllDoublesSketch {
  private final int k_; // configured size of K.
  private final int m_; // configured size of M.
  private long n_;      // number of items input into this sketch.
  private int minK_;    // dynamic minK for error estimation after merging with different k.
  private boolean isLevelZeroSorted_;
  private double minDoubleItem_;
  private double maxDoubleItem_;
  private double[] doubleItems_;

  /**
   * New instance heap constructor with a given parameters <em>k</em> and <em>m</em>.
   *
   * @param k parameter that controls size of the sketch and accuracy of estimates.
   * <em>k</em> can be between <em>m</em> and 65535, inclusive.
   * The default <em>k</em> = 200 results in a normalized rank error of about 1.65%.
   * Larger <em>k</em> will have smaller error but the sketch will be larger (and slower).
   * @param m parameter controls the minimum level width in items. It can be 2, 4, 6 or 8.
   * The DEFAULT_M, which is 8 is recommended. Other sizes of <em>m</em> should be considered
   * experimental as they have not been as well characterized.
   */
  KllHeapDoublesSketch(final int k, final int m) {
    super(null, null);
    KllHelper.checkM(m);
    KllHelper.checkK(k, m);
    this.k_ = k;
    this.m_ = m;
    n_ = 0;
    minK_ = k;
    isLevelZeroSorted_ = false;
    levelsArr = new int[] {k, k};
    minDoubleItem_ = Double.NaN;
    maxDoubleItem_ = Double.NaN;
    doubleItems_ = new double[k];
  }

  static KllHeapDoublesSketch heapifyImpl(final Memory srcMem) {
    Objects.requireNonNull(srcMem, "Parameter 'srcMem' must not be null");
    final KllMemoryValidate memVal = new KllMemoryValidate(srcMem, DOUBLES_SKETCH);
    return new KllHeapDoublesSketch(srcMem, memVal);
  }
  
  /**
   * Heapify constructor.
   * @param srcMem Memory object that contains data serialized by this sketch.
   * @param memValidate the MemoryValidate object
   */
  private KllHeapDoublesSketch(final Memory srcMem, final KllMemoryValidate memValidate) {
    super(null, null);
    k_ = memValidate.k;
    m_ = memValidate.m;
    n_ = memValidate.n;
    minK_ = memValidate.minK;
    levelsArr = memValidate.levelsArr;
    isLevelZeroSorted_ = memValidate.level0Sorted;
    final boolean serialVersionUpdatable = getMemorySerVer(srcMem) == SERIAL_VERSION_UPDATABLE;

    if (memValidate.empty && !serialVersionUpdatable) {
      minDoubleItem_ = Double.NaN;
      maxDoubleItem_ = Double.NaN;
      doubleItems_ = new double[k_];
    }
    else if (memValidate.singleItem && !serialVersionUpdatable) {
      final double item = srcMem.getDouble(DATA_START_ADR_SINGLE_ITEM);
      minDoubleItem_ = maxDoubleItem_ = item;
      doubleItems_ = new double[k_];
      doubleItems_[k_ - 1] = item;
    }
    else { //Full or updatableMemFormat
      int offsetBytes = DATA_START_ADR;
      offsetBytes += (serialVersionUpdatable ? levelsArr.length * Integer.BYTES : (levelsArr.length - 1) * Integer.BYTES);
      minDoubleItem_ = srcMem.getDouble(offsetBytes);
      offsetBytes += Double.BYTES;
      maxDoubleItem_ = srcMem.getDouble(offsetBytes);
      offsetBytes += Double.BYTES;
      final int capacityItems = levelsArr[getNumLevels()];
      final int retainedItems = capacityItems - levelsArr[0];
      doubleItems_ = new double[capacityItems];
      final int shift = levelsArr[0];
      if (serialVersionUpdatable) {
        offsetBytes += shift * Double.BYTES;
        srcMem.getDoubleArray(offsetBytes, doubleItems_, shift, retainedItems);
      } else {
        srcMem.getDoubleArray(offsetBytes, doubleItems_, shift, retainedItems);
      }
    }
  }

  @Override
  public int getK() { return k_; }

  @Override
  public long getN() { return n_; }

  @Override
  double[] getDoubleItemsArray() { return doubleItems_; }

  @Override
  double getDoubleSingleItem() {
    if (n_ != 1L) { kllSketchThrow(NOT_SINGLE_ITEM); return Double.NaN; }
    return doubleItems_[k_ - 1];
  }

  @Override
  int getM() { return m_; }

  @Override
  double getMaxDoubleItem() { return maxDoubleItem_; }

  @Override
  double getMinDoubleItem() { return minDoubleItem_; }

  @Override
  int getMinK() { return minK_; }

  @Override
  void incN() { n_++; }

  @Override
  void incNumLevels() { } //not used here

  @Override
  boolean isLevelZeroSorted() { return isLevelZeroSorted_; }

  @Override
  void setDoubleItemsArray(final double[] doubleItems) { doubleItems_ = doubleItems; }

  @Override
  void setDoubleItemsArrayAt(final int index, final double item) { doubleItems_[index] = item; }

  @Override
  void setLevelZeroSorted(final boolean sorted) { this.isLevelZeroSorted_ = sorted; }

  @Override
  void setMaxDoubleItem(final double item) { maxDoubleItem_ = item; }

  @Override
  void setMinDoubleItem(final double item) { minDoubleItem_ = item; }

  @Override
  void setMinK(final int minK) { minK_ = minK; }

  @Override
  void setN(final long n) { n_ = n; }

  @Override
  void setNumLevels(final int numLevels) {  } //not used here

}
