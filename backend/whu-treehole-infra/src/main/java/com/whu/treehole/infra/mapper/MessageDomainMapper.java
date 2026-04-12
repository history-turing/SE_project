package com.whu.treehole.infra.mapper;

import com.whu.treehole.infra.model.DmConversationData;
import org.apache.ibatis.annotations.Param;

public interface MessageDomainMapper {

    DmConversationData selectSingleConversationBetweenUsers(@Param("userId") Long userId,
                                                            @Param("peerUserId") Long peerUserId);
}
