package org.arquillian.smart.testing.ftest.failed;

import java.nio.file.Path;
import java.util.List;
import org.arquillian.smart.testing.ftest.testbed.project.Project;
import org.arquillian.smart.testing.ftest.testbed.rules.GitClone;
import org.arquillian.smart.testing.ftest.testbed.rules.TestBed;
import org.arquillian.smart.testing.ftest.testbed.testresults.TestResult;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.arquillian.smart.testing.ftest.failed.SurefireReportHandlingUtils.copySurefireReportsFromPreviousBuild;
import static org.arquillian.smart.testing.ftest.failed.SurefireReportHandlingUtils.storeSurefireReportsForFailingBuild;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Mode.SELECTING;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.AFFECTED;
import static org.arquillian.smart.testing.ftest.testbed.configuration.Strategy.FAILED;
import static org.assertj.core.api.Assertions.assertThat;

public class HistoricalChangesFailedTestsSelectionExecutionFunctionalTest {

    @ClassRule
    public static final GitClone GIT_CLONE = new GitClone();

    @Rule
    public TestBed testBed = new TestBed();

    @Test
    public void should_only_execute_previously_failing_tests_when_failed_is_enabled() throws Exception {
        // given
        final Project project = testBed.getProject();

        project.configureSmartTesting()
                .executionOrder(AFFECTED)
                .inMode(SELECTING)
            .enable();

        project.applyAsCommits("Single method modification - return value");

        try {
            project
                .buildOptions()
                    .withSystemProperties("git.commit", "HEAD", "git.previous.commit", "HEAD~")
                    .configure()
                .build();
        } catch (IllegalStateException ex) {
            //swallow this exception; build should fail.
        }

        List<Path> reportPaths = storeSurefireReportsForFailingBuild(project);

        project.configureSmartTesting()
                .executionOrder(FAILED)
                .inMode(SELECTING)
            .enable();

        final List<TestResult> expectedTestResults = project
            .applyAsCommits("Single test method modification - constant value");

        copySurefireReportsFromPreviousBuild(project, reportPaths);

        // when
        final List<TestResult> actualTestResults = project
            .buildOptions()
                .withSystemProperties("git.commit", "HEAD", "git.previous.commit", "HEAD~")
                .configure()
            .build();

        // then
        assertThat(actualTestResults).containsAll(expectedTestResults).hasSameSizeAs(expectedTestResults);
    }
}
