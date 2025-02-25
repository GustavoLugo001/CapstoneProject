package com.example.jwtdemo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TreeRepository extends JpaRepository<Tree, Long>{

    List<Tree> findByOwnerId(Long ownerId);
    List<Tree> findByOwnerIdIn(List<Long> ownerIds);


    @Query("SELECT t FROM Tree t JOIN t.usersWithAccess u WHERE u.id = :userId")
    List<Tree> findTreesUserCanModify(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Tree t JOIN t.usersWithAccess u WHERE t.id = :treeId AND u.id = :userId")
    Integer countUserPermissions(@Param("userId") Long userId, @Param("treeId") Long treeId);

    @Query("SELECT t FROM Tree t WHERE t.nextWateringDate <= CURRENT_DATE")
    List<Tree> findTreesNeedingWater();

    @Query("SELECT t FROM Tree t WHERE t.nextFertilizationDate <= CURRENT_DATE")
    List<Tree> findTreesNeedingFertilization();
    
    @Query("SELECT t FROM Tree t WHERE t.owner.id IN :ownerIds")
    List<Tree> findByOwnerIds(@Param("ownerIds") List<Long> ownerIds);


}
