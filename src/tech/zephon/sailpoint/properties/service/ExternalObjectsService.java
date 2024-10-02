package tech.zephon.sailpoint.properties.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Custom;
import sailpoint.object.Identity;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.tools.xml.PersistentArrayList;
import sailpoint.tools.xml.PersistentHashMap;

/**
 * The resource class which defines the REST interface. Each REST end point is defined, the appropriate authorization is checked and the corresponding service method is called.
 *
 * @author
 *
 */
@Path("SPPropertiesService")
@RequiredRight(value = "SPPropertiesService")
@Produces(
        {
            sailpoint.rest.scim.MediaType.SCIM, MediaType.APPLICATION_JSON
        })
@Consumes(
        {
            sailpoint.rest.scim.MediaType.SCIM, MediaType.WILDCARD
        })
public class ExternalObjectsService extends BasePluginResource
{

    protected Logger log = Logger.getLogger(ExternalObjectsService.class);

    
    @GET
    @Path("/customobject")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomObject(@QueryParam("filter") String filter) throws Exception
    {

        authorize(new ExternalObjectsAuthorizer());
        log.error("Filter:" + filter);
        JsonHelper jsonHelper = new JsonHelper();
        
        if (filter == null || filter.isEmpty())
        {
            Map error = jsonHelper.makeErrorMap("Error","Invalid Parameters");
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
       
        //ObjectMapper mapper = new ObjectMapper();
        
        
        Identity identity = getLoggedInUser();
        log.debug("Logged in user=" + identity.getName());
        log.debug("Logged in user scope=" + identity.getAssignedScope());

        log.debug("Logged in user getControlledScopes=" + identity.getControlledScopes());

        SailPointContext context = SailPointFactory.getCurrentContext();

        Custom customObj = null;
        try
        {
            customObj = context.getObjectByName(Custom.class, filter);
            if (customObj == null)
            {
                Map error =  jsonHelper.makeErrorMap("Error", "Custom Object is null or invalid filter");
                return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(error)
                    .build();
            }
        }
        catch (Exception e)
        {
            log.error("Error getting custom object :" + filter);
             Map error = jsonHelper.makeErrorMap("Error","Error getting custom object :" + filter);
             return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .build();
        }
        Attributes<String, Object> objects = customObj.getAttributes();
        Map customMap = objects.getMap();

        log.error("customMap=" + customMap);
        //String json = mapper.writeValueAsString(customMap);
        String json = jsonHelper.getJson(customMap);
        log.debug("json:" + json);
        return Response
                    .status(Response.Status.OK)
                    .entity(customMap)
                    .build();

    }

    @GET
    @Path("/application")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map getApplication(@QueryParam("filter") String filter) throws Exception
    {
        authorize(new ExternalObjectsAuthorizer());
        JsonHelper jsonHelper = new JsonHelper();
        log.error("Filter:" + filter);
        try
        {
            if (filter == null || filter.trim().isEmpty())
            {
                return jsonHelper.makeErrorMap("Error","Invalid Parameters");
            }
        }
        catch (Exception e)
        {
            log.error("Error:" + e.getMessage(), e);
            return jsonHelper.makeErrorMap("Error", "application Object is null or invalid filter");
        }

        Identity identity = getLoggedInUser();

        SailPointContext context = SailPointFactory.getCurrentContext();

        Application application = null;
        try
        {
            application = context.getObjectByName(Application.class, filter);
            if (application == null)
            {
                return jsonHelper.makeErrorMap("Error", "application Object is null or invalid filter");
            }
        }
        catch (Exception e)
        {
            log.error("Error getting custom object :" + filter,e);
            return jsonHelper.makeErrorMap("Error", "Error getting custom object:" + filter + " Exception:" + e.getMessage());
        }

        Attributes<String, Object> objects = application.getAttributes();
        Map appMap = objects.getMap();
        PersistentHashMap settingsMap = null;
        Set<String> keys = objects.keySet();
        for (String key : keys)
        {
            //log.error("Key:" + key);
            //log.error("Value:" + objects.get(key));
            if ("domainSettings".equals(key))
            {
                PersistentArrayList setting = (PersistentArrayList) application.getAttributes().getMap().get("domainSettings");
                settingsMap = (PersistentHashMap) setting.get(0);
                //appMap.putAll(settingsMap);
                break;
            }
        }

        keys = settingsMap.keySet();
        for (String key : keys)
        {
            //log.error("Key:" + key);
            //log.error("Value:" + settingsMap.get(key));
            if ("password".equals(key))
            {
                String dec = context.decrypt((String) appMap.get(key));
                appMap.put("creds", dec);
            }
        }
        Map respMap = new HashMap();
        String port = (String)settingsMap.get("port");
        String pw = (String)settingsMap.get("password");
        String dec = context.decrypt(pw);
        List<String> servers = (List<String>)settingsMap.get("servers");
        if (servers != null | servers.size() > 0)
        {
            String server = servers.get(0);
            respMap.put("host", server);
        }
        respMap.put("port",port);
        respMap.put("useSSL", (Boolean)settingsMap.get("useSSL"));
        respMap.put("bindDN",(String)settingsMap.get("user"));
        respMap.put("password", dec);
        
        log.debug("customMap=" + respMap);
        //String json = mapper.writeValueAsString(respMap);
        String json = jsonHelper.getJson(respMap);
        log.error("json:" + json);
        //return json;
        return respMap;

    }

    @Override
    public String getPluginName()
    {
        return "SPPropertiesService";
    }
}
