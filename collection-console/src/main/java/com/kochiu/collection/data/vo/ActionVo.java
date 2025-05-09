package com.kochiu.collection.data.vo;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ActionVo {

    Long actionId;
    String actionCode;
    String actionName;
    boolean selected;

    public boolean equals(Object object){
        if(object instanceof ActionVo actionVo){
            return actionVo.getActionId().equals(this.actionId);
        }
        else{
            return false;
        }
    }

    public int hashCode(){
        return actionId.hashCode();
    }
}
