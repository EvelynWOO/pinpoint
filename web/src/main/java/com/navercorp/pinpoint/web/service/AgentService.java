/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.cluster.PinpointRouteResponse;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadStatusList;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.util.List;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public interface AgentService {

    AgentInfoBo getAgentInfo(String applicationName, String agentId, long startTimeStamp);
    AgentInfoBo getAgentInfo(String applicationName, String agentId, long startTimeStamp, boolean checkDB);
    List<AgentInfoBo> getAgentInfoList(String applicationName);

    PinpointRouteResponse invoke(AgentInfoBo agentInfoList, TBase tBase) throws TException;
    PinpointRouteResponse invoke(AgentInfoBo agentInfoList, TBase tBase, long timeout) throws TException;
    PinpointRouteResponse invoke(AgentInfoBo agentInfoList, byte[] payload) throws TException;
    PinpointRouteResponse invoke(AgentInfoBo agentInfoList, byte[] payload, long timeout) throws TException;

    Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, TBase tBase) throws TException;
    Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, TBase tBase, long timeout) throws TException;
    Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException;
    Map<AgentInfoBo, PinpointRouteResponse> invoke(List<AgentInfoBo> agentInfoList, byte[] payload, long timeout) throws TException;

    AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfoBo> agentInfoList) throws TException;
    AgentActiveThreadStatusList getActiveThreadStatus(List<AgentInfoBo> agentInfoList, byte[] payload) throws TException;

}
