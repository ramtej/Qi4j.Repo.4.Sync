package org.qi4j.library.apache.camel;

import org.junit.Test;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.library.apache.camel.assembly.CamelInMemoryAssembler;
import org.qi4j.library.fileconfig.FileConfigurationOverride;
import org.qi4j.library.fileconfig.FileConfigurationService;
import org.qi4j.test.AbstractQi4jTest;
import org.qi4j.test.EntityTestAssembler;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CamelTest extends AbstractQi4jTest {


    @Override
    public void assemble(ModuleAssembly module)
            throws AssemblyException {
        // Config module
        ModuleAssembly config = module.layer().module("config");
        new EntityTestAssembler().assemble(config);

        // EntityStore
        new EntityTestAssembler().assemble(module);

        // Camel
        new CamelInMemoryAssembler().withConfigModule(config).withConfigVisibility(Visibility.layer).assemble(module);
        CamelConfiguration camelConfig = config.forMixin(CamelConfiguration.class).declareDefaults();
        camelConfig.demoConfigFlag().set(Boolean.TRUE);

        // FileConfig
        FileConfigurationOverride override = new FileConfigurationOverride().withData(new File("build/qi4j-data")).
                withLog(new File("build/qi4j-logs")).withTemporary(new File("build/qi4j-temp"));
        module.services(FileConfigurationService.class).
                setMetaInfo(override);

    }


    @Service
    private CamelSupport camelService;

    @Test
    public void whenGetContextIsNotNull()
            throws Exception {

        camelService.getContext();

    }


}