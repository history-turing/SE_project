package com.whu.treehole.infra.mapper;

/* 认证 Mapper 负责用户凭证与默认资料初始化。 */

import com.whu.treehole.infra.model.AuthCredentialData;
import com.whu.treehole.infra.model.PermissionData;
import com.whu.treehole.infra.model.RoleData;
import com.whu.treehole.infra.model.UserCreateData;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AuthMapper {

    AuthCredentialData selectCredentialByUsername(@Param("username") String username);

    AuthCredentialData selectCredentialByEmail(@Param("email") String email);

    AuthCredentialData selectCredentialByUserId(@Param("userId") Long userId);

    void insertUser(UserCreateData userCreateData);

    void insertCredential(AuthCredentialData authCredentialData);

    void updateLastLoginAt(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    void insertUserBadge(@Param("userId") Long userId,
                         @Param("badgeName") String badgeName,
                         @Param("sortOrder") int sortOrder);

    void insertUserStat(@Param("userId") Long userId,
                        @Param("statLabel") String statLabel,
                        @Param("statValue") String statValue,
                        @Param("sortOrder") int sortOrder);

    List<RoleData> selectRolesByUserId(@Param("userId") Long userId);

    List<PermissionData> selectPermissionsByUserId(@Param("userId") Long userId);

    String selectAccountStatusByUserId(@Param("userId") Long userId);
}
