package com.example.projective.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String slug;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Workspace> workspaces = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    public void addWorkspace(Workspace ws) {
        workspaces.add(ws);
        ws.setTeam(this);
    }

    public void removeWorkspace(Workspace ws) {
        workspaces.remove(ws);
        ws.setTeam(null);
    }

    public void addProject(Project project) {
        projects.add(project);
        project.setTeam(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.setTeam(null);
    }
}
