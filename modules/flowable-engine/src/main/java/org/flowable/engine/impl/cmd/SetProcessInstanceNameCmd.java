/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.impl.cmd;

import java.io.Serializable;

import org.flowable.engine.common.api.FlowableException;
import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.engine.common.api.FlowableObjectNotFoundException;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.runtime.ProcessInstance;

public class SetProcessInstanceNameCmd implements Command<Void>, Serializable {

    private static final long serialVersionUID = 1L;

    protected String processInstanceId;
    protected String name;

    public SetProcessInstanceNameCmd(String processInstanceId, String name) {
        this.processInstanceId = processInstanceId;
        this.name = name;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        if (processInstanceId == null) {
            throw new FlowableIllegalArgumentException("processInstanceId is null");
        }

        ExecutionEntity execution = commandContext.getExecutionEntityManager().findById(processInstanceId);

        if (execution == null) {
            throw new FlowableObjectNotFoundException("process instance " + processInstanceId + " doesn't exist", ProcessInstance.class);
        }

        if (!execution.isProcessInstanceType()) {
            throw new FlowableObjectNotFoundException("process instance " + processInstanceId + " doesn't exist, the given ID references an execution, though", ProcessInstance.class);
        }

        if (execution.isSuspended()) {
            throw new FlowableException("process instance " + processInstanceId + " is suspended, cannot set name");
        }

        // Actually set the name
        execution.setName(name);

        // Record the change in history
        commandContext.getHistoryManager().recordProcessInstanceNameChange(processInstanceId, name);

        return null;
    }

}
