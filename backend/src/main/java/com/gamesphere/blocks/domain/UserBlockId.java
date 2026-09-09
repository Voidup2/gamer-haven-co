package com.gamesphere.blocks.domain;

import java.io.Serializable;

public class UserBlockId implements Serializable {
    private Long blocker;
    private Long blocked;
    public UserBlockId() {}
    public UserBlockId(Long blocker, Long blocked) { this.blocker = blocker; this.blocked = blocked; }
    public Long getBlocker() { return blocker; }
    public Long getBlocked() { return blocked; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof UserBlockId that)) return false; return java.util.Objects.equals(blocker, that.blocker) && java.util.Objects.equals(blocked, that.blocked); }
    @Override public int hashCode() { return java.util.Objects.hash(blocker, blocked); }
}
