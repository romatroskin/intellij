/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.sync.aspects.strategy;

import com.google.idea.blaze.base.model.BlazeVersionData;
import com.intellij.openapi.extensions.ExtensionPointName;
import javax.annotation.Nullable;

/** Extension point for providing an aspect strategy */
public interface AspectStrategyProvider {
  ExtensionPointName<AspectStrategyProvider> EP_NAME =
      ExtensionPointName.create("com.google.idea.blaze.AspectStrategyProvider");

  @Nullable
  AspectStrategy getAspectStrategy(BlazeVersionData blazeVersionData);

  static AspectStrategy findAspectStrategy(BlazeVersionData blazeVersionData) {
    for (AspectStrategyProvider provider : AspectStrategyProvider.EP_NAME.getExtensions()) {
      AspectStrategy aspectStrategy = provider.getAspectStrategy(blazeVersionData);
      if (aspectStrategy != null) {
        return aspectStrategy;
      }
    }
    // Should never get here
    throw new IllegalStateException("No aspect strategy found.");
  }
}
