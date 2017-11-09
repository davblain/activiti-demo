package org.korbit.test.activiti.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.korbit.test.activiti.models.ActionType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Map;
@Getter
@Setter
public class ActionDto implements Serializable {
    @NotNull
    String creator;
    LocalTime time = LocalTime.now();
    Map<String,String> data;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    ActionType type;
}
