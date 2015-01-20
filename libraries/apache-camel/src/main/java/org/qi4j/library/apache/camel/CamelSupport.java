package org.qi4j.library.apache.camel;

import org.apache.camel.CamelContext;
import org.qi4j.api.service.ServiceActivation;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:09 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CamelSupport extends ServiceActivation
{

    String foo();

    CamelContext getContext();

    // String client();

    // String index();

    // String entitiesType();

   // boolean demoConfigFlag();

   // void spatialMapping(String type, String property);

}

