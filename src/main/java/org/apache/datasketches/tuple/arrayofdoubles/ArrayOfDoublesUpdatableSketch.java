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

package org.apache.datasketches.tuple.arrayofdoubles;

import org.apache.datasketches.common.ResizeFactor;
import org.apache.datasketches.hash.MurmurHash3;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.WritableMemory;
import org.apache.datasketches.thetacommon.ThetaUtil;
import org.apache.datasketches.tuple.Util;

/**
 * The top level for updatable tuple sketches of type ArrayOfDoubles.
 */
public abstract class ArrayOfDoublesUpdatableSketch extends ArrayOfDoublesSketch {

  final long seed_;

  ArrayOfDoublesUpdatableSketch(final int numValues, final long seed) {
    super(numValues);
    seed_ = seed;
  }

  /**
   * Heapify the given Memory as an ArrayOfDoublesUpdatableSketch
   * @param mem the given Memory
   * @return an ArrayOfDoublesUpdatableSketch
   */
  public static ArrayOfDoublesUpdatableSketch heapify(final Memory mem) {
    return heapify(mem, ThetaUtil.DEFAULT_UPDATE_SEED);
  }

  /**
   * Heapify the given Memory and seed as a ArrayOfDoublesUpdatableSketch
   * @param mem the given Memory
   * @param seed the given seed
   * @return an ArrayOfDoublesUpdatableSketch
   */
  public static ArrayOfDoublesUpdatableSketch heapify(final Memory mem, final long seed) {
    return new HeapArrayOfDoublesQuickSelectSketch(mem, seed);
  }

  /**
   * Wrap the given WritableMemory as an ArrayOfDoublesUpdatableSketch
   * @param mem the given Memory
   * @return an ArrayOfDoublesUpdatableSketch
   */
  public static ArrayOfDoublesUpdatableSketch wrap(final WritableMemory mem) {
    return wrap(mem, ThetaUtil.DEFAULT_UPDATE_SEED);
  }

  /**
   * Wrap the given WritableMemory and seed as a ArrayOfDoublesUpdatableSketch
   * @param mem the given Memory
   * @param seed the given seed
   * @return an ArrayOfDoublesUpdatableSketch
   */
  public static ArrayOfDoublesUpdatableSketch wrap(final WritableMemory mem, final long seed) {
    return new DirectArrayOfDoublesQuickSelectSketch(mem, seed);
  }

  /**
   * Updates this sketch with a long key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given long key
   * @param values The given values
   */
  public void update(final long key, final double[] values) {
    update(new long[] {key}, values);
  }

  /**
   * Updates this sketch with a double key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given double key
   * @param values The given values
   */
  public void update(final double key, final double[] values) {
    update(Util.doubleToLongArray(key), values);
  }

  /**
   * Updates this sketch with a String key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given String key
   * @param values The given values
   */
  public void update(final String key, final double[] values) {
    update(Util.stringToByteArray(key), values);
  }

  /**
   * Updates this sketch with a byte[] key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given byte[] key
   * @param values The given values
   */
  public void update(final byte[] key, final double[] values) {
    if (key == null || key.length == 0) { return; }
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Updates this sketch with a int[] key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given int[] key
   * @param values The given values
   */
  public void update(final int[] key, final double[] values) {
    if (key == null || key.length == 0) { return; }
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Updates this sketch with a long[] key and double values.
   * The values will be stored or added to the ones associated with the key
   *
   * @param key The given long[] key
   * @param values The given values
   */
  public void update(final long[] key, final double[] values) {
    if (key == null || key.length == 0) { return; }
    insertOrIgnore(MurmurHash3.hash(key, seed_)[0] >>> 1, values);
  }

  /**
   * Gets the configured nominal number of entries
   * @return nominal number of entries
   */
  public abstract int getNominalEntries();

  /**
   * Gets the configured resize factor
   * @return resize factor
   */
  public abstract ResizeFactor getResizeFactor();

  /**
   * Gets the configured sampling probability
   * @return sampling probability
   */
  public abstract float getSamplingProbability();

  /**
   * Rebuilds reducing the actual number of entries to the nominal number of entries if needed
   */
  public abstract void trim();

  /**
   * Resets this sketch an empty state.
   */
  public abstract void reset();

  /**
   * Gets an on-heap compact representation of the sketch
   * @return compact sketch
   */
  @Override
  public ArrayOfDoublesCompactSketch compact() {
    return compact(null);
  }

  /**
   * Gets an off-heap compact representation of the sketch using the given memory
   * @param dstMem memory for the compact sketch (can be null)
   * @return compact sketch (off-heap if memory is provided)
   */
  @Override
  public ArrayOfDoublesCompactSketch compact(final WritableMemory dstMem) {
    if (dstMem == null) {
      return new HeapArrayOfDoublesCompactSketch(this);
    }
    return new DirectArrayOfDoublesCompactSketch(this, dstMem);
  }

  abstract int getCurrentCapacity();

  long getSeed() {
    return seed_;
  }

  @Override
  short getSeedHash() {
    return Util.computeSeedHash(seed_);
  }

  /**
   * Insert if key is less than thetaLong and not a duplicate, otherwise ignore.
   * @param key the hash value of the input value
   * @param values array of values to update the summary
   */
  abstract void insertOrIgnore(long key, double[] values);

}
