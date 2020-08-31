// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class ThreadPoolService {

    private ExecutorService threadPool;
    private final Object sync = new Object();

    public ExecutorService getExecutorService() {
        return this.threadPool;
    }

    @PreDestroy
    public void preDestroy() {
        if (threadPool == null) {
            return;
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(600, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void createDeletePoolIfNeeded(int size) {
        if (threadPool != null) {
            return;
        }

        synchronized (sync) {
            if (threadPool == null) {
                threadPool = new ThreadPoolExecutor(size, size * 10,
                        60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
            }
        }
    }
}