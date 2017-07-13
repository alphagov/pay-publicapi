package uk.gov.pay.api.filter;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Namespace;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import java.io.IOException;
import java.util.HashMap;

public class XRayHttpClientFilter implements ClientRequestFilter, ClientResponseFilter {
    private static final String AWS_TRACE_HEADER = "X-Amzn-Trace-Id";
    private static final int TOO_MANY_REQUESTS = 429;

    private final AWSXRayRecorder recorder = AWSXRay.getGlobalRecorder();

    @Override
    public void filter(ClientRequestContext requestContext)
            throws IOException {

        Subsegment subsegment = recorder.beginSubsegment(requestContext.getUri().getHost());
        try {
            if (subsegment != null) {
                subsegment.setNamespace(Namespace.REMOTE.toString());
                requestContext.getHeaders().add(AWS_TRACE_HEADER, generateTraceHeader(subsegment));
                HashMap requestInformation = new HashMap();
                requestInformation.put("url", requestContext.getUri());
                requestInformation.put("method", requestContext.getMethod());
                subsegment.putHttp("request", requestInformation);
            }
        } catch (Exception exception) {
            if (subsegment != null) {
                subsegment.addException(exception);
            }
        } finally {
            if (subsegment != null) {
                subsegment.end();
            }
        }

    }

    @Override
    public void filter(ClientRequestContext requestContext,
                       ClientResponseContext responseContext) throws IOException {

        Subsegment subsegment = recorder.getCurrentSubsegment();
        try {
            if (subsegment != null) {
                int responseCode = responseContext.getStatus();
                switch (responseCode / 100) {
                    case 4:
                        subsegment.setError(true);
                        if (responseCode == TOO_MANY_REQUESTS) {
                            subsegment.setThrottle(true);
                        }
                        break;
                    case 5:
                        subsegment.setFault(true);
                }
                HashMap responseInformation = new HashMap();
                responseInformation.put("status", Integer.valueOf(responseCode));
                responseInformation.put("content_length", Long.valueOf(responseContext.getLength()));
                subsegment.putHttp("response", responseInformation);
            }
        } catch (Exception exception) {
            if (subsegment != null) {
                subsegment.addException(exception);
            }
        } finally {
            if (subsegment != null) {
                subsegment.end();
            }
        }
    }

    private String generateTraceHeader(Subsegment subsegment) {
        Segment parentSegment = subsegment.getParentSegment();
        return new TraceHeader(
                parentSegment.getTraceId(),
                parentSegment.isSampled() ?
                        subsegment.getId() :
                        null,
                parentSegment.isSampled() ?
                        TraceHeader.SampleDecision.SAMPLED :
                        TraceHeader.SampleDecision.NOT_SAMPLED
        ).toString();
    }
}
