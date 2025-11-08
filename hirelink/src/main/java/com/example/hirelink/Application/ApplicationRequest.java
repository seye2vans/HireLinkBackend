// ApplicationRequest.java
package com.example.hirelink.Application;

import lombok.Data;

@Data
public class ApplicationRequest {
    private Long jobId;
    private Long applicantId;
    private String resumeUrl;
    private String coverLetter;
}
