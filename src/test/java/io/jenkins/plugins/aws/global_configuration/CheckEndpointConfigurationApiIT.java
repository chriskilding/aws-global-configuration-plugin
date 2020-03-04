package io.jenkins.plugins.aws.global_configuration;

import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckEndpointConfigurationApiIT extends AbstractCheckEndpointConfigurationIT {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Override
    protected Result validate(String serviceEndpoint) {
        return validateRequest(String.format("descriptorByName/io.jenkins.plugins.aws.global_configuration.EndpointConfiguration/testEndpointConfiguration?serviceEndpoint=%s", serviceEndpoint));
    }

    @Override
    protected Result validate(String serviceEndpoint, String signingRegion) {
        return validateRequest(String.format("descriptorByName/io.jenkins.plugins.aws.global_configuration.EndpointConfiguration/testEndpointConfiguration?serviceEndpoint=%s&signingRegion=%s", serviceEndpoint, signingRegion));
    }

    private Result validateRequest(String path) {
        final JenkinsRule.JSONWebResponse response = doPost(path, "");

        final ParsedBody parsedBody = getValidationMessage(response.getContentAsString(StandardCharsets.UTF_8));

        if (parsedBody.status.equals("ok")) {
            return Result.success(parsedBody.msg);
        } else {
            return Result.error(parsedBody.msg);
        }
    }


    private JenkinsRule.JSONWebResponse doPost(String path, Object json) {
        try {
            return r.postJSON(path, json);
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParsedBody getValidationMessage(String body) {
        final Pattern p = Pattern.compile("<div class=(.+)><img.+>(.+)</div>");
        final Matcher matcher = p.matcher(body);
        if (matcher.find()) {
            final String status = matcher.group(1);
            final String msg = matcher.group(2);
            return new ParsedBody(msg, status);
        } else {
            throw new RuntimeException("Could not parse response body");
        }
    }

    private static class ParsedBody {
        final String msg;
        final String status;

        private ParsedBody(String msg, String status) {
            this.msg = msg;
            this.status = status;
        }
    }
}
