package org.qi4j.library.apache.camel.memory;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.library.apache.camel.CamelSupport;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */

@Mixins( CamelMemorySupport.class )
public interface CamelInMemoryService extends CamelSupport, ServiceComposite

{}
