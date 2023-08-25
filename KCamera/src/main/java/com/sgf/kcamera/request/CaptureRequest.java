package com.sgf.kcamera.request;


import com.sgf.kcamera.KCustomerRequestStrategy;

public class CaptureRequest {
    private final KCustomerRequestStrategy mKCustomerRequestStrategy;

    private CaptureRequest(Builder builder) {
        mKCustomerRequestStrategy = builder.mKCustomerRequestStrategy;
    }

    public KCustomerRequestStrategy getCustomerRequestStrategy() {
        return mKCustomerRequestStrategy;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public static class Builder {
        private KCustomerRequestStrategy mKCustomerRequestStrategy;

        public Builder setCustomerRequestStrategy(KCustomerRequestStrategy strategy) {
            mKCustomerRequestStrategy = strategy;
            return this;
        }

        public CaptureRequest builder() {
            return new CaptureRequest(this);
        }
    }
}
