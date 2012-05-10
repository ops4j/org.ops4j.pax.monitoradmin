/*
 * Copyright (c) 2012 Dmytro Pishchukhin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.monitoradmin;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.monitoradmin.mocks.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.monitor.*;

import java.security.AllPermission;
import java.security.Permission;
import java.util.HashMap;

/**
 * @author dmytro.pishchukhin
 */
public class MonitorAdminImplSecurityTest {
    private MockOsgiVisitor osgiVisitor;
    private MockLogVisitor logVisitor;
    private MonitorAdminCommon common;

    @Before
    public void init() {
        osgiVisitor = new MockOsgiVisitor();
        logVisitor = new MockLogVisitor();
        common = new MonitorAdminCommon(osgiVisitor, logVisitor);
    }

    @After
    public void uninit() {
        if (common != null) {
            common.cancelAllJobs();
        }
    }

    private Bundle createMockBundle(Permission... permisions) {
        if (permisions == null || permisions.length == 0) {
            return new SecutiryMockBundle(new AllPermission());
        } else {
            return new SecutiryMockBundle(permisions);
        }
    }

    @Test
    public void testGetMonitorableNames_NoMonitorableAvailable() throws Exception {
        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());
        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(0, monitorableNames.length);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();
        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid2"),
                new MockMonitorable());
        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid3"),
                new MockMonitorable());
        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid1"),
                new MockMonitorable());
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(3, monitorableNames.length);
        Assert.assertEquals("com.acme.pid1", monitorableNames[0]);
        Assert.assertEquals("com.acme.pid2", monitorableNames[1]);
        Assert.assertEquals("com.acme.pid3", monitorableNames[2]);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_WithMonitorPermission_for_all_sv() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();
        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid2"), new MockMonitorable());
        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid3"), new MockMonitorable());
        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid1"), new MockMonitorable());
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(new MonitorPermission("*/*", MonitorPermission.READ)));

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(3, monitorableNames.length);
        Assert.assertEquals("com.acme.pid1", monitorableNames[0]);
        Assert.assertEquals("com.acme.pid2", monitorableNames[1]);
        Assert.assertEquals("com.acme.pid3", monitorableNames[2]);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_WithMonitorPermission_not_for_all_sv() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("com.acme.pid2/*", MonitorPermission.PUBLISH)),
                "com.acme.pid2"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid3"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid1"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(new MonitorPermission("*/*", MonitorPermission.READ)));

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(2, monitorableNames.length);
        Assert.assertEquals("com.acme.pid1", monitorableNames[0]);
        Assert.assertEquals("com.acme.pid2", monitorableNames[1]);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_WithMonitorPermission_not_for_all_sv2() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("com.acme.pid2/*", MonitorPermission.PUBLISH)),
                "com.acme.pid2"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid3"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid1"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(new MonitorPermission("com.acme.pid2/*", MonitorPermission.READ)));

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(1, monitorableNames.length);
        Assert.assertEquals("com.acme.pid2", monitorableNames[0]);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_WithMonitorPermission_not_for_all_sv3() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid2.very.long.monitorable.id"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid3"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));

        map.put(new MonitorableMockServiceReference(createMockBundle(new MonitorPermission("*/*", MonitorPermission.PUBLISH)),
                "com.acme.pid1"), new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0)));
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(new MonitorPermission("com.acme.pid2/*", MonitorPermission.READ)));

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(0, monitorableNames.length);
    }


    @Test
    public void testGetStatusVariable_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id");
        Assert.assertNotNull(sv);
        Assert.assertEquals("sv.id", sv.getID());
        Assert.assertEquals(StatusVariable.CM_CC, sv.getCollectionMethod());
        Assert.assertEquals(StatusVariable.TYPE_INTEGER, sv.getType());
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test
    public void testGetStatusVariable_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.PUBLISH)),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common,
                createMockBundle(new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.READ)));

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id");
        Assert.assertNotNull(sv);
        Assert.assertEquals("sv.id", sv.getID());
        Assert.assertEquals(StatusVariable.CM_CC, sv.getCollectionMethod());
        Assert.assertEquals(StatusVariable.TYPE_INTEGER, sv.getType());
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariable_WithMonitorPermissions_noPublishPermission() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common,
                createMockBundle(new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.READ)));

        monitorAdmin.getStatusVariable("com.acme.pid/sv.id");
    }

    @Test(expected = SecurityException.class)
    public void testGetStatusVariable_WithMonitorPermissions_noReadPermission() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.PUBLISH)),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(NonePermission.INSTANCE));

        monitorAdmin.getStatusVariable("com.acme.pid/sv.id");
    }

    @Test
    public void testGetDescription_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        String description = monitorAdmin.getDescription("com.acme.pid/sv.id");
        Assert.assertNotNull(description);
        Assert.assertEquals("sv.id", description);
    }

    @Test
    public void testGetDescription_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.PUBLISH)),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.READ)
        ));

        String description = monitorAdmin.getDescription("com.acme.pid/sv.id");
        Assert.assertNotNull(description);
        Assert.assertEquals("sv.id", description);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDescription_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.READ)
        ));

        monitorAdmin.getDescription("com.acme.pid/sv.id");
    }

    @Test(expected = SecurityException.class)
    public void testGetDescription_NoReadPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id", StatusVariable.CM_CC, 0));

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id", MonitorPermission.PUBLISH)),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(NonePermission.INSTANCE));

        monitorAdmin.getDescription("com.acme.pid/sv.id");
    }

    @Test
    public void testGetStatusVariableNames_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(2, names.length);
        Assert.assertTrue("sv.id1".equals(names[0]));
        Assert.assertTrue("sv.id2".equals(names[1]));
    }

    @Test
    public void testGetStatusVariableNames_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.READ)
        ));

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(2, names.length);
        Assert.assertTrue("sv.id1".equals(names[0]));
        Assert.assertTrue("sv.id2".equals(names[1]));
    }

    @Test
    public void testGetStatusVariableNames_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(NonePermission.INSTANCE),
                "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.READ)
        ));

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(0, names.length);
    }

    @Test
    public void testGetStatusVariableNames_NoReadPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE));

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(0, names.length);
    }

    @Test
    public void testGetStatusVariableNames_WithPartialPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test"),
                new StatusVariable("sv.id3", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.READ),
                new MonitorPermission("com.acme.pid/sv.id3", MonitorPermission.READ)
        ));

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(1, names.length);
        Assert.assertTrue("sv.id2".equals(names[0]));
    }

    @Test
    public void testGetStatusVariables_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(2, variables.length);
        Assert.assertTrue("sv.id1".equals(variables[0].getID()));
        Assert.assertTrue("sv.id2".equals(variables[1].getID()));
    }

    @Test
    public void testGetStatusVariables_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.READ)
        ));

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(2, variables.length);
        Assert.assertTrue("sv.id1".equals(variables[0].getID()));
        Assert.assertTrue("sv.id2".equals(variables[1].getID()));
    }

    @Test
    public void testGetStatusVariables_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.READ)
        ));

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(0, variables.length);
    }

    @Test
    public void testGetStatusVariables_NoReadPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/*", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(0, variables.length);
    }

    @Test
    public void testGetStatusVariables_WithPartialPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test"),
                new StatusVariable("sv.id3", StatusVariable.CM_CC, "test")
        );

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.READ),
                new MonitorPermission("com.acme.pid/sv.id3", MonitorPermission.READ)
        ));

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(1, variables.length);
        Assert.assertTrue("sv.id2".equals(variables[0].getID()));
    }

    @Test
    public void testResetStatusVariable_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.READ),
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.RESET)
        ));

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(15, sv.getInteger());

        boolean result = monitorAdmin.resetStatusVariable("com.acme.pid/sv.id1");
        Assert.assertTrue(result);

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test
    public void testResetStatusVariable_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(15, sv.getInteger());

        boolean result = monitorAdmin.resetStatusVariable("com.acme.pid/sv.id1");
        Assert.assertTrue(result);

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResetStatusVariable_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.READ),
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.RESET)
        ));

        monitorAdmin.resetStatusVariable("com.acme.pid/sv.id1");
    }

    @Test(expected = SecurityException.class)
    public void testResetStatusVariable_NoResetPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );

        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.READ)
        ));

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(15, sv.getInteger());

        monitorAdmin.resetStatusVariable("com.acme.pid/sv.id1");
    }

    @Test
    public void testSwitchEvents_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);

        String[] paths = common.getDisabledNotificationPaths();
        Assert.assertNotNull(paths);
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);

        monitorAdmin.switchEvents("*/sv.id1", false);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("*/*", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);
    }

    @Test
    public void testSwitchEvents_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.SWITCHEVENTS)
        ));

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);

        String[] paths = common.getDisabledNotificationPaths();
        Assert.assertNotNull(paths);
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);

        monitorAdmin.switchEvents("*/sv.id1", false);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("*/*", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.SWITCHEVENTS)
        ));

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_NoPublishPermissions_WithWildcard() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.SWITCHEVENTS)
        ));

        monitorAdmin.switchEvents("*/*", false);
    }

    @Test(expected = SecurityException.class)
    public void testSwitchEvents_NoSwitchEventdPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);
    }

    @Test(expected = SecurityException.class)
    public void testSwitchEvents_NoSwitchEventdPermissions_WithWildcard() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(new StatusVariable("sv.id1", StatusVariable.CM_CC, 0));
        monitorable.setNotificationSupport("sv.id1", true);
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        monitorAdmin.switchEvents("*/*", false);
    }

    @Test
    public void testStartJob_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        MonitoringJob job = monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1);
        job.stop();
    }

    @Test
    public void testStartJob_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        MonitoringJob job = monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1);
        job.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1);
    }

    @Test(expected = SecurityException.class)
    public void testStartJob_NoStartJobPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1);
    }

    @Test
    public void testStartScheduledJob_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        MonitoringJob job = monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 5, 0);
        job.stop();
    }

    @Test
    public void testStartScheduledJob_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        MonitoringJob job = monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 5, 0);
        job.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_NoPublishPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        map.put(new MonitorableMockServiceReference(createMockBundle(
                NonePermission.INSTANCE
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 5, 0);
    }

    @Test(expected = SecurityException.class)
    public void testStartScheduledJob_NoStartJobPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 5, 0);
    }

    @Test(expected = SecurityException.class)
    public void testStartScheduledJob_NoStartJobFrequencyPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB + ":10")
        ));

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 5, 0);
    }

    @Test
    public void testGetRunningJobs_WithAllPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle());

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(2, jobs.length);

        scheduleJob.stop();

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(1, jobs.length);

        job.stop();

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);
    }

    @Test
    public void testGetRunningJobs_WithMonitorPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(2, jobs.length);

        scheduleJob.stop();

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(1, jobs.length);

        job.stop();

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);
    }

    @Test
    public void testGetRunningJobs_WithoutPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                NonePermission.INSTANCE
        ));

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        scheduleJob.stop();
        job.stop();
    }

    @Test
    public void testGetRunningJobs_WithPartialPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB)
        ));

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(1, jobs.length);

        scheduleJob.stop();
        job.stop();
    }

    @Test
    public void testGetRunningJobs_WithNoFrequencyPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB + ":10")
        ));

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        scheduleJob.stop();
        job.stop();
    }

    @Test
    public void testGetRunningJobs_WithFrequencyPermissions() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable(
                new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
                new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        );
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference(createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.PUBLISH),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.PUBLISH)
        ), "com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdminJobsCreator = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id1", MonitorPermission.STARTJOB),
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB)
        ));

        MonitorAdmin monitorAdminJobsConsumer = new MonitorAdminImpl(logVisitor, common, createMockBundle(
                new MonitorPermission("com.acme.pid/sv.id2", MonitorPermission.STARTJOB + ":2")
        ));

        MonitoringJob[] jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdminJobsCreator.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);
        MonitoringJob scheduleJob = monitorAdminJobsCreator.startScheduledJob("initiator", new String[]{"com.acme.pid/sv.id2"}, 5, 0);

        jobs = monitorAdminJobsConsumer.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(1, jobs.length);

        scheduleJob.stop();
        job.stop();
    }
}
