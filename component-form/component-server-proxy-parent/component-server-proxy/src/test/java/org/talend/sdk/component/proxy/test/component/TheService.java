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
package org.talend.sdk.component.proxy.test.component;

import java.net.MalformedURLException;
import java.net.URL;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.DynamicValues;
import org.talend.sdk.component.api.service.completion.Values;
import org.talend.sdk.component.api.service.healthcheck.HealthCheck;
import org.talend.sdk.component.api.service.healthcheck.HealthCheckStatus;
import org.talend.sdk.component.proxy.test.component.component1.Connection1;

@Service
public class TheService {

    @HealthCheck("validate-connection-1")
    public HealthCheckStatus validateConnection(@Option("configuration") final Connection1 connection1) {
        try {
            new URL(connection1.getUrl());
        } catch (MalformedURLException e) {
            return new HealthCheckStatus(HealthCheckStatus.Status.KO, "url invalid case:" + e.getMessage());
        }

        return new HealthCheckStatus(HealthCheckStatus.Status.OK, "Connection successful");
    }

    @DynamicValues("action-with-error")
    public Values actionWithError() {
        throw new IllegalStateException("This is an error from the action.");
    }

}
