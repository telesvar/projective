package com.example.projective.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.projective.entity.Issue;
import com.example.projective.entity.IssueStatus;
import com.example.projective.exception.InvalidStateTransitionException;
import com.example.projective.exception.ResourceNotFoundException;
import com.example.projective.payload.IssuePayload;
import com.example.projective.repository.IssueRepository;
import com.example.projective.repository.WorkspaceRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueService {

    private final IssueRepository issueRepository;
    private final WorkspaceRepository workspaceRepository;

    public IssuePayload.View createIssue(Long workspaceId, IssuePayload.Create dto) {
        var workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id " + workspaceId));

        Issue parent = null;
        if (dto.parentId() != null) {
            parent = issueRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent issue not found"));
            if (!parent.getWorkspace().getId().equals(workspaceId)) {
                throw new IllegalArgumentException("Parent issue belongs to different workspace");
            }
        }

        var issue = new Issue();
        issue.setTitle(dto.title());
        issue.setDescription(dto.description());
        issue.setType(dto.type());
        issue.setPoints(dto.points());
        issue.setStatus(dto.status() != null ? dto.status() : com.example.projective.entity.IssueStatus.TODO);
        issue.setWorkspace(workspace);
        issue.setParent(parent);
        var saved = issueRepository.save(issue);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<IssuePayload.View> getIssues(Long workspaceId) {
        return issueRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IssuePayload.View getIssue(Long workspaceId, Long issueId) {
        return toView(getVerifiedIssue(workspaceId, issueId));
    }

    public IssuePayload.View updateIssue(Long workspaceId, Long issueId, IssuePayload.Create dto) {
        var issue = getVerifiedIssue(workspaceId, issueId);
        if (dto.title() != null)
            issue.setTitle(dto.title());
        issue.setDescription(dto.description());
        if (dto.points() != null)
            issue.setPoints(dto.points());
        if (dto.status() != null)
            issue.setStatus(dto.status());
        var saved = issueRepository.save(issue);
        return toView(saved);
    }

    public void changeStatus(Long workspaceId, Long issueId, IssueStatus newStatus) {
        var issue = getVerifiedIssue(workspaceId, issueId);
        if (!issue.getStatus().canTransitionTo(newStatus)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition from " + issue.getStatus() + " to " + newStatus);
        }
        // if closing parent ensure all subtasks done
        if (newStatus == IssueStatus.DONE && issue.getSubtasks() != null && !issue.getSubtasks().isEmpty()) {
            long notDone = issueRepository.countByParentIdAndStatusNot(issueId, IssueStatus.DONE);
            if (notDone > 0) {
                throw new InvalidStateTransitionException("All subtasks must be DONE before closing this issue");
            }
        }
        issue.setStatus(newStatus);
        issueRepository.save(issue);
    }

    public void deleteIssue(Long workspaceId, Long issueId) {
        var issue = getVerifiedIssue(workspaceId, issueId);
        issueRepository.delete(issue);
    }

    private Issue getVerifiedIssue(Long workspaceId, Long issueId) {
        var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        if (!issue.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Issue does not belong to workspace " + workspaceId);
        }
        return issue;
    }

    private IssuePayload.View toView(Issue entity) {
        return new IssuePayload.View(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getType(),
                entity.getStatus(), entity.getPoints(), entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getWorkspace() != null ? entity.getWorkspace().getId() : null);
    }
}
