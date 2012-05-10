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

import java.security.Permission;

/**
 * @author dpishchukhin
 */
public class NonePermission extends Permission {
    public static final NonePermission INSTANCE = new NonePermission();

    public NonePermission() {
        super("<no permissions>");
    }

    @Override
    public boolean implies(Permission permission) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NonePermission;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String getActions() {
        return "<none actions>";
    }
}
