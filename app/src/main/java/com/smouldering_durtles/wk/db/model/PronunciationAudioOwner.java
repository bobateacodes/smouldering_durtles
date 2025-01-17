/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smouldering_durtles.wk.db.model;

import com.smouldering_durtles.wk.api.model.PronunciationAudio;

import java.util.List;

/**
 * Interface for a subject-like object that contains pronunciation audio.
 * Apart from Subject itself, it is implemented by a trimmed down subject subset,
 * to make audio scanning more efficient.
 */
public interface PronunciationAudioOwner {
    /**
     * Get the subject's ID.
     *
     * @return the ID
     */
    long getId();

    /**
     * Get the subject's level.
     *
     * @return the level
     */
    int getLevel();

    /**
     * Parsed version of pronunciationAudios, inflated on demand.
     *
     * @return the list of audio records
     */
    List<PronunciationAudio> getParsedPronunciationAudios();
}
