package org.qi4j.library.apache.camel;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * Created with IntelliJ IDEA.
 * User: jakes
 * Date: 12/7/13
 * Time: 7:05 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CamelConfiguration extends ConfigurationComposite {


    /**
     * Cluster name.
     * Defaults to 'qi4j_cluster'.
     */
    @Optional
    Property<String> clusterName();

    /**
     * Index name.
     * Defaults to 'qi4j_index'.
     */
    @Optional Property<String> index();

    /**
     * Set to true to index non aggregated associations as if they were aggregated.
     * WARN: Don't use this if your domain model contains circular dependencies.
     * Defaults to 'FALSE'.
     */
    @UseDefaults
    Property<Boolean> demoConfigFlag();

}
