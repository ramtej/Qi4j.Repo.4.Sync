/*
 * Copyright (c) 2012, Paul Merlin.
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
package org.qi4j.runtime.activation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.qi4j.api.activation.Activator;
import org.qi4j.api.activation.PassivationException;
import org.qi4j.functional.Iterables;

/**
 * Instance of a Qi4j Activators of one Activation target. Contains ordered
 * Activators and roll the Activation on the target.
 * 
 * @param <ActivateeType> Type of the activation target
 */
public class ActivatorsInstance<ActivateeType>
        implements Activator<ActivateeType>
{
    public static final ActivatorsInstance EMPTY = new ActivatorsInstance( Collections.emptyList() );

    private final Iterable<Activator<ActivateeType>> activators;

    public ActivatorsInstance( Iterable<Activator<ActivateeType>> activators )
    {
        this.activators = activators;
    }

    public void beforeActivation( ActivateeType activating )
            throws Exception
    {
        for( Activator<ActivateeType> activator : activators ) {
            activator.beforeActivation( activating );
        }
    }

    public void afterActivation( ActivateeType activated )
            throws Exception
    {
        for( Activator<ActivateeType> activator : activators ) {
            activator.afterActivation( activated );
        }
    }

    public void beforePassivation( ActivateeType passivating )
            throws Exception
    {
        List<Exception> exceptions = new ArrayList<Exception>();
        for( Activator<ActivateeType> activator : Iterables.reverse( activators ) ) {
            try
            {
                activator.beforePassivation( passivating );
            }
            catch( Exception ex ) {
                exceptions.add( ex );
            }
        }
        if( exceptions.isEmpty() )
        {
            return;
        }
        if( exceptions.size() == 1 )
        {
            throw exceptions.get( 0 );
        }
        throw new PassivationException( exceptions );
    }

    public void afterPassivation( ActivateeType passivated )
            throws Exception
    {
        List<Exception> exceptions = new ArrayList<Exception>();
        for( Activator<ActivateeType> activator : Iterables.reverse( activators ) ) {
            try
            {
                activator.afterPassivation( passivated );
            }
            catch( Exception ex ) {
                exceptions.add( ex );
            }
        }
        if( exceptions.isEmpty() )
        {
            return;
        }
        if( exceptions.size() == 1 )
        {
            throw exceptions.get( 0 );
        }
        throw new PassivationException( exceptions );
    }

}