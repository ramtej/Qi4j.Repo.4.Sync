package org.qi4j.library.apache.camel.internal;

import org.qi4j.api.common.Visibility;
import org.qi4j.bootstrap.Assembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:13 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCamelAssembler<AssemblerType extends AbstractCamelAssembler>
        implements Assembler
{
    private String identity;

    private Visibility visibility = Visibility.module;

    private ModuleAssembly configModule;

    private Visibility configVisibility = Visibility.module;

    public final AssemblerType withIdentity( String identity )
    {
        this.identity = identity;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withVisibility( Visibility visibility )
    {
        this.visibility = visibility;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withConfigVisibility( Visibility configVisibility )
    {
        this.configVisibility = configVisibility;
        return ( AssemblerType ) this;
    }

    public final AssemblerType withConfigModule( ModuleAssembly configModule )
    {
        this.configModule = configModule;
        return ( AssemblerType ) this;
    }

    @Override
    public final void assemble( ModuleAssembly module )
            throws AssemblyException
    {
        if ( identity == null ) {
            identity = "apache-camel";
        }
        if ( configModule == null ) {
            configModule = module;
        }
        doAssemble( identity, module, visibility, configModule, configVisibility );
    }

    protected abstract void doAssemble( String identity,
                                        ModuleAssembly module, Visibility visibility,
                                        ModuleAssembly configModule, Visibility configVisibility )
            throws AssemblyException;

}
