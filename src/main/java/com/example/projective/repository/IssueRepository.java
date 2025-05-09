package com.example.projective.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.projective.entity.Issue;

import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByProjectId(Long projectId);

    List<Issue> findByParentId(Long parentId);

    long countByParentIdAndStatusNot(Long parentId, com.example.projective.entity.IssueStatus status);
}
