/**
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.parameter.command.TacokitCommand;

import lombok.Data;

/**
 * ElementParameter which represents button and stores button command, which is executed on button pushed
 */
@Data
public class ButtonParameter extends TaCoKitElementParameter {

    private TacokitCommand command;

    public ButtonParameter(final IElement element) {
        super(element);
        setFieldType(EParameterFieldType.TACOKIT_BUTTON);
    }

}