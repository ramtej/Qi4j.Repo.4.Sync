/*
 * Copyright 2014 Jiri Jetmar.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.spatial.formats.conversions;

import org.qi4j.api.structure.Module;
import org.qi4j.library.spatial.formats.conversions.from.FromHelper;

public class TConversions
{

    public static FromHelper Convert(Module module)
    {
        return new FromHelper(module);
    }
}
