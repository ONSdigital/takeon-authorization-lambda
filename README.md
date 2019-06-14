### **Authorisation Lambda**

This Lambda function is used to access IBM URL by performing Basic Authorisation using Username and Password
supplied by them and passing input by POST request. IBM going to generate random CSRF token that is used by ONS in
authorising subsequent calls from other Lambda functions.

### **Lambda Input**

{
  "refresh_groups": true,
  "requested_lifetime": 7200
}


 ### **The Output from from Lambda contains CSRF token and would be similar to**

 {
   "expiration": "7200",
   "csrf_token": "eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjA0NDU2NTcsInN1YiI6InZldHNUYWtlT24uZmlkQHQzMDIxIn0.CMnFLMmwfBTfv7Yt5eEYQ2jocb6vOQRk1-6KQ1DFYFE",
   "statusCode": "201"
 }



### **Configuration**

The following configuration is required:


EXTERNAL_URL - IBM external URL - https://ons.bpm.ibmcloud.com:443/baw/dev/bpm/system/login

USER_NAME - IBM User Name - vetstakeon.fid@t3021

PASSWORD - The Password provided by IBM


#### **Deployment**

https://eu-west-2.console.aws.amazon.com/lambda/home?region=eu-west-2#/functions/authorization-lambda?tab=graph


#### **Confluence Documentation**

https://collaborate2.ons.gov.uk/confluence/pages/viewpage.action?spaceKey=CBAU&title=Spike+-+Basic+Authorisation+and+retrieving+CSRF+token+from+IBM+-+Creating+Lambda+Function