/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.service;

/**
 * Describes the File Copying service behaviour.
 *
 * @author Alex Arana
 */
public interface FileCopyingService {

    /**
     * Copies the content from a source file to a destination file.
     * TODO: Implement this method using Apache commons-vfs
     *
     * @param sourceURI Source URI string
     * @param targetURI Target URI string
     */
    void copyFiles(String sourceURI, String targetURI);
}