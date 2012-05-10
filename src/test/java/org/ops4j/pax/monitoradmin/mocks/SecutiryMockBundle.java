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

package org.ops4j.pax.monitoradmin.mocks;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockBundle;

import java.security.Permission;
import java.util.Dictionary;

/**
 * @author dpishchukhin
 */
public class SecutiryMockBundle extends MockBundle {
    private Permission[] permissions;

    public SecutiryMockBundle() {
    }

    public SecutiryMockBundle(Permission... permissions) {
        this.permissions = permissions;
    }

    public SecutiryMockBundle(Dictionary headers) {
        super(headers);
    }

    public SecutiryMockBundle(BundleContext context) {
        super(context);
    }

    public SecutiryMockBundle(String symName) {
        super(symName);
    }

    public SecutiryMockBundle(String symName, Dictionary headers, BundleContext context) {
        super(symName, headers, context);
    }

    @Override
    public boolean hasPermission(Object permission) {
        if (permissions != null) {
            for (Permission perm : permissions) {
                if (perm.implies((Permission) permission)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
}
