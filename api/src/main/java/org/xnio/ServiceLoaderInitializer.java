/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xnio;

import java.util.Iterator;
import java.util.ServiceLoader;
import static org.xnio._private.Messages.msg;

/**
 *
 * @author jdenise
 */
public class ServiceLoaderInitializer {
    public static Xnio INSTANCE;

    static {
        INSTANCE = doGetInstance(null, ServiceLoader.load(XnioProvider.class, Xnio.class.getClassLoader()));
    }
    private static synchronized Xnio doGetInstance(final String provider, final ServiceLoader<XnioProvider> serviceLoader) {
        final Iterator<XnioProvider> iterator = serviceLoader.iterator();
        for (;;) {
            try {
                if (! iterator.hasNext()) break;
                final XnioProvider xnioProvider = iterator.next();
                try {
                    if (provider == null || provider.equals(xnioProvider.getName())) {
                        return xnioProvider.getInstance();
                    }
                } catch (Throwable t) {
                    msg.debugf(t, "Not loading provider %s", xnioProvider.getName());
                }
            } catch (Throwable t) {
                msg.debugf(t, "Skipping non-loadable provider");
            }
        }
        try {
            Xnio xnio = Xnio.OsgiSupport.doGetOsgiService();
            if (xnio != null) {
                return xnio;
            }
        } catch (NoClassDefFoundError t) {
            // Ignore
        } catch (Throwable t) {
            msg.debugf(t, "Not using OSGi service");
        }
        throw msg.noProviderFound();
    }
    public static Xnio getInstance() {
        return INSTANCE;
    }
}
