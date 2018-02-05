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

import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_VALIDATION_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ACTION_VALIDATION_PARAMETERS;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_TARGET;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONDITION_IF_VALUE;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONFIG_NAME;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.CONFIG_TYPE;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.MAIN_FORM;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.ORDER_SEPARATOR;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_PREFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_GRIDLAYOUT_SUFFIX;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.UI_OPTIONS_ORDER;
import static org.talend.sdk.component.studio.model.parameter.Metadatas.VALUE_SEPARATOR;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.talend.sdk.component.server.front.model.PropertyValidation;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

import lombok.Data;
import lombok.experimental.Delegate;

/**
 * Extends functionality of {@link SimplePropertyDefinition}
 * It doesn't allow to change <code>delegate</code> state
 */
class PropertyDefinitionDecorator extends SimplePropertyDefinition {

    /**
     * Separator in property path
     */
    private static final String PATH_SEPARATOR = ".";

    /**
     * Denotes that some property has no parent property
     */
    private static final String NO_PARENT_ID = "";

    /**
     * Suffix used in id ({@link SimplePropertyDefinition#getPath()}), which denotes Array typed property
     * (which is Table property in Studio)
     */
    private static final String ARRAY_PATH = "[]";

    /**
     * Regex, which contains 2 separators used in ui:gridlayout value: ',' and '|'
     * It used to split ui:gridlayout value
     */
    private static final String GRIDLAYOUT_SEPARATOR = ",|\\|";

    @Delegate
    private final SimplePropertyDefinition delegate;

    /**
     * Creates instance by wrapping existing {@link SimplePropertyDefinition} instance
     * All calls to {@link SimplePropertyDefinition} API will be delegated to wrapped instance
     * 
     * @param property {@link SimplePropertyDefinition} to wrap
     */
    PropertyDefinitionDecorator(final SimplePropertyDefinition property) {
        this.delegate = property;
    }

    /**
     * Wraps {@link Collection} or {@link SimplePropertyDefinition} to {@link Collection} of
     * {@link PropertyDefinitionDecorator}
     * 
     * @param properties original properties collection
     * @return wrapped properties
     */
    static Collection<PropertyDefinitionDecorator> wrap(final Collection<SimplePropertyDefinition> properties) {
        return properties.stream().map(property -> new PropertyDefinitionDecorator(property)).collect(
                Collectors.toList());
    }

    /**
     * Returns path for parent {@link SimplePropertyDefinition}
     * 
     * @return parent path
     */
    String getParentPath() {
        final String path = delegate.getPath();
        if (!path.contains(PATH_SEPARATOR)) {
            return NO_PARENT_ID;
        }
        String parentPath = path.substring(0, path.lastIndexOf("."));
        // following is true, when parent has type=ARRAY
        if (parentPath.endsWith(ARRAY_PATH)) {
            parentPath = parentPath.substring(0, parentPath.lastIndexOf("[]"));
        }
        return parentPath;
    }

    /**
     * Returns children names specified in ui::gridlayout metadata value.
     * Note, if Property has no any grid layout value, then such Property
     * has Main form by default. Such Main form contains either Properties
     * specified in ui::optionsorder metadata value or all Properties, when
     * ui::optionsorder is also absent
     * 
     * @param form Name of UI form
     * @return children names specified in ui::gridlayout metadata value
     */
    Set<String> getChildrenNames(final String form) {
        if (hasGridLayout(form)) {
            final String gridLayout = delegate.getMetadata().get(buildGridLayoutKey(form));
            final String[] names = gridLayout.split(GRIDLAYOUT_SEPARATOR);
            return new HashSet<>(Arrays.asList(names));
        }
        return Collections.emptySet();
    }

    /**
     * Returns children names specified in ui:optionsorder metadata value.
     * 
     * @return children names specified in ui:optionsorder metadata value
     */
    Set<String> getOptionsOrderNames() {
        if (hasOptionsOrder()) {
            final String optionsOrder = delegate.getMetadata().get(UI_OPTIONS_ORDER);
            final String[] names = optionsOrder.split(ORDER_SEPARATOR);
            return new HashSet<>(Arrays.asList(names));
        }
        return Collections.emptySet();
    }

    /**
     * Computes order in which children should appear on UI
     * Order is represented as a map, which key is child name and value is child order
     * 
     * @param form Name of UI form
     * @return order map or null, if there is no order for specified form
     */
    HashMap<String, Integer> getChildrenOrder(final String form) {
        if (MAIN_FORM.equals(form)) {
            return getMainChildrenOrder();
        }
        return getGridLayoutOrder(form);
    }

    /**
     * Computes order in which Main children should appear on UI.
     * It is computes either according ui:gridlayout or ui:optionsorder.
     * If both metadata is absent, then there is no order for Main form and
     * null is returned. </br>
     * Order is represented as a map, which key is child name and value is child order
     * 
     * @return order map or null, if there is no order for Main form
     */
    private HashMap<String, Integer> getMainChildrenOrder() {
        if (hasGridLayout(MAIN_FORM)) {
            return getGridLayoutOrder(MAIN_FORM);
        }
        if (hasOptionsOrder()) {
            return getOptionsOrder();
        }
        return null;
    }

    /**
     * Computes order in which children should appear on UI according ui:gridlayout metadata value
     * Order is represented as a map, which key is child name and value is child order
     * 
     * @param form Name of UI form
     * @return order map or null, if there is no grid layout for specified form
     */
    private HashMap<String, Integer> getGridLayoutOrder(final String form) {
        if (!hasGridLayout(form)) {
            return null;
        }
        final HashMap<String, Integer> order = new HashMap<>();
        final String gridLayout = delegate.getMetadata().get(buildGridLayoutKey(form));
        final String[] children = gridLayout.split(GRIDLAYOUT_SEPARATOR);
        for (int i = 0; i < children.length; i++) {
            order.put(children[i], i);
        }
        return order;
    }

    /**
     * Computes order in which children should appear on UI according ui:optionsorder metadata value
     * Order is represented as a map, which key is child name and value is child order
     * 
     * @return order map or null, if there is no grid layout for specified form
     */
    private HashMap<String, Integer> getOptionsOrder() {
        final HashMap<String, Integer> order = new HashMap<>();
        final String optionsOrder = delegate.getMetadata().get(UI_OPTIONS_ORDER);
        final String[] children = optionsOrder.split(ORDER_SEPARATOR);
        for (int i = 0; i < children.length; i++) {
            order.put(children[i], i);
        }
        return order;
    }

    /**
     * Checks whether it has grid layout metadata for specified <code>form</code>
     * 
     * @param form Name of UI form
     * @return true, if is has requested grid layout; false - otherwise
     */
    boolean hasGridLayout(final String form) {
        if (form == null) {
            return false;
        }
        return delegate.getMetadata().containsKey(buildGridLayoutKey(form));
    }

    /**
     * Checks whether it has any ui:gridlayout metadata
     * 
     * @return true, if it has any grid layout; false - otherwise
     */
    boolean hasGridLayouts() {
        final Set<String> keys = delegate.getMetadata().keySet();
        return keys.stream().filter(key -> key.startsWith(UI_GRIDLAYOUT_PREFIX)).count() > 0;
    }

    /**
     * Checks whether it has options order metadata
     * 
     * @return true, if it has; false - otherwise
     */
    boolean hasOptionsOrder() {
        return delegate.getMetadata().containsKey(UI_OPTIONS_ORDER);
    }

    /**
     * Checks whether it has configurationtype::type metadata
     * 
     * @return true, it has configuration type; false - otherwise
     */
    boolean hasConfigurationType() {
        return delegate.getMetadata().containsKey(CONFIG_TYPE);
    }

    /**
     * Returns configurationtype::type metadata value or null if it is absent
     * 
     * @return configuration type
     */
    String getConfigurationType() {
        return delegate.getMetadata().get(CONFIG_TYPE);
    }

    /**
     * Returns configurationtype::name metadata value or null if it is absent
     * 
     * @return configuration type name
     */
    String getConfigurationTypeName() {
        return delegate.getMetadata().get(CONFIG_NAME);
    }

    /**
     * Builds full grid layout metadata key with specified <code>formName</code>
     * 
     * @param formName Name of UI form
     * @return grid layout metadata key
     */
    private String buildGridLayoutKey(final String formName) {
        return UI_GRIDLAYOUT_PREFIX + formName + UI_GRIDLAYOUT_SUFFIX;
    }

    /**
     * Checks whether specified <code>child</code> is a column on specified <code>form</code>
     * It is a column, when it is second property on the same line.
     * Consider following ui::gridlayout value: "p1|p2,p3,p4".
     * p1 and p2 are not columns and p3, p4 are columns.
     * 
     * @param child Child Property name
     * @param form Name of form
     * @return true, if it is column; false - otherwise
     */
    boolean isColumn(final String child, final String form) {
        if (!hasGridLayout(form)) {
            return false;
        }
        final String gridLayout = delegate.getMetadata().get(buildGridLayoutKey(form));
        final String[] rows = gridLayout.split("\\|");
        for (final String row : rows) {
            final String[] columns = row.split(",");
            for (int i = 1; i < columns.length; i++) {
                if (child.equals(columns[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    Stream<Condition> getConditions() {
        return delegate.getMetadata().entrySet().stream().filter(e -> e.getKey().startsWith(CONDITION_IF_TARGET)).map(
                e -> {
                    final String[] split = e.getKey().split("::");
                    final String valueKey =
                            CONDITION_IF_VALUE + (split.length == 4 ? "::" + split[split.length - 1] : "");
                    return new Condition(e.getValue(),
                            delegate.getMetadata().getOrDefault(valueKey, "true").split(VALUE_SEPARATOR));
                });
    }

    /**
     * Checks whether it has one of constraints
     * 
     * @return true, if it has constraint; false - otherwise
     */
    boolean hasConstraint() {
        final PropertyValidation validation = delegate.getValidation();
        if (validation == null) {
            return false;
        }
        if (validation.getRequired() != null) {
            return true;
        }
        if (validation.getUniqueItems() != null) {
            return true;
        }
        if (validation.getMax() != null) {
            return true;
        }
        if (validation.getMaxItems() != null) {
            return true;
        }
        if (validation.getMaxLength() != null) {
            return true;
        }
        if (validation.getMin() != null) {
            return true;
        }
        if (validation.getMinItems() != null) {
            return true;
        }
        if (validation.getMinLength() != null) {
            return true;
        }
        if (validation.getPattern() != null) {
            return true;
        }
        return false;
    }

    boolean isRequired() {
        final PropertyValidation validation = delegate.getValidation();
        if (validation == null) {
            return false;
        }
        final Boolean isRequired = validation.getRequired();
        if (isRequired != null && isRequired) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether it has action::validation metadata
     * 
     * @return true, it it has action::validation metadata; false otherwise
     */
    boolean hasValidation() {
        return delegate.getMetadata().containsKey(ACTION_VALIDATION_NAME)
                && delegate.getMetadata().containsKey(ACTION_VALIDATION_PARAMETERS);
    }

    String getValidationName() {
        if (!hasValidation()) {
            throw new IllegalStateException("Property has no validation");
        }
        return delegate.getMetadata().get(ACTION_VALIDATION_NAME);

    }

    List<String> getValidationParameters() {
        if (!hasValidation()) {
            throw new IllegalStateException("Property has no validation");
        }
        final String parametersValue = delegate.getMetadata().get(ACTION_VALIDATION_PARAMETERS);
        return Arrays.asList(parametersValue.split(VALUE_SEPARATOR));
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Data
    public static class Condition {

        private final String target;

        private final String[] values;
    }
}