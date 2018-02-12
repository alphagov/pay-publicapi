package uk.gov.pay.api.utils;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.pay.api.app.config.PublicApiConfig;

public class BuildTrustStoreCommand extends ConfiguredCommand<PublicApiConfig> {

    public BuildTrustStoreCommand() {
        super("buildTrustStore", "Build TLS trust store from certificates");
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
    }

    @Override
    protected void run(Bootstrap<PublicApiConfig> bs, Namespace ns, PublicApiConfig conf) {
        TrustStoreLoader.initialiseTrustStore();
    }
}
