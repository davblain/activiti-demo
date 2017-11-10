package org.korbit.test.activiti.domain;

import lombok.Getter;
import lombok.Setter;
import org.korbit.test.activiti.models.ActionType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class GroupPermission {

    @Id
    @GeneratedValue
    UUID uuid;
    @Column(unique = true)
    String groupId;
    @ElementCollection(fetch = FetchType.EAGER)
    List<ActionType> actionTypesIfNotAssigner = new ArrayList<>();
    @ElementCollection
    List<ActionType> actionTypesIfAssigner = new ArrayList<>();
    public GroupPermission() {

    }

}
