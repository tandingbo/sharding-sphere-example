/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.example.jdbc.main.orche.java.zookeeper;

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.example.jdbc.fixture.DataRepository;
import io.shardingsphere.example.jdbc.fixture.DataSourceUtil;
import io.shardingsphere.jdbc.orchestration.api.OrchestrationMasterSlaveDataSourceFactory;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationType;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.jdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class OrchestrationMasterSlaveMain {
    
    private static final String ZOOKEEPER_CONNECTION_STRING = "localhost:2181";
    
    private static final String NAMESPACE = "orchestration-java-demo";
    
    public static void main(final String[] args) throws SQLException {
        //        new DataRepository(getDataSourceByCloudConfig()).demo();
        DataSource dataSource = getDataSourceByLocalConfig();
        new DataRepository(dataSource).demo();
        OrchestrationMasterSlaveDataSourceFactory.closeQuietly(dataSource);
    }
    
    private static DataSource getDataSourceByLocalConfig() throws SQLException {
        return OrchestrationMasterSlaveDataSourceFactory.createDataSource(
                createDataSourceMap(), crateMasterSlaveRuleConfig(), new ConcurrentHashMap<String, Object>(), new OrchestrationConfiguration("orchestration-master-slave-data-source", getZookeeperConfiguration(), true, OrchestrationType.MASTER_SLAVE));
    }
    
    private static DataSource getDataSourceByCloudConfig() throws SQLException {
        return OrchestrationMasterSlaveDataSourceFactory.createDataSource(
                null, null, null, new OrchestrationConfiguration("orchestration-master-slave-data-source", getZookeeperConfiguration(), false, OrchestrationType.MASTER_SLAVE));
    }
    
    private static RegistryCenterConfiguration getZookeeperConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists(ZOOKEEPER_CONNECTION_STRING);
        result.setNamespace(NAMESPACE);
        return result;
    }
    
    private static Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
        result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
        result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
        return result;
    }
    
    private static MasterSlaveRuleConfiguration crateMasterSlaveRuleConfig() {
        return new MasterSlaveRuleConfiguration("demo_master_slave", "demo_ds_master", Lists.newArrayList("demo_ds_slave_0", "demo_ds_slave_1"));
    }
}
