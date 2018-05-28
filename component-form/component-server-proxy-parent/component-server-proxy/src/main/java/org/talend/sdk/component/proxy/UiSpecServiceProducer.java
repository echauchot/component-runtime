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
package org.talend.sdk.component.proxy;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.talend.sdk.component.form.api.Client;
import org.talend.sdk.component.form.api.UiSpecService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class UiSpecServiceProducer {

    @Produces
    @Dependent
    public UiSpecService uiSpecService(final Client client, final Jsonb jsonb) {
        return new UiSpecService(client, jsonb);
    }

    @Produces
    @ApplicationScoped
    public Jsonb jsonb() {
        return JsonbBuilder.create();
    }

    public void releaseUiSpecService(@Disposes final UiSpecService uiSpecService) {
        try {
            uiSpecService.close();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void releaseJsonb(@Disposes final Jsonb jsonb) {
        try {
            jsonb.close();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}