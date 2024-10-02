package tech.zephon.sailpoint.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.junit.Test;

import sailpoint.tools.GeneralException;
import tech.zephon.sailpoint.properties.service.Logger;

public class OAuthClientSOP
{

    // replace with logger of your choice
    private static final Logger logger = Logger.getLogger(OAuthClientSOP.class);

    private static final String TOKENURL = "http://sailpoint.testserver.org:8080/identityiq/oauth2/token";
    private static final String CLIENTID = "igwVW5akszURWVMQkMXc89gvIVkWqkKT";
    private static final String CLIENTPW = "OSr89yEmmYl2fkOH";
    private static final String APPURL = "http://sailpoint.testserver.org:8080/identityiq/plugin/rest/SPPropertiesService/application?filter=<FILTERVALUE>";
    private static final String CUSTOMOBJURL = "http://sailpoint.testserver.org:8080/identityiq/plugin/rest/SPPropertiesService/customobject?filter=<FILTERVALUE>";

    @Test
    public void mainTest()
    {
        String oauthToken = null;
        try
        {
            oauthToken = this.getAccessToken(TOKENURL, CLIENTID, CLIENTPW);
            if (oauthToken == null)
            {
                throw new Exception("No Token in response");
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            logger.error("Error:" + e.getMessage(), e);
        }
        System.out.println("Start up");
        try
        {
            logger.debug("Get custom object");
            logger.debug("\n");
            Map parms = this.getCustomObject(oauthToken, CUSTOMOBJURL, "TestCustomObject");
            logger.debug("Parms:" + parms);
            logger.debug("\n");
            logger.debug("Get invalid custom object");
            logger.debug("\n");
            parms = this.getCustomObject(oauthToken, CUSTOMOBJURL, "TeestCustomObject");
            logger.debug("Parms:" + parms);
            logger.debug("\n");
            logger.debug("Get application");
            logger.debug("\n");
            parms = this.getApplication(oauthToken, APPURL, "Active Directory");
            logger.debug("Parms:" + parms);
            logger.debug("\n");
            logger.debug("Get invalid application");
            logger.debug("\n");
            parms = this.getApplication(oauthToken, APPURL, "X-Active Directory");
            logger.debug("Parms:" + parms);
           
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            logger.error("Error:" + e.getMessage(), e);
        }
    }

    private String getAccessToken(String tokenUrl, String clientId, String clientCreds) throws GeneralException, JSONException, UnsupportedEncodingException, IOException
    {

        logger.debug("Start getAccessToken");
        String encodedHttpResponse = null;

        HttpClient httpClient = HttpClientBuilder
                .create()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
        HttpPost httpPost = new HttpPost(tokenUrl);
        HttpResponse httpResponse = null;
        StringEntity input = null;

        String inputstring = "client_id=" + clientId + "&client_secret=" + clientCreds + "&grant_type=" + "client_credentials";
        input = new StringEntity(inputstring);

        httpPost.setEntity(input);

        String content = EntityUtils.toString(input);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        String credentials = clientId + ":" + clientCreds;
        httpPost.addHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString(new String(credentials).getBytes()));

        Header[] headers = httpPost.getAllHeaders();

        for (Header header : headers)
        {
            logger.debug(header.getName() + ":" + header.getValue());
        }
        httpResponse = httpClient.execute(httpPost);

        if (httpResponse != null)
        {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity resEntity = httpResponse.getEntity();
            encodedHttpResponse = IOUtils.toString(resEntity.getContent(), "UTF-8");
            if (httpResponse != null && statusCode == 200)
            {
                
                if (resEntity != null)
                {
                    return encodedHttpResponse;
                }
            }
            else
            {
                logger.debug("Bad Http Response " + encodedHttpResponse);
            }
        }
        else
        {
            logger.debug("Access Tocken Http Response is NULL for " + input);
        }

        logger.debug("End getAccessToken");
        return encodedHttpResponse;

    }
    
    private Map getCustomObject(String oauthToken, String appUrl, String objName) throws GeneralException, JSONException, UnsupportedEncodingException, IOException
    {
        logger.debug("Start getCustomObject");
        String encodedHttpResponse = null;

        HttpClient httpClient = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();
        String encoded = this.encodeValue(objName);
        String endPoint = appUrl.replace("<FILTERVALUE>", encoded);
        //HttpGet httpGet = new HttpGet(endPoint); 
        HttpGet httpGet = this.getHttpObject(oauthToken, endPoint);
        ObjectMapper mapper = new ObjectMapper();
        HttpResponse httpResponse = httpClient.execute(httpGet);

        if (httpResponse != null)
        {
            logger.debug(" httpResponse= " + httpResponse);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            logger.debug("StatusLine=" + httpResponse.getStatusLine());
            logger.debug("StatusCode=" + statusCode);
            if (httpResponse != null && statusCode == 200)
            {
                HttpEntity resEntity = httpResponse.getEntity();
                logger.debug("resEntity=" + resEntity);
                if (resEntity != null)
                {
                    encodedHttpResponse = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
                    logger.debug("Http Response status is   " + statusCode + " responseString=" + encodedHttpResponse);
                    //return new HashMap();
                }
            }
            else
            {
                logger.debug("Bad Http Response " + IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8"));
                return new HashMap();
            }
        }
        else
        {
            logger.debug("Http Response is NULL");
        }
        Map respMap = mapper.readValue(encodedHttpResponse, Map.class);
        logger.debug("End getCustomObject");
        return respMap;
        
        
    }
    
    
    private Map getApplication(String oauthToken, String appUrl, String appName) throws GeneralException, JSONException, UnsupportedEncodingException, IOException
    {

        logger.debug("Start getApplication");
        String encodedHttpResponse = null;

        HttpClient httpClient = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                .build();

        String encoded = this.encodeValue(appName);
        logger.debug("encoded:" + encoded);
        String endPoint = appUrl.replace("<FILTERVALUE>", encoded);
        //HttpGet httpGet = new HttpGet(endPoint); 
        HttpGet httpGet = this.getHttpObject(oauthToken, endPoint);
        ObjectMapper mapper = new ObjectMapper();
        HttpResponse httpResponse = httpClient.execute(httpGet);

        if (httpResponse != null)
        {
            logger.debug(" httpResponse= " + httpResponse);

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            logger.debug("StatusLine=" + httpResponse.getStatusLine());
            logger.debug("StatusCode=" + statusCode);
            if (httpResponse != null && statusCode == 200)
            {
                HttpEntity resEntity = httpResponse.getEntity();
                logger.debug("resEntity=" + resEntity);
                if (resEntity != null)
                {
                    encodedHttpResponse = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
                    logger.debug("Http Response status is   " + statusCode + " responseString=" + encodedHttpResponse);
                    //return new HashMap();
                }
            }
            else
            {
                logger.debug("Bad Http Response " + encodedHttpResponse);
                return new HashMap();
            }

        }
        else
        {
            logger.debug("Http Response is NULL");
        }
        Map respMap = mapper.readValue(encodedHttpResponse, Map.class);
        logger.debug("End getApplication");
        return respMap;

    }

    private HttpPost postHttpObject(String oauthToken,String endPoint)
    {
        
        HttpPost httpPost = new HttpPost(endPoint);
        HttpResponse httpResponse = null;
        String bearerToken = this.getTokenHeader(oauthToken);
        httpPost.addHeader("Authorization", "Bearer " + bearerToken);
        //httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        //httpPost.setHeader("Content-type", "application/json");
        return httpPost;
    }
    
    
    private HttpGet getHttpObject(String oauthToken, String endPoint)
    {

        HttpGet httpGet = new HttpGet(endPoint);
        HttpResponse httpResponse = null;

        httpGet.addHeader("Content-Type", "application/json");
        //httpPost.addHeader("Accept", "application/json");   

        String bearerToken = this.getTokenHeader(oauthToken);

        httpGet.addHeader("Authorization", "Bearer " + bearerToken);

        Header[] headers = httpGet.getAllHeaders();

        logger.debug("http post Request= " + httpGet.toString());
        logger.debug("http Post Headers: ");
        for (Header header : headers)
        {
            logger.debug(header.getName() + ":" + header.getValue());
        }
        return httpGet;
    }
    

    private String getTokenHeader(String oauthToken)
    {
        ObjectMapper mapper = new ObjectMapper();
        Map jsonResponseMap = null;
        try
        {
            jsonResponseMap = mapper.readValue(oauthToken, Map.class);
        }
        catch (Exception e)
        {
            logger.error("Error parsing token:" + e.getMessage(), e);
            return null;
        }

        logger.debug("oauthTokenResponse json map value=" + jsonResponseMap);
        String bearerToken = null;

        if (jsonResponseMap != null)
        {
            String tokenType = (String) jsonResponseMap.get("token_type");
            logger.debug("jsonResponseMap tokenType" + tokenType);
            String accessToken = (String) jsonResponseMap.get("access_token");
            //logger.debug("jsonResponseMap accessToken" + accessToken);
            String expiresIn = String.valueOf(jsonResponseMap.get("expires_in"));
            logger.debug("jsonResponseMap expiresIn" + expiresIn);

            if (accessToken != null)
            {
                bearerToken = accessToken;
            }

            logger.debug("token_type : '" + tokenType + "'.");
            logger.debug("expires_in : '" + expiresIn);
            logger.debug("access_token : " + accessToken + "\n");
        }
        return bearerToken;
    }
    
    private String encodeValue(String value) {
        try {
            String str = URLEncoder.encode(value, StandardCharsets.UTF_8);
            str = str.replaceAll("\\+", "%20");
            return str;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
