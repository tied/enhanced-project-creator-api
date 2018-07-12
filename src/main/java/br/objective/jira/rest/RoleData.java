package br.objective.jira.rest;

import java.util.Objects;

import com.atlassian.jira.security.roles.ProjectRole;

public class RoleData {

    public Long id;
    public String name;

    public RoleData() {}

    public RoleData(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static RoleData from(ProjectRole role) {
        return new RoleData(role.getId(), role.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleData roleData = (RoleData) o;
        return Objects.equals(id, roleData.id) &&
                Objects.equals(name, roleData.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }
}
