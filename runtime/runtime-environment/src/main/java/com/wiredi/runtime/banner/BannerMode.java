package com.wiredi.runtime.banner;

import com.wiredi.logging.Logging;

import java.util.List;

public enum BannerMode implements BannerPrinter {

    DISABLED {
        @Override
        public boolean willPrint() {
            return false;
        }

        @Override
        public void print(List<String> content) {}
    },
    CONSOLE {
        @Override
        public void print(List<String> content) {
            System.out.print(String.join("", content));
        }
    },
    LOGGER {
        @Override
        public void print(List<String> content) {
            content.forEach(logger::info);
        }
    };

    private static final Logging logger = Logging.getInstance(BannerMode.class);

    @Override
    public boolean willPrint() {
        return true;
    }
}
