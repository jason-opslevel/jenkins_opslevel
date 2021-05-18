package io.jenkins.plugins.sample;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import javax.json.JsonObject;
import javax.json.Json;
import java.io.IOException;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {

    private final String name;
    private boolean useFrench;

    @DataBoundConstructor
    public HelloWorldBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isUseFrench() {
        return useFrench;
    }

    @DataBoundSetter
    public void setUseFrench(boolean useFrench) {
        this.useFrench = useFrench;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        String result = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("https://opslevel-jason.ngrok.io/integrations/deploy/ba439766-bbbd-4596-8114-7789fef7920a");
        httppost.addHeader("content-type", "application/x-www-form-urlencoded");

        DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
        String now = ZonedDateTime.now().format(dtf);

        // Build the JSON string
        JsonObject json = Json.createObjectBuilder()
            .add("service", "test_cart")
            .add("deployer", Json.createObjectBuilder()
                .add("email", "jason@opslevel.com"))
            .add("deployed_at", now)
            .add("description", "Deployed By Jenkins")
            .build();


        // send a JSON data
        String json_string = json.toString();
        httppost.setEntity(new StringEntity(json_string));
        listener.getLogger().println(new String(json_string));
        CloseableHttpResponse response = httpClient.execute(httppost);

        // Log the result
        result = EntityUtils.toString(response.getEntity());
        listener.getLogger().println(result);
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            if (value.length() < 4)
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

}
