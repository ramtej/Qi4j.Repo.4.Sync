/*
 * Copyright 2006 Niclas Hedhman.
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
package org.qi4j.query.set1;

import org.qi4j.composite.PropertyParameter;

public class Mixin1Impl
    implements Mixin1
{
    private String name;
    private String description;
    private String bar;

    public Mixin1Impl( @PropertyParameter( "name" )String name, @PropertyParameter( "description" )String bar, @PropertyParameter( "bar" )String str )
    {
        this.bar = bar;
        this.name = name;
        this.description = str;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getBar()
    {
        return bar;
    }

    public void setBar( String bar )
    {
        this.bar = bar;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }
}
