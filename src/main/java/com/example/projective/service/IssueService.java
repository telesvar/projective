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
import com.example.projective.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;

    public IssuePayload.View createIssue(String teamSlug, String workspaceSlug, Long projectId, IssuePayload.Create dto) {
        var project = projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(projectId, workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Issue parent = null;
        if (dto.parentId() != null) {
            parent = issueRepository.findById(dto.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent issue not found"));
            if (!parent.getProject().getId().equals(projectId)) {
                throw new IllegalArgumentException("Parent issue belongs to different project");
            }
        }

        var issue = new Issue();
        issue.setTitle(dto.title());
        issue.setDescription(dto.description());
        issue.setType(dto.type());
        issue.setPoints(dto.points());
        issue.setStatus(dto.status() != null ? dto.status() : com.example.projective.entity.IssueStatus.TODO);
        issue.setProject(project);
        issue.setParent(parent);
        var saved = issueRepository.save(issue);
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public List<IssuePayload.View> getIssues(String teamSlug, String workspaceSlug, Long projectId) {
        var project = projectRepository.findByIdAndWorkspaceSlugAndWorkspaceTeamSlug(projectId, workspaceSlug, teamSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return issueRepository.findByProjectId(project.getId()).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IssuePayload.View getIssue(String teamSlug, String workspaceSlug, Long projectId, Long issueId) {
        return toView(getVerifiedIssue(teamSlug, workspaceSlug, projectId, issueId));
    }

    public IssuePayload.View updateIssue(String teamSlug, String workspaceSlug, Long projectId, Long issueId, IssuePayload.Create dto) {
        var issue = getVerifiedIssue(teamSlug, workspaceSlug, projectId, issueId);
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

    public void changeStatus(String teamSlug, String workspaceSlug, Long projectId, Long issueId, IssueStatus newStatus) {
        var issue = getVerifiedIssue(teamSlug, workspaceSlug, projectId, issueId);
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

    public void deleteIssue(String teamSlug, String workspaceSlug, Long projectId, Long issueId) {
        var issue = getVerifiedIssue(teamSlug, workspaceSlug, projectId, issueId);
        issueRepository.delete(issue);
    }

    private Issue getVerifiedIssue(String teamSlug, String workspaceSlug, Long projectId, Long issueId) {
        var issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
        if (!issue.getProject().getId().equals(projectId)
                || !issue.getProject().getWorkspace().getSlug().equals(workspaceSlug)
                || !issue.getProject().getWorkspace().getTeam().getSlug().equals(teamSlug)) {
            throw new ResourceNotFoundException("Issue does not belong to specified project/workspace/team");
        }
        return issue;
    }

    private IssuePayload.View toView(Issue entity) {
        return new IssuePayload.View(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getType(),
                entity.getStatus(), entity.getPoints(), entity.getParent() != null ? entity.getParent().getId() : null,
                entity.getProject() != null ? entity.getProject().getId() : null);
    }
}
