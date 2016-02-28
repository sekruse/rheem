package org.qcri.rheem.spark.channels;

import org.apache.spark.api.java.JavaRDD;
import org.qcri.rheem.core.plan.executionplan.Channel;
import org.qcri.rheem.core.plan.executionplan.ChannelInitializer;
import org.qcri.rheem.core.plan.executionplan.ExecutionTask;
import org.qcri.rheem.core.platform.ChannelDescriptor;

/**
 * Describes the situation where one {@link JavaRDD} is operated on, producing a further {@link JavaRDD}.
 * <p><i>NB: We might be more specific: Distinguish between cached/uncached and pipelined/aggregated.</i></p>
 */
public class RddChannel extends Channel {

    private static final boolean IS_REUSABLE = true;

    private static final boolean IS_INTERNAL = true;

    public static final ChannelDescriptor DESCRIPTOR = new ChannelDescriptor(RddChannel.class);

    protected RddChannel(ChannelDescriptor descriptor,
                         ExecutionTask producer,
                         int outputIndex) {
        super(descriptor, producer, outputIndex);
        assert descriptor == DESCRIPTOR;
    }

    private RddChannel(RddChannel parent) {
        super(parent);
    }

    @Override
    public boolean isReusable() {
        return IS_REUSABLE;
    }

    @Override
    public boolean isInterStageCapable() {
        return IS_REUSABLE;
    }

    @Override
    public boolean isInterPlatformCapable() {
        return IS_REUSABLE & !IS_INTERNAL;
    }

    @Override
    public RddChannel copy() {
        return new RddChannel(this);
    }

    static class Initializer implements ChannelInitializer {

        @Override
        public Channel setUpOutput(ChannelDescriptor descriptor, ExecutionTask executionTask, int index) {
            final Channel existingOutputChannel = executionTask.getOutputChannel(index);
            if (existingOutputChannel == null) {
                return new RddChannel(descriptor, executionTask, index);
            } else if (existingOutputChannel instanceof RddChannel) {
                return existingOutputChannel;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void setUpInput(Channel channel, ExecutionTask executionTask, int index) {
            assert channel instanceof RddChannel;
            channel.addConsumer(executionTask, index);
        }

        @Override
        public boolean isReusable() {
            return true;
        }

        @Override
        public boolean isInternal() {
            return true;
        }
    }

}
