package uk.gov.ons.authorization.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandlerResponse {
    private String expiration;
    private String csrf_token;
    private String statusCode;
    private String exceptionInfo;

}
