package org.qi4j.library.apache.camel.internal;

import org.qi4j.library.apache.camel.CamelSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:32 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCamelSupport implements CamelSupport {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractCamelSupport.class);


    @Override
    public final void activateService()
            throws Exception
    {
        activateCamel();

        System.out.println("Activating Camel");

    }

    @Override
    public final void passivateService()
            throws Exception
    {
        passivateCamel();
    }



    protected abstract void activateCamel()
            throws Exception;

    protected void passivateCamel()
            throws Exception
    {
        // NOOP
    }
}
