package org.arquillian.smart.testing.mvn.ext;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.arquillian.smart.testing.Configuration;
import org.arquillian.smart.testing.Logger;
import org.arquillian.smart.testing.hub.storage.ChangeStorage;
import org.arquillian.smart.testing.hub.storage.LocalChangeStorage;
import org.arquillian.smart.testing.scm.Change;
import org.arquillian.smart.testing.scm.spi.ChangeResolver;
import org.arquillian.smart.testing.spi.JavaSPILoader;
import org.codehaus.plexus.component.annotations.Component;

import static java.util.stream.StreamSupport.stream;

@Component(role = AbstractMavenLifecycleParticipant.class,
    description = "Entry point to install and manage Smart-Testing extension. Takes care of adding needed dependencies and "
        + "configures it on the fly.",
    hint = "smart-testing")
class SmartTestingMavenConfigurer extends AbstractMavenLifecycleParticipant {

    private static final Logger logger = Logger.getLogger(SmartTestingMavenConfigurer.class);

    private final ChangeStorage changeStorage = new LocalChangeStorage(".");

    private Configuration configuration;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

        configuration = Configuration.load();

        if (configuration.isDisabled()) {
            return;
        }

        if (configuration.areStrategiesDefined()) {
            configureExtension(session, configuration);
            calculateChanges();
        } else {
            logger.warn("Smart Testing is installed but no strategies are provided using %s system property.",
                Configuration.SMART_TESTING);
        }
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        if (configuration.isDisabled()) {
            return;
        }

        if (configuration.areStrategiesDefined()) {
            changeStorage.purgeAll();
        } else {
            logger.warn("Smart Testing is installed but no strategies are provided using %s system property.",
                Configuration.SMART_TESTING);
        }
    }

    private void calculateChanges() {
        final Iterable<ChangeResolver> changeResolvers =
            new JavaSPILoader().all(ChangeResolver.class, ChangeResolver::isApplicable);
        final Collection<Change> changes = stream(changeResolvers.spliterator(), false).map(ChangeResolver::diff)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        changeStorage.store(changes);
    }

    private void configureExtension(MavenSession session, Configuration configuration) {
        final MavenProjectConfigurator mavenProjectConfigurator = new MavenProjectConfigurator(configuration);
        session.getAllProjects().forEach(mavenProject -> mavenProjectConfigurator.configureTestRunner(mavenProject.getModel()));
    }
}
