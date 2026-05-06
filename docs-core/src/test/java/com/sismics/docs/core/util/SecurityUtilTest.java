package com.sismics.docs.core.util;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

public class SecurityUtilTest {

    @Test
    public void testSkipAclCheck_containsAdmin() {
        assertTrue(SecurityUtil.skipAclCheck(Arrays.asList("admin", "user")));
    }

    @Test
    public void testSkipAclCheck_containsAdministrators() {
        assertTrue(SecurityUtil.skipAclCheck(Arrays.asList("administrators", "user")));
    }

    @Test
    public void testSkipAclCheck_notContains() {
        assertFalse(SecurityUtil.skipAclCheck(Collections.singletonList("user")));
    }
}
