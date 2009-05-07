/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.runtime.composite;

import org.qi4j.api.common.ConstructionException;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.composite.CompositeBuilder;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.StateHolder;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.composite.CompositeInstance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * JAVADOC
 */
public final class CompositeBuilderInstance<T>
    implements CompositeBuilder<T>
{
    private static final Method TYPE_METHOD;
    private static final Method METAINFO_METHOD;

    static
    {
        try
        {
            TYPE_METHOD = Composite.class.getMethod( "type" );
            METAINFO_METHOD = Composite.class.getMethod( "metaInfo", Class.class );
        }
        catch( NoSuchMethodException e )
        {
            throw new InternalError( "Qi4j Core Runtime codebase is corrupted. Contact Qi4j team: CompositeBuilderInstance" );
        }
    }

    private final ModuleInstance moduleInstance;
    private final CompositeModel compositeModel;

    // lazy initialized in accessor
    private UsesInstance uses;

    // lazy initialized in accessor
    private CompositeInstance prototypeInstance;

    // lazy initialized in accessor
    private StateHolder state;

    public CompositeBuilderInstance( ModuleInstance moduleInstance, CompositeModel compositeModel, UsesInstance uses )
    {
        this( moduleInstance, compositeModel );
        this.uses = uses;
    }

    public CompositeBuilderInstance( ModuleInstance moduleInstance, CompositeModel compositeModel )
    {
        this.moduleInstance = moduleInstance;

        this.compositeModel = compositeModel;
    }

    public Class<T> compositeType()
    {
        return (Class<T>) compositeModel.type();
    }

    public CompositeBuilder<T> use( Object... usedObjects )
    {
        getUses().use( usedObjects );

        return this;
    }

    public T prototype()
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = compositeModel.newCompositeInstance(moduleInstance, getUses(), getState() );
        }

        return prototypeInstance.<T>proxy();
    }

    public <K> K prototypeFor( Class<K> mixinType )
    {
        // Instantiate given value type
        if( prototypeInstance == null )
        {
            prototypeInstance = compositeModel.newCompositeInstance(moduleInstance, getUses(), getState() );
        }

        return prototypeInstance.newProxy( mixinType );
    }

    public T newInstance() throws ConstructionException
    {
        StateHolder instanceState;
        if( state == null )
        {
            instanceState = compositeModel.newInitialState();
        }
        else
        {
            instanceState = compositeModel.newState( state );
        }

        compositeModel.state().checkConstraints( instanceState );

        CompositeInstance compositeInstance = compositeModel.newCompositeInstance( moduleInstance, uses == null ? UsesInstance.NO_USES : uses, instanceState );
        return compositeInstance.<T>proxy();
    }

    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return true;
            }

            public T next()
            {
                return newInstance();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected UsesInstance getUses()
    {
        if( uses == null )
        {
            uses = new UsesInstance();
        }
        return uses;
    }

    private StateHolder getState()
    {
        if( state == null )
        {
            state = compositeModel.newBuilderState();
        }

        return state;
    }

    protected class StateInvocationHandler
        implements InvocationHandler
    {
        public StateInvocationHandler()
        {
        }

        public Object invoke( Object o, Method method, Object[] objects ) throws Throwable
        {
            if( Property.class.isAssignableFrom( method.getReturnType() ) )
            {
                return getState().getProperty( method );
            }
            else if( method.equals( TYPE_METHOD ) )
            {
                return compositeModel.type();
            }
            else if( method.equals( METAINFO_METHOD ) )
            {
                return compositeModel.metaInfo().get( (Class<? extends Object>) objects[ 0 ] );
            }
            else
            {
                throw new IllegalArgumentException( "Method does not represent state: " + method );
            }
        }
    }
}
