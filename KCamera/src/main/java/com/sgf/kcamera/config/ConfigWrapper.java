package com.sgf.kcamera.config;


public class ConfigWrapper {

    private ConfigStrategy mConfig = new DefaultConfigStrategy();
    public ConfigWrapper() { }

    public ConfigWrapper(ConfigStrategy config) {
        if (config != null) {
            mConfig = config;
        }
    }

    public ConfigStrategy getConfig() {
        return mConfig;
    }
}
