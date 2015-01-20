package org.qi4j.library.apache.camel.memory;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import org.qi4j.library.apache.camel.CamelConfiguration;
import org.qi4j.library.apache.camel.internal.AbstractCamelSupport;
import org.qi4j.library.fileconfig.FileConfiguration;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CamelMemorySupport extends AbstractCamelSupport {


    @This
    private Configuration<CamelConfiguration> configuration;

    @This
    private Identity hasIdentity;

    @Service
    private FileConfiguration fileConfig;

    private CamelContext context;



    protected  void activateCamel()
            throws Exception

    {

        configuration.refresh();
        CamelConfiguration config = configuration.get();


        context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                // from("test-jms:queue:test.queue").to("file://test");
                from("direct:in").to("stream:out");
            }
        });

        context.start();

        System.out.println("Configuration done..");

    }


    @Override
    public void passivateCamel()
            throws Exception
    {
        // node.close();
        // node = null;
    }

    public String foo() {
           return "Camel";
    }

    public  void getProcessEngine() {
             //  return processEngine;
    }

    public CamelContext getContext() {
        return context;
    }


}
