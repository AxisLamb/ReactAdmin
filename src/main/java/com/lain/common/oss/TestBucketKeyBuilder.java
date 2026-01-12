package com.lain.common.oss;

import com.lain.config.oss.model.BucketKey;
import com.lain.config.oss.model.BucketKeyBuilder;

public class TestBucketKeyBuilder implements BucketKeyBuilder {
    public static BucketKey build() {
        return new TestBucketKeyBuilder().bucketKey();
    }

    @Override
    public String getModular() {
        return "OS";
    }

    @Override
    public String getTable() {
        return "LAIN";
    }

    @Override
    public String getField() {
        return "imgs";
    }
}
