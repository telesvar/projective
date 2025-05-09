package com.example.projective.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.projective.entity.*;
import com.example.projective.exception.InvalidStateTransitionException;
import com.example.projective.payload.IssuePayload;
import com.example.projective.repository.IssueRepository;
import com.example.projective.repository.WorkspaceRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    private IssueService issueService;

    private Workspace workspace;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        workspace = Workspace.builder().id(1L).name("WS").slug("ws").build();
        issueService = new IssueService(issueRepository, workspaceRepository);
    }

    @Test
    void changeStatus_shouldAllowAdjacentMove() {
        Issue issue = Issue.builder().id(10L).workspace(workspace).status(IssueStatus.TODO).build();
        when(issueRepository.findById(10L)).thenReturn(Optional.of(issue));

        issueService.changeStatus(1L, 10L, IssueStatus.IN_PROGRESS);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
        verify(issueRepository).save(issue);
    }

    @Test
    void changeStatus_skipStep_shouldThrow() {
        Issue issue = Issue.builder().id(11L).workspace(workspace).status(IssueStatus.TODO).build();
        when(issueRepository.findById(11L)).thenReturn(Optional.of(issue));

        assertThatThrownBy(() -> issueService.changeStatus(1L, 11L, IssueStatus.IN_REVIEW))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void changeStatus_closeParentWithOpenSubtasks_shouldThrow() {
        Issue parent = Issue.builder().id(20L).workspace(workspace).status(IssueStatus.IN_PROGRESS).build();
        when(issueRepository.findById(20L)).thenReturn(Optional.of(parent));
        when(issueRepository.countByParentIdAndStatusNot(eq(20L), eq(IssueStatus.DONE))).thenReturn(1L);

        assertThatThrownBy(() -> issueService.changeStatus(1L, 20L, IssueStatus.DONE))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void createIssue_invalidParentWorkspace_shouldThrow() {
        Issue parent = Issue.builder().id(30L).workspace(Workspace.builder().id(2L).build()).build();
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(issueRepository.findById(30L)).thenReturn(Optional.of(parent));

        IssuePayload.Create dto = new IssuePayload.Create("Child", null, IssueType.TASK, null, 30L, null);

        assertThatThrownBy(() -> issueService.createIssue(1L, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
