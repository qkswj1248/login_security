package com.banban.login_security.mapper;

import com.banban.login_security.domain.agent.AgentPermission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AgentPermissionMapper {
    void insert(AgentPermission agentPermission);
    /*
    1. sql로 찾아서 가져오기
    - 성능적으로 좋음
    - 근데 다른 권한들의 정보가 필요하면 불편
    2. 다 가져와서 filter링 하기
    - 다른 권한들 정보 필요하면 좋음 -> 권한 조합할 때 자주 쓸 듯
    - 무거움
     */
    List<AgentPermission> findByAgentId(Long agent_id);
    List<AgentPermission> findByResource(@Param("agent_id") Long agent_id, @Param("resource") String resource, @Param("action") String action);
}
