/*
 * Copyright 2010-2012 Luca Garulli (l.garulli(at)orientechnologies.com)
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

package com.orientechnologies.orient.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.orientechnologies.orient.core.id.ORID;

/**
 * @author <a href="mailto:enisher@gmail.com">Artem Orobets</a>
 */
public abstract class OAbstractMapCache<T extends Map<ORID, ?>> implements OCache {
  protected final T           cache;
  private final AtomicBoolean enabled = new AtomicBoolean(false);

  public OAbstractMapCache(T cache) {
    this.cache = cache;
  }

  @Override
  public void startup() {
    enable();
  }

  @Override
  public void shutdown() {
    disable();
  }

  @Override
  public boolean isEnabled() {
    return enabled.get();
  }

  @Override
  public boolean enable() {
    return enabled.compareAndSet(false, true);
  }

  @Override
  public boolean disable() {
    clear();
    return enabled.compareAndSet(true, false);
  }

  @Override
  public void clear() {
    if (!isEnabled())
      return;

    cache.clear();
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public Collection<ORID> keys() {
    return new ArrayList<ORID>(cache.keySet());
  }
}
