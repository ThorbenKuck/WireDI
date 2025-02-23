package com.wiredi.runtime.properties.loader;

import org.yaml.snakeyaml.Yaml;

public interface YamlFactory {

    static YamlFactory simple() {
        return Yaml::new;
    }

    Yaml create();
}
