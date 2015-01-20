package org.qi4j.library.apache.camel.assembly;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.apache.camel.CamelConfiguration;
import org.qi4j.library.apache.camel.internal.AbstractCamelAssembler;
import org.qi4j.library.apache.camel.memory.CamelInMemoryService;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class CamelInMemoryAssembler extends AbstractCamelAssembler<CamelInMemoryAssembler> {

    @Override
    protected void doAssemble( String identity,
                               ModuleAssembly module, Visibility visibility,
                               ModuleAssembly configModule, Visibility configVisibility )
            throws AssemblyException
    {
        module.services( CamelInMemoryService.class ).
                identifiedBy( identity ).
                visibleIn( visibility ).
                 instantiateOnStartup();



        // module.services( OrgJsonValueSerializationService.class ).
        //        taggedWith( ValueSerialization.Formats.JSON );

        configModule.entities( CamelConfiguration.class ).
                visibleIn( configVisibility );
    }

}
