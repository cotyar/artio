/*
 * Copyright 2015-2016 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.fix_gateway.replication;

import io.aeron.Publication;
import io.aeron.logbuffer.BufferClaim;

/**
 * A Clustered Publication is a publication that support the raft protocol and allows
 * for publication of messages if you're the leader of the cluster.
 */
public class ClusterPublication extends ClusterablePublication
{
    private final Publication dataPublication;
    private final ClusterNode clusterNode;
    private final long reservedValue;

    public ClusterPublication(final Publication dataPublication, final ClusterNode clusterNode, final int streamId)
    {
        this.dataPublication = dataPublication;
        this.clusterNode = clusterNode;
        this.reservedValue = ReservedValue.ofClusterStreamId(streamId);
    }

    public long tryClaim(final int length, final BufferClaim bufferClaim)
    {
        if (!clusterNode.isPublishable())
        {
            return CANT_PUBLISH;
        }

        final long position = dataPublication.tryClaim(length, bufferClaim);
        if (position > 0)
        {
            bufferClaim.reservedValue(reservedValue);
        }
        return position;
    }

    public long commitPosition()
    {
        return clusterNode.commitPosition();
    }
}