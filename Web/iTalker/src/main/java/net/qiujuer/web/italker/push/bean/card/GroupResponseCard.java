package net.qiujuer.web.italker.push.bean.card;

import com.google.gson.annotations.Expose;

import java.util.Set;

public class GroupResponseCard {
    @Expose
    String groupId;
    @Expose
    Set<String> members;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }
}
