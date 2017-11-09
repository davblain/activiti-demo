package org.korbit.test.activiti.repository;

import org.korbit.test.activiti.domain.GroupPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupPermissionRepository extends JpaRepository<GroupPermission,UUID> {
    GroupPermission findGroupPermissionByGroupId(String groupId);
}
