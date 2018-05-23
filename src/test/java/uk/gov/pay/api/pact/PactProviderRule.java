package uk.gov.pay.api.pact;

import au.com.dius.pact.consumer.PactProviderRuleMk2;
import au.com.dius.pact.model.FileSource;
import au.com.dius.pact.model.RequestResponsePact;
import org.mockserver.socket.PortFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static au.com.dius.pact.model.PactReader.loadPact;
import static com.google.common.io.Resources.getResource;
import static java.lang.String.format;
import static java.util.Arrays.stream;

public class PactProviderRule extends PactProviderRuleMk2 {
    
    public PactProviderRule(String provider, Object target) {
        super(provider, "localhost", PortFactory.findFreePort(), target);
    }

    @Override
    protected Map<String, RequestResponsePact> getPacts(String fragment) {
        HashMap<String, RequestResponsePact> pacts = new HashMap<>();
        for (Method m : target.getClass().getMethods()) {
            Optional.ofNullable(m.getAnnotation(Pacts.class)).ifPresent(pactsAnnotation -> stream(pactsAnnotation.pacts()).forEach(fileName -> {
                if (fileName.contains(provider)) {
                    RequestResponsePact pact = (RequestResponsePact) loadPact(new FileSource<>(new File(getResource(format("pacts/%s.json", fileName)).getFile())));
                    pacts.put(provider, pact);
                }
            }));
        }
        return pacts;
    }
}
