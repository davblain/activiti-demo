package org.korbit.test.activiti.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Date;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DelegateNotification.class,name = "delegate")
})
public class Notification {
    private String creator;
    private Date time = new Date();
    Notification(String creator) {
        this.creator = creator;
    }
}
