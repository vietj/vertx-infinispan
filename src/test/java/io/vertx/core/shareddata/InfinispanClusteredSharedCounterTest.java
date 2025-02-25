/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.core.shareddata;

import io.vertx.Lifecycle;
import io.vertx.LoggingTestWatcher;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.junit.Ignore;
import org.junit.Rule;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Segismont
 */
// Ignore clustered counter tests until ISPN-9608 is resolved
@Ignore
public class InfinispanClusteredSharedCounterTest extends ClusteredSharedCounterTest {

  private static final Logger log = LoggerFactory.getLogger(InfinispanClusteredSharedCounterTest.class);

  @Rule
  public LoggingTestWatcher watchman = new LoggingTestWatcher();

  @Override
  public void setUp() throws Exception {
    Random random = new Random();
    System.setProperty("vertx.infinispan.test.auth.token", new BigInteger(128, random).toString(32));
    super.setUp();
  }

  @Override
  protected void clusteredVertx(VertxOptions options, Handler<AsyncResult<Vertx>> ar) {
    CountDownLatch latch = new CountDownLatch(1);
    Future<Vertx> future = Future.future();
    future.setHandler(ar);
    super.clusteredVertx(options, asyncResult -> {
      if (asyncResult.succeeded()) {
        future.complete(asyncResult.result());
      } else {
        future.fail(asyncResult.cause());
      }
      latch.countDown();
    });
    try {
      assertTrue(latch.await(2, TimeUnit.MINUTES));
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new InfinispanClusterManager();
  }

  @Override
  protected void closeClustered(List<Vertx> clustered) throws Exception {
    Lifecycle.closeClustered(clustered);
  }
}
