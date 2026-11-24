package com.patrick.taskmanager.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface RefreshTokenRepository extends CrudRepository<RefreshToken, String>{
    List<RefreshToken> findAllByUserId(Long userId);
}
