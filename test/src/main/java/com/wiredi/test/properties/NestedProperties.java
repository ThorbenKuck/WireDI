package com.wiredi.test.properties;

import com.wiredi.annotations.properties.Entry;
import com.wiredi.annotations.properties.Property;
import com.wiredi.annotations.properties.PropertyBinding;
import com.wiredi.annotations.properties.PropertySource;

@PropertySource(
        value = "classpath:nested.yaml",
        entries = {
                @Entry(key = "some.manually_added.exampleProperty", value = "foo")
        }
)
@PropertyBinding(prefix = "first.second", file = "classpath:nested.yaml")
public class NestedProperties {

    private final String third;

    public NestedProperties(@Property(name = "third.fourth.fifth") String third) {
        this.third = third;
    }

    public String getThird() {
        return third;
    }

//    private final Third third;
//
//    public NestedProperties(Third third) {
//        this.third = third;
//    }
//
//    public Third getThird() {
//        return third;
//    }
//
//    public static class Third {
//        private final Fourth fourth;
//
//        public Third(Fourth fourth) {
//            this.fourth = fourth;
//        }
//
//        public Fourth getFourth() {
//            return fourth;
//        }
//
//
//        public static class Fourth {
//            private final String fifth;
//
//            public Fourth(String fifth) {
//                this.fifth = fifth;
//            }
//
//            public String getFifth() {
//                return fifth;
//            }
//        }
//    }
}
