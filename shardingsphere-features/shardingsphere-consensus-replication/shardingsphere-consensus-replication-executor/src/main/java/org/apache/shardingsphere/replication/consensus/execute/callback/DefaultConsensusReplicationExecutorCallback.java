/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.replication.consensus.execute.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.callback.RawExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Default consensus replication executor callback.
 */
@Slf4j
public final class DefaultConsensusReplicationExecutorCallback implements RawExecutorCallback<RawSQLExecuteUnit, ExecuteResult> {
    
    static {
        ShardingSphereServiceLoader.register(ConsensusReplicationExecutorCallback.class);
    }
    
    private final Collection<ConsensusReplicationExecutorCallback> replicaExecutorCallbacks;
    
    public DefaultConsensusReplicationExecutorCallback() {
        replicaExecutorCallbacks = ShardingSphereServiceLoader.newServiceInstances(ConsensusReplicationExecutorCallback.class);
        if (null == replicaExecutorCallbacks || replicaExecutorCallbacks.isEmpty()) {
            throw new ShardingSphereException("not found replica executor callback impl");
        }
    }
    
    @Override
    public Collection<ExecuteResult> execute(final Collection<RawSQLExecuteUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) throws SQLException {
        return replicaExecutorCallbacks.iterator().next().execute(inputs, isTrunkThread, dataMap);
    }
}
