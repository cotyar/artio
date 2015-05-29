/*
 * Copyright 2015 Real Logic Ltd.
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
package uk.co.real_logic.fix_gateway;

import uk.co.real_logic.agrona.IoUtil;
import uk.co.real_logic.agrona.concurrent.AtomicBuffer;
import uk.co.real_logic.agrona.concurrent.CountersManager;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.nio.MappedByteBuffer;

/**
 *
 * A single file of the labelsBuffer, followed by the countersBuffer.
 *
 */
public final class CountersFile implements AutoCloseable
{
    private final MappedByteBuffer mappedByteBuffer;
    private final int length;

    public CountersFile(final StaticConfiguration configuration)
    {
        final File file = new File(configuration.counterBuffersFile());
        IoUtil.deleteIfExists(file);
        length = configuration.counterBuffersLength();
        mappedByteBuffer = IoUtil.mapNewFile(file, length * 2);
    }

    public CountersManager createCountersManager()
    {
        final AtomicBuffer mappedFile = new UnsafeBuffer(mappedByteBuffer);
        final AtomicBuffer labelsBuffer = new UnsafeBuffer(mappedFile, 0, length);
        final AtomicBuffer countersBuffer = new UnsafeBuffer(mappedFile, length, length);
        return new CountersManager(labelsBuffer, countersBuffer);
    }


    @Override
    public void close()
    {
        IoUtil.unmap(mappedByteBuffer);
    }
}