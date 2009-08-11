/*  Copyright 2007 Niclas Hedhman.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.runtime.unitofwork;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.TypeName;
import org.qi4j.api.composite.AmbiguousTypeException;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.EntityTypeNotFoundException;
import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import static org.qi4j.api.unitofwork.UnitOfWorkCallback.UnitOfWorkStatus.*;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkException;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.runtime.entity.EntityInstance;
import org.qi4j.runtime.entity.EntityModel;
import org.qi4j.runtime.query.QueryBuilderFactoryImpl;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleUnitOfWork;
import org.qi4j.spi.entity.ConcurrentEntityStateModificationException;
import org.qi4j.spi.entity.EntityNotFoundException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.EntityStoreException;
import org.qi4j.spi.entity.StateCommitter;
import org.qi4j.spi.structure.ModuleSPI;
import org.qi4j.spi.unitofwork.EntityStoreUnitOfWork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public final class UnitOfWorkInstance
{
    public static final ThreadLocal<Stack<UnitOfWorkInstance>> current;

    final HashMap<EntityReference, EntityState> stateCache;
    final HashMap<InstanceKey, EntityInstance> instanceCache;
    final HashMap<EntityStore, EntityStoreUnitOfWork> storeUnitOfWork;

    private boolean open;

    private boolean paused;

    /**
     * Lazy query builder factory.
     */
    private Usecase usecase;

    private List<UnitOfWorkCallback> callbacks;
    private UnitOfWorkStore unitOfWorkStore;
    private MetaInfo metaInfo = new MetaInfo();

    static
    {
        current = new ThreadLocal<Stack<UnitOfWorkInstance>>()
        {
            protected Stack<UnitOfWorkInstance> initialValue()
            {
                return new Stack<UnitOfWorkInstance>();
            }
        };
        QueryBuilderFactoryImpl.initialize();
    }

    public UnitOfWorkInstance( Usecase usecase )
    {
        this.open = true;
        stateCache = new HashMap<EntityReference, EntityState>();
        instanceCache = new HashMap<InstanceKey, EntityInstance>();
        storeUnitOfWork = new HashMap<EntityStore, EntityStoreUnitOfWork>();
        current.get().push( this );
        paused = false;
        this.usecase = usecase;
    }

    // Nested unit of work
    public UnitOfWorkInstance( Usecase nestedUsecase, UnitOfWorkStore unitOfWorkStore )
    {
        this( nestedUsecase );
        this.unitOfWorkStore = unitOfWorkStore;
    }

    public EntityStoreUnitOfWork getEntityStoreUnitOfWork( EntityStore store, ModuleSPI module )
    {
        EntityStoreUnitOfWork uow = storeUnitOfWork.get( store );
        if( uow == null )
        {
            uow = store.newUnitOfWork( usecase, metaInfo(), module );
            storeUnitOfWork.put( store, uow );
        }
        return uow;
    }

    public EntityInstance get( EntityReference identity, ModuleUnitOfWork uow, List<EntityModel> potentialModels, List<ModuleInstance> potentialModules, Class mixinType )
        throws EntityTypeNotFoundException, NoSuchEntityException
    {
        checkOpen();

        EntityState entityState = stateCache.get( identity );
        EntityInstance entityInstance;
        if( entityState == null )
        {   // Not yet in cache

            entityState = getParentEntityState( identity );

            // Check if this is a root UoW, or if no parent UoW knows about this entity
            EntityModel model = null;
            ModuleInstance module = null;
            if( unitOfWorkStore == null || entityState == null )
            {
                // Figure out what EntityStore to use
                for( int i = 0; i < potentialModules.size(); i++ )
                {
                    ModuleInstance potentialModule = potentialModules.get( i );
                    EntityStore store = potentialModule.entities().entityStore();
                    EntityStoreUnitOfWork storeUow = getEntityStoreUnitOfWork( store, potentialModule );
                    try
                    {
                        entityState = storeUow.getEntityState( identity );
                    }
                    catch( EntityNotFoundException e )
                    {
                        continue;
                    }

                    // See if any types match
                    EntityModel potentialModel = potentialModels.get( i );
                    TypeName typeRef = potentialModel.entityType().type();
                    if( entityState.isOfType( typeRef ) )
                    {
                        // Found it!
                        // Check for ambiguity
                        if( model != null )
                        {
                            throw new AmbiguousTypeException( mixinType, model.type(), potentialModel.type() );
                        }

                        model = potentialModel;
                        module = potentialModule;
                    }
                }
            }
            else
            {
                // Nested UnitOfWork and we found state in parent
                // See if any types match
                for( int i = 0; i < potentialModules.size(); i++ )
                {
                    ModuleInstance potentialModule = potentialModules.get( i );
                    EntityModel potentialModel = potentialModels.get( i );
                    TypeName typeRef = potentialModel.entityType().type();
                    if( entityState.isOfType( typeRef ) )
                    {
                        // Found it!
                        // Check for ambiguity
                        if( model != null )
                        {
                            throw new AmbiguousTypeException( mixinType, model.type(), potentialModel.type() );
                        }

                        model = potentialModel;
                        module = potentialModule;
                    }
                }
            }

            // Check if model was found
            if( model == null )
            {
                // Check if state was found
                if( entityState == null )
                {
                    throw new NoSuchEntityException( identity );
                }
                else
                {
                    throw new EntityTypeNotFoundException( mixinType.getName() );
                }
            }

            // Create instance
            entityInstance = new EntityInstance( uow, module, model, identity, entityState );

            stateCache.put( identity, entityState );
            InstanceKey instanceKey = new InstanceKey( model.entityType().type(), identity );
            instanceCache.put( instanceKey, entityInstance );
        }
        else
        {
            // Check if it has been removed
            if( entityState.status() == EntityStatus.REMOVED )
            {
                throw new NoSuchEntityException( identity );
            }

            // Find instance in cache
            InstanceKey instanceKey = new InstanceKey();
            for( EntityModel potentialModel : potentialModels )
            {
                instanceKey.update( potentialModel.entityType().type(), identity );
                EntityInstance instance = instanceCache.get( instanceKey );
                if( instance != null )
                {
                    return instance; // Found it!
                }
            }

            // State is in UoW, but no model for this mixin type has been used before
            // See if any types match
            EntityModel model = null;
            ModuleInstance module = null;
            for( int i = 0; i < potentialModels.size(); i++ )
            {
                EntityModel potentialModel = potentialModels.get( i );
                TypeName typeRef = potentialModel.entityType().type();
                if( entityState.isOfType( typeRef ) )
                {
                    // Found it!
                    // Check for ambiguity
                    if( model != null )
                    {
                        throw new AmbiguousTypeException( mixinType, model.type(), potentialModel.type() );
                    }

                    model = potentialModel;
                    module = potentialModules.get( i );
                }
            }

            // Create instance
            entityInstance = new EntityInstance( uow, module, model, identity, entityState );

            instanceKey.update( model.entityType().type(), identity );
            instanceCache.put( instanceKey, entityInstance );
        }

        return entityInstance;
    }

    public void refresh( Object entity )
        throws UnitOfWorkException, NoSuchEntityException
    {
        checkOpen();

        EntityComposite entityComposite = (EntityComposite) entity;
        EntityInstance entityInstance = EntityInstance.getEntityInstance( entityComposite );

        EntityStatus entityStatus = entityInstance.status();
        if( entityStatus == EntityStatus.REMOVED )
        {
            throw new NoSuchEntityException( entityInstance.identity() );
        }
        else if( entityStatus == EntityStatus.NEW )
        {
            return; // Don't try to refresh newly created state
        }

        // Refresh the state
        try
        {
            entityInstance.refresh();
        }
        catch( EntityNotFoundException e )
        {
            throw new NoSuchEntityException( entityInstance.identity() );
        }
        catch( EntityStoreException e )
        {
            throw new UnitOfWorkException( e );
        }
    }

    public void refresh()
        throws UnitOfWorkException
    {
        // Refresh the entire unit of work
        // TODO Needs to be reimplemented. Btw, what does it mean to "refresh" a UoW anyway?
    }

    public Usecase usecase()
    {
        return usecase;
    }

    public MetaInfo metaInfo()
    {
        return metaInfo;
    }

    public void pause()
    {
        if( !paused )
        {
            paused = true;
            current.get().pop();
        }
        else
        {
            throw new UnitOfWorkException( "Unit of work is not active" );
        }
    }

    public void resume()
    {
        if( paused )
        {
            paused = false;
            current.get().push( this );
        }
        else
        {
            throw new UnitOfWorkException( "Unit of work has not been paused" );
        }
    }

    public void complete()
        throws UnitOfWorkCompletionException
    {
        complete( false );
    }

    public void apply()
        throws UnitOfWorkCompletionException, ConcurrentEntityModificationException
    {
        complete( true );
    }

    public void refresh( EntityReference identity )
    {
        if( unitOfWorkStore != null )
        {
            unitOfWorkStore.refresh( identity );
        }

        EntityState entityState = stateCache.get( identity );
        if( entityState != null )
        {
            entityState.refresh();
        }
    }

    private void complete( boolean completeAndContinue )
        throws UnitOfWorkCompletionException
    {
        checkOpen();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<UnitOfWorkCallback>( callbacks );

        // Check callbacks
        notifyBeforeCompletion( currentCallbacks );

        // Commit state to EntityStores
        List<StateCommitter> committers = applyChanges();

        // Commit all changes
        for( StateCommitter committer : committers )
        {
            committer.commit();
        }

        if( completeAndContinue )
        {
            continueWithState();
        }
        else
        {
            close();
        }

        // Call callbacks
        notifyAfterCompletion( currentCallbacks, COMPLETED );

        callbacks = currentCallbacks;
    }

    public void discard()
    {
        if( !isOpen() )
        {
            return;
        }
        close();

        // Copy list so that it cannot be modified during completion
        List<UnitOfWorkCallback> currentCallbacks = callbacks == null ? null : new ArrayList<UnitOfWorkCallback>( callbacks );

        // Call callbacks
        notifyAfterCompletion( currentCallbacks, DISCARDED );

        for (EntityStoreUnitOfWork entityStoreUnitOfWork : storeUnitOfWork.values())
        {
            entityStoreUnitOfWork.discard();
        }

        callbacks = currentCallbacks;
    }

    private void close()
    {
        checkOpen();

        if( !isPaused() )
        {
            current.get().pop();
        }
        open = false;

        for( EntityInstance entityInstance : instanceCache.values() )
        {
            entityInstance.discard();
        }

        stateCache.clear();
    }

    public boolean isOpen()
    {
        return open;
    }

    public void addUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        if( callbacks == null )
        {
            callbacks = new ArrayList<UnitOfWorkCallback>();
        }

        callbacks.add( callback );
    }

    public void removeUnitOfWorkCallback( UnitOfWorkCallback callback )
    {
        if( callbacks != null )
        {
            callbacks.remove( callback );
        }
    }

    public void createEntity( EntityInstance instance )
    {
        stateCache.put( instance.identity(), instance.entityState() );
        InstanceKey instanceKey = new InstanceKey( instance.entityModel().entityType().type(), instance.identity() );
        instanceCache.put( instanceKey, instance );
    }

    private List<StateCommitter> applyChanges()
        throws UnitOfWorkCompletionException
    {
        List<StateCommitter> committers = new ArrayList<StateCommitter>();
        for( Map.Entry<EntityStore, EntityStoreUnitOfWork> entry : storeUnitOfWork.entrySet() )
        {
            EntityStore entityStore = entry.getKey();
            EntityStoreUnitOfWork entityStoreUnitOfWork = entry.getValue();

            try
            {
                StateCommitter committer = entityStoreUnitOfWork.apply();
                committers.add( committer );
            }
            catch( Exception e )
            {
                // Cancel all previously prepared stores
                for( StateCommitter committer : committers )
                {
                    committer.cancel();
                }

                if( e instanceof ConcurrentEntityStateModificationException )
                {
                    // If we cancelled due to concurrent modification, then create the proper exception for it!
                    ConcurrentEntityStateModificationException mee = (ConcurrentEntityStateModificationException) e;
                    Collection<EntityReference> modifiedEntityIdentities = mee.modifiedEntities();
                    Collection<EntityComposite> modifiedEntities = new ArrayList<EntityComposite>();
                    for( EntityReference modifiedEntityIdentity : modifiedEntityIdentities )
                    {
                        Collection<EntityInstance> instances = instanceCache.values();
                        for( EntityInstance instance : instances )
                        {
                            if( instance.identity().equals( modifiedEntityIdentity ) )
                            {
                                modifiedEntities.add( instance.<EntityComposite>proxy() );
                            }
                        }
                    }
                    throw new ConcurrentEntityModificationException( modifiedEntities );
                }
                else
                {
                    throw new UnitOfWorkCompletionException( e );
                }
            }
        }
        return committers;
    }

    private void continueWithState()
    {
        Iterator<EntityInstance> entityInstances = instanceCache.values().iterator();
        while( entityInstances.hasNext() )
        {
            EntityInstance entityInstance = entityInstances.next();
            if( entityInstance.status() == EntityStatus.REMOVED )
            {
                entityInstances.remove();
            }

        }

        Iterator<EntityState> stateStores = stateCache.values().iterator();
        while( stateStores.hasNext() )
        {
            EntityState entityState = stateStores.next();
            if( entityState.status() != EntityStatus.REMOVED )
            {
                entityState.hasBeenApplied();
            }
            else
            {
                stateStores.remove();
            }
        }
    }

    private void notifyBeforeCompletion( List<UnitOfWorkCallback> callbacks )
        throws UnitOfWorkCompletionException
    {
        // Notify explicitly registered callbacks
        if( callbacks != null )
        {
            for( UnitOfWorkCallback callback : callbacks )
            {
                callback.beforeCompletion();
            }
        }

        // Notify entities
        try
        {
            new ForEachEntity()
            {
                protected void execute( EntityInstance instance )
                    throws Exception
                {
                    if( instance.<Object>proxy() instanceof UnitOfWorkCallback )
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                        callback.beforeCompletion();
                    }
                }
            }.execute();
        }
        catch( UnitOfWorkCompletionException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            throw new UnitOfWorkCompletionException( e );
        }
    }

    private void notifyAfterCompletion( List<UnitOfWorkCallback> callbacks, final UnitOfWorkCallback.UnitOfWorkStatus status )
    {
        if( callbacks != null )
        {
            for( UnitOfWorkCallback callback : callbacks )
            {
                try
                {
                    callback.afterCompletion( status );
                }
                catch( Exception e )
                {
                    // Ignore
                }
            }
        }

        // Notify entities
        try
        {
            new ForEachEntity()
            {
                protected void execute( EntityInstance instance )
                    throws Exception
                {
                    if( instance.<Object>proxy() instanceof UnitOfWorkCallback )
                    {
                        UnitOfWorkCallback callback = UnitOfWorkCallback.class.cast( instance.proxy() );
                        callback.afterCompletion( status );
                    }
                }
            }.execute();
        }
        catch( Exception e )
        {
            // Ignore
        }

    }

    EntityState getCachedState( EntityReference entityId )
    {
        return stateCache.get( entityId );
    }

    EntityState getParentEntityState( EntityReference identity )
    {
        if( unitOfWorkStore != null )
        {
            return unitOfWorkStore.getParentEntityState( identity );
        }
        else
        {
            return null;
        }
    }

    public void checkOpen()
    {
        if( !isOpen() )
        {
            throw new UnitOfWorkException( "Unit of work has been closed" );
        }
    }

    public boolean isPaused()
    {
        return paused;
    }



    @Override public String toString()
    {
        return "UnitOfWork " + hashCode() + "(" + usecase + "): entities:" + stateCache.size();
    }

    public void remove( EntityReference entityReference )
    {
        stateCache.remove( entityReference.identity() );
    }

    abstract class ForEachEntity
    {
        public void execute() throws Exception
        {
            for( EntityInstance entityInstance : instanceCache.values() )
            {
                execute( entityInstance );
            }
        }

        protected abstract void execute( EntityInstance instance ) throws Exception;
    }

    private static class InstanceKey
    {
        private TypeName typeName;
        private EntityReference entityReference;

        private InstanceKey()
        {
        }

        private InstanceKey( TypeName typeName, EntityReference entityReference )
        {
            this.typeName = typeName;
            this.entityReference = entityReference;
        }

        public TypeName typeName()
        {
            return typeName;
        }

        public EntityReference entityReference()
        {
            return entityReference;
        }

        public void update( TypeName typeName, EntityReference entityReference )
        {
            this.typeName = typeName;
            this.entityReference = entityReference;
        }

        @Override
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            InstanceKey that = (InstanceKey) o;

            if( !entityReference.equals( that.entityReference ) )
            {
                return false;
            }
            if( !typeName.equals( that.typeName ) )
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = typeName.hashCode();
            result = 31 * result + entityReference.hashCode();
            return result;
        }
    }
}
