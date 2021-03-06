/*
 * Copyright (c) 2013 - present Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package endtoend.objc.infer;

import static org.hamcrest.MatcherAssert.assertThat;
import static utils.matchers.ResultContainsErrorInMethod.contains;
import static utils.matchers.ResultContainsNoErrorInMethod.doesNotContain;

import com.google.common.collect.ImmutableList;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import utils.DebuggableTemporaryFolder;
import utils.InferException;
import utils.InferResults;
import utils.InferRunner;

public class MemoryLeaksFromModelsTest {

  public static final String memory_leak_file =
      "infer/tests/codetoanalyze/objc/errors/memory_leaks_benchmark/" +
          "NSStringInitWithBytesNoCopyExample.m";

  private static ImmutableList<String> inferCmd;

  public static final String MEMORY_LEAK = "MEMORY_LEAK";

  @ClassRule
  public static DebuggableTemporaryFolder folder =
      new DebuggableTemporaryFolder();

  @BeforeClass
  public static void runInfer() throws InterruptedException, IOException {
    inferCmd = InferRunner.createiOSInferCommandWithMLBuckets(
        folder,
        memory_leak_file,
        "cf",
        false);
  }

  @Test
  public void whenInferRunsOnFBCreateURLQueryStringBodyEscapingThenMLIsNotFound()
      throws InterruptedException, IOException, InferException {
    InferResults inferResults = InferRunner.runInferObjC(inferCmd);
    assertThat(
        "Results should not contain memory leak",
        inferResults,
        doesNotContain(
            MEMORY_LEAK,
            memory_leak_file,
            "FBCreateURLQueryStringBodyEscaping"));
  }

  @Test
  public void whenInferRunsOnRandomBytesThenMLIsNotFound()
      throws InterruptedException, IOException, InferException {
    InferResults inferResults = InferRunner.runInferObjC(inferCmd);
    assertThat(
        "Results should not contain memory leak",
        inferResults,
        doesNotContain(
            MEMORY_LEAK,
            memory_leak_file,
            "randomBytes:"));
  }

  @Test
  public void whenInferRunsOn_macForIVThenMLIsFound()
      throws InterruptedException, IOException, InferException {
    InferResults inferResults = InferRunner.runInferObjC(inferCmd);
    assertThat(
        "Results should contain memory leak",
        inferResults,
        contains(
            MEMORY_LEAK,
            memory_leak_file,
            "macForIV:"
        )
    );
  }

  @Test
  public void whenInferRunsOnHexStringValueThenMLIsNotFound()
      throws InterruptedException, IOException, InferException {
    InferResults inferResults = InferRunner.runInferObjC(inferCmd);
    assertThat(
        "Results should not contain memory leak",
        inferResults,
        doesNotContain(
            MEMORY_LEAK,
            memory_leak_file,
            "hexStringValue"));
  }

}
