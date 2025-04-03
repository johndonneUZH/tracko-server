package ch.uzh.ifi.hase.soprafs24.models.change;

import ch.uzh.ifi.hase.soprafs24.constant.ChangeType;

public class ChangeRegister {
    private ChangeType changeType;
    private String changeDescription;

    public ChangeType getChangeType() { return changeType; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }

    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
}

