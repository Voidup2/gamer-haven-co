package com.gamesphere.groups;

import com.gamesphere.groups.api.GroupRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupApiContractTest {
    @Test
    void groupRequestAcceptsPublicGroupConfiguration() {
        GroupRequest request = new GroupRequest("PC Gamers", "PC gaming community", null, true);
        assertEquals("PC Gamers", request.name());
        assertTrue(request.publicGroup());
    }
}
