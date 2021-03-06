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
package com.google.idea.blaze.cpp;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.idea.blaze.base.ideinfo.CIdeInfo;
import com.google.idea.blaze.base.ideinfo.CToolchainIdeInfo;
import com.google.idea.blaze.base.model.primitives.ExecutionRootPath;
import com.google.idea.blaze.base.sync.workspace.ExecutionRootPathResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.lang.workspace.headerRoots.HeaderRoots;
import com.jetbrains.cidr.lang.workspace.headerRoots.HeadersSearchRoot;
import com.jetbrains.cidr.lang.workspace.headerRoots.IncludedHeadersRoot;
import com.jetbrains.cidr.toolchains.CompilerInfoCache;
import java.io.File;
import java.util.Objects;

/** Data used by a {@link BlazeResolveConfiguration}. */
final class BlazeResolveConfigurationData {

  final BlazeCompilerSettings compilerSettings;

  final ImmutableList<HeadersSearchRoot> cLibraryIncludeRoots;
  final ImmutableList<HeadersSearchRoot> cppLibraryIncludeRoots;
  final HeaderRoots projectIncludeRoots;
  final BlazeCompilerMacros compilerMacros;
  final CToolchainIdeInfo toolchainIdeInfo;

  static BlazeResolveConfigurationData create(
      Project project,
      ExecutionRootPathResolver executionRootPathResolver,
      ImmutableMap<File, VirtualFile> headerRoots,
      CIdeInfo cIdeInfo,
      CToolchainIdeInfo toolchainIdeInfo,
      BlazeCompilerSettings compilerSettings,
      CompilerInfoCache compilerInfoCache) {
    ImmutableSet.Builder<ExecutionRootPath> systemIncludesBuilder = ImmutableSet.builder();
    systemIncludesBuilder.addAll(cIdeInfo.transitiveSystemIncludeDirectories);
    systemIncludesBuilder.addAll(toolchainIdeInfo.builtInIncludeDirectories);
    systemIncludesBuilder.addAll(toolchainIdeInfo.unfilteredToolchainSystemIncludes);

    ImmutableSet.Builder<ExecutionRootPath> userIncludesBuilder = ImmutableSet.builder();
    userIncludesBuilder.addAll(cIdeInfo.transitiveIncludeDirectories);
    userIncludesBuilder.addAll(cIdeInfo.localIncludeDirectories);

    ImmutableSet.Builder<ExecutionRootPath> userQuoteIncludesBuilder = ImmutableSet.builder();
    userQuoteIncludesBuilder.addAll(cIdeInfo.transitiveQuoteIncludeDirectories);

    ImmutableList.Builder<String> defines = ImmutableList.builder();
    defines.addAll(cIdeInfo.transitiveDefines);
    defines.addAll(cIdeInfo.localDefines);

    ImmutableMap<String, String> features = ImmutableMap.of();

    return new BlazeResolveConfigurationData(
        project,
        executionRootPathResolver,
        headerRoots,
        systemIncludesBuilder.build(),
        systemIncludesBuilder.build(),
        userQuoteIncludesBuilder.build(),
        userIncludesBuilder.build(),
        userIncludesBuilder.build(),
        defines.build(),
        features,
        compilerSettings,
        compilerInfoCache,
        toolchainIdeInfo);
  }

  private BlazeResolveConfigurationData(
      Project project,
      ExecutionRootPathResolver executionRootPathResolver,
      ImmutableMap<File, VirtualFile> headerRoots,
      ImmutableCollection<ExecutionRootPath> cSystemIncludeDirs,
      ImmutableCollection<ExecutionRootPath> cppSystemIncludeDirs,
      ImmutableCollection<ExecutionRootPath> quoteIncludeDirs,
      ImmutableCollection<ExecutionRootPath> cIncludeDirs,
      ImmutableCollection<ExecutionRootPath> cppIncludeDirs,
      ImmutableCollection<String> defines,
      ImmutableMap<String, String> features,
      BlazeCompilerSettings compilerSettings,
      CompilerInfoCache compilerInfoCache,
      CToolchainIdeInfo toolchainIdeInfo) {
    this.toolchainIdeInfo = toolchainIdeInfo;

    HeaderRootsCollector headerRootsCollector =
        new HeaderRootsCollector(project, executionRootPathResolver, headerRoots);
    ImmutableList.Builder<HeadersSearchRoot> cIncludeRootsBuilder = ImmutableList.builder();
    headerRootsCollector.collectHeaderRoots(
        cIncludeRootsBuilder, cIncludeDirs, true /* isUserHeader */);
    headerRootsCollector.collectHeaderRoots(
        cIncludeRootsBuilder, cSystemIncludeDirs, false /* isUserHeader */);
    this.cLibraryIncludeRoots = cIncludeRootsBuilder.build();

    ImmutableList.Builder<HeadersSearchRoot> cppIncludeRootsBuilder = ImmutableList.builder();
    headerRootsCollector.collectHeaderRoots(
        cppIncludeRootsBuilder, cppIncludeDirs, true /* isUserHeader */);
    headerRootsCollector.collectHeaderRoots(
        cppIncludeRootsBuilder, cppSystemIncludeDirs, false /* isUserHeader */);
    this.cppLibraryIncludeRoots = cppIncludeRootsBuilder.build();

    ImmutableList.Builder<HeadersSearchRoot> quoteIncludeRootsBuilder = ImmutableList.builder();
    headerRootsCollector.collectHeaderRoots(
        quoteIncludeRootsBuilder, quoteIncludeDirs, true /* isUserHeader */);
    this.projectIncludeRoots = new HeaderRoots(quoteIncludeRootsBuilder.build());

    this.compilerSettings = compilerSettings;
    this.compilerMacros =
        new BlazeCompilerMacros(project, compilerInfoCache, compilerSettings, defines, features);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BlazeResolveConfigurationData)) {
      return false;
    }
    BlazeResolveConfigurationData otherData = (BlazeResolveConfigurationData) other;
    return this.cLibraryIncludeRoots.equals(otherData.cLibraryIncludeRoots)
        && this.cppLibraryIncludeRoots.equals(otherData.cppLibraryIncludeRoots)
        && this.projectIncludeRoots.equals(otherData.projectIncludeRoots)
        && this.compilerMacros.equals(otherData.compilerMacros)
        && this.toolchainIdeInfo.equals(otherData.toolchainIdeInfo)
        && this.compilerSettings
            .getCompilerVersion()
            .equals(otherData.compilerSettings.getCompilerVersion());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        cLibraryIncludeRoots,
        cppLibraryIncludeRoots,
        projectIncludeRoots,
        compilerMacros,
        toolchainIdeInfo,
        compilerSettings.getCompilerVersion());
  }

  private static class HeaderRootsCollector {
    private final Project project;
    private final ExecutionRootPathResolver executionRootPathResolver;
    private final ImmutableMap<File, VirtualFile> virtualFileCache;

    HeaderRootsCollector(
        Project project,
        ExecutionRootPathResolver executionRootPathResolver,
        ImmutableMap<File, VirtualFile> virtualFileCache) {
      this.project = project;
      this.executionRootPathResolver = executionRootPathResolver;
      this.virtualFileCache = virtualFileCache;
    }

    void collectHeaderRoots(
        ImmutableList.Builder<HeadersSearchRoot> roots,
        ImmutableCollection<ExecutionRootPath> paths,
        boolean isUserHeader) {
      for (ExecutionRootPath executionRootPath : paths) {
        ImmutableList<File> possibleDirectories =
            executionRootPathResolver.resolveToIncludeDirectories(executionRootPath);
        for (File f : possibleDirectories) {
          VirtualFile vf = virtualFileCache.get(f);
          if (vf != null) {
            roots.add(new IncludedHeadersRoot(project, vf, false /* recursive */, isUserHeader));
          }
        }
      }
    }
  }
}
