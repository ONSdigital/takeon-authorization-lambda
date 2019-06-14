package uk.gov.ons.authorization.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import uk.gov.ons.authorization.common.Constants;
import uk.gov.ons.authorization.entity.AuthorizationRequest;
import lombok.extern.log4j.Log4j2;
import uk.gov.ons.authorization.entity.HandlerResponse;
import uk.gov.ons.authorization.entity.HttpStatusInfo;
import uk.gov.ons.authorization.util.PropertiesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

@Log4j2
public class AuthorizationLambdaHandler implements RequestHandler<AuthorizationRequest, HandlerResponse> {


    private static final String AUTHORIZATION = "AUTHORIZATION";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final String DATA_TYPE = "application/json";
    private static final String XSTREAM = "X-Stream";
    private static final String XSTREAM_VALUE = "true";
    private static final String CHARACTER_SET = "US-ASCII";
    private static final String BASIC = "Basic ";
    private static final String COLON = ":";

    /**
     * @param request AuthorizationRequest
     * @param context Context
     * @return HandlerResponse The response containing CSRF Token
     */

    @Override
    public HandlerResponse handleRequest(AuthorizationRequest request, Context context) {

        HandlerResponse responseResult;
        String responseData;
        HttpStatusInfo statusInfo = new HttpStatusInfo();
        try {
            log.info("Refresh Groups {} ", request.getRefresh_groups());
            log.info("Requested Lifetime {} ", request.getRequested_lifetime());
            String restUrl = PropertiesUtil.getProperty(Constants.EXTERNAL_URL);
            String username = PropertiesUtil.getProperty(Constants.USER_NAME);
            String password = PropertiesUtil.getProperty(Constants.PASSWORD);
            String jsonRequestData = new ObjectMapper().writeValueAsString(request);

            HttpPost httpPost = createConnectivity(restUrl, username, password);
            responseData = executeReq(jsonRequestData, httpPost, statusInfo);
            ObjectMapper mapper = new ObjectMapper();
            responseResult = mapper.readValue(responseData, HandlerResponse.class);
            responseResult.setStatusCode(statusInfo.getStatusCode());

        } catch (Exception e) {
            log.error("An exception was raised handling the Authorization Lambda request.", e);
            processExceptionAn(e, statusInfo);
            HandlerResponse exceptionResponse = HandlerResponse.builder()
                    .statusCode(statusInfo.getStatusCode())
                    .exceptionInfo(statusInfo.getExceptionInfo()).build();
            return exceptionResponse;
        }
        return responseResult;
    }


    /**
     * @param restUrl  String IBM URL
     * @param username String IBM UserName
     * @param password String IBM Password
     * @return HttpPost Post Connection
     */
    private HttpPost createConnectivity(String restUrl, String username, String password) {
        HttpPost post = new HttpPost(restUrl);
        String auth = new StringBuilder(username).append(COLON).append(password).toString();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName(CHARACTER_SET)));
        String authHeader = BASIC + new String(encodedAuth);
        post.setHeader(AUTHORIZATION, authHeader);
        post.setHeader(CONTENT_TYPE, DATA_TYPE);
        post.setHeader(ACCEPT, DATA_TYPE);
        post.setHeader(XSTREAM, XSTREAM_VALUE);
        return post;
    }

    /**
     * @param jsonData String
     * @param httpPost HttpPost
     * @return executeResult String
     */
    private String executeReq(String jsonData, HttpPost httpPost, HttpStatusInfo statusInfo)
            throws IOException {
        String executeResult;
        try {
            executeResult = executeHttpRequest(jsonData, httpPost, statusInfo);
        } catch (UnsupportedEncodingException ex) {
            log.error("error while encoding api url  { } ", ex);
            throw ex;
        } catch (IOException eIO) {
            log.error("ioException occurred while sending http request  { } ", eIO);
            statusInfo.setExceptionInfo(eIO.getMessage());
            throw eIO;
        } catch (Exception e) {
            log.error("exception occurred while sending http request  { } ", e);
            throw e;
        } finally {
            httpPost.releaseConnection();
        }
        return executeResult;
    }

    /**
     * @param excep      Exception
     * @param statusInfo HttpStatusInfo
     */
    private void processExceptionAn(Exception excep, HttpStatusInfo statusInfo) {
        statusInfo.setExceptionInfo(excep.getMessage());
    }

    /**
     * @param jsonData String
     * @param httpPost HttpPost
     * @return result String
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private String executeHttpRequest(String jsonData, HttpPost httpPost, HttpStatusInfo statusInfo) throws IOException {
        HttpResponse response;
        String line = "";
        StringBuffer result = new StringBuffer();
        try {
            httpPost.setEntity(new StringEntity(jsonData));
            HttpClient client = HttpClientBuilder.create().build();
            response = client.execute(httpPost);
            log.info("Post parameters  {} ", jsonData);
            log.info("Response Code {} ", response.getStatusLine().getStatusCode());
            statusInfo.setStatusCode(String.valueOf(response.getStatusLine().getStatusCode()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            log.info("Final Result from IBM {} ", result.toString());

        } catch (IOException eIO) {
            throw eIO;
        } catch (Exception e) {
            throw e;
        }

        return result.toString();
    }
}
