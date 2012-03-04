/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package griffon.plugins.simpledb

import griffon.util.CallableWithArgs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
final class SimpledbEnhancer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpledbEnhancer)

    private SimpledbEnhancer() {}
    
    static void enhance(MetaClass mc, SimpledbProvider provider = SimpledbServerHolder.instance) {
        if(LOG.debugEnabled) LOG.debug("Enhancing $mc with $provider")
        mc.withSimpledb = {Closure closure ->
            provider.withSimpledb('default', closure)
        }
        mc.withSimpledb << {String clientName, Closure closure ->
            provider.withSimpledb(clientName, closure)
        }
        mc.withSimpledb << {CallableWithArgs callable ->
            provider.withSimpledb('default', callable)
        }
        mc.withSimpledb << {String clientName, CallableWithArgs callable ->
            provider.withSimpledb(clientName, callable)
        }
    }
}
