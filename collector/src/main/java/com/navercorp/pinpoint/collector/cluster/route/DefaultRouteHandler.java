/*
 * Copyright 2014 NAVER Corp.
 *
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
 */

package com.navercorp.pinpoint.collector.cluster.route;

import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.TargetClusterPoint;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.io.TCommandTypeVersion;

/**
 * @author koo.taejin
 * @author HyunGil Jeong
 */
public class DefaultRouteHandler extends AbstractRouteHandler<RequestEvent> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RouteFilterChain<RequestEvent> requestFilterChain;
    private final RouteFilterChain<ResponseEvent> responseFilterChain;

    public DefaultRouteHandler(ClusterPointLocator<TargetClusterPoint> targetClusterPointLocator,
            RouteFilterChain<RequestEvent> requestFilterChain,
            RouteFilterChain<ResponseEvent> responseFilterChain) {
        super(targetClusterPointLocator);

        this.requestFilterChain = requestFilterChain;
        this.responseFilterChain = responseFilterChain;
    }

    @Override
    public void addRequestFilter(RouteFilter<RequestEvent> filter) {
        this.requestFilterChain.addLast(filter);
    }

    @Override
    public void addResponseFilter(RouteFilter<ResponseEvent> filter) {
        this.responseFilterChain.addLast(filter);
    }

    @Override
    public TCommandTransferResponse onRoute(RequestEvent event) {
        requestFilterChain.doEvent(event);

        TCommandTransferResponse routeResult = onRoute0(event);

        responseFilterChain.doEvent(new ResponseEvent(event, event.getRequestId(), routeResult));

        return routeResult;
    }

    private TCommandTransferResponse onRoute0(RequestEvent event) {
        TCommandTransferResponse response = new TCommandTransferResponse();

        TBase<?,?> requestObject = event.getRequestObject();
        if (requestObject == null) {
            return createResponse(TRouteResult.EMPTY_REQUEST);
        }

        TargetClusterPoint clusterPoint = findClusterPoint(event.getDeliveryCommand());
        if (clusterPoint == null) {
            return createResponse(TRouteResult.AGNET_NOT_FOUND);
        }

        TCommandTypeVersion commandVersion = TCommandTypeVersion.getVersion(clusterPoint.gerVersion());
        if (!commandVersion.isSupportCommand(requestObject)) {
            return createResponse(TRouteResult.AGENT_NOT_SUPPORTED_COMMAND);
        }

        Future<ResponseMessage> future = clusterPoint.request(event.getDeliveryCommand().getPayload());
        future.await();
        ResponseMessage responseMessage = future.getResult();
        if (responseMessage == null) {
            return createResponse(TRouteResult.TIMEOUT);
        }

        return createResponse(TRouteResult.OK, responseMessage.getMessage());
    }

    private TCommandTransferResponse createResponse(TRouteResult result) {
        return createResponse(result, new byte[0]);
    }

    private TCommandTransferResponse createResponse(TRouteResult result, byte[] payload) {
        TCommandTransferResponse response = new TCommandTransferResponse();
        response.setRouteResult(result);
        response.setPayload(payload);
        return response;
    }

}
