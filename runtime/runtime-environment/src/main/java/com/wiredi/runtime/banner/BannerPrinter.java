package com.wiredi.runtime.banner;

import java.util.List;

public interface BannerPrinter {

    boolean willPrint();

    void print(List<String> lines);

}
