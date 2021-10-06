package com.clickhouse.client.grpc;

import org.testng.Assert;
import org.testng.annotations.Test;
import com.clickhouse.client.BaseIntegrationTest;
import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.ClickHouseRequest;
import com.clickhouse.client.grpc.config.ClickHouseGrpcClientOption;

public class ClickHouseGrpcChannelFactoryTest extends BaseIntegrationTest {
    @Test(groups = { "integration" })
    public void testGetFactory() throws Exception {
        ClickHouseNode server = getServer(ClickHouseProtocol.GRPC);
        try (ClickHouseClient client = ClickHouseClient.newInstance()) {
            ClickHouseRequest<?> request = client.connect(server);
            Assert.assertTrue(ClickHouseGrpcChannelFactory.getFactory(request.getConfig(),
                    server) instanceof NettyChannelFactoryImpl);
            Assert.assertTrue(ClickHouseGrpcChannelFactory.getFactory(
                    request.option(ClickHouseGrpcClientOption.USE_OKHTTP, true).getConfig(),
                    server) instanceof OkHttpChannelFactoryImpl);
            Assert.assertTrue(ClickHouseGrpcChannelFactory.getFactory(
                    request.option(ClickHouseGrpcClientOption.USE_OKHTTP, false).getConfig(),
                    server) instanceof NettyChannelFactoryImpl);
        }
    }
}
