/*
 * Copyright 2012 Paul Merlin.
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
package org.qi4j.index.elasticsearch.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.index.elasticsearch.ElasticSearchConfiguration;
import org.qi4j.index.elasticsearch.extensions.spatial.configuration.SpatialConfiguration;
import org.qi4j.index.elasticsearch.internal.AbstractElasticSearchAssembler;
import org.qi4j.index.elasticsearch.memory.ESMemoryIndexQueryService;
import org.qi4j.library.spatial.assembly.TGeometryAssembler;
import org.qi4j.valueserialization.orgjson.OrgJsonValueSerializationService;

public class ESMemoryIndexQueryAssembler
    extends AbstractElasticSearchAssembler<ESMemoryIndexQueryAssembler>
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.services( ESMemoryIndexQueryService.class ).
            identifiedBy( identity() ).
            visibleIn( visibility() ).
            instantiateOnStartup();

        module.services( OrgJsonValueSerializationService.class ).
            taggedWith( ValueSerialization.Formats.JSON );
/**
        module.values(SpatialConfiguration.Configuration.class,
                SpatialConfiguration.FinderConfiguration.class,
                SpatialConfiguration.IndexerConfiguration.class,
                SpatialConfiguration.IndexingMethod.class,
                SpatialConfiguration.ProjectionSupport.class).
                visibleIn(visibility());
*/

        new TGeometryAssembler().visibleIn(Visibility.module).assemble(module);


        if( hasConfig() )
        {
            configModule().entities( ElasticSearchConfiguration.class ).
                visibleIn( configVisibility() );
        }
    }
}
