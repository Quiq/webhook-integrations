package com.centricient.sample;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class HookConsumerApplication extends Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new HookConsumerApplication().run(args);
    }

    @Override
    public void run(String... arguments) throws Exception {
        if (arguments.length == 0) {
            super.run("server", "hook-consumer.yml");
        } else {
            super.run(arguments);
        }
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new MainResource());
    }
}
