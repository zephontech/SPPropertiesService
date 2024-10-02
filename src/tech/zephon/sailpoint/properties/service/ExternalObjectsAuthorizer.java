package tech.zephon.sailpoint.properties.service;

import java.util.List;
import org.apache.log4j.Logger;

import sailpoint.authorization.Authorizer;
import sailpoint.authorization.UnauthorizedAccessException;
import sailpoint.object.Capability;
import sailpoint.tools.GeneralException;
import sailpoint.web.UserContext;

/**
 * Authorizer which checks to see if the currently logged in user has in effect admin rights to the Search Plugin.
 *
 * @author
 *
 */
public class ExternalObjectsAuthorizer implements Authorizer
{
    // Insert code below this line

    protected Logger log = Logger.getLogger(ExternalObjectsAuthorizer.class.getName());

    /**
     * Constructor.
     *
     */
    public ExternalObjectsAuthorizer()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void authorize(UserContext userContext) throws GeneralException
    {
        List<Capability> capabilities = userContext.getLoggedInUser().getCapabilityManager().getEffectiveCapabilities();
        for(Capability capability : capabilities)
        {
            log.error("Capability:" + capability.getDisplayName());
        }
        
        log.error("User has capability SystemAdministrator=" + userContext.getLoggedInUser().getCapabilityManager().hasCapability("SystemAdministrator"));
        log.error("User has capability SPPropertiesService=" + userContext.getLoggedInUser().getCapabilityManager().hasCapability("SPPropertiesService"));

        if (!(userContext.getLoggedInUser().getCapabilityManager().hasCapability("SystemAdministrator")
                || userContext.getLoggedInUser().getCapabilityManager().hasCapability("SPPropertiesService")))
        {
            log.error("User has capability SystemAdministrator=" + userContext.getLoggedInUser().getCapabilityManager().hasCapability("SystemAdministrator"));
            log.error("User has capability SPPropertiesService=" + userContext.getLoggedInUser().getCapabilityManager().hasCapability("SPPropertiesService"));
            throw new UnauthorizedAccessException("User does not have required access to the User List Resource Extended Plugin");
        }
    }
}
