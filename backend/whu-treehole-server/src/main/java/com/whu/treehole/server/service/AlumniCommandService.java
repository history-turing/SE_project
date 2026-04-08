package com.whu.treehole.server.service;

/* 校友命令服务负责联系人关注状态切换。 */

import com.whu.treehole.common.exception.BusinessException;
import com.whu.treehole.domain.dto.ToggleResponse;
import com.whu.treehole.infra.mapper.PortalCommandMapper;
import com.whu.treehole.infra.model.FollowStateData;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlumniCommandService {

    private final PortalCommandMapper portalCommandMapper;

    public AlumniCommandService(PortalCommandMapper portalCommandMapper) {
        this.portalCommandMapper = portalCommandMapper;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "alumniPage", allEntries = true),
            @CacheEvict(cacheNames = "profilePage", allEntries = true)
    })
    public ToggleResponse toggleFollow(long userId, String contactCode) {
        FollowStateData followState = portalCommandMapper.selectFollowState(userId, contactCode);
        Long contactId = followState == null ? portalCommandMapper.selectContactId(contactCode) : followState.getContactId();
        if (contactId == null) {
            throw new BusinessException(4043, "联系人不存在");
        }

        boolean nextFollowed = !Boolean.TRUE.equals(followState == null ? null : followState.getFollowed());
        if (followState == null) {
            portalCommandMapper.insertFollowState(userId, contactId, nextFollowed);
        } else {
            portalCommandMapper.updateFollowState(userId, contactId, nextFollowed);
        }
        return new ToggleResponse(contactCode, nextFollowed, null);
    }
}
