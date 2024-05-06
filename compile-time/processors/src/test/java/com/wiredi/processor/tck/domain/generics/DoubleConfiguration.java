package com.wiredi.processor.tck.domain.generics;

import com.wiredi.annotations.Primary;
import com.wiredi.annotations.Provider;
import com.wiredi.annotations.Wire;

//@Wire
public class DoubleConfiguration {

//    @Primary
//    @Provider(respect = Provider.SuperTypes.DECLARED)
    public PrimaryDoubleImpl primaryDouble() {
        return new PrimaryDoubleImpl();
    }

}
