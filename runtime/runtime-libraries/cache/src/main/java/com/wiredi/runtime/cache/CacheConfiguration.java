package com.wiredi.runtime.cache;

public record CacheConfiguration(
        boolean hitOnOverride,
        boolean reorderOnHit,
        int capacity
) {

    public static CacheConfiguration DEFAULT = new CacheConfiguration(true, false, 50);

    public static Builder newInstance() {
        return new Builder();
    }

    public static class Builder {
        boolean hitOnOverride = false;
        boolean reorderOnHit = false;
        int capacity = 50;

        public Builder withHitOnOverride(boolean hitOnOverride) {
            this.hitOnOverride = hitOnOverride;
            return this;
        }

        public Builder withReorderOnHit(boolean reorderOnHit) {
            this.reorderOnHit = reorderOnHit;
            return this;
        }

        public Builder withCapacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public CacheConfiguration build() {
            return new CacheConfiguration(hitOnOverride, reorderOnHit, capacity);
        }
    }
}
