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

import org.osgi.service.monitor.MonitorListener;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author dpishchukhin
 */
public class MockMonitorable implements Monitorable {
    private SortedMap<String, StatusVariable> variables = new TreeMap<String, StatusVariable>();
    private Set<String> notifiableVariables = new HashSet<String>();
    private String monitorableId;
    private MonitorListener listener;

    public MockMonitorable(StatusVariable... statusVariables) {
        setStatusVariables(statusVariables);
    }

    public void setMonitorableId(String monitorableId) {
        this.monitorableId = monitorableId;
    }

    public void setListener(MonitorListener listener) {
        this.listener = listener;
    }

    public void setStatusVariables(StatusVariable... statusVariables) {
        for (StatusVariable statusVariable : statusVariables) {
            variables.put(statusVariable.getID(), statusVariable);
        }
    }

    public void setNotificationSupport(String id, boolean on) {
        if (on) {
            notifiableVariables.add(id);
        } else {
            notifiableVariables.remove(id);
        }
    }

    public String[] getStatusVariableNames() {
        Set<String> names = variables.keySet();
        return names.toArray(new String[names.size()]);
    }

    public StatusVariable getStatusVariable(String s) throws IllegalArgumentException {
        StatusVariable statusVariable = variables.get(s);
        if (statusVariable == null) {
            throw new IllegalArgumentException();
        }
        return statusVariable;
    }

    public boolean notifiesOnChange(String s) throws IllegalArgumentException {
        return notifiableVariables.contains(s);
    }

    public boolean resetStatusVariable(String s) throws IllegalArgumentException {
        StatusVariable statusVariable = getStatusVariable(s);
        if (statusVariable != null) {
            switch (statusVariable.getType()) {
                case StatusVariable.TYPE_BOOLEAN:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), false));
                    break;
                case StatusVariable.TYPE_FLOAT:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), 0));
                    break;
                case StatusVariable.TYPE_INTEGER:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), 0));
                    break;
                case StatusVariable.TYPE_STRING:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), ""));
                    break;
            }
            if (notifiableVariables.contains(s) && listener != null) {
                listener.updated(monitorableId, getStatusVariable(s));
            }
            return true;
        }
        return false;
    }

    public String getDescription(String s) throws IllegalArgumentException {
        return getStatusVariable(s).getID();
    }

    public void setNewStatusVariableValue(String s, String value) {
        StatusVariable statusVariable = getStatusVariable(s);
        if (statusVariable != null) {
            switch (statusVariable.getType()) {
                case StatusVariable.TYPE_BOOLEAN:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), Boolean.valueOf(value)));
                    break;
                case StatusVariable.TYPE_FLOAT:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), Float.valueOf(value)));
                    break;
                case StatusVariable.TYPE_INTEGER:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), Integer.valueOf(value)));
                    break;
                case StatusVariable.TYPE_STRING:
                    setStatusVariables(new StatusVariable(statusVariable.getID(), statusVariable.getCollectionMethod(), value));
                    break;
            }
            if (notifiableVariables.contains(s) && listener != null) {
                listener.updated(monitorableId, getStatusVariable(s));
            }
        }
    }
}
