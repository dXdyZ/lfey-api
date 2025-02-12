package com.lfey.lfenuserservice.repository.cache;

import com.lfey.lfenuserservice.entity.PendingUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingUserRepository extends CrudRepository<PendingUser, String> {
    Boolean existsByEmail(String email);
}
