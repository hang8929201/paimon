/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.manifest;

import org.apache.paimon.table.sink.CommitMessage;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Manifest commit message. */
public class ManifestCommittable {

    private final long identifier;
    @Nullable private final Long watermark;
    private final Map<Integer, Long> logOffsets;
    private final Map<String, String> properties;
    private final List<CommitMessage> commitMessages;

    public ManifestCommittable(long identifier) {
        this(identifier, null);
    }

    public ManifestCommittable(long identifier, @Nullable Long watermark) {
        this.identifier = identifier;
        this.watermark = watermark;
        this.logOffsets = new HashMap<>();
        this.commitMessages = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public ManifestCommittable(
            long identifier,
            @Nullable Long watermark,
            Map<Integer, Long> logOffsets,
            List<CommitMessage> commitMessages) {
        this(identifier, watermark, logOffsets, commitMessages, new HashMap<>());
    }

    public ManifestCommittable(
            long identifier,
            @Nullable Long watermark,
            Map<Integer, Long> logOffsets,
            List<CommitMessage> commitMessages,
            Map<String, String> properties) {
        this.identifier = identifier;
        this.watermark = watermark;
        this.logOffsets = logOffsets;
        this.commitMessages = commitMessages;
        this.properties = properties;
    }

    public void addFileCommittable(CommitMessage commitMessage) {
        commitMessages.add(commitMessage);
    }

    public void addLogOffset(int bucket, long offset, boolean allowDuplicate) {
        if (!allowDuplicate && logOffsets.containsKey(bucket)) {
            throw new RuntimeException(
                    String.format(
                            "bucket-%d appears multiple times, which is not possible.", bucket));
        }
        long newOffset = Math.max(logOffsets.getOrDefault(bucket, offset), offset);
        logOffsets.put(bucket, newOffset);
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public long identifier() {
        return identifier;
    }

    @Nullable
    public Long watermark() {
        return watermark;
    }

    public Map<Integer, Long> logOffsets() {
        return logOffsets;
    }

    public List<CommitMessage> fileCommittables() {
        return commitMessages;
    }

    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ManifestCommittable that = (ManifestCommittable) o;
        return Objects.equals(identifier, that.identifier)
                && Objects.equals(watermark, that.watermark)
                && Objects.equals(logOffsets, that.logOffsets)
                && Objects.equals(commitMessages, that.commitMessages)
                && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, watermark, logOffsets, commitMessages, properties);
    }

    @Override
    public String toString() {
        return String.format(
                "ManifestCommittable {"
                        + "identifier = %s, "
                        + "watermark = %s, "
                        + "logOffsets = %s, "
                        + "commitMessages = %s, "
                        + "properties = %s}",
                identifier, watermark, logOffsets, commitMessages, properties);
    }
}
