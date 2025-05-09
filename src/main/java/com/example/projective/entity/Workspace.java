package com.example.projective.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "workspaces", uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "slug"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> projects = new java.util.ArrayList<>();

    public void addProject(Project project) {
        projects.add(project);
        project.setWorkspace(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setWorkspace(null);
    }
}
