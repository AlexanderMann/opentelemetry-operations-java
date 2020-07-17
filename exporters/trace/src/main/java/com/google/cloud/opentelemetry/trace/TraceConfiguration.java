package com.google.cloud.opentelemetry.trace;

import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import com.google.cloud.ServiceOptions;
import com.google.cloud.trace.v2.stub.TraceServiceStub;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.opentelemetry.common.AttributeValue;
import java.util.Collections;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link TraceConfiguration}.
 *
 * @since 0.12
 */
@AutoValue
@Immutable
public abstract class TraceConfiguration {

    private static final String DEFAULT_PROJECT_ID = Strings.nullToEmpty(ServiceOptions.getDefaultProjectId());

    @VisibleForTesting
    static final Duration DEFAULT_DEADLINE = Duration.ofSeconds(10, 0);

    TraceConfiguration() {}

    /**
     * Returns the {@link Credentials}.
     *
     * @return the {@code Credentials}.
     * @since 0.12
     */
    @Nullable
    public abstract Credentials getCredentials();

    /**
     * Returns the cloud project id.
     *
     * @return the cloud project id.
     * @since 0.12
     */
    public abstract String getProjectId();

    /**
     * Returns a TraceServiceStub instance used to make RPC calls.
     *
     * @return the trace service stub.
     * @since 0.16
     */
    @Nullable
    public abstract TraceServiceStub getTraceServiceStub();

    /**
     * Returns a map of attributes that is added to all the exported spans.
     *
     * @return the map of attributes that is added to all the exported spans.
     * @since 0.19
     */
    public abstract Map<String, AttributeValue> getFixedAttributes();

    /**
     * Returns the deadline for exporting to Trace backend.
     *
     * <p>
     * Default value is 10 seconds.
     *
     * @return the export deadline.
     * @since 0.22
     */
    public abstract Duration getDeadline();

    /**
     * Returns a new {@link Builder}.
     *
     * @return a {@code Builder}.
     * @since 0.12
     */
    public static Builder builder() {
        return new AutoValue_TraceConfiguration.Builder().setProjectId(DEFAULT_PROJECT_ID)
                .setFixedAttributes(Collections.<String, AttributeValue>emptyMap()).setDeadline(DEFAULT_DEADLINE);
    }

    /**
     * Builder for {@link TraceConfiguration}.
     *
     * @since 0.12
     */
    @AutoValue.Builder
    public abstract static class Builder {

        @VisibleForTesting
        static final Duration ZERO = Duration.ZERO;

        Builder() {
        }

        /**
         * Sets the {@link Credentials} used to authenticate API calls.
         *
         * @param credentials the {@code Credentials}.
         * @return this.
         * @since 0.12
         */
        public abstract Builder setCredentials(Credentials credentials);

        /**
         * Sets the cloud project id.
         *
         * @param projectId the cloud project id.
         * @return this.
         * @since 0.12
         */
        public abstract Builder setProjectId(String projectId);

        /**
         * Sets the trace service stub used to send gRPC calls.
         *
         * @param traceServiceStub the {@code TraceServiceStub}.
         * @return this.
         * @since 0.16
         */
        public abstract Builder setTraceServiceStub(TraceServiceStub traceServiceStub);

        /**
         * Sets the map of attributes that is added to all the exported spans.
         *
         * @param fixedAttributes the map of attributes that is added to all the
         *                        exported spans.
         * @return this.
         * @since 0.16
         */
        public abstract Builder setFixedAttributes(Map<String, AttributeValue> fixedAttributes);

        /**
         * Sets the deadline for exporting to Trace backend.
         *
         * <p>
         * If both {@code TraceServiceStub} and {@code Deadline} are set,
         * {@code TraceServiceStub} takes precedence and {@code Deadline} will not be
         * respected.
         *
         * @param deadline the export deadline.
         * @return this
         * @since 0.22
         */
        public abstract Builder setDeadline(Duration deadline);

        abstract String getProjectId();

        abstract Map<String, AttributeValue> getFixedAttributes();

        abstract Duration getDeadline();

        abstract TraceConfiguration autoBuild();

        /**
         * Builds a {@link TraceConfiguration}.
         *
         * @return a {@code TraceConfiguration}.
         * @since 0.12
         */
        public TraceConfiguration build() {
            // Make a defensive copy of fixed attributes.
            setFixedAttributes(
                    Collections.unmodifiableMap(new LinkedHashMap<String, AttributeValue>(getFixedAttributes())));
            Preconditions.checkArgument(!Strings.isNullOrEmpty(getProjectId()),
                    "Cannot find a project ID from either configurations or application default.");
            for (Map.Entry<String, AttributeValue> fixedAttribute : getFixedAttributes().entrySet()) {
                Preconditions.checkNotNull(fixedAttribute.getKey(), "attribute key");
                Preconditions.checkNotNull(fixedAttribute.getValue(), "attribute value");
            }
            Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
            return autoBuild();
        }
    }
}